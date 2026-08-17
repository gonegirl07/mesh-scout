package com.meshscout.app.ui.permissions

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** The states that matter to the permission flow shown to the user. */
enum class PermissionStatus {
    NotRequested,
    Granted,
    Denied,
    PermanentlyDenied
}

data class PermissionUiState(
    val status: PermissionStatus = PermissionStatus.NotRequested,
    val showRationale: Boolean = true,
    val showSettingsDialog: Boolean = false,
    val isRequestInProgress: Boolean = false
)

/**
 * Keeps permission decisions out of the composable and remembers that a request was made.
 *
 * The small SharedPreferences flag is intentional: Android reports both "not asked yet" and
 * "can't ask again" as shouldShowRequestPermissionRationale == false. Remembering that the
 * request was launched lets us distinguish those states after the app process is recreated.
 */
class PermissionViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = application.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    /**
     * Returns the permission group needed for Wi-Fi scan results on this Android version.
     *
     * Android 12 requires fine and coarse location to be requested together. MeshScout still
     * requires fine location on API 32 and below because Wi-Fi scan results are protected by it.
     */
    fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }

    /** Re-checks permission state, for example when the activity returns from App settings. */
    fun refresh(activity: Activity) {
        updateState(activity = activity, forceSettingsDialog = false)
    }

    fun onPermissionRequestStarted() {
        val requestedPermissions = preferences
            .getStringSet(KEY_REQUESTED_PERMISSIONS, emptySet<String>())
            .orEmpty()
            .toMutableSet()
        requestedPermissions += requiredPermissions().toSet()
        preferences.edit()
            .putStringSet(KEY_REQUESTED_PERMISSIONS, requestedPermissions)
            .apply()
        _uiState.update { state ->
            state.copy(isRequestInProgress = true)
        }
    }

    fun onPermissionResult(activity: Activity, result: Map<String, Boolean>) {
        // The callback map is used to make sure the progress indicator is cleared immediately.
        val permissionWasDenied = result.values.any { granted -> !granted }
        updateState(
            activity = activity,
            forceSettingsDialog = permissionWasDenied
        )
    }

    fun continueWithoutPermission() {
        _uiState.update { state ->
            state.copy(
                showRationale = false,
                showSettingsDialog = false,
                isRequestInProgress = false
            )
        }
    }

    fun dismissSettingsDialog() {
        _uiState.update { state ->
            state.copy(showSettingsDialog = false)
        }
    }

    private fun updateState(activity: Activity, forceSettingsDialog: Boolean) {
        val status = determineStatus(activity)
        _uiState.update { previousState ->
            val becamePermanentlyDenied =
                status == PermissionStatus.PermanentlyDenied &&
                    previousState.status != PermissionStatus.PermanentlyDenied

            previousState.copy(
                status = status,
                // "Not now" only dismisses the current rationale screen. A later refresh must
                // make the rationale available again while permission is still requestable.
                showRationale = status == PermissionStatus.NotRequested ||
                    status == PermissionStatus.Denied,
                showSettingsDialog = when {
                    status == PermissionStatus.Granted -> false
                    status == PermissionStatus.PermanentlyDenied &&
                        (forceSettingsDialog || becamePermanentlyDenied) -> true
                    status != PermissionStatus.PermanentlyDenied -> false
                    else -> previousState.showSettingsDialog
                },
                isRequestInProgress = false
            )
        }
    }

    private fun determineStatus(activity: Activity): PermissionStatus {
        val permissions = requiredPermissions()
        val hasRequestedCurrentPermissions = preferences
            .getStringSet(KEY_REQUESTED_PERMISSIONS, emptySet<String>())
            .orEmpty()
            .containsAll(permissions.toSet())
        val allGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                activity,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            return PermissionStatus.Granted
        }

        val hasPermissionThatCannotBeRequestedAgain = hasRequestedCurrentPermissions &&
            permissions.any { permission ->
                ContextCompat.checkSelfPermission(
                    activity,
                    permission
                ) != PackageManager.PERMISSION_GRANTED &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            }

        return when {
            hasPermissionThatCannotBeRequestedAgain -> PermissionStatus.PermanentlyDenied
            hasRequestedCurrentPermissions -> PermissionStatus.Denied
            else -> PermissionStatus.NotRequested
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "meshscout_permissions"
        const val KEY_REQUESTED_PERMISSIONS = "wifi_permission_requested_set"
    }
}
