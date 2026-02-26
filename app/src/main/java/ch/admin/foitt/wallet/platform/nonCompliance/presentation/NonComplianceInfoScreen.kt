package ch.admin.foitt.wallet.platform.nonCompliance.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.clusterLazyListItem
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.composables.presentation.verticalSafeDrawing
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportReason
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun NonComplianceInfoScreen(viewModel: NonComplianceInfoViewModel) {
    NonComplianceInfoScreenContent(
        reportReason = viewModel.reportReason,
        onMoreInformation = viewModel::onMoreInformation,
        onContinue = viewModel::onContinue,
    )
}

@Composable
private fun NonComplianceInfoScreenContent(
    reportReason: NonComplianceReportReason,
    onMoreInformation: () -> Unit,
    onContinue: () -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxSize()
        .background(color = WalletTheme.colorScheme.surfaceContainerLow)
) {
    val buttonHeight = remember { mutableStateOf(0.dp) }

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

        item { Spacer(modifier = Modifier.height(Sizes.s02)) }

        nonComplianceReportReasonListItem(
            nonComplianceReportReason = reportReason,
            onMoreInformation = onMoreInformation,
        )

        item { Spacer(modifier = Modifier.height(Sizes.s02)) }

        item {
            WalletTexts.LabelMedium(
                modifier = Modifier.padding(horizontal = Sizes.s04),
                text = stringResource(R.string.tk_nonCompliance_report_info_footer),
            )
        }

        item { Spacer(modifier = Modifier.height(buttonHeight.value)) }
    }

    HeightReportingLayout(
        modifier = Modifier
            .widthIn(max = Sizes.contentMaxWidth)
            .align(Alignment.BottomCenter)
            .horizontalSafeDrawing()
            .verticalSafeDrawing(),
        onContentHeightMeasured = { measuredHeight ->
            buttonHeight.value = measuredHeight
        }
    ) {
        Buttons.FilledPrimary(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Sizes.s04)
                .padding(bottom = Sizes.s04),
            text = stringResource(R.string.tk_global_continue),
            onClick = onContinue,
        )
    }
}

private fun LazyListScope.nonComplianceReportReasonListItem(
    nonComplianceReportReason: NonComplianceReportReason,
    onMoreInformation: () -> Unit,
) {
    clusterLazyListItem(
        isFirstItem = true,
        isLastItem = false,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Sizes.s04, vertical = Sizes.s06),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Sizes.s01)
        ) {
            Icon(
                modifier = Modifier
                    .size(Sizes.s16)
                    .padding(Sizes.s02),
                painter = painterResource(R.drawable.wallet_ic_lightbulb),
                contentDescription = null,
                tint = WalletTheme.colorScheme.onSurface,
            )

            WalletTexts.TitleMedium(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { heading() },
                text = stringResource(R.string.tk_nonCompliance_report_info_title),
                textAlign = TextAlign.Center,
            )

            val body = when (nonComplianceReportReason) {
                NonComplianceReportReason.EXCESSIVE_DATA_REQUEST -> R.string.tk_nonCompliance_report_info_body
            }

            WalletTexts.BodyMedium(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(body),
                textAlign = TextAlign.Center,
            )
        }
    }
    clusterLazyListItem(
        isFirstItem = false,
        isLastItem = true,
    ) {
        ExternalLinkListItem(onClick = onMoreInformation)
    }
}

@Composable
fun ExternalLinkListItem(
    onClick: () -> Unit,
) {
    val title = stringResource(R.string.tk_nonCompliance_report_info_moreInformation_link_text)

    ListItem(
        modifier = Modifier
            .clickable(onClick = onClick)
            .spaceBarKeyClickable(onClick),
        colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val linkAltText = stringResource(R.string.tk_global_externalLink_alt)
                WalletTexts.BodyMedium(
                    modifier = Modifier.semantics {
                        contentDescription = "$title $linkAltText"
                        role = Role.Button
                    },
                    text = title,
                    color = WalletTheme.colorScheme.error,
                )
                Icon(
                    painter = painterResource(R.drawable.wallet_ic_chevron_medium),
                    contentDescription = null,
                    tint = WalletTheme.colorScheme.error,
                )
            }
        },
    )
}

@WalletAllScreenPreview
@Composable
private fun NonComplianceInfoScreenPreview() {
    WalletTheme {
        NonComplianceInfoScreenContent(
            reportReason = NonComplianceReportReason.EXCESSIVE_DATA_REQUEST,
            onMoreInformation = {},
            onContinue = {}
        )
    }
}
