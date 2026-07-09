package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialCardMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Gradients
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun CredentialCardCreditFormat(
    credentialCardState: CredentialCardState,
    modifier: Modifier = Modifier,
    contentPaddingValues: PaddingValues = PaddingValues(
        start = Sizes.s06,
        top = Sizes.s06,
        end = Sizes.s06,
        bottom = Sizes.s04,
    ),
) {
    val contentColor = credentialCardState.contentColor

    Surface(
        modifier = modifier
            .aspectRatio(Sizes.CREDIT_CARD_ASPECT_RATIO),
        shape = RoundedCornerShape(Sizes.cornerSmall),
        color = credentialCardState.backgroundColor,
        contentColor = contentColor,
    ) {
        if (credentialCardState.isCredentialFromBetaIssuer) {
            DemoWatermark(color = contentColor)
        }
        if (credentialCardState.useDefaultBackground) {
            CredentialBackgroundFallbackPattern()
        }

        val inactiveColor = WalletTheme.colorScheme.inactiveOverlay
        val inactiveModifier = if (credentialCardState.isDeferred) {
            Modifier.drawBehind {
                drawRoundRect(color = inactiveColor)
                drawRoundRect(
                    color = credentialCardState.backgroundColor,
                    style = dashedStroke,
                    cornerRadius = CornerRadius(Sizes.s05.toPx()),
                )
            }
        } else {
            Modifier
        }

        Column(
            modifier = inactiveModifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(brush = Gradients.diagonalCredentialBrush())
                    drawRect(brush = Gradients.leftBottomRadialLargeCredentialBrush(size))
                    drawRect(brush = Gradients.bottomCenterRadialCredentialBrush(size))
                }
                .padding(contentPaddingValues)
        ) {
            CredentialContent(credentialCardState, contentColor)
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(Sizes.s04))
            Badges(
                credentialState = credentialCardState,
                textColor = credentialCardState.contentColor,
                isCredentialFromBetaIssuer = credentialCardState.isCredentialFromBetaIssuer,
            )
        }
    }
}

@Composable
private fun CredentialContent(
    credentialCardState: CredentialCardState,
    textColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        CredentialText(
            modifier = Modifier.weight(1f),
            credentialCardState = credentialCardState,
            textColor = textColor
        )
        credentialCardState.logo?.let {
            Spacer(modifier = Modifier.width(Sizes.s04))
            Icon(
                painter = it,
                contentDescription = null,
                modifier = Modifier.size(Sizes.credentialLargeIconSize),
                tint = textColor,
            )
        }
    }
}

@Composable
private fun CredentialText(
    modifier: Modifier = Modifier,
    credentialCardState: CredentialCardState,
    textColor: Color,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        credentialCardState.title?.let {
            WalletTexts.MediumCredentialTitle(
                text = it,
                color = textColor,
            )
        }
        credentialCardState.subtitle?.let {
            WalletTexts.MediumCredentialSubtitle(
                text = it,
                color = textColor.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun Badges(
    isCredentialFromBetaIssuer: Boolean,
    credentialState: CredentialCardState,
    textColor: Color,
) = Row {
    if (isCredentialFromBetaIssuer) {
        DemoBadge()
        Spacer(modifier = Modifier.width(Sizes.s02))
    }
    if (credentialState.deferredStatus != null) {
        DeferredCredentialStatusBadge(credentialState.deferredStatus)
    } else {
        CredentialStatusBadge(credentialState.status, textColor)
    }
}

private val dashedStroke = Stroke(
    width = 8f,
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f)),
)

private class CredentialCardCreditFormatPreviewParams :
    PreviewParameterProvider<ComposableWrapper<CredentialCardState>> {
    override val values = CredentialCardMocks.mocks
}

@WalletComponentPreview
@Composable
fun CredentialCardCreditFormatPreview(
    @PreviewParameter(CredentialCardCreditFormatPreviewParams::class) state: ComposableWrapper<CredentialCardState>,
) {
    WalletTheme {
        CredentialCardCreditFormat(
            credentialCardState = state.value(),
        )
    }
}
