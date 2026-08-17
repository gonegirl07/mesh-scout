package com.meshscout.app.ui.debug

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.meshscout.app.R
import com.meshscout.app.data.wifi.AndroidWifiScanner
import com.meshscout.app.data.wifi.ScanError
import com.meshscout.app.data.wifi.WifiReading
import com.meshscout.app.data.wifi.WifiScanState
import com.meshscout.app.ui.permissions.PermissionStatus
import com.meshscout.app.ui.theme.MeshScoutTheme
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Temporary screen used only to exercise [AndroidWifiScanner] on a real device (Issue #22).
 *
 * The scanner is created for this screen and is always stopped + closed when the composable
 * leaves the composition.
 */
@Composable
fun WifiDebugScreen(
    permissionStatus: PermissionStatus,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scanner = remember(context) {
        AndroidWifiScanner(context.applicationContext)
    }

    BackHandler(onBack = onBack)

    DisposableEffect(scanner) {
        onDispose {
            scanner.stopScanning()
            scanner.close()
        }
    }

    val scanState by scanner.state.collectAsState()
    val nextScanAllowedAtMillis by scanner.nextScanAllowedAtMillis.collectAsState()
    val isScanReady by scanner.isScanReady.collectAsState()

    var targetSsid by rememberSaveable { mutableStateOf("") }
    var targetBssid by rememberSaveable { mutableStateOf("") }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            nowMillis = System.currentTimeMillis()
        }
    }

    WifiDebugContent(
        permissionStatus = permissionStatus,
        targetSsid = targetSsid,
        onTargetSsidChange = { targetSsid = it },
        targetBssid = targetBssid,
        onTargetBssidChange = { targetBssid = it },
        scanState = scanState,
        nextScanAllowedAtMillis = nextScanAllowedAtMillis,
        isScanReady = isScanReady,
        nowMillis = nowMillis,
        onStartScanning = {
            scanner.startScanning(
                targetBssid = targetBssid.toOptionalTarget(),
                targetSsid = targetSsid.toOptionalTarget()
            )
        },
        onStopScanning = scanner::stopScanning,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
private fun WifiDebugContent(
    permissionStatus: PermissionStatus,
    targetSsid: String,
    onTargetSsidChange: (String) -> Unit,
    targetBssid: String,
    onTargetBssidChange: (String) -> Unit,
    scanState: WifiScanState,
    nextScanAllowedAtMillis: Long?,
    isScanReady: Boolean,
    nowMillis: Long,
    onStartScanning: () -> Unit,
    onStopScanning: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reading = scanState.currentReading()
    val averageRssi = (scanState as? WifiScanState.Success)?.averageRssi
    val none = stringResource(R.string.debug_value_none)
    val isScanning = scanState !is WifiScanState.Idle

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.debug_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = stringResource(R.string.debug_temporary_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = stringResource(
                R.string.debug_permission_status,
                permissionStatus.toDisplayString()
            ),
            style = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField(
            value = targetSsid,
            onValueChange = onTargetSsidChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.debug_target_ssid)) },
            singleLine = true
        )
        OutlinedTextField(
            value = targetBssid,
            onValueChange = onTargetBssidChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.debug_target_bssid)) },
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onStartScanning,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.debug_start_scanning))
            }
            OutlinedButton(
                onClick = onStopScanning,
                enabled = isScanning,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.debug_stop_scanning))
            }
        }

        DebugValue(
            text = stringResource(
                R.string.debug_scan_state,
                scanState.toDisplayString()
            )
        )
        DebugValue(
            text = stringResource(
                R.string.debug_rssi,
                reading?.rssi?.let { stringResource(R.string.rssi_dbm, it) } ?: none
            )
        )
        DebugValue(
            text = stringResource(
                R.string.debug_average_rssi,
                averageRssi?.let { stringResource(R.string.rssi_dbm, it) } ?: none
            )
        )
        DebugValue(
            text = stringResource(
                R.string.debug_ssid,
                reading?.ssid.takeUnless { it.isNullOrBlank() } ?: none
            )
        )
        DebugValue(
            text = stringResource(
                R.string.debug_bssid,
                reading?.bssid.takeUnless { it.isNullOrBlank() } ?: none
            )
        )
        DebugValue(
            text = stringResource(
                R.string.debug_timestamp,
                reading?.timestamp?.let { formatDebugTimestamp(it) } ?: none
            )
        )
        DebugValue(
            text = stringResource(
                R.string.debug_is_fresh,
                reading?.isFresh?.toDebugBoolean() ?: none
            )
        )
        DebugValue(
            text = stringResource(
                R.string.debug_next_scan_allowed,
                formatNextScanAllowed(nextScanAllowedAtMillis, nowMillis, none)
            )
        )
        DebugValue(
            text = stringResource(
                R.string.debug_is_scan_ready,
                isScanReady.toDebugBoolean()
            )
        )

        TextButton(onClick = onBack) {
            Text(text = stringResource(R.string.back))
        }
    }
}

