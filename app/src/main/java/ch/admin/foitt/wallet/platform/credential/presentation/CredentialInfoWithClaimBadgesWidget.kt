package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.platform.badges.domain.model.BadgeType
import ch.admin.foitt.wallet.platform.badges.presentation.NonSensitiveClaimInfoBadge
import ch.admin.foitt.wallet.platform.badges.presentation.SensitiveClaimInfoBadge
import ch.admin.foitt.wallet.platform.badges.presentation.model.ClaimBadgeUiState
import ch.admin.foitt.wallet.platform.composables.presentation.clusterLazyListItem
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

fun LazyListScope.credentialInfoWithClaimBadgesWidget(
    credentialCardState: CredentialCardState,
    claimBadgesUiStates: List<ClaimBadgeUiState> = emptyList(),
    onBadge: (BadgeType) -> Unit = {},
    paddingValues: PaddingValues = PaddingValues(
        horizontal = Sizes.s04
    )
) {
    credentialCardListItem(
        credentialCardState = credentialCardState,
        paddingValues = paddingValues,
        isLastItem = claimBadgesUiStates.isEmpty()
    )

    if (claimBadgesUiStates.isNotEmpty()) {
        this.claimInfoBadgeListItem(
            claimBadges = claimBadgesUiStates,
            onBadge = onBadge,
            paddingValues = paddingValues,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
private fun LazyListScope.claimInfoBadgeListItem(
    claimBadges: List<ClaimBadgeUiState> = emptyList(),
    onBadge: (BadgeType) -> Unit,
    paddingValues: PaddingValues = PaddingValues(0.dp),
) = clusterLazyListItem(
    isFirstItem = false,
    isLastItem = true,
    showDivider = false,
    paddingValues = paddingValues,
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
        headlineContent = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Sizes.s02)
            ) {
                claimBadges.forEach { badge ->
                    when (badge.isSensitive) {
                        true -> SensitiveClaimInfoBadge(badge.localizedLabel, onClick = onBadge)
                        false -> NonSensitiveClaimInfoBadge(badge.localizedLabel, onClick = onBadge)
                    }
                }
            }
        }
    )
}

@WalletAllScreenPreview
@Composable
private fun CredentialInfoWithClaimBadgesWidgetPreview() {
    val cardState = CredentialMocks.cardState01
    WalletTheme {
        WalletLayouts.LazyColumn {
            credentialInfoWithClaimBadgesWidget(
                credentialCardState = cardState,
                onBadge = {}
            )
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            credentialInfoWithClaimBadgesWidget(
                credentialCardState = cardState,
                claimBadgesUiStates = listOf(
                    ClaimBadgeUiState(
                        localizedLabel = "Sensitive Claim",
                        isSensitive = true
                    ),
                    ClaimBadgeUiState(
                        localizedLabel = "Claim 2",
                        isSensitive = false
                    ),
                    ClaimBadgeUiState(
                        localizedLabel = "Non-sensitive Claim",
                        isSensitive = false
                    ),
                    ClaimBadgeUiState(
                        localizedLabel = "Some Claim",
                        isSensitive = false
                    ),
                ),
                onBadge = {}
            )
        }
    }
}
