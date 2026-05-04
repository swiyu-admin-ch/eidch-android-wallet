@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.badges.presentation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.Dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.badges.domain.model.BadgeType
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.utils.replaceContentDescription
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun TrustBadge(
    trustStatus: TrustStatus,
    onClick: (BadgeType.ActorInfoBadge) -> Unit,
) = when (trustStatus) {
    TrustStatus.TRUSTED -> TrustBadgeTrusted(onClick = onClick)
    TrustStatus.NOT_TRUSTED -> TrustBadgeNotTrusted(onClick = onClick)
    TrustStatus.EXTERNAL -> TrustBadgeNotInSystem(onClick = onClick)
    TrustStatus.UNKNOWN -> {}
}

@Composable
fun NonComplianceBadge(
    actorComplianceState: ActorComplianceState,
    onClick: (BadgeType.ActorInfoBadge) -> Unit,
) = when (actorComplianceState) {
    ActorComplianceState.REPORTED -> NonCompliantActorBadge(
        onClick = onClick,
    )

    ActorComplianceState.NOT_REPORTED,
    ActorComplianceState.UNKNOWN -> {
    }
}

@Composable
fun LegitimateActorBadge(
    actorType: ActorType,
    vcSchemaTrustStatus: VcSchemaTrustStatus,
    onClick: (BadgeType.ActorInfoBadge) -> Unit,
) = when (actorType) {
    ActorType.ISSUER if vcSchemaTrustStatus == VcSchemaTrustStatus.TRUSTED -> {
        LegitimateIssuerBadge(onClick = onClick)
    }

    ActorType.ISSUER if vcSchemaTrustStatus == VcSchemaTrustStatus.NOT_TRUSTED -> {
        NonLegitimateIssuerBadge(onClick = onClick)
    }

    ActorType.VERIFIER if vcSchemaTrustStatus == VcSchemaTrustStatus.TRUSTED -> {
        LegitimateVerifierBadge(onClick = onClick)
    }

    ActorType.VERIFIER if vcSchemaTrustStatus == VcSchemaTrustStatus.NOT_TRUSTED -> {
        NonLegitimateVerifierBadge(onClick = onClick)
    }

    else -> {}
}

@Composable
fun TrustBadgeTrusted(
    onClick: ((BadgeType.ActorInfoBadge) -> Unit)? = null,
) = Badge(
    icon = R.drawable.wallet_ic_trusted,
    text = stringResource(R.string.tk_issuer_trusted),
    contentColor = WalletTheme.colorScheme.onLightTertiary,
    backgroundColor = WalletTheme.colorScheme.lightTertiary,
    onClick = onClick?.let {
        {
            it.invoke(BadgeType.ActorInfoBadge.VerifiedTrust)
        }
    }
)

@Composable
fun TrustBadgeNotTrusted(
    onClick: ((BadgeType.ActorInfoBadge) -> Unit)? = null,
) = Badge(
    icon = R.drawable.wallet_ic_questionmark_small,
    text = stringResource(R.string.tk_issuer_notTrusted),
    contentColor = WalletTheme.colorScheme.primary,
    backgroundColor = WalletTheme.colorScheme.lightPrimary,
    onClick = onClick?.let {
        {
            it.invoke(BadgeType.ActorInfoBadge.NotVerifiedTrust)
        }
    }
)

@Composable
fun TrustBadgeNotInSystem(
    onClick: ((BadgeType.ActorInfoBadge) -> Unit)? = null,
) = Badge(
    icon = R.drawable.wallet_ic_non_legitimate_actor,
    text = stringResource(R.string.tk_issuer_notInSystem),
    contentColor = WalletTheme.colorScheme.onLightError,
    backgroundColor = WalletTheme.colorScheme.lightError,
    onClick = onClick?.let {
        {
            it.invoke(BadgeType.ActorInfoBadge.NotInSystemTrust)
        }
    }
)

@Composable
fun LegitimateIssuerBadge(
    onClick: ((BadgeType.ActorInfoBadge) -> Unit)? = null,
) = Badge(
    icon = R.drawable.wallet_ic_legitimate_actor,
    text = stringResource(R.string.tk_issuer_legitimate),
    contentColor = WalletTheme.colorScheme.onLightTertiary,
    backgroundColor = WalletTheme.colorScheme.lightTertiary,
    onClick = onClick?.let {
        {
            it.invoke(BadgeType.ActorInfoBadge.LegitimateIssuer)
        }
    }
)

@Composable
fun NonLegitimateIssuerBadge(
    onClick: ((BadgeType.ActorInfoBadge) -> Unit)? = null,
) = Badge(
    icon = R.drawable.wallet_ic_non_legitimate_actor,
    text = stringResource(R.string.tk_issuer_notLegitimate),
    contentColor = WalletTheme.colorScheme.onLightError,
    backgroundColor = WalletTheme.colorScheme.lightError,
    onClick = onClick?.let {
        {
            it.invoke(BadgeType.ActorInfoBadge.NonLegitimateIssuer)
        }
    }
)

