package com.meshscout.app.data.wifi

import kotlinx.coroutines.flow.StateFlow
import java.io.Closeable

/**
 * One observation of an access point from the Wi-Fi scan cache.
 *
 * [timestamp] is epoch time in milliseconds. [rssi] is the raw value reported by Android in
 * dBm. The scanner does not replace this value with the rolling average; consumers can display
 * both the latest raw reading and [WifiScanState.Success.averageRssi].
 *
 * A reading is marked stale when it is older than the scanner's freshness window, or when a
 * later scan no longer contains the target. Stale readings are deliberately retained so the UI
 * can explain that it is showing the last known value instead of flashing an empty screen.
 */
data class WifiReading(
    val ssid: String?,
    val bssid: String?,
    val rssi: Int,
    val timestamp: Long,
    val isAveraged: Boolean = false,
    val isFresh: Boolean = true
)

/** Reasons a scan cannot currently produce a target reading. */
enum class ScanError {
    /** The caller has not been granted the permission required by the current Android version. */
    NoPermission,

    /** The device-wide Location toggle is disabled. */
    LocationDisabled,

    /** Wi-Fi is disabled on the device. */
    WifiDisabled,

    /** Android or this scanner rejected the request because the throttle window is active. */
    Throttled,

    /** The scan completed, but no result matched the requested BSSID or SSID. */
    NoMatchingNetwork,

    /** The app is not visible in the foreground, so a scan was not attempted. */
    NotInForeground,

    /** The platform returned a failure that cannot be classified more precisely. */
    Unknown
}

/**
 * Observable state emitted by [WifiScanner].
 *
 * [Error.lastReading] is populated when a previous successful reading exists. It is marked
 * stale because the current scan did not produce a fresh matching value.
 */
sealed class WifiScanState {
    /** No active scan session exists. */
    data object Idle : WifiScanState()

    /** A scan request has been accepted or the scanner is waiting for its result. */
    data object Scanning : WifiScanState()

    /** A target result was found. */
    data class Success(
        val reading: WifiReading,
        val averageRssi: Int?
    ) : WifiScanState()

    /** A scan could not produce a current target reading. */
    data class Error(
        val reason: ScanError,
        val lastReading: WifiReading? = null,
        val nextScanAllowedAtMillis: Long? = null
    ) : WifiScanState()
}

/**
 * Foreground-only Wi-Fi scanner contract.
 *
 * Call [startScanning] only while the user is in the app's active measurement mode. The Android
 * implementation also observes the process lifecycle and stops immediately when the app leaves
 * the foreground. Call [stopScanning] when the measurement mode ends.
 *
 * A target BSSID is preferred because multiple access points can advertise the same SSID. If a
 * BSSID is not available, pass an SSID. When neither target is supplied, the strongest visible
 * result is returned, which is useful for an initial controller-discovery step.
 */
interface WifiScanner : Closeable {
    /** Latest scanner state. */
    val state: StateFlow<WifiScanState>

    /**
     * Epoch timestamp at which the local throttle allows another request, or null when ready.
     * This is a client-side lower bound; Android may apply additional device-specific limits.
     */
    val nextScanAllowedAtMillis: StateFlow<Long?>

    /** True when the local throttle permits a request at the current instant. */
    val isScanReady: StateFlow<Boolean>

    /** Starts a throttled scan session and immediately requests the first scan. */
    fun startScanning(targetBssid: String? = null, targetSsid: String? = null)

    /** Stops listening and cancels all future requests. */
    fun stopScanning()

    /** Requests one scan using the target supplied to the current session. */
    fun requestSingleScan()
}
