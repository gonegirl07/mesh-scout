package com.meshscout.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meshscout.app.ui.debug.WifiDebugScreen
import com.meshscout.app.ui.permissions.PermissionRationaleScreen
import com.meshscout.app.ui.permissions.PermissionSettingsDialog
import com.meshscout.app.ui.permissions.PermissionStatus
import com.meshscout.app.ui.permissions.PermissionViewModel
import com.meshscout.app.ui.theme.MeshScoutTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeshScoutTheme {
                MeshScoutApp()
            }
        }
    }
}

@Composable
private fun MeshScoutApp(
    permissionViewModel: PermissionViewModel = viewModel()
) {
    val activity = LocalContext.current as Activity
    val permissionState by permissionViewModel.uiState.collectAsState()
    var showWifiDebug by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionViewModel.onPermissionResult(activity, result)
    }

    val requestPermission = {
        permissionViewModel.onPermissionRequestStarted()
        permissionLauncher.launch(permissionViewModel.requiredPermissions())
    }

    // Re-check after returning from App settings so the screen changes immediately if access
    // was enabled there.
    LaunchedEffect(activity) {
        permissionViewModel.refresh(activity)
    }

    DisposableEffect(activity) {
        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                permissionViewModel.refresh(activity)
            }
        }

        activity.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            activity.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (permissionState.showRationale) {
                PermissionRationaleScreen(
                    onContinue = requestPermission,
                    onNotNow = permissionViewModel::continueWithoutPermission,
                    continueEnabled = !permissionState.isRequestInProgress,
                    showDeniedNote = permissionState.status == PermissionStatus.Denied
                )
            } else if (showWifiDebug) {
                WifiDebugScreen(
                    modifier = Modifier.fillMaxSize(),
                    permissionStatus = permissionState.status,
                    onBack = { showWifiDebug = false }
                )
            } else {
                HomeScreen(
                    modifier = Modifier.fillMaxSize(),
                    permissionStatus = permissionState.status,
                    permissionActionEnabled = !permissionState.isRequestInProgress,
                    onRequestPermission = requestPermission,
                    onOpenSettings = {
                        permissionViewModel.dismissSettingsDialog()
                        openAppSettings(activity)
                    },
                    onOpenWifiDebug = { showWifiDebug = true }
                )
            }

            if (permissionState.showSettingsDialog) {
                PermissionSettingsDialog(
                    onOpenSettings = {
                        permissionViewModel.dismissSettingsDialog()
                        openAppSettings(activity)
                    },
                    onNotNow = permissionViewModel::dismissSettingsDialog
                )
            }
        }
    }
}

private fun openAppSettings(activity: Activity) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", activity.packageName, null)
    )
    activity.startActivity(intent)
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    permissionStatus: PermissionStatus = PermissionStatus.Granted,
    permissionActionEnabled: Boolean = true,
    onRequestPermission: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenWifiDebug: () -> Unit = {}
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineSmall
        )

        when (permissionStatus) {
            PermissionStatus.Granted -> Unit

            PermissionStatus.NotRequested,
            PermissionStatus.Denied -> {
                Text(
                    text = stringResource(
                        if (permissionStatus == PermissionStatus.Denied) {
                            R.string.home_permission_denied_message
                        } else {
                            R.string.home_permission_required_message
                        }
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )

                Button(
                    onClick = onRequestPermission,
                    enabled = permissionActionEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.home_grant_permission))
                }
            }

            PermissionStatus.PermanentlyDenied -> {
                Text(
                    text = stringResource(R.string.home_permission_settings_message),
                    style = MaterialTheme.typography.bodyLarge
                )

                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.permission_open_settings))
                }
            }
        }

        Button(
            onClick = onOpenWifiDebug,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.debug_open_wifi))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    MeshScoutTheme {
        HomeScreen()
    }
}