@Composable
fun LegitimateVerifierBadge(
    onClick: ((BadgeType.ActorInfoBadge) -> Unit)? = null,
) = Badge(
    icon = R.drawable.wallet_ic_legitimate_actor,
    text = stringResource(R.string.tk_verifier_legitimate),
    contentColor = WalletTheme.colorScheme.onLightTertiary,
    backgroundColor = WalletTheme.colorScheme.lightTertiary,
    onClick = onClick?.let {
        {
            it.invoke(BadgeType.ActorInfoBadge.LegitimateVerifier)
        }
    }
)

@Composable
fun NonLegitimateVerifierBadge(
    onClick: ((BadgeType.ActorInfoBadge) -> Unit)? = null,
) = Badge(
    icon = R.drawable.wallet_ic_non_legitimate_actor,
    text = stringResource(R.string.tk_verifier_notLegitimate),
    contentColor = WalletTheme.colorScheme.onLightError,
    backgroundColor = WalletTheme.colorScheme.lightError,
    onClick = onClick?.let {
        {
            it.invoke(BadgeType.ActorInfoBadge.NonLegitimateVerifier)
        }
    }
)

@Composable
fun NonSensitiveClaimInfoBadge(
    claimLabel: String,
    onClick: ((BadgeType.ClaimInfoBadge) -> Unit)? = null,
) = Badge(
    text = claimLabel,
    contentColor = WalletTheme.colorScheme.primary,
    backgroundColor = WalletTheme.colorScheme.lightPrimary,
    onClick = onClick?.let {
        {
            it.invoke(BadgeType.ClaimInfoBadge.NonSensitiveClaim(claimLabel))
        }
    }
)

@Composable
fun SensitiveClaimInfoBadge(
    claimLabel: String,
    onClick: ((BadgeType.ClaimInfoBadge) -> Unit)? = null,
) {
    val textDescription = "${stringResource(R.string.tk_global_sensitive_data)}: $claimLabel"
    Badge(
        icon = R.drawable.wallet_ic_warning,
        text = claimLabel,
        contentColor = WalletTheme.colorScheme.onSensitiveBadge,
        backgroundColor = WalletTheme.colorScheme.sensitiveBadge,
        onClick = onClick?.let {
            {
                it.invoke(BadgeType.ClaimInfoBadge.SensitiveClaim(claimLabel))
            }
        },
        textModifier = Modifier.semantics {
            contentDescription = textDescription
        },
    )
}

@Composable
fun SensitiveBadge() {
    Badge(
        text = stringResource(R.string.tk_global_sensitive_data),
        contentColor = WalletTheme.colorScheme.onSensitiveBadge,
        backgroundColor = WalletTheme.colorScheme.sensitiveBadge,
        onClick = null,
    )
}

@Composable
fun NonCompliantActorBadge(
    onClick: ((BadgeType.ActorInfoBadge) -> Unit)? = null,
) = Badge(
    icon = R.drawable.wallet_ic_non_legitimate_actor,
    text = stringResource(R.string.tk_actor_nonCompliant),
    contentColor = WalletTheme.colorScheme.onLightError,
    backgroundColor = WalletTheme.colorScheme.lightError,
    onClick = onClick?.let {
        {
            it.invoke(BadgeType.ActorInfoBadge.NonCompliantActor)
        }
    }
)

@Composable
private fun Badge(
    @DrawableRes icon: Int? = null,
    text: String,
    contentColor: Color,
    backgroundColor: Color,
    onClick: (() -> Unit)?,
    textModifier: Modifier = if (onClick != null) {
        Modifier.replaceContentDescription(text + "," + stringResource(R.string.tk_accessibility_information_double_tap))
    } else {
        Modifier.replaceContentDescription(stringResource(R.string.tk_accessibility_information_about) + "," + text)
    }
) = Box(
    modifier = Modifier
        .padding(top = Sizes.s02, bottom = Sizes.s02),
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(Sizes.s10))
            .then(
                if (onClick != null) {
                    Modifier
                        .clickable(
                            onClick = { onClick() },
                            role = Role.Button,
                        )
                        .spaceBarKeyClickable(onSpace = { onClick() })
                } else {
                    Modifier
                }
            )
            .background(backgroundColor)
            .padding(
                top = Sizes.s02,
                bottom = Sizes.s02,
                start = Sizes.s03,
                end = if (icon == null) Sizes.s03 else Sizes.s04
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconHeight: Dp = with(LocalDensity.current) {
            // match icon height to height of font used in the trailing text
            WalletTheme.typography.labelMedium.fontSize.toDp()
        }

        icon?.let {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier
                    .size(iconHeight)
            )
            Spacer(modifier = Modifier.width(Sizes.s01))
        }
        WalletTexts.LabelMedium(
            text = text,
            color = contentColor,
            modifier = textModifier
        )
    }
}

@ExperimentalLayoutApi
@PreviewFontScale
@Composable
private fun BadgePreview() {
    WalletTheme {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Sizes.s02)
        ) {
            NonSensitiveClaimInfoBadge(claimLabel = "Info")
            SensitiveClaimInfoBadge(claimLabel = "Info")
            TrustBadgeTrusted()
            TrustBadgeNotTrusted()
            TrustBadgeNotInSystem()
            LegitimateIssuerBadge()
            NonLegitimateIssuerBadge()
            LegitimateVerifierBadge()
            NonLegitimateVerifierBadge()
            NonCompliantActorBadge()
        }
    }
}
