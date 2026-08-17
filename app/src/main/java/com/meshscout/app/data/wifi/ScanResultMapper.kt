package com.meshscout.app.data.wifi

import android.net.wifi.ScanResult

/** Pure mapping and matching helpers for Android scan results. */
internal object ScanResultMapper {

    /**
     * Finds the strongest result matching the preferred BSSID, the fallback SSID, or any result
     * when no target was supplied.
     */
    fun findMatchingResult(
        results: List<ScanResult>,
        targetBssid: String?,
        targetSsid: String?
    ): ScanResult? {
        val normalizedBssid = targetBssid.normalizeBssid()
        val normalizedSsid = targetSsid.normalizeSsid()

        return when {
            normalizedBssid != null -> results
                .asSequence()
                .filter { it.BSSID.normalizeBssid() == normalizedBssid }
                .maxByOrNull { it.level }

            normalizedSsid != null -> results
                .asSequence()
                .filter { it.SSID.normalizeSsid() == normalizedSsid }
                .maxByOrNull { it.level }

            else -> results.maxByOrNull { it.level }
        }
    }

    /** Converts one Android result to the app-facing reading model. */
    fun toReading(result: ScanResult, timestampMillis: Long): WifiReading =
        WifiReading(
            ssid = result.SSID.normalizeSsid(),
            bssid = result.BSSID.normalizeBssid(),
            rssi = result.level,
            timestamp = timestampMillis,
            isAveraged = false,
            isFresh = true
        )

    private fun String?.normalizeBssid(): String? =
        this
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.lowercase()

    private fun String?.normalizeSsid(): String? {
        val value = this?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (value.equals(UNKNOWN_SSID, ignoreCase = true)) return null
        return value.removeSurrounding("\"")
            .takeIf { it.isNotEmpty() }
    }

    private const val UNKNOWN_SSID = "<unknown ssid>"
}
