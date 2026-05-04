package ch.admin.foitt.wallet.platform.credential.presentation

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
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun DeferredCredentialStatusBadge(
    status: DeferredProgressionState?,
    modifier: Modifier = Modifier,
) = AnimatedContent(
    modifier = modifier,
    targetState = status,
    transitionSpec = {
        fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
    },
    label = "fadingAnimation",
) { deferredCredentialStatus ->
    deferredCredentialStatus?.let {
        val altText = deferredCredentialStatus.getAltText()
        Box(
            modifier = Modifier
                .heightIn(min = Sizes.labelHeight)
                .clearAndSetSemantics {
                    contentDescription = altText
                }
                .background(
                    color = when (deferredCredentialStatus) {
                        DeferredProgressionState.INVALID -> WalletTheme.colorScheme.lightOrange
                        DeferredProgressionState.IN_PROGRESS -> WalletTheme.colorScheme.lightPrimary
                    },
                    shape = RoundedCornerShape(Sizes.s04)
                )
                .border(
                    Sizes.line01,
                    color = when (deferredCredentialStatus) {
                        DeferredProgressionState.INVALID,
                        DeferredProgressionState.IN_PROGRESS -> Color.Transparent
                    },
                    shape = RoundedCornerShape(Sizes.s04)
                )
                .padding(start = Sizes.s03, end = Sizes.s04),
            contentAlignment = Alignment.Center,
        ) {
            DeferredCredentialStatusLabel(deferredCredentialStatus)
        }
    }
}

@Composable
private fun DeferredCredentialStatusLabel(
    status: DeferredProgressionState
) {
    val color = when (status) {
        DeferredProgressionState.IN_PROGRESS -> WalletTheme.colorScheme.onLightPrimary
        DeferredProgressionState.INVALID -> WalletTheme.colorScheme.onLightError
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(Sizes.s03),
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

@Composable
internal fun DeferredProgressionState.getAltText(): String = when (this) {
    DeferredProgressionState.IN_PROGRESS -> stringResource(R.string.tk_deferred_credential_status_inProgress)
    DeferredProgressionState.INVALID -> stringResource(R.string.tk_deferred_credential_status_invalid)
}

private class DeferredCredentialStatusProvider : PreviewParameterProvider<DeferredProgressionState> {
    override val values: Sequence<DeferredProgressionState> = sequenceOf(
        DeferredProgressionState.IN_PROGRESS,
        DeferredProgressionState.INVALID
    )
}

@Composable
@WalletComponentPreview
private fun DeferredCredentialStatusBadgePreview(
    @PreviewParameter(DeferredCredentialStatusProvider::class) status: DeferredProgressionState
) {
    WalletTheme {
        DeferredCredentialStatusBadge(
            status = status
        )
    }
}
