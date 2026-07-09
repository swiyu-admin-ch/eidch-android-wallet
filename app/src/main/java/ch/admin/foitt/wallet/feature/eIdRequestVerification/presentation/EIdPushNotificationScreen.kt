package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.EIdPushNotificationUiState
import ch.admin.foitt.wallet.platform.composables.AdaptiveBottomButtonBar
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.StandardErrorScreen
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.nonFocusableAccessibilityAnchor
import ch.admin.foitt.wallet.platform.permission.domain.model.NoOpAccompanistPermissionState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.platform.utils.openAppDetailsSettings
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EIdPushNotificationScreen(
    viewModel: EIdPushNotificationViewModel
) {
    val currentActivity = LocalActivity.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    BackHandler(enabled = true, viewModel::onClose)

    val notificationPermissionState: PermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            onPermissionResult = viewModel::handlePermissionResult,
        )
    } else {
        remember {
            NoOpAccompanistPermissionState(
                permission = "android.permission.POST_NOTIFICATIONS",
                status = PermissionStatus.Granted,
                onPermissionResult = viewModel::handlePermissionResult,
            )
        }
    }

    LaunchedEffect(notificationPermissionState.status) {
        // Android 13+: permission granted auto-setup notification
        // Android 12-: Let the user choose
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && notificationPermissionState.status == PermissionStatus.Granted) {
            viewModel.handlePermissionResult(true)
        }
    }

    when (uiState) {
        EIdPushNotificationUiState.Initial -> {
            EIdPushNotificationContent(
                onAccept = { notificationPermissionState.launchPermissionRequest() },
                onSkip = viewModel::skipPushNotification,
            )
        }

        EIdPushNotificationUiState.Loading -> {
            LoadingOverlay(true)
        }

        EIdPushNotificationUiState.SettingsRationale -> {
            EIdPushNotificationPermissionDeniedContent(
                onGoToSettings = currentActivity::openAppDetailsSettings,
                onSkip = viewModel::skipPushNotification,
            )
        }

        is EIdPushNotificationUiState.Error -> {
            WalletLayouts.StandardErrorScreen(
                primaryText = R.string.tk_pushNotificationPermission_setup_error_primary,
                secondaryText = R.string.tk_pushNotificationPermission_setup_error_secondary,
                primaryActionText = R.string.tk_global_retry,
                secondaryActionText = R.string.tk_pushNotificationPermission_secondaryButton,
                primaryAction = uiState.onRetry,
                secondaryAction = uiState.onSkip,
            )
        }
    }
}

@Composable
private fun EIdPushNotificationContent(
    onAccept: () -> Unit,
    onSkip: () -> Unit,
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                iconRes = R.drawable.wallet_ic_push_notification_colored,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
            )
        },
        stickyBottomContent = {
            AdaptiveBottomButtonBar(
                buttons = listOf(
                    {
                        Buttons.FilledPrimary(
                            text = stringResource(R.string.tk_global_continue_button),
                            onClick = onAccept,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    {
                        Buttons.TonalSecondary(
                            text = stringResource(R.string.tk_pushNotificationPermission_secondaryButton),
                            onClick = onSkip,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                )
            )
        }
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))

        WalletTexts.TitleScreen(
            text = stringResource(R.string.tk_pushNotificationPermission_title),
        )

        Spacer(modifier = Modifier.height(Sizes.s06))

        WalletTexts.BodyLarge(
            text = stringResource(R.string.tk_pushNotificationPermission_body),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EIdPushNotificationPermissionDeniedContent(
    onGoToSettings: () -> Unit,
    onSkip: () -> Unit,
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                iconRes = R.drawable.wallet_ic_push_notification_off,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
            )
        },
        stickyBottomContent = {
            AdaptiveBottomButtonBar(
                buttons = listOf(
                    {
                        Buttons.FilledPrimary(
                            text = stringResource(R.string.tk_global_tothesettings),
                            onClick = onGoToSettings,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    {
                        Buttons.TonalSecondary(
                            text = stringResource(R.string.tk_pushNotificationPermission_secondaryButton),
                            onClick = onSkip,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                )
            )
        }
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))

        WalletTexts.TitleScreen(
            text = stringResource(R.string.tk_pushNotificationPermission_denied_title),
            modifier = Modifier.nonFocusableAccessibilityAnchor(),
        )

        Spacer(modifier = Modifier.height(Sizes.s06))

        WalletTexts.BodyLarge(
            text = stringResource(R.string.tk_pushNotificationPermission_denied_body),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun EIdPushNotificationContentPreview() {
    WalletTheme {
        EIdPushNotificationContent(
            onAccept = {},
            onSkip = {},
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun EIdPushNotificationPermissionDeniedContentPreview() {
    WalletTheme {
        EIdPushNotificationPermissionDeniedContent(
            onGoToSettings = {},
            onSkip = {},
        )
    }
}
