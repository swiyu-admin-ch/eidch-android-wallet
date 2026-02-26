package ch.admin.foitt.wallet.platform.nonCompliance.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.clusterLazyListItem
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportReason
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun NonComplianceListScreen(viewModel: NonComplianceListViewModel) {
    NonComplianceListScreenContent(
        reasons = viewModel.reasons,
        onReason = viewModel::onReason,
    )
}

@Composable
private fun NonComplianceListScreenContent(
    reasons: List<NonComplianceReportReason>,
    onReason: (NonComplianceReportReason) -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxSize()
        .background(color = WalletTheme.colorScheme.surfaceContainerLow)
) {
    WalletLayouts.LazyColumn(
        modifier = Modifier
            .widthIn(max = Sizes.contentMaxWidth)
            .horizontalSafeDrawing()
            .align(Alignment.TopCenter),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(start = Sizes.s04, top = Sizes.s02, end = Sizes.s04, bottom = Sizes.s04),
        useTopInsets = false,
    ) {
        item {
            WalletLayouts.TopInsetSpacer(
                shouldScrollUnderTopBar = true,
                scaffoldPaddings = LocalScaffoldPaddings.current,
            )
        }

        reasons.forEachIndexed { index, reason ->
            nonComplianceListItem(
                nonComplianceReportReason = reason,
                isFirstItem = reasons.indices.first == index,
                isLastItem = reasons.indices.last == index,
                onClick = onReason,
            )
        }

        item { Spacer(modifier = Modifier.height(Sizes.s02)) }

        item {
            WalletTexts.LabelMedium(
                modifier = Modifier.padding(horizontal = Sizes.s04),
                text = stringResource(R.string.tk_nonCompliance_list_footer),
            )
        }
    }
}

private fun LazyListScope.nonComplianceListItem(
    nonComplianceReportReason: NonComplianceReportReason,
    isFirstItem: Boolean,
    isLastItem: Boolean,
    onClick: (NonComplianceReportReason) -> Unit,
) = clusterLazyListItem(
    isFirstItem = isFirstItem,
    isLastItem = isLastItem,
) {
    val (title, subtitle) = when (nonComplianceReportReason) {
        NonComplianceReportReason.EXCESSIVE_DATA_REQUEST ->
            R.string.tk_nonCompliance_list_excessiveData_title to R.string.tk_nonCompliance_list_excessiveData_body
    }
    ListItem(
        modifier = Modifier
            .clickable { onClick(nonComplianceReportReason) }
            .semantics {
                role = Role.Button
            }
            .spaceBarKeyClickable { onClick(nonComplianceReportReason) },
        colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
        headlineContent = {
            Column {
                WalletTexts.BodyLarge(
                    text = stringResource(title),
                    color = WalletTheme.colorScheme.onSurface,
                )
                WalletTexts.BodyMedium(
                    text = stringResource(subtitle),
                    color = WalletTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        trailingContent = {
            Icon(
                painter = painterResource(R.drawable.wallet_ic_chevron_right),
                contentDescription = null,
                tint = WalletTheme.colorScheme.onSurface,
            )
        }
    )
}

@WalletAllScreenPreview
@Composable
private fun NonComplianceListScreenPreview() {
    WalletTheme {
        NonComplianceListScreenContent(
            reasons = listOf(NonComplianceReportReason.EXCESSIVE_DATA_REQUEST),
            onReason = {}
        )
    }
}
