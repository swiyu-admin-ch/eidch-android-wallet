package ch.admin.foitt.wallet.platform.permission.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@SuppressLint("ComposeModifierReused")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScaffold(
    permissions: List<String>,
    modifier: Modifier = Modifier,
    requestImmediately: Boolean = false,
    permissionRationaleContent: @Composable (onHandled: () -> Unit) -> Unit = { _ -> },
    permissionNotGrantedContent: @Composable (handler: PermissionHandler) -> Unit = {},
    permissionGrantedContent: @Composable () -> Unit = {},
    onPermissionGranted: () -> Unit = {},
) {
    if (LocalInspectionMode.current) {
        // Treat permission as granted for previews
        Box(modifier = modifier) {
            permissionGrantedContent()
        }
        return
    }

    var showPermissionRationale by rememberSaveable { mutableStateOf(false) }
    var shouldRequestPermission by rememberSaveable { mutableStateOf(false) }
    var wasDenied by rememberSaveable { mutableStateOf(false) }

    val permissionState = rememberMultiplePermissionsState(permissions) { grants ->
        val isGranted = grants.all { (_, isGranted) -> isGranted }
        if (isGranted) {
            wasDenied = false
            onPermissionGranted()
        } else {
            wasDenied = true
        }

        showPermissionRationale = false
        shouldRequestPermission = false
    }

    Box(modifier = modifier) {
        // Display the appropriate content based on the permission state
        if (permissionState.allPermissionsGranted) {
            permissionGrantedContent()
        } else if (showPermissionRationale) {
            permissionRationaleContent.invoke {
                wasDenied = false
            }
        } else {
            val callback = PermissionHandler {
                shouldRequestPermission = true
            }
            permissionNotGrantedContent(callback)
        }
    }

    LaunchedEffect(wasDenied) {
        if (wasDenied && !permissionState.shouldShowRationale) {
            showPermissionRationale = true
        }
    }

    LaunchedEffect(requestImmediately) {
        // If the permission should be requested immediately (as opposed to a user action in the [permissionNotGrantedContent]), set the flag to request the permission
        if (requestImmediately) {
            shouldRequestPermission = true
        }
    }

    LaunchedEffect(shouldRequestPermission) {
        // Actively request the permission or trigger the rationale if the flag is set
        if (shouldRequestPermission) {
            requestPermissionOrSettingsChange(
                permission = permissions.first(),
                isGranted = permissionState.allPermissionsGranted,
                onRequestPermission = {
                    wasDenied = false
                    permissionState.launchMultiplePermissionRequest()
                },
                onPermissionGranted = {
                    showPermissionRationale = false
                },
                onShowRationale = {
                    showPermissionRationale = true
                },
            )

            shouldRequestPermission = false
        }
    }
}

private fun requestPermissionOrSettingsChange(
    permission: String,
    isGranted: Boolean,
    onRequestPermission: () -> Unit,
    onPermissionGranted: () -> Unit,
    onShowRationale: () -> Unit,
) {
    when {
        isGranted -> {
            // Permission is granted, immediately invoke the callback
            onPermissionGranted()
        }
        permission == Manifest.permission.ACCESS_FINE_LOCATION -> {
            onRequestPermission()
        }
        Build.VERSION.SDK_INT < getPermissionIntroducedInSdkVersion(permission) -> {
            // The permission launcher does nothing if the permission was introduced in a higher SDK version, show the rationale to jump to the app settings
            onShowRationale()
        }
        else -> {
            // Request the permission
            onRequestPermission()
        }
    }
}

private fun getPermissionIntroducedInSdkVersion(permission: String): Int {
    return when (permission) {
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN -> Build.VERSION_CODES.S
        else -> Build.VERSION_CODES.BASE
    }
}
