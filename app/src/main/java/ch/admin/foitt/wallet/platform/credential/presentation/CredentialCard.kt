package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.Dp
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Gradients
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun CredentialCardSmall(
    credentialState: CredentialCardState,
    modifier: Modifier = Modifier,
) = CredentialCardWithIcon(
    credentialState = credentialState,
    width = Sizes.credentialSmallWidth,
    height = Sizes.credentialSmallHeight,
    roundedCornerSize = Sizes.s03,
    modifier = modifier,
)

@Composable
fun CredentialCardVerySmall(
    credentialState: CredentialCardState,
    modifier: Modifier = Modifier,
) = CredentialCardWithIcon(
    credentialState = credentialState,
    width = Sizes.credentialVerySmallWidth,
    height = Sizes.credentialVerySmallHeight,
    roundedCornerSize = Sizes.s02,
    modifier = modifier,
)

@Composable
private fun CredentialCardWithIcon(
    credentialState: CredentialCardState,
    width: Dp,
    height: Dp,
    roundedCornerSize: Dp,
    modifier: Modifier = Modifier,
) = Surface(
    modifier = modifier
        .width(width)
        .height(height),
    shape = RoundedCornerShape(size = roundedCornerSize),
    color = credentialState.backgroundColor,
    contentColor = Color.Unspecified,
) {
    if (credentialState.isCredentialFromBetaIssuer) {
        DemoWatermark(
            color = credentialState.contentColor,
            large = false,
        )
    }
    if (credentialState.useDefaultBackground) {
        CredentialBackgroundFallbackPattern(
            useSmall = true
        )
    }

    val inactiveColor = WalletTheme.colorScheme.inactiveOverlay
    val inactiveModifier = if (credentialState.isUnaccepted) {
        Modifier.drawBehind {
            drawRoundRect(color = inactiveColor)
            drawRoundRect(
                color = credentialState.backgroundColor,
                style = dashedStroke,
                cornerRadius = CornerRadius(roundedCornerSize.toPx()),
            )
        }
    } else {
        Modifier
    }

    Box(
        modifier = inactiveModifier
            .drawBehind {
                drawRect(brush = Gradients.diagonalCredentialBrush())
            }
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        credentialState.logo?.let { logo ->
            Icon(
                painter = logo,
                contentDescription = null,
                modifier = Modifier.size(Sizes.credentialSmallIconSize),
                tint = credentialState.contentColor,
            )
        }
    }
}

private val dashedStroke = Stroke(
    width = 8f,
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f)),
)

private class CredentialCardSmallPreviewParams :
    PreviewParameterProvider<ComposableWrapper<CredentialCardState>> {
    override val values = CredentialMocks.cardStates
}

@WalletComponentPreview
@Composable
private fun CredentialCardSmallPreview(
    @PreviewParameter(CredentialCardSmallPreviewParams::class) state: ComposableWrapper<CredentialCardState>,
) {
    WalletTheme {
        CredentialCardSmall(
            credentialState = state.value(),
        )
    }
}
