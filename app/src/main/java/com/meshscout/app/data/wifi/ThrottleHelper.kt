package com.meshscout.app.data.wifi

import java.util.ArrayDeque

/**
 * Small, deterministic client-side guard for Android's foreground scan limits.
 *
 * Android's exact policy can vary by OS release and manufacturer. This helper intentionally uses
 * the conservative foreground rule of at most four requests in two minutes, with at least thirty
 * seconds between requests. It does not claim to know whether another application has consumed
 * the platform's scan budget.
 */
class ThrottleHelper(
    private val maxRequests: Int = DEFAULT_MAX_REQUESTS,
    private val windowMillis: Long = DEFAULT_WINDOW_MILLIS,
    private val minimumIntervalMillis: Long = DEFAULT_MINIMUM_INTERVAL_MILLIS,
    private val rejectedRetryMillis: Long = DEFAULT_REJECTED_RETRY_MILLIS
) {

    private val requestTimes = ArrayDeque<Long>()
    private var rejectedUntilMillis: Long? = null

    init {
        require(maxRequests > 0) { "maxRequests must be positive" }
        require(windowMillis > 0) { "windowMillis must be positive" }
        require(minimumIntervalMillis > 0) { "minimumIntervalMillis must be positive" }
        require(rejectedRetryMillis > 0) { "rejectedRetryMillis must be positive" }
    }

    /** Returns true when a request may be attempted at [nowMillis]. */
    @Synchronized
    fun canRequest(nowMillis: Long): Boolean =
        nextAllowedAtMillis(nowMillis) == null

    /** Records a request that Android accepted. */
    @Synchronized
    fun recordRequest(nowMillis: Long) {
        prune(nowMillis)
        requestTimes.addLast(nowMillis)
    }

    /**
     * Records a request that Android rejected without throwing.
     *
     * [WifiManager.startScan] exposes only a Boolean, so a short retry guard prevents a rejected
     * request from becoming a tight loop while still allowing the caller to recover.
     */
    @Synchronized
    fun recordRejectedRequest(nowMillis: Long) {
        val retryAt = nowMillis + rejectedRetryMillis
        rejectedUntilMillis = maxOf(rejectedUntilMillis ?: retryAt, retryAt)
    }

    /** Returns the earliest time a new request may be attempted, or null when ready. */
    @Synchronized
    fun nextAllowedAtMillis(nowMillis: Long): Long? {
        prune(nowMillis)

        val localThrottleAt = buildList {
            requestTimes.peekLast()?.let { add(it + minimumIntervalMillis) }
            if (requestTimes.size >= maxRequests) {
                requestTimes.peekFirst()?.let { add(it + windowMillis) }
            }
        }.maxOrNull()

        val platformRejectedAt = rejectedUntilMillis?.takeIf { it > nowMillis }
        return listOfNotNull(localThrottleAt, platformRejectedAt)
            .maxOrNull()
            ?.takeIf { it > nowMillis }
    }

    /** Clears local history. The scanner normally keeps history for its lifetime. */
    @Synchronized
    fun reset() {
        requestTimes.clear()
        rejectedUntilMillis = null
    }

    private fun prune(nowMillis: Long) {
        while (requestTimes.peekFirst()?.let { nowMillis - it >= windowMillis } == true) {
            requestTimes.removeFirst()
        }
        if (rejectedUntilMillis != null && rejectedUntilMillis!! <= nowMillis) {
            rejectedUntilMillis = null
        }
    }

    companion object {
        const val DEFAULT_MAX_REQUESTS = 4
        const val DEFAULT_WINDOW_MILLIS = 2 * 60 * 1000L
        const val DEFAULT_MINIMUM_INTERVAL_MILLIS = 30 * 1000L
        const val DEFAULT_REJECTED_RETRY_MILLIS = 30 * 1000L
    }
}
