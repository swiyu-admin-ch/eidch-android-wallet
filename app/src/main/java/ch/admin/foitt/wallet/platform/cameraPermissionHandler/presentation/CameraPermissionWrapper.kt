package ch.admin.foitt.wallet.platform.cameraPermissionHandler.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.OnPermissionResult
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.PermissionState
import ch.admin.foitt.wallet.platform.permission.presentation.PermissionBlockedScreenContent
import ch.admin.foitt.wallet.platform.permission.presentation.PermissionIntroScreenContent
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.platform.utils.openAppDetailsSettings
import ch.admin.foitt.wallet.theme.WalletTheme

private val cameraPermission = Manifest.permission.CAMERA
private fun hasPermission(
    currentActivity: FragmentActivity
) = ActivityCompat.checkSelfPermission(currentActivity.applicationContext, cameraPermission) == PackageManager.PERMISSION_GRANTED
private fun shouldShowRationale(currentActivity: FragmentActivity) = ActivityCompat.shouldShowRequestPermissionRationale(
    currentActivity,
    cameraPermission,
)

@Composable
fun CameraPermissionWrapper(
    permissionState: PermissionState,
    onCameraPermissionResult: OnPermissionResult,
    permissionGrantedContent: @Composable () -> Unit,
) {
    val currentActivity = LocalActivity.current

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { permissionsGranted ->
            val shouldShowRationale = shouldShowRationale(currentActivity)

            onCameraPermissionResult(
                permissionGranted = permissionsGranted,
                shouldShowRationale = shouldShowRationale,
                isActivePrompt = true,
            )
        },
    )

    fun onOpenSettings() = currentActivity.openAppDetailsSettings()

    fun onPrompt() = cameraPermissionLauncher.launch(cameraPermission)

    OnResumeEventHandler {
        val hasPermission = hasPermission(currentActivity)
        val shouldShowRationale = shouldShowRationale(currentActivity)
        onCameraPermissionResult(
            permissionGranted = hasPermission,
            shouldShowRationale = shouldShowRationale,
            isActivePrompt = false,
        )
    }

    LaunchedEffect(permissionState) {
        val hasPermission = hasPermission(currentActivity)
        if (permissionState is PermissionState.AutoPrompt && !hasPermission) {
            onPrompt()
        }
    }

    CameraPermissionScreenContent(
        permissionState = permissionState,
        onAllow = ::onPrompt,
        onOpenSettings = ::onOpenSettings,
        permissionGrantedContent = permissionGrantedContent,
    )
}

@Composable
private fun CameraPermissionScreenContent(
    permissionState: PermissionState,
    onAllow: () -> Unit,
    onOpenSettings: () -> Unit,
    permissionGrantedContent: @Composable () -> Unit,
) = when (permissionState) {
    PermissionState.AutoPrompt,
    PermissionState.Initial -> {}

    PermissionState.Granted -> permissionGrantedContent()

    PermissionState.Blocked -> PermissionBlockedScreenContent(
        onOpenSettings = onOpenSettings,
    )

    PermissionState.Intro,
    PermissionState.ManualPrompt -> PermissionIntroScreenContent(
        onPrompt = onAllow,
    )

    PermissionState.Rationale -> PermissionRationalScreenContent(
        onPrompt = onAllow,
    )
}

@WalletComponentPreview
@Composable
private fun CameraPermissionScreenPreview() {
    WalletTheme {
        CameraPermissionScreenContent(
            permissionState = PermissionState.Intro,
            onAllow = {},
            onOpenSettings = {},
            permissionGrantedContent = {
                Text(text = "Permission granted")
            }
        )
    }
}
