package ch.admin.foitt.wallet.feature.settings.presentation

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
import ch.admin.foitt.wallet.platform.composables.presentation.addTopScaffoldPadding
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletListItems
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    SettingsScreenContent(
        onSecurityAndPrivacy = viewModel::onSecurityAndPrivacy,
        onLanguage = viewModel::onLanguage,
        onHelp = viewModel::onHelp,
        onFeedback = viewModel::onFeedback,
        onLicences = viewModel::onLicenses,
        onImprint = viewModel::onImprint,
        onDevsSettings = viewModel.onDevsViewer,
        otpEnabled = viewModel.otpBypassValue.collectAsStateWithLifecycle(false).value,
        onLottie = viewModel.onLottieViewer,
        onChangeOtpBypass = viewModel::onChangeOtpBypass
    )
}

@Composable
private fun SettingsScreenContent(
    otpEnabled: Boolean,
    onSecurityAndPrivacy: () -> Unit,
    onLanguage: () -> Unit,
    onHelp: () -> Unit,
    onFeedback: () -> Unit,
    onLicences: () -> Unit,
    onImprint: () -> Unit,
    onChangeOtpBypass: () -> Unit,
    onDevsSettings: Boolean,
    onLottie: (() -> Unit)?,
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
        WalletSection(
            onSecurityAndPrivacy = onSecurityAndPrivacy,
            onLanguage = onLanguage,
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        GeneralSection(
            onHelp = onHelp,
            onFeedback = onFeedback,
            onLicenses = onLicences,
            onImprint = onImprint,
        )
        if (onDevsSettings) {
            Spacer(modifier = Modifier.height(Sizes.s06))
            DevsSection(
                otpEnabled = otpEnabled,
                onLottie = onLottie,
                onChangeOtpBypass = onChangeOtpBypass,
            )
        }
    }
}

@Composable
private fun WalletSection(
    onSecurityAndPrivacy: () -> Unit,
    onLanguage: () -> Unit,
) = SettingsSection(
    title = stringResource(R.string.tk_settings_wallet_sectionTitle)
) {
    WalletListItems.ClickableTextSettingsItem(
        title = stringResource(R.string.tk_settings_wallet_securityPrivacy),
        leadingIcon = R.drawable.wallet_ic_lock_small,
        onClick = onSecurityAndPrivacy,
    )
    WalletListItems.Divider()
    WalletListItems.ClickableTextSettingsItem(
        title = stringResource(R.string.tk_settings_wallet_language),
        leadingIcon = R.drawable.wallet_ic_globe,
        onClick = onLanguage,
    )
}

@Composable
private fun DevsSection(
    otpEnabled: Boolean,
    onLottie: (() -> Unit)?,
    onChangeOtpBypass: () -> Unit
) = SettingsSection(
    title = stringResource(R.string.tk_settings_devs_title)
) {
    onLottie?.let {
        WalletListItems.ClickableTextSettingsItem(
            title = "Lottie animation",
            leadingIcon = R.drawable.wallet_ic_questionmark,
            onClick = onLottie,
        )
        WalletListItems.Divider()
    }
    WalletListItems.SwitchSettingsItem(
        title = stringResource(R.string.tk_eidRequest_otp_settings_toggle),
        subtitle = null,
        isSwitchChecked = otpEnabled,
        onSwitchChange = { onChangeOtpBypass() },
    )
}

@Composable
private fun GeneralSection(
    onHelp: () -> Unit,
    onFeedback: () -> Unit,
    onLicenses: () -> Unit,
    onImprint: () -> Unit,
) = SettingsSection(
    title = stringResource(R.string.tk_settings_general_sectionTitle)
) {
    WalletListItems.LinkSettingsItem(
        title = stringResource(R.string.tk_settings_general_help_link_text),
        leadingIcon = R.drawable.wallet_ic_questionmark,
        onClick = onHelp,
    )
    WalletListItems.Divider()
    WalletListItems.LinkSettingsItem(
        title = stringResource(R.string.tk_settings_general_feedback_link_text),
        leadingIcon = R.drawable.wallet_ic_feedback,
        onClick = onFeedback,
    )
    WalletListItems.Divider()
    WalletListItems.ClickableTextSettingsItem(
        title = stringResource(R.string.tk_settings_general_licences),
        leadingIcon = R.drawable.wallet_ic_licenses,
        onClick = onLicenses,
    )
    WalletListItems.Divider()
    WalletListItems.ClickableTextSettingsItem(
        title = stringResource(R.string.tk_settings_general_imprint),
        leadingIcon = R.drawable.wallet_ic_info,
        onClick = onImprint,
    )
}

private class DevsScreenPreviewParams : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(
        false,
        true,
    )
}

@WalletAllScreenPreview
@Composable
fun SettingsScreenPreview(
    @PreviewParameter(DevsScreenPreviewParams::class) previewParams: Boolean,
) {
    WalletTheme {
        SettingsScreenContent(
            onSecurityAndPrivacy = {},
            onLanguage = {},
            onHelp = {},
            onFeedback = {},
            onLicences = {},
            onImprint = {},
            onDevsSettings = true,
            otpEnabled = previewParams,
            onLottie = {},
            onChangeOtpBypass = {}
        )
    }
}
