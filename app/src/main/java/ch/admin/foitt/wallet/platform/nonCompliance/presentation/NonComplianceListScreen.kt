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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.clusterLazyListItem
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceIssuerReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceVerifierReportReason
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTexts.TitleMediumEmphasized
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun NonComplianceListScreen(viewModel: NonComplianceListViewModel) {
    NonComplianceListScreenContent(
        reasons = viewModel.reasons,
        onReason = viewModel::onReason,
        onMoreInformation = viewModel::onMoreInformation,
        onPrivacy = viewModel::onPrivacy
    )
}

@Composable
private fun NonComplianceListScreenContent(
    reasons: List<NonComplianceReportReason>,
    onReason: (NonComplianceReportReason) -> Unit,
    onMoreInformation: () -> Unit,
    onPrivacy: () -> Unit
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

        nonComplianceReportReasonListItem(
            onMoreInformation = onMoreInformation,
            onPrivacy = onPrivacy
        )

        item { Spacer(modifier = Modifier.height(Sizes.s06)) }

        item {
            TitleMediumEmphasized(
                text = stringResource(R.string.tk_nonCompliance_list_title),
                modifier = Modifier
                    .padding(PaddingValues(start = Sizes.s04, end = Sizes.s04, bottom = Sizes.s02))
                    .semantics { heading() },
                color = WalletTheme.colorScheme.secondary,
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
    }
}

private fun LazyListScope.nonComplianceReportReasonListItem(
    onMoreInformation: () -> Unit,
    onPrivacy: () -> Unit,
) {
    clusterLazyListItem(
        isFirstItem = true,
        isLastItem = false,
        divider = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Sizes.s04, vertical = Sizes.s06),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(Sizes.s01)
        ) {
            Icon(
                modifier = Modifier
                    .size(Sizes.s16),
                painter = painterResource(R.drawable.wallet_ic_flag_colored),
                contentDescription = null,
                tint = Color.Unspecified,
            )

            WalletTexts.TitleMedium(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { heading() },
                text = stringResource(R.string.nonCompliance_reportIntro_title),
            )

            WalletTexts.BodyMedium(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.nonCompliance_reportIntro_body)
            )
        }
    }
    clusterLazyListItem(
        isFirstItem = false,
        isLastItem = false,
    ) {
        ExternalLinkListItem(
            title = stringResource(R.string.tk_nonCompliance_report_info_moreInformation_link_text),
            onClick = onMoreInformation
        )
    }

    clusterLazyListItem(
        isFirstItem = false,
        isLastItem = true,
    ) {
        ExternalLinkListItem(
            title = stringResource(R.string.nonCompliance_reportIntro_privacyStatement_linkText),
            onClick = onPrivacy
        )
    }
}

@Composable
fun ExternalLinkListItem(
    title: String,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .clickable(onClick = onClick)
            .spaceBarKeyClickable(onClick),
        colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val linkAltText = stringResource(R.string.tk_global_externalLink_hint)
                WalletTexts.BodyMedium(
                    modifier = Modifier.semantics {
                        contentDescription = "$title $linkAltText"
                        role = Role.Button
                    },
                    text = title,
                    color = WalletTheme.colorScheme.error,
                )
                Icon(
                    modifier = Modifier
                        .padding(start = Sizes.s01)
                        .size(Sizes.s04),
                    painter = painterResource(R.drawable.wallet_ic_external_link),
                    contentDescription = null,
                    tint = WalletTheme.colorScheme.error,
                )
            }
        },
    )
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
        NonComplianceVerifierReportReason.EXCESSIVE_DATA_REQUEST ->
            R.string.tk_nonCompliance_list_excessiveData_title to R.string.tk_nonCompliance_list_excessiveData_body
        NonComplianceIssuerReportReason.DUBIOUS_CREDENTIAL_ISSUANCE ->
            R.string.nonCompliance_reportType_dubiousCredentialIssuance_title to
                R.string.nonCompliance_reportType_dubiousCredentialIssuance_body
        NonComplianceIssuerReportReason.SENSITIVE_DATA_NOT_DECLARED ->
            R.string.nonCompliance_reportType_sensitiveDataNotDeclared_title to
                R.string.nonCompliance_reportType_sensitiveDataNotDeclared_body
        NonComplianceIssuerReportReason.FALSE_IDENTITY ->
            R.string.nonCompliance_reportType_falseIdentity_title to R.string.nonCompliance_reportType_falseIdentityIssuer_body
        NonComplianceIssuerReportReason.ILLEGAL_CONTENT_OR_PURPOSE ->
            R.string.nonCompliance_reportType_illegalContentOrPurpose_title to
                R.string.nonCompliance_reportType_illegalContentOrPurpose_body
        NonComplianceVerifierReportReason.VERIFICATION_NOT_REGISTERED ->
            R.string.nonCompliance_reportType_verificationNotRegistered_title to
                R.string.nonCompliance_reportType_verificationNotRegistered_body
        NonComplianceVerifierReportReason.FALSE_IDENTITY_VERIFIER ->
            R.string.nonCompliance_reportType_falseIdentity_title to R.string.nonCompliance_reportType_falseIdentityVerifier_body
        NonComplianceVerifierReportReason.OTHER,
        NonComplianceIssuerReportReason.OTHER ->
            R.string.nonCompliance_reportType_other_title to R.string.nonCompliance_reportType_other_body
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
            reasons = listOf(NonComplianceVerifierReportReason.EXCESSIVE_DATA_REQUEST),
            onReason = {},
            onMoreInformation = {},
            onPrivacy = {}
        )
    }
}
