package ch.admin.foitt.wallet.feature.settings.presentation.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.settings.presentation.composables.ClickableTextSettingsItem
import ch.admin.foitt.wallet.feature.settings.presentation.composables.LinkSettingsItem
import ch.admin.foitt.wallet.feature.settings.presentation.composables.SettingsSection
import ch.admin.foitt.wallet.feature.settings.presentation.composables.SwitchSettingsItem
import ch.admin.foitt.wallet.platform.composables.ToastAnimated
import ch.admin.foitt.wallet.platform.composables.presentation.addTopScaffoldPadding
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.OnPauseEventHandler
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletListItems
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun SecuritySettingsScreen(
    viewModel: SecuritySettingsViewModel
) {
    OnPauseEventHandler {
        viewModel.hidePassphraseChangeSuccessToast()
    }

    SecuritySettingsScreenContent(
        biometricsHardwareIsAvailable = viewModel.biometricsHardwareIsAvailable.collectAsStateWithLifecycle(true).value,
        biometricsEnabled = viewModel.isBiometricsToggleEnabled.collectAsStateWithLifecycle(false).value,
        shareAnalysisEnabled = viewModel.shareAnalysisEnabled.collectAsStateWithLifecycle().value,
        showPassphraseDeletionMessage = viewModel.showPassphraseDeletionMessage.collectAsStateWithLifecycle(false).value,
        isToastVisible = viewModel.isToastVisible.collectAsStateWithLifecycle(false).value,
        onChangePin = viewModel::onChangePassphrase,
        onChangeBiometrics = viewModel::onChangeBiometrics,
        onDataProtection = viewModel::onDataProtection,
        onShareAnalysisChange = viewModel::onShareAnalysisChange,
        onActivityList = viewModel::onActivityList,
        onDataAnalysis = viewModel::onDataAnalysis,
    )
}

@Composable
private fun SecuritySettingsScreenContent(
    biometricsHardwareIsAvailable: Boolean,
    biometricsEnabled: Boolean,
    shareAnalysisEnabled: Boolean,
    showPassphraseDeletionMessage: Boolean,
    isToastVisible: Boolean,
    onChangePin: () -> Unit,
    onChangeBiometrics: () -> Unit,
    onDataProtection: () -> Unit,
    onShareAnalysisChange: (Boolean) -> Unit,
    onActivityList: () -> Unit,
    onDataAnalysis: () -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxSize()
        .background(WalletTheme.colorScheme.surfaceContainerLow)
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .addTopScaffoldPadding()
            .verticalScroll(state = rememberScrollState())
            .horizontalSafeDrawing()
            .bottomSafeDrawing()
            .padding(
                bottom = Sizes.s04,
            )
    ) {
        SecuritySection(
            biometricsAvailableThroughDeviceSettings = biometricsHardwareIsAvailable,
            biometricsEnabled = biometricsEnabled,
            showPassphraseDeletionMessage = showPassphraseDeletionMessage,
            onChangePin = onChangePin,
            onChangeBiometrics = onChangeBiometrics,
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        AnalysisSection(
            shareAnalysisEnabled = shareAnalysisEnabled,
            onDataProtection = onDataProtection,
            onShareAnalysisChange = onShareAnalysisChange,
            onActivityList = onActivityList,
            onDataAnalysis = onDataAnalysis,
        )
    }
    ToastAnimated(
        isVisible = isToastVisible,
        isSnackBarDesign = false,
        messageToast = R.string.tk_changepassword_successful_notification,
        contentBottomPadding = Sizes.s10
    )
}

@Composable
private fun SecuritySection(
    biometricsAvailableThroughDeviceSettings: Boolean,
    biometricsEnabled: Boolean,
    showPassphraseDeletionMessage: Boolean,
    onChangePin: () -> Unit,
    onChangeBiometrics: () -> Unit
) = SettingsSection(
    title = stringResource(R.string.tk_settings_securityPrivacy_security_sectionTitle)
) {
    WalletListItems.ClickableTextSettingsItem(
        title = stringResource(R.string.tk_settings_securityPrivacy_security_changePassword),
        onClick = onChangePin,
    )
    WalletListItems.Divider()
    WalletListItems.SwitchSettingsItem(
        title = stringResource(R.string.tk_settings_securityPrivacy_security_unlock),
        subtitle = biometricsDescription(biometricsAvailableThroughDeviceSettings, showPassphraseDeletionMessage),
        isSwitchEnabled = biometricsAvailableThroughDeviceSettings,
        isSwitchChecked = biometricsEnabled,
        onSwitchChange = { onChangeBiometrics() },
    )
}

@Composable
fun AnalysisSection(
    shareAnalysisEnabled: Boolean,
    onShareAnalysisChange: (Boolean) -> Unit,
    onDataAnalysis: () -> Unit,
    onActivityList: () -> Unit,
    onDataProtection: () -> Unit,
) = SettingsSection(
    title = stringResource(R.string.tk_settings_securityPrivacy_dataProtection_sectionTitle)
) {
    WalletListItems.SwitchSettingsItem(
        title = stringResource(id = R.string.tk_settings_securityPrivacy_dataProtection_shareData_primary),
        subtitle = stringResource(R.string.tk_settings_securityPrivacy_dataProtection_shareData_secondary),
        isSwitchChecked = shareAnalysisEnabled,
        onSwitchChange = onShareAnalysisChange,
    )
    WalletListItems.Divider()
    WalletListItems.ClickableTextSettingsItem(
        title = stringResource(id = R.string.tk_settings_securityPrivacy_dataProtection_diagnosticData),
        onClick = onDataAnalysis,
    )
    WalletListItems.Divider()
    WalletListItems.ClickableTextSettingsItem(
        title = stringResource(id = R.string.tk_settings_securityPrivacy_dataProtection_activityHistory),
        leadingIcon = R.drawable.wallet_ic_history,
        onClick = onActivityList,
    )
    WalletListItems.Divider()
    WalletListItems.LinkSettingsItem(
        title = stringResource(id = R.string.tk_settings_securityPrivacy_dataProtection_privacyPolicy_link_text),
        onClick = onDataProtection,
        leadingIcon = R.drawable.wallet_ic_shield_person_small,
    )
}

@Composable
private fun biometricsDescription(biometricHardwareAvailable: Boolean, showPassphraseDeletionMessage: Boolean): String? {
    val stringId = when {
        !biometricHardwareAvailable -> R.string.tk_settings_securityPrivacy_biometrics_noHardware
        showPassphraseDeletionMessage -> R.string.tk_settings_securityPrivacy_biometrics_pinChanged
        else -> null
    }
    return stringId?.let { stringResource(id = it) }
}

private class SecuritySettingsPreviewParams : PreviewParameterProvider<Pair<Boolean, Boolean>> {
    override val values = sequenceOf(
        Pair(false, false),
        Pair(true, false),
        Pair(false, true),
        Pair(true, true),
    )
}

@WalletAllScreenPreview
@Composable
private fun SecuritySettingsScreenPreview(
    @PreviewParameter(SecuritySettingsPreviewParams::class) previewParams: Pair<Boolean, Boolean>,
) {
    WalletTheme {
        SecuritySettingsScreenContent(
            biometricsHardwareIsAvailable = previewParams.first,
            biometricsEnabled = previewParams.second,
            shareAnalysisEnabled = true,
            isToastVisible = false,
            onChangeBiometrics = {},
            onChangePin = {},
            onDataProtection = {},
            onShareAnalysisChange = {},
            onActivityList = {},
            onDataAnalysis = {},
            showPassphraseDeletionMessage = false,
        )
    }
}
