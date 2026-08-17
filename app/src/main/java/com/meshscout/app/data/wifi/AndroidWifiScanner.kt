package com.meshscout.app.data.wifi

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.ArrayDeque
import kotlin.math.roundToInt

/** A clock abstraction that keeps scanner timestamps deterministic in tests. */
fun interface WifiClock {
    fun nowMillis(): Long
}

/** Production clock used for reading timestamps and throttle deadlines. */
object SystemWifiClock : WifiClock {
    override fun nowMillis(): Long = System.currentTimeMillis()
}

/**
 * Production [WifiScanner] implementation backed by [android.net.wifi.WifiManager].
 *
 * The scanner registers the result listener before calling the platform's asynchronous
 * `startScan()` API. On API 30 and newer it prefers `registerScanResultsCallback`; the Android
 * platform boundary falls back to `SCAN_RESULTS_AVAILABLE_ACTION` when necessary.
 *
 * The scanner owns a small coroutine scope only when the caller does not provide one. A caller
 * that supplies a scope should cancel that scope with the owning ViewModel or component. Calling
 * [close] always unregisters listeners and cancels the scanner's internal jobs.
 */
class AndroidWifiScanner internal constructor(
    private val platform: WifiScannerPlatform,
    scope: CoroutineScope?,
    private val lifecycle: Lifecycle,
    private val clock: WifiClock,
    private val throttle: ThrottleHelper,
    private val staleAfterMillis: Long,
    private val averageWindowSize: Int,
    dispatcher: CoroutineDispatcher
) : WifiScanner, DefaultLifecycleObserver {

    /** Convenience constructor used by the app and future ViewModels. */
    constructor(
        context: Context,
        scope: CoroutineScope? = null,
        lifecycle: Lifecycle = ProcessLifecycleOwner.get().lifecycle,
        clock: WifiClock = SystemWifiClock,
        throttle: ThrottleHelper = ThrottleHelper(),
        staleAfterMillis: Long = DEFAULT_STALE_AFTER_MILLIS,
        averageWindowSize: Int = DEFAULT_AVERAGE_WINDOW_SIZE,
        dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
    ) : this(
        platform = AndroidWifiScannerPlatform(context),
        scope = scope,
        lifecycle = lifecycle,
        clock = clock,
        throttle = throttle,
        staleAfterMillis = staleAfterMillis,
        averageWindowSize = averageWindowSize,
        dispatcher = dispatcher
    )

    private val ownsScope = scope == null
    private val scannerScope = scope ?: CoroutineScope(SupervisorJob() + dispatcher)
    private val lock = Any()

    private val _state = MutableStateFlow<WifiScanState>(WifiScanState.Idle)
    override val state: StateFlow<WifiScanState> = _state.asStateFlow()

    private val _nextScanAllowedAtMillis = MutableStateFlow<Long?>(null)
    override val nextScanAllowedAtMillis: StateFlow<Long?> =
        _nextScanAllowedAtMillis.asStateFlow()

    private val _isScanReady = MutableStateFlow(true)
    override val isScanReady: StateFlow<Boolean> = _isScanReady.asStateFlow()

    private val recentReadings = ArrayDeque<WifiReading>()
    private val resultsListener: (Boolean) -> Unit = { resultsUpdated ->
        handleScanResults(resultsUpdated)
    }

    private var targetBssid: String? = null
    private var targetSsid: String? = null
    private var lastReading: WifiReading? = null
    private var isSessionActive = false
    private var isContinuous = false
    private var isListenerRegistered = false
    private var isClosed = false
    private var scanJob: Job? = null
    private var clockJob: Job? = null

    init {
        require(staleAfterMillis > 0) { "staleAfterMillis must be positive" }
        require(averageWindowSize > 0) { "averageWindowSize must be positive" }
        lifecycle.addObserver(this)
        clockJob = scannerScope.launch {
            while (isActive) {
                synchronized(lock) {
                    if (isClosed) return@launch
                    updateThrottleStateLocked(clock.nowMillis())
                    markReadingStaleIfNeededLocked(clock.nowMillis())
                }
                delay(CLOCK_UPDATE_INTERVAL_MILLIS)
            }
        }
    }

    /** Starts a foreground-only throttled scan loop for the requested target. */
    override fun startScanning(targetBssid: String?, targetSsid: String?) {
        synchronized(lock) {
            if (isClosed) return

            stopInternalLocked()
            this.targetBssid = targetBssid.cleanBssid()
            this.targetSsid = targetSsid.cleanSsid()
            recentReadings.clear()
            lastReading = null

            val prerequisiteError = prerequisiteErrorLocked()
            if (prerequisiteError != null) {
                publishErrorLocked(prerequisiteError)
                return
            }

            isSessionActive = true
            isContinuous = true
            if (!registerListenerLocked()) return

            _state.value = WifiScanState.Scanning
        }

        requestSingleScanInternal()

        synchronized(lock) {
            if (!isClosed && isSessionActive && isContinuous && scanJob == null) {
                scanJob = scannerScope.launch { runScanLoop() }
            }
        }
    }

    /** Stops all future requests and unregisters the platform listener. */
    override fun stopScanning() {
        synchronized(lock) {
            if (isClosed) return
            stopInternalLocked()
            _state.value = WifiScanState.Idle
            updateThrottleStateLocked(clock.nowMillis())
        }
    }

    /** Requests one scan, either within the current session or as a one-shot session. */
    override fun requestSingleScan() {
        synchronized(lock) {
            if (isClosed) return

            if (!isSessionActive) {
                val prerequisiteError = prerequisiteErrorLocked()
                if (prerequisiteError != null) {
                    publishErrorLocked(prerequisiteError)
                    return
                }

                isSessionActive = true
                isContinuous = false
                if (!registerListenerLocked()) return
                _state.value = WifiScanState.Scanning
            }
        }

        requestSingleScanInternal()
    }

    /** Stops scanning as soon as the app is no longer visible. */
    override fun onStop(owner: LifecycleOwner) {
        stopScanning()
    }

    /** Releases listeners, lifecycle observation, and coroutine resources. */
    override fun close() {
        synchronized(lock) {
            if (isClosed) return
            isClosed = true
            stopInternalLocked()
            clockJob?.cancel()
            clockJob = null
            lifecycle.removeObserver(this)
        }

        if (ownsScope) scannerScope.cancel()
    }

    private suspend fun runScanLoop() {
        while (isActive) {
            val waitMillis = synchronized(lock) {
                if (!isSessionActive || !isContinuous || isClosed) {
                    return@synchronized null
                }

                val now = clock.nowMillis()
                throttle.nextAllowedAtMillis(now)?.let { nextAllowed ->
                    (nextAllowed - now).coerceAtLeast(1L)
                } ?: 0L
            } ?: return

            if (waitMillis > 0) delay(waitMillis)
            requestSingleScanInternal()
        }
    }

    private fun registerListenerLocked(): Boolean {
        isListenerRegistered = true
        return try {
            platform.registerScanResultsListener(resultsListener)
            true
        } catch (_: SecurityException) {
            stopInternalLocked()
            publishErrorLocked(ScanError.NoPermission)
            false
        } catch (_: RuntimeException) {
            stopInternalLocked()
            publishErrorLocked(ScanError.Unknown)
            false
        }
    }

    private fun requestSingleScanInternal() {
        synchronized(lock) {
            if (isClosed || !isSessionActive) return

            val prerequisiteError = prerequisiteErrorLocked()
            if (prerequisiteError != null) {
                stopInternalLocked()
                publishErrorLocked(prerequisiteError)
                return
            }

            val now = clock.nowMillis()
            if (!throttle.canRequest(now)) {
                publishErrorLocked(ScanError.Throttled)
                finishOneShotLocked()
                return
            }

            // Set this before calling into the platform so a test double (or an unusual vendor
            // implementation) that delivers results synchronously cannot overwrite a completed
            // Success state after startScan() returns.
            _state.value = WifiScanState.Scanning
            val started = try {
                platform.startScan()
            } catch (_: SecurityException) {
                stopInternalLocked()
                publishErrorLocked(ScanError.NoPermission)
                return
            } catch (_: RuntimeException) {
                stopInternalLocked()
                publishErrorLocked(ScanError.Unknown)
                return
            }

            if (!started) {
                throttle.recordRejectedRequest(now)
                publishErrorLocked(ScanError.Throttled)
                finishOneShotLocked()
                return
            }

            throttle.recordRequest(now)
            updateThrottleStateLocked(now)
        }
    }

    private fun handleScanResults(resultsUpdated: Boolean) {
        synchronized(lock) {
            if (isClosed || !isSessionActive) return

            if (!resultsUpdated) {
                publishErrorLocked(ScanError.Throttled)
                finishOneShotLocked()
                return
            }

            val results = try {
                platform.getScanResults()
            } catch (_: SecurityException) {
                stopInternalLocked()
                publishErrorLocked(ScanError.NoPermission)
                return
            } catch (_: RuntimeException) {
                stopInternalLocked()
                publishErrorLocked(ScanError.Unknown)
                return
            }

            val result = ScanResultMapper.findMatchingResult(
                results = results,
                targetBssid = targetBssid,
                targetSsid = targetSsid
            )

            if (result == null) {
                publishErrorLocked(ScanError.NoMatchingNetwork)
                finishOneShotLocked()
                return
            }

            val reading = ScanResultMapper.toReading(
                result = result,
                timestampMillis = clock.nowMillis()
            )
            recentReadings.addLast(reading)
            while (recentReadings.size > averageWindowSize) {
                recentReadings.removeFirst()
            }
            lastReading = reading

            _state.value = WifiScanState.Success(
                reading = reading,
                averageRssi = recentReadings
                    .map { it.rssi }
                    .average()
                    .roundToInt()
            )
            updateThrottleStateLocked(clock.nowMillis())
            finishOneShotLocked()
        }
    }

    private fun prerequisiteErrorLocked(): ScanError? = when {
        !lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) -> ScanError.NotInForeground
        !platform.hasRequiredPermissions() -> ScanError.NoPermission
        !platform.isLocationEnabled() -> ScanError.LocationDisabled
        !platform.isWifiEnabled() -> ScanError.WifiDisabled
        else -> null
    }

    private fun finishOneShotLocked() {
        if (!isContinuous) {
            unregisterListenerLocked()
            isSessionActive = false
        }
    }

    private fun stopInternalLocked() {
        scanJob?.cancel()
        scanJob = null
        unregisterListenerLocked()
        isSessionActive = false
        isContinuous = false
    }

    private fun unregisterListenerLocked() {
        if (!isListenerRegistered) return
        try {
            platform.unregisterScanResultsListener()
        } catch (_: RuntimeException) {
            // Listener cleanup must not crash a lifecycle transition.
        } finally {
            isListenerRegistered = false
        }
    }

    private fun publishErrorLocked(reason: ScanError) {
        val nextAllowed = updateThrottleStateLocked(clock.nowMillis())
        _state.value = WifiScanState.Error(
            reason = reason,
            lastReading = lastReading?.copy(isFresh = false),
            nextScanAllowedAtMillis = nextAllowed
        )
    }

    private fun updateThrottleStateLocked(nowMillis: Long): Long? {
        val nextAllowed = throttle.nextAllowedAtMillis(nowMillis)
        _nextScanAllowedAtMillis.value = nextAllowed
        _isScanReady.value = nextAllowed == null
        return nextAllowed
    }

    private fun markReadingStaleIfNeededLocked(nowMillis: Long) {
        val currentState = _state.value as? WifiScanState.Success ?: return
        if (!currentState.reading.isFresh) return

        val age = nowMillis - currentState.reading.timestamp
        if (age >= staleAfterMillis) {
            _state.value = currentState.copy(
                reading = currentState.reading.copy(isFresh = false)
            )
        }
    }

    private fun String?.cleanBssid(): String? =
        this?.trim()?.takeIf { it.isNotEmpty() }

    private fun String?.cleanSsid(): String? =
        this?.trim()?.removeSurrounding("\"")?.takeIf { it.isNotEmpty() }

    companion object {
        const val DEFAULT_STALE_AFTER_MILLIS = 15 * 1000L
        const val DEFAULT_AVERAGE_WINDOW_SIZE = 5
        private const val CLOCK_UPDATE_INTERVAL_MILLIS = 1000L
    }
}
