package com.meshscout.app.data.wifi

import android.annotation.SuppressLint
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

/**
 * Small Android boundary used by [AndroidWifiScanner]. Keeping this boundary separate makes the
 * scanner's state machine and throttle behavior unit-testable with a fake platform.
 */
internal interface WifiScannerPlatform {
    fun hasRequiredPermissions(): Boolean

    fun isLocationEnabled(): Boolean

    fun isWifiEnabled(): Boolean

    fun registerScanResultsListener(listener: (resultsUpdated: Boolean) -> Unit)

    fun unregisterScanResultsListener()

    fun startScan(): Boolean

    fun getScanResults(): List<ScanResult>
}

/** Android implementation using the API-30 callback and a broadcast fallback on older releases. */
@SuppressLint("MissingPermission")
internal class AndroidWifiScannerPlatform(context: Context) : WifiScannerPlatform {

    private val applicationContext = context.applicationContext
    private val wifiManager = requireNotNull(
        applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    ) {
        "Wi-Fi service is unavailable"
    }
    private val locationManager = applicationContext
        .getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val mainExecutor: Executor = ContextCompat.getMainExecutor(applicationContext)

    private var listener: ((Boolean) -> Unit)? = null
    private var isRegistered = false
    private var registeredWithCallback = false
    private var api30Callback: Any? = null

    private val scanResultsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) return

            listener?.invoke(
                intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, true)
            )
        }
    }

    override fun hasRequiredPermissions(): Boolean {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Keep this in sync with PermissionViewModel. The scanner never requests permissions.
            arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }

        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    override fun isLocationEnabled(): Boolean {
        val manager = locationManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            manager.isLocationEnabled
        } else {
            isProviderEnabled(manager, LocationManager.GPS_PROVIDER) ||
                isProviderEnabled(manager, LocationManager.NETWORK_PROVIDER)
        }
    }

    override fun isWifiEnabled(): Boolean = wifiManager.isWifiEnabled

    override fun registerScanResultsListener(listener: (resultsUpdated: Boolean) -> Unit) {
        unregisterScanResultsListener()
        this.listener = listener

        var callbackRegistrationError: RuntimeException? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                registerApi30Callback()
                return
            } catch (error: RuntimeException) {
                // A few vendor implementations have been observed to reject callback
                // registration. Keep the broadcast path as a compatibility fallback.
                callbackRegistrationError = error
                api30Callback = null
                registeredWithCallback = false
            }
        }

        try {
            registerBroadcastReceiver()
        } catch (fallbackError: RuntimeException) {
            this.listener = null
            throw callbackRegistrationError ?: fallbackError
        }
    }

    override fun unregisterScanResultsListener() {
        if (!isRegistered && listener == null) return

        try {
            if (registeredWithCallback && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                unregisterApi30Callback()
            } else if (isRegistered) {
                applicationContext.unregisterReceiver(scanResultsReceiver)
            }
        } catch (_: RuntimeException) {
            // The receiver/callback may already have been removed by the platform during a
            // process transition. Cleanup should remain idempotent.
        } finally {
            listener = null
            api30Callback = null
            isRegistered = false
            registeredWithCallback = false
        }
    }

    @Suppress("DEPRECATION")
    override fun startScan(): Boolean = wifiManager.startScan()

    @Suppress("DEPRECATION")
    override fun getScanResults(): List<ScanResult> = wifiManager.scanResults

    @RequiresApi(Build.VERSION_CODES.R)
    private fun registerApi30Callback() {
        val callback = Api30ScanResultsCallback {
            listener?.invoke(true)
        }
        api30Callback = callback
        wifiManager.registerScanResultsCallback(mainExecutor, callback)
        registeredWithCallback = true
        isRegistered = true
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun unregisterApi30Callback() {
        val callback = api30Callback as? Api30ScanResultsCallback ?: return
        wifiManager.unregisterScanResultsCallback(callback)
    }

    private fun registerBroadcastReceiver() {
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.registerReceiver(
                applicationContext,
                scanResultsReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        } else {
            @Suppress("DEPRECATION")
            applicationContext.registerReceiver(scanResultsReceiver, filter)
        }
        isRegistered = true
        registeredWithCallback = false
    }

    private fun isProviderEnabled(locationManager: LocationManager, provider: String): Boolean =
        try {
            locationManager.isProviderEnabled(provider)
        } catch (_: RuntimeException) {
            false
        }

    @RequiresApi(Build.VERSION_CODES.R)
    private class Api30ScanResultsCallback(
        private val onResultsAvailable: () -> Unit
    ) : WifiManager.ScanResultsCallback() {
        override fun onScanResultsAvailable() {
            onResultsAvailable()
        }
    }
}