@Composable
private fun DebugValue(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun PermissionStatus.toDisplayString(): String = stringResource(
    when (this) {
        PermissionStatus.NotRequested -> R.string.debug_permission_not_requested
        PermissionStatus.Granted -> R.string.debug_permission_granted
        PermissionStatus.Denied -> R.string.debug_permission_denied
        PermissionStatus.PermanentlyDenied -> R.string.debug_permission_permanently_denied
    }
)

@Composable
private fun WifiScanState.toDisplayString(): String = when (this) {
    WifiScanState.Idle -> stringResource(R.string.debug_scan_state_idle)
    WifiScanState.Scanning -> stringResource(R.string.debug_scan_state_scanning)
    is WifiScanState.Success -> stringResource(R.string.debug_scan_state_success)
    is WifiScanState.Error -> stringResource(
        R.string.debug_scan_state_error,
        reason.toDisplayString()
    )
}

@Composable
private fun ScanError.toDisplayString(): String = stringResource(
    when (this) {
        ScanError.NoPermission -> R.string.debug_scan_error_no_permission
        ScanError.LocationDisabled -> R.string.debug_scan_error_location_disabled
        ScanError.WifiDisabled -> R.string.debug_scan_error_wifi_disabled
        ScanError.Throttled -> R.string.debug_scan_error_throttled
        ScanError.NoMatchingNetwork -> R.string.debug_scan_error_no_matching_network
        ScanError.NotInForeground -> R.string.debug_scan_error_not_in_foreground
        ScanError.Unknown -> R.string.debug_scan_error_unknown
    }
)

@Composable
private fun Boolean.toDebugBoolean(): String = stringResource(
    if (this) R.string.debug_boolean_true else R.string.debug_boolean_false
)

private fun WifiScanState.currentReading(): WifiReading? = when (this) {
    is WifiScanState.Success -> reading
    is WifiScanState.Error -> lastReading
    WifiScanState.Idle, WifiScanState.Scanning -> null
}

private fun String.toOptionalTarget(): String? = trim().takeIf { it.isNotEmpty() }

private fun formatDebugTimestamp(timestampMillis: Long): String {
    val formatted = TIMESTAMP_FORMATTER.format(Instant.ofEpochMilli(timestampMillis))
    return "$timestampMillis ($formatted)"
}

private fun formatNextScanAllowed(
    nextScanAllowedAtMillis: Long?,
    nowMillis: Long,
    none: String
): String {
    if (nextScanAllowedAtMillis == null) return none
    val remainingMillis = nextScanAllowedAtMillis - nowMillis
    val remainingLabel = if (remainingMillis > 0) {
        "${(remainingMillis / 1000L).coerceAtLeast(0L)}s"
    } else {
        "0s"
    }
    return "$nextScanAllowedAtMillis ($remainingLabel)"
}

private val TIMESTAMP_FORMATTER: DateTimeFormatter =
    DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withZone(ZoneId.systemDefault())

@Preview(showBackground = true)
@Composable
private fun WifiDebugPreview() {
    MeshScoutTheme {
        WifiDebugContent(
            permissionStatus = PermissionStatus.Granted,
            targetSsid = "HomeMesh",
            onTargetSsidChange = {},
            targetBssid = "",
            onTargetBssidChange = {},
            scanState = WifiScanState.Success(
                reading = WifiReading(
                    ssid = "HomeMesh",
                    bssid = "aa:bb:cc:dd:ee:ff",
                    rssi = -57,
                    timestamp = 1_724_000_000_000L,
                    isFresh = true
                ),
                averageRssi = -59
            ),
            nextScanAllowedAtMillis = 1_724_000_004_000L,
            isScanReady = false,
            nowMillis = 1_724_000_000_000L,
            onStartScanning = {},
            onStopScanning = {},
            onBack = {}
        )
    }
}
