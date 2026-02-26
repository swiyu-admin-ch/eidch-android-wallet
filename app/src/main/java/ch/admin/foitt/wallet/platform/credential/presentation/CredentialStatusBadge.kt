package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
internal fun CredentialStatusBadge(
    status: CredentialDisplayStatus?,
    textColor: Color,
    modifier: Modifier = Modifier,
) = AnimatedContent(
    modifier = modifier,
    targetState = status,
    transitionSpec = {
        fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
    },
    label = "fadingAnimation",
) { credentialStatus ->
    credentialStatus?.let {
        val altText = credentialStatus.getAltText()
        Box(
            modifier = Modifier
                .heightIn(min = Sizes.labelHeight)
                .clearAndSetSemantics {
                    contentDescription = altText
                }
                .background(
                    color = when (credentialStatus) {
                        CredentialDisplayStatus.Valid,
                        CredentialDisplayStatus.Unsupported,
                        CredentialDisplayStatus.Unknown -> Color.Transparent
                        is CredentialDisplayStatus.NotYetValid,
                        is CredentialDisplayStatus.Expired,
                        CredentialDisplayStatus.Revoked,
                        CredentialDisplayStatus.Suspended -> WalletTheme.colorScheme.lightErrorFixed
                    },
                    shape = RoundedCornerShape(Sizes.s04)
                )
                .border(
                    Sizes.line01,
                    color = when (credentialStatus) {
                        is CredentialDisplayStatus.Expired,
                        CredentialDisplayStatus.Revoked,
                        is CredentialDisplayStatus.NotYetValid,
                        CredentialDisplayStatus.Suspended -> Color.Transparent
                        CredentialDisplayStatus.Valid,
                        CredentialDisplayStatus.Unsupported,
                        CredentialDisplayStatus.Unknown -> textColor
                    },
                    shape = RoundedCornerShape(Sizes.s04)
                )
                .padding(start = Sizes.s03, end = Sizes.s04),
            contentAlignment = Alignment.Center,
        ) {
            CredentialStatusLabel(credentialStatus, textColor)
        }
    }
}

@Composable
private fun CredentialStatusLabel(
    status: CredentialDisplayStatus,
    textColor: Color
) {
    val color = when (status) {
        CredentialDisplayStatus.Valid,
        CredentialDisplayStatus.Unsupported,
        CredentialDisplayStatus.Unknown -> textColor
        is CredentialDisplayStatus.NotYetValid,
        is CredentialDisplayStatus.Expired,
        CredentialDisplayStatus.Revoked,
        CredentialDisplayStatus.Suspended -> WalletTheme.colorScheme.onLightErrorFixed
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = status.getIcon()),
            contentDescription = null,
            tint = color,
        )
        Spacer(modifier = Modifier.size(Sizes.s01))
        Text(
            text = status.getText(),
            color = color,
            style = WalletTheme.typography.labelMedium,
        )
    }
}

@DrawableRes
internal fun CredentialDisplayStatus.getIcon(): Int = when (this) {
    CredentialDisplayStatus.Valid -> R.drawable.wallet_ic_checkmark
    is CredentialDisplayStatus.NotYetValid -> R.drawable.wallet_ic_hourglass
    is CredentialDisplayStatus.Expired,
    CredentialDisplayStatus.Revoked -> R.drawable.wallet_ic_invalid
    CredentialDisplayStatus.Suspended -> R.drawable.wallet_ic_front_hand
    CredentialDisplayStatus.Unsupported,
    CredentialDisplayStatus.Unknown -> R.drawable.wallet_ic_warning
}

@Composable
internal fun CredentialDisplayStatus.getText(): String = when (this) {
    CredentialDisplayStatus.Valid -> stringResource(R.string.tk_credential_status_valid)
    CredentialDisplayStatus.Unsupported,
    CredentialDisplayStatus.Unknown -> stringResource(R.string.tk_credential_status_unknown)
    is CredentialDisplayStatus.NotYetValid -> getNotYetValidText(validFrom, isAltText = false)
    is CredentialDisplayStatus.Expired -> stringResource(R.string.tk_credential_status_invalid)
    CredentialDisplayStatus.Revoked -> stringResource(R.string.tk_credential_status_revoked)
    CredentialDisplayStatus.Suspended -> stringResource(R.string.tk_credential_status_suspended)
}

@Composable
private fun getNotYetValidText(validFrom: Instant, isAltText: Boolean): String {
    val numberOfDays = ChronoUnit.DAYS.between(Instant.now(), validFrom)
    val isValidInLessThan24h = numberOfDays < 1

    return when {
        isValidInLessThan24h && isAltText -> stringResource(R.string.tk_credential_status_soon_alt)
        isValidInLessThan24h -> stringResource(R.string.tk_credential_status_soon)
        isAltText -> stringResource(R.string.tk_credential_status_notValidYet_alt, numberOfDays)
        else -> stringResource(R.string.tk_credential_status_notValidYet, numberOfDays)
    }
}

@Composable
internal fun CredentialDisplayStatus.getAltText(): String = when (this) {
    CredentialDisplayStatus.Valid -> stringResource(R.string.tk_credential_status_valid_alt)
    CredentialDisplayStatus.Unsupported,
    CredentialDisplayStatus.Unknown -> stringResource(R.string.tk_credential_status_unknown_alt)
    is CredentialDisplayStatus.NotYetValid -> getNotYetValidText(validFrom, isAltText = true)
    is CredentialDisplayStatus.Expired -> stringResource(R.string.tk_credential_status_invalid_alt)
    CredentialDisplayStatus.Revoked -> stringResource(R.string.tk_credential_status_revoked_alt)
    CredentialDisplayStatus.Suspended -> stringResource(R.string.tk_credential_status_suspended_alt)
}

private class CredentialStatusProvider : PreviewParameterProvider<CredentialDisplayStatus> {
    override val values: Sequence<CredentialDisplayStatus> = sequenceOf(
        CredentialDisplayStatus.Valid,
        CredentialDisplayStatus.Revoked,
        CredentialDisplayStatus.Suspended,
        CredentialDisplayStatus.Unsupported,
        CredentialDisplayStatus.Unknown,
        CredentialDisplayStatus.Expired(expiredAt = Instant.MIN),
        CredentialDisplayStatus.NotYetValid(validFrom = Instant.now().plusSeconds(3000000)),
        CredentialDisplayStatus.NotYetValid(validFrom = Instant.now().plusSeconds(3600)),
    )
}

@Composable
@WalletComponentPreview
private fun CredentialStatusBadgePreview(@PreviewParameter(CredentialStatusProvider::class) status: CredentialDisplayStatus) {
    WalletTheme {
        CredentialStatusBadge(
            status = status,
            textColor = Color.Black
        )
    }
}
