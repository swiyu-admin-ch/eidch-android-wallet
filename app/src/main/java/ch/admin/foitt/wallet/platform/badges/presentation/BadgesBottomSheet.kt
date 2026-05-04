package ch.admin.foitt.wallet.platform.badges.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.badges.presentation.model.BadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.composables.presentation.ClusterListItem
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.utils.TraversalIndex
import ch.admin.foitt.wallet.platform.utils.setIsTraversalGroup
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeBottomSheet(
    sheetState: SheetState,
    badgeBottomSheetUiState: BadgeBottomSheetUiState,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        modifier = Modifier.setIsTraversalGroup(index = TraversalIndex.HIGH1),
        sheetState = sheetState,
        containerColor = WalletTheme.colorScheme.surfaceContainerLow,
        onDismissRequest = onDismiss,
    ) {
        ModalBottomSheetContent(badgeBottomSheetUiState)
    }
}

@Composable
private fun ModalBottomSheetContent(
    badgeBottomSheetUiState: BadgeBottomSheetUiState
) = when (badgeBottomSheetUiState) {
    is BadgeBottomSheetUiState.TrustVerified -> BottomSheetContent(
        badge = {
            TrustBadgeTrusted()
        },
        title = stringResource(R.string.tk_badgeInformation_inTrustRegistry_primary, badgeBottomSheetUiState.actorName),
        body = stringResource(R.string.tk_badgeInformation_inTrustRegistry_secondary),
        hint = stringResource(
            R.string.tk_badgeInformation_inTrustRegistry_hint,
            badgeBottomSheetUiState.actorName,
            badgeBottomSheetUiState.actorName,
        ),
        onMoreInformation = badgeBottomSheetUiState.onMoreInformation,
    )

    is BadgeBottomSheetUiState.TrustNotVerified -> BottomSheetContent(
        badge = {
            TrustBadgeNotTrusted()
        },
        title = stringResource(R.string.tk_badgeInformation_inBaseRegistry_primary, badgeBottomSheetUiState.actorName),
        body = stringResource(R.string.tk_badgeInformation_inBaseRegistry_secondary),
        hint = stringResource(
            R.string.tk_badgeInformation_inBaseRegistry_hint,
            badgeBottomSheetUiState.actorName,
            badgeBottomSheetUiState.actorName,
        ),
        onMoreInformation = badgeBottomSheetUiState.onMoreInformation,
    )

    is BadgeBottomSheetUiState.TrustNotInSystem -> BottomSheetContent(
        badge = {
            TrustBadgeNotInSystem()
        },
        title = stringResource(R.string.tk_badgeInformation_notInSystem_primary, badgeBottomSheetUiState.actorName),
        body = stringResource(R.string.tk_badgeInformation_notInSystem_secondary),
        hint = stringResource(
            R.string.tk_badgeInformation_notInSystem_hint,
            badgeBottomSheetUiState.actorName,
            badgeBottomSheetUiState.actorName,
        ),
        onMoreInformation = badgeBottomSheetUiState.onMoreInformation,
    )

    is BadgeBottomSheetUiState.LegitimateIssuer -> BottomSheetContent(
        badge = {
            LegitimateIssuerBadge()
        },
        title = stringResource(R.string.tk_badgeInformation_legitimateIssuer_primary, badgeBottomSheetUiState.actorName),
        onMoreInformation = badgeBottomSheetUiState.onMoreInformation,
    )

    is BadgeBottomSheetUiState.NonLegitimateIssuer -> BottomSheetContent(
        badge = {
            NonLegitimateIssuerBadge()
        },
        title = stringResource(R.string.tk_badgeInformation_notLegitimateIssuer_primary, badgeBottomSheetUiState.actorName),
        onMoreInformation = badgeBottomSheetUiState.onMoreInformation,
    )

    is BadgeBottomSheetUiState.LegitimateVerifier -> BottomSheetContent(
        badge = {
            LegitimateVerifierBadge()
        },
        title = stringResource(R.string.tk_badgeInformation_legitimateVerifier_primary, badgeBottomSheetUiState.actorName),
        onMoreInformation = badgeBottomSheetUiState.onMoreInformation,
    )

    is BadgeBottomSheetUiState.NonLegitimateVerifier -> BottomSheetContent(
        badge = {
            NonLegitimateVerifierBadge()
        },
        title = stringResource(R.string.tk_badgeInformation_notLegitimateVerifier_primary, badgeBottomSheetUiState.actorName),
        onMoreInformation = badgeBottomSheetUiState.onMoreInformation,
    )

    is BadgeBottomSheetUiState.SensitiveClaim -> {}

    is BadgeBottomSheetUiState.NonCompliantActor -> BottomSheetContent(
        badge = {
            NonCompliantActorBadge()
        },
        title = stringResource(R.string.tk_badgeInformation_nonCompliant_primary, badgeBottomSheetUiState.actorName),
        body = stringResource(R.string.tk_badgeInformation_nonCompliant_secondary),
        bodySecondary = badgeBottomSheetUiState.reason,
        hint = stringResource(
            R.string.tk_badgeInformation_nonCompliant_hint,
            badgeBottomSheetUiState.actorName,
            badgeBottomSheetUiState.actorName,
        ),
        onMoreInformation = badgeBottomSheetUiState.onMoreInformation,
    )

    is BadgeBottomSheetUiState.NonSensitiveClaimInfo -> BottomSheetContent(
        badge = {
            NonSensitiveClaimInfoBadge(claimLabel = badgeBottomSheetUiState.claimLabel)
        },
        title = stringResource(R.string.tk_badgeInformation_nonSensitiveClaimInfo_primary),
        body = stringResource(R.string.tk_badgeInformation_nonSensitiveClaimInfo_secondary),
        onMoreInformation = badgeBottomSheetUiState.onMoreInformation,
    )

    is BadgeBottomSheetUiState.SensitiveClaimInfo -> BottomSheetContent(
        badge = {
            SensitiveClaimInfoBadge(claimLabel = badgeBottomSheetUiState.claimLabel)
        },
        title = stringResource(R.string.tk_badgeInformation_sensitiveClaimInfo_primary),
        body = stringResource(R.string.tk_badgeInformation_sensitiveClaimInfo_secondary),
        onMoreInformation = badgeBottomSheetUiState.onMoreInformation,
    )
}

