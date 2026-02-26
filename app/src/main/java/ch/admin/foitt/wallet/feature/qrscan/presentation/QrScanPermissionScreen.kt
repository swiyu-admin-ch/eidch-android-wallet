package ch.admin.foitt.wallet.feature.qrscan.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.PermissionState
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.presentation.PermissionBlockedScreenContent
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.presentation.PermissionIntroScreenContent
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.presentation.PermissionRationalScreenContent
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun QrScanPermissionScreen(viewModel: QrScanPermissionViewModel) {
    val currentActivity = LocalActivity.current
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { permissionsGranted ->
            viewModel.onCameraPermissionResult(
                permissionGranted = permissionsGranted,
                activity = currentActivity,
            )
        },
    )

    SideEffect {
        viewModel.setPermissionLauncher(cameraPermissionLauncher)
    }

    LaunchedEffect(viewModel) {
        viewModel.navigateToFirstScreen(currentActivity)
    }

    QrScanPermissionScreenContent(
        permissionState = viewModel.permissionState.collectAsStateWithLifecycle().value,
        onAllow = viewModel::onCameraPermissionPrompt,
        onOpenSettings = viewModel::onOpenSettings,
    )
}

@Composable
private fun QrScanPermissionScreenContent(
    permissionState: PermissionState,
    onAllow: () -> Unit,
    onOpenSettings: () -> Unit,
) = when (permissionState) {
    PermissionState.Granted,
    PermissionState.Initial -> {}
    PermissionState.Blocked -> PermissionBlockedScreenContent(
        onOpenSettings = onOpenSettings,
    )
    PermissionState.Intro -> PermissionIntroScreenContent(
        onAllow = onAllow,
    )
    PermissionState.Rationale -> PermissionRationalScreenContent(
        onAllow = onAllow,
    )
}

@WalletComponentPreview
@Composable
private fun QrScanPermissionScreenPreview() {
    WalletTheme {
        QrScanPermissionScreenContent(
            permissionState = PermissionState.Intro,
            onAllow = {},
            onOpenSettings = {},
        )
    }
}
