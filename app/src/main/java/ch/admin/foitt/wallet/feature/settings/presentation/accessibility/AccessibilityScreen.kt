package ch.admin.foitt.wallet.feature.settings.presentation.accessibility

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.settings.presentation.composables.LinkSettingsItem
import ch.admin.foitt.wallet.feature.settings.presentation.composables.SettingsCard
import ch.admin.foitt.wallet.platform.composables.presentation.addTopScaffoldPadding
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletListItems
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun AccessibilityScreen(viewModel: AccessibilityViewModel) {
    AccessibilityScreenContent(
        onDeclaration = viewModel::onDeclaration,
        onReportIssue = viewModel::onReportIssue
    )
}

@Composable
private fun AccessibilityScreenContent(
    onDeclaration: () -> Unit,
    onReportIssue: () -> Unit
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
        AccessibilityLinksSection(
            onDeclaration = onDeclaration,
            onReportIssue = onReportIssue
        )
    }
}

@Composable
private fun AccessibilityLinksSection(
    onDeclaration: () -> Unit,
    onReportIssue: () -> Unit
) {
    SettingsCard {
        WalletListItems.LinkSettingsItem(
            title = stringResource(R.string.tk_settings_accessibility_declaration_link_text),
            leadingIcon = R.drawable.wallet_ic_letter,
            onClick = onDeclaration,
        )
        WalletListItems.Divider()
        WalletListItems.LinkSettingsItem(
            title = stringResource(R.string.tk_settings_accessibility_report_issue_link_text),
            leadingIcon = R.drawable.wallet_ic_feedback,
            onClick = onReportIssue,
        )
    }
}