@Composable
private fun BottomSheetContent(
    badge: @Composable () -> Unit,
    title: String,
    body: String? = null,
    bodySecondary: String? = null,
    hint: String? = null,
    onMoreInformation: () -> Unit,
) = Column(
    modifier = Modifier.padding(horizontal = Sizes.s06, vertical = Sizes.s02)
) {
    badge()
    WalletTexts.BodyLargeEmphasized(
        text = title,
        color = WalletTheme.colorScheme.onSurface,
        modifier = Modifier.semantics { heading() },
    )
    Spacer(modifier = Modifier.height(Sizes.s02))
    body?.let {
        WalletTexts.BodyLarge(
            text = it,
            color = WalletTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Sizes.s02))
    }
    bodySecondary?.let {
        ClusterListItem(
            isFirstItem = true,
            isLastItem = true,
            backgroundColor = WalletTheme.colorScheme.surface,
            cornerSize = Sizes.s03,
        ) {
            WalletTexts.BodyLarge(
                modifier = Modifier.padding(horizontal = Sizes.s04, vertical = Sizes.s03),
                text = it,
                color = WalletTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(Sizes.s02))
    }
    hint?.let {
        WalletTexts.BodyLarge(
            text = it,
            color = WalletTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Sizes.s02))
    }
    MoreInformationButton(onClick = onMoreInformation)
}

@Composable
private fun MoreInformationButton(
    onClick: () -> Unit,
) = Row(
    modifier = Modifier
        .clickable(onClick = onClick)
        .spaceBarKeyClickable(onSpace = onClick),
    verticalAlignment = Alignment.CenterVertically,
) {
    val linkText = stringResource(R.string.tk_badgeInformation_furtherInformation_link_text)
    val linkAltText = stringResource(R.string.tk_global_externalLink_alt)

    WalletTexts.LabelLargeEmphasized(
        modifier = Modifier.semantics {
            contentDescription = "$linkText $linkAltText"
            role = Role.Button
        },
        text = linkText,
        color = WalletTheme.colorScheme.error,
    )
    Icon(
        painter = painterResource(R.drawable.wallet_ic_chevron_medium),
        contentDescription = null,
        tint = WalletTheme.colorScheme.error,
    )
}

private class BadgesBottomSheetPreviewParams : PreviewParameterProvider<BadgeBottomSheetUiState> {
    override val values = sequenceOf(
        BadgeBottomSheetUiState.TrustVerified(actorName = "Preview actor", onMoreInformation = {}),
        BadgeBottomSheetUiState.TrustNotVerified(actorName = "Preview actor", onMoreInformation = {}),
        BadgeBottomSheetUiState.LegitimateIssuer(actorName = "Preview actor", onMoreInformation = {}),
        BadgeBottomSheetUiState.NonLegitimateIssuer(actorName = "Preview actor", onMoreInformation = {}),
        BadgeBottomSheetUiState.LegitimateVerifier(actorName = "Preview actor", onMoreInformation = {}),
        BadgeBottomSheetUiState.NonLegitimateVerifier(actorName = "Preview actor", onMoreInformation = {}),
        BadgeBottomSheetUiState.NonCompliantActor(
            actorName = "Preview actor",
            reason = "[Actor Name] is requesting surname, given name, and date of birth, instead of an anonymous age proof only.",
            onMoreInformation = {}
        ),
        BadgeBottomSheetUiState.SensitiveClaimInfo(claimLabel = "Label of sensitive claim", onMoreInformation = {}),
        BadgeBottomSheetUiState.NonSensitiveClaimInfo(claimLabel = "Label of non-Sensitive claim", onMoreInformation = {}),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@WalletComponentPreview
@Composable
private fun BadgesBottomSheetPreview(
    @PreviewParameter(BadgesBottomSheetPreviewParams::class) badgeBottomSheetUiState: BadgeBottomSheetUiState
) {
    WalletTheme {
        ModalBottomSheetContent(badgeBottomSheetUiState)
    }
}
