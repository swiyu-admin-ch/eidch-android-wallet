package ch.admin.foitt.wallet.feature.settings.presentation.impressum

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.BuildConfig
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.settings.presentation.composables.LinkSettingsItem
import ch.admin.foitt.wallet.feature.settings.presentation.composables.SettingsSection
import ch.admin.foitt.wallet.feature.settings.presentation.composables.SpecialLinkSettingsItem
import ch.admin.foitt.wallet.feature.settings.presentation.composables.TextSettingsItem
import ch.admin.foitt.wallet.feature.settings.presentation.composables.VersionSettingsItem
import ch.admin.foitt.wallet.platform.composables.presentation.addTopScaffoldPadding
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletListItems
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun ImpressumScreen(viewModel: ImpressumViewModel) {
    ImpressumScreenContent(
        onGithub = viewModel::onGithub,
        onMoreInformation = viewModel::onMoreInformation,
        onLegals = viewModel::onLegals,
    )
}

@Composable
private fun ImpressumScreenContent(
    onGithub: () -> Unit,
    onMoreInformation: () -> Unit,
    onLegals: () -> Unit,
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
        AppVersionSection(
            onGithub = onGithub,
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        PublisherSection(
            onMoreInformation = onMoreInformation,
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        LegalSection(
            onLegals = onLegals,
        )
    }
}

@Composable
private fun AppVersionSection(
    onGithub: () -> Unit,
) = SettingsSection(
    title = stringResource(R.string.tk_settings_imprint_appInformation_sectionTitle),
) {
    WalletListItems.VersionSettingsItem(
        title = stringResource(R.string.tk_settings_imprint_appInformation_buildNumber),
        version = BuildConfig.VERSION_CODE.toString()
    )
    WalletListItems.Divider()
    WalletListItems.VersionSettingsItem(
        title = stringResource(R.string.tk_settings_imprint_appInformation_appVersion),
        version = BuildConfig.VERSION_NAME
    )
    WalletListItems.Divider()
    WalletListItems.TextSettingsItem(
        title = stringResource(R.string.tk_settings_imprint_appInformation_body)
    )
    WalletListItems.SpecialLinkSettingsItem(
        title = stringResource(R.string.tk_settings_imprint_appInformation_github_link_text),
        onClick = onGithub
    )
}

@Composable
private fun PublisherSection(
    onMoreInformation: () -> Unit,
) = SettingsSection(
    title = stringResource(R.string.tk_settings_imprint_publisher_sectionTitle),
) {
    Image(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Sizes.s04, top = Sizes.s04, end = Sizes.s04),
        painter = painterResource(id = R.drawable.wallet_ic_bit_info),
        contentDescription = stringResource(R.string.tk_settings_imprint_publisher_name),
        contentScale = ContentScale.FillWidth,
    )
    WalletListItems.SpecialLinkSettingsItem(
        title = stringResource(R.string.tk_settings_imprint_publisher_link_text),
        onClick = onMoreInformation
    )
}

@Composable
private fun LegalSection(
    onLegals: () -> Unit,
) = SettingsSection(
    title = stringResource(R.string.tk_settings_imprint_legal_sectionTitle),
) {
    WalletListItems.LinkSettingsItem(
        title = stringResource(R.string.tk_settings_imprint_legal_termsOfUse_link_text),
        onClick = onLegals,
        leadingIcon = R.drawable.wallet_ic_letter
    )
    WalletListItems.Divider()
    WalletListItems.TextSettingsItem(
        title = stringResource(R.string.tk_settings_imprint_legal_disclaimer_primary),
        subtitle = stringResource(R.string.tk_settings_imprint_legal_disclaimer_secondary)
    )
}

@WalletAllScreenPreview
@Composable
fun ImpressumScreenPreview() {
    WalletTheme {
        ImpressumScreenContent(
            onGithub = {},
            onMoreInformation = {},
            onLegals = {},
        )
    }
}
