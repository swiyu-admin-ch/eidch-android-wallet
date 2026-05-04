package ch.admin.foitt.wallet.platform.actorMetadata.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.badges.domain.model.BadgeType
import ch.admin.foitt.wallet.platform.badges.presentation.LegitimateActorBadge
import ch.admin.foitt.wallet.platform.badges.presentation.NonComplianceBadge
import ch.admin.foitt.wallet.platform.badges.presentation.TrustBadge
import ch.admin.foitt.wallet.platform.composables.Avatar
import ch.admin.foitt.wallet.platform.composables.AvatarSize
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InvitationHeader(
    actorUiState: ActorUiState,
    onBadge: (BadgeType) -> Unit,
    modifier: Modifier = Modifier,
) = Card(
    shape = RoundedCornerShape(bottomStart = Sizes.s09, bottomEnd = Sizes.s09),
    colors = CardDefaults.cardColors(containerColor = WalletTheme.colorScheme.surface)
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(start = Sizes.s04, end = Sizes.s04, top = Sizes.s04, bottom = Sizes.s02),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                imagePainter = actorUiState.painter ?: fallBackIcon(actorUiState.actorType),
                size = AvatarSize.LARGE,
                imageTint = WalletTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.size(Sizes.s04))
            WalletTexts.TitleMedium(
                text = actorUiState.name ?: fallBackName(actorUiState.actorType),
                color = WalletTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
        }

        Spacer(modifier = Modifier.height(Sizes.s02))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Sizes.s03),
        ) {
            TrustBadge(
                trustStatus = actorUiState.trustStatus,
                onClick = onBadge,
            )

            NonComplianceBadge(
                actorComplianceState = actorUiState.actorComplianceState,
                onClick = onBadge,
            )

            LegitimateActorBadge(
                actorType = actorUiState.actorType,
                vcSchemaTrustStatus = actorUiState.vcSchemaTrustStatus,
                onClick = onBadge,
            )
        }
    }
}

@Composable
private fun fallBackIcon(actorType: ActorType) = when (actorType) {
    ActorType.ISSUER,
    ActorType.VERIFIER -> painterResource(R.drawable.wallet_ic_actor_default)

    ActorType.UNKNOWN -> null
}

@Composable
private fun fallBackName(actorType: ActorType): String = when (actorType) {
    ActorType.ISSUER -> stringResource(R.string.tk_credential_offer_issuer_name_unknown)
    ActorType.VERIFIER -> stringResource(R.string.presentation_verifier_name_unknown)
    ActorType.UNKNOWN -> ""
}

private data class InvitationHeaderPreviewParam(
    val actorName: String?,
    val actorLogo: Int,
    val trustStatus: TrustStatus,
    val vcSchemaTrustStatus: VcSchemaTrustStatus,
    val actorComplianceState: ActorComplianceState,
)

private class InvitationHeaderPreviewParams : PreviewParameterProvider<InvitationHeaderPreviewParam> {
    override val values: Sequence<InvitationHeaderPreviewParam> = sequenceOf(
        InvitationHeaderPreviewParam(
            actorName = "Issuer Name",
            actorLogo = R.drawable.wallet_ic_eid,
            trustStatus = TrustStatus.TRUSTED,
            vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
            actorComplianceState = ActorComplianceState.REPORTED,
        ),
        InvitationHeaderPreviewParam(
            actorName = "Issuer Name",
            actorLogo = R.drawable.wallet_ic_eid,
            trustStatus = TrustStatus.TRUSTED,
            vcSchemaTrustStatus = VcSchemaTrustStatus.NOT_TRUSTED,
            actorComplianceState = ActorComplianceState.NOT_REPORTED,
        ),
        InvitationHeaderPreviewParam(
            actorName = "Issuer with a veeeeryyyyy loooonnnnnng name",
            actorLogo = R.drawable.ic_launcher_background,
            trustStatus = TrustStatus.TRUSTED,
            vcSchemaTrustStatus = VcSchemaTrustStatus.UNPROTECTED,
            actorComplianceState = ActorComplianceState.NOT_REPORTED,
        ),
        InvitationHeaderPreviewParam(
            actorName = "Issuer Name not trusted",
            actorLogo = R.drawable.wallet_ic_actor_default,
            trustStatus = TrustStatus.NOT_TRUSTED,
            vcSchemaTrustStatus = VcSchemaTrustStatus.UNPROTECTED,
            actorComplianceState = ActorComplianceState.NOT_REPORTED,
        ),
        InvitationHeaderPreviewParam(
            actorName = "Issuer Name trust unknown",
            actorLogo = R.drawable.wallet_ic_dotted_cross,
            trustStatus = TrustStatus.UNKNOWN,
            vcSchemaTrustStatus = VcSchemaTrustStatus.UNPROTECTED,
            actorComplianceState = ActorComplianceState.NOT_REPORTED,
        ),
        InvitationHeaderPreviewParam(
            actorName = null,
            actorLogo = R.drawable.wallet_ic_dotted_cross,
            trustStatus = TrustStatus.UNKNOWN,
            vcSchemaTrustStatus = VcSchemaTrustStatus.UNPROTECTED,
            actorComplianceState = ActorComplianceState.UNKNOWN,
        ),
    )
}

@WalletComponentPreview
@Composable
private fun InvitationHeaderPreview(
    @PreviewParameter(InvitationHeaderPreviewParams::class) previewParams: InvitationHeaderPreviewParam,
) {
    WalletTheme {
        InvitationHeader(
            actorUiState = ActorUiState(
                name = previewParams.actorName,
                painter = painterResource(previewParams.actorLogo),
                trustStatus = previewParams.trustStatus,
                vcSchemaTrustStatus = previewParams.vcSchemaTrustStatus,
                actorType = ActorType.ISSUER,
                actorComplianceState = previewParams.actorComplianceState,
                nonComplianceReason = null,
            ),
            onBadge = {}
        )
    }
}
