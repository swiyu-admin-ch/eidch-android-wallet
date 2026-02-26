package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun CredentialListRow(
    credentialState: CredentialCardState,
    showDivider: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Unspecified,
) {
    val clickableModifier = if (!credentialState.isDeferred) {
        Modifier
            .clickable(onClick = onClick)
            .spaceBarKeyClickable(onClick)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .then(clickableModifier)
            .background(backgroundColor)
            .padding(start = Sizes.s04, top = Sizes.s03, end = Sizes.s06, bottom = Sizes.s03),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CredentialCardSmall(credentialState = credentialState)
        Spacer(modifier = Modifier.width(Sizes.s04))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            credentialState.title?.let {
                WalletTexts.BodyLarge(
                    text = credentialState.title,
                    color = WalletTheme.colorScheme.onSurface,
                )
            }
            credentialState.subtitle?.let {
                WalletTexts.BodyLarge(
                    text = credentialState.subtitle,
                    color = WalletTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                showStatus(credentialState)
            }
        }
        if (!credentialState.isDeferred) {
            Spacer(modifier = Modifier.width(Sizes.s04))
            Icon(
                modifier = Modifier.size(Sizes.s06),
                painter = painterResource(id = R.drawable.wallet_ic_chevron),
                contentDescription = null,
                tint = WalletTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(start = Sizes.s04)
        )
    }
}

@Composable
private fun showStatus(credentialState: CredentialCardState) {
    when {
        credentialState.deferredStatus != null -> DeferredCredentialStatus(credentialState.deferredStatus)
        credentialState.progressionState == VerifiableProgressionState.UNACCEPTED -> UnacceptedCredentialStatus()
        credentialState.status != null -> {
            // Demo badge is only shown on accepted credentials
            if (credentialState.isCredentialFromBetaIssuer) {
                DemoBadge()
                Spacer(modifier = Modifier.width(Sizes.s02))
            }
            CredentialStatus(status = credentialState.status,)
        }
    }
}

@Composable
private fun CredentialStatus(
    status: CredentialDisplayStatus,
) = CredentialListBadge(
    text = status.getText(),
    contentColor = status.getContentColor(),
    iconRes = status.getIcon(),
)

@Composable
private fun UnacceptedCredentialStatus() = ReadyBadge()

@Composable
private fun DeferredCredentialStatus(
    deferredState: DeferredProgressionState,
) = CredentialListBadge(
    text = deferredState.getText(),
    contentColor = WalletTheme.colorScheme.onSurfaceVariant,
    iconRes = deferredState.getIcon(),
)

@Composable
private fun CredentialListBadge(
    text: String,
    contentColor: Color,
    @DrawableRes iconRes: Int,
) {
    val bodyTextHeight = with(LocalDensity.current) {
        WalletTheme.typography.bodyMedium.lineHeight.toDp()
    }

    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.sizeIn(
            maxWidth = bodyTextHeight,
            maxHeight = bodyTextHeight,
        )
    )
    Spacer(modifier = Modifier.width(Sizes.s01))
    WalletTexts.Body(
        text = text,
        color = contentColor,
    )
}

@Composable
private fun CredentialDisplayStatus.getContentColor() = when (this) {
    CredentialDisplayStatus.Valid,
    CredentialDisplayStatus.Unsupported,
    CredentialDisplayStatus.Unknown -> WalletTheme.colorScheme.onSurfaceVariant
    is CredentialDisplayStatus.NotYetValid,
    is CredentialDisplayStatus.Expired,
    CredentialDisplayStatus.Revoked,
    CredentialDisplayStatus.Suspended -> WalletTheme.colorScheme.error
}

@DrawableRes
internal fun DeferredProgressionState.getIcon(): Int = when (this) {
    DeferredProgressionState.IN_PROGRESS -> R.drawable.wallet_ic_alarm
    DeferredProgressionState.INVALID -> R.drawable.wallet_ic_cross
}

@Composable
internal fun DeferredProgressionState.getText(): String = when (this) {
    DeferredProgressionState.IN_PROGRESS -> stringResource(R.string.tk_deferred_credential_status_inProgress)
    DeferredProgressionState.INVALID -> stringResource(R.string.tk_deferred_credential_status_invalid)
}

private class CredentialListRowPreviewParams : PreviewParameterProvider<ComposableWrapper<CredentialCardState>> {
    override val values = CredentialMocks.cardStates
}

@WalletComponentPreview
@Composable
private fun CredentialListRowPreview(
    @PreviewParameter(CredentialListRowPreviewParams::class) state: ComposableWrapper<CredentialCardState>,
) {
    WalletTheme {
        CredentialListRow(
            credentialState = state.value(),
            showDivider = true,
            onClick = {},
        )
    }
}
