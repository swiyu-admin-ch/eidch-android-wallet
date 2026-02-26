package ch.admin.foitt.wallet.feature.settings.presentation.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.settings.presentation.composables.SettingsCard
import ch.admin.foitt.wallet.feature.settings.presentation.composables.TextSettingsItem
import ch.admin.foitt.wallet.platform.composables.presentation.addTopScaffoldPadding
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletListItems
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun DataAnalysisScreen() {
    DataAnalysisScreenContent()
}

@Composable
private fun DataAnalysisScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WalletTheme.colorScheme.surfaceContainerLow)
            .addTopScaffoldPadding()
            .verticalScroll(rememberScrollState())
            .horizontalSafeDrawing()
            .bottomSafeDrawing()
    ) {
        SettingsCard(
            modifier = Modifier.padding(Sizes.s04)
        ) {
            WalletTexts.BodyMedium(
                text = stringResource(id = R.string.tk_settings_diagnosticData_body),
                color = WalletTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Sizes.s02))
            WalletListItems.TextSettingsItem(
                title = stringResource(R.string.tk_settings_diagnosticData_generalError),
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.wallet_ic_checkmark_big),
                        contentDescription = null
                    )
                }
            )
            WalletListItems.TextSettingsItem(
                title = stringResource(R.string.tk_settings_diagnosticData_communicationError),
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.wallet_ic_checkmark_big),
                        contentDescription = null
                    )
                }
            )
            WalletListItems.TextSettingsItem(
                title = stringResource(R.string.tk_settings_diagnosticData_appCrash),
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.wallet_ic_checkmark_big),
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@WalletAllScreenPreview
@Composable
private fun DataAnalysisScreenContentPreview() {
    WalletTheme {
        DataAnalysisScreenContent()
    }
}
