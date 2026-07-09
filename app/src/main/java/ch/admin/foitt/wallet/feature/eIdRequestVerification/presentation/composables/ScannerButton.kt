package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletButtonColors
import ch.admin.foitt.wallet.theme.WalletTheme

@Suppress("CyclomaticComplexMethod")
@Composable
fun ScannerButton(
    onClick: () -> Unit,
    state: ScannerButtonState,
    modifier: Modifier = Modifier,
    buttonSize: Dp = Sizes.s20,
    stateTexts: ScannerButtonAltTexts,
) = Box(
    modifier = modifier.size(buttonSize),
    contentAlignment = Alignment.Center,
) {
    val progress by animateFloatAsState(
        targetValue = when (state) {
            is ScannerButtonState.Scanning -> state.completionRatio
            ScannerButtonState.Done -> 1f
            else -> 0f
        },
        label = "ScannerProgress",
        animationSpec = tween(durationMillis = 250, easing = LinearEasing),
    )

    Button(
        onClick = onClick,
        enabled = state != ScannerButtonState.Done,
        shape = CircleShape,
        colors = WalletButtonColors.brandRed(),
        contentPadding = PaddingValues(),
        modifier = Modifier
            .fillMaxSize()
            .semantics(mergeDescendants = true) {
                stateDescription = stateTexts.getString(state)
            }
    ) {
        val transition = updateTransition(state)

        val animatedCornerRadius by transition.animateDp { state ->
            when (state) {
                ScannerButtonState.Initializing,
                ScannerButtonState.Done,
                ScannerButtonState.Ready -> buttonSize / 2f
                is ScannerButtonState.Scanning -> Sizes.s01
            }
        }

        val animatedPadding by transition.animateDp { state ->
            when (state) {
                ScannerButtonState.Done -> Sizes.s05
                ScannerButtonState.Initializing,
                ScannerButtonState.Ready -> Sizes.s04
                is ScannerButtonState.Scanning -> Sizes.s05
            }
        }

        when (state) {
            ScannerButtonState.Done -> {
                Icon(
                    painter = painterResource(R.drawable.wallet_ic_checkmark_big),
                    contentDescription = null,
                    tint = WalletTheme.colorScheme.onWhiteTransparentFixed,
                    modifier = Modifier
                        .padding(animatedPadding)
                        .fillMaxSize(),
                )
            }
            ScannerButtonState.Initializing,
            ScannerButtonState.Ready -> {
                Box(
                    modifier = Modifier
                        .padding(animatedPadding)
                        .fillMaxSize()
                        .background(
                            shape = RoundedCornerShape(corner = CornerSize(animatedCornerRadius)),
                            color = WalletTheme.colorScheme.errorContainer,
                        )
                )
            }
            is ScannerButtonState.Scanning -> {
                Box(
                    modifier = Modifier
                        .padding(animatedPadding)
                        .fillMaxSize()
                        .background(
                            shape = RoundedCornerShape(corner = CornerSize(animatedCornerRadius)),
                            color = WalletTheme.colorScheme.errorContainer,
                        )
                )
            }
        }
    }
    if (state is ScannerButtonState.Scanning) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    hideFromAccessibility()
                },
            color = WalletTheme.colorScheme.onWhiteTransparentFixed,
            strokeWidth = Sizes.s01,
            trackColor = Color.Transparent,
            strokeCap = StrokeCap.Round,
        )
    }
}

data class ScannerButtonAltTexts(
    val done: String,
    val ready: String,
    val scanning: String,
)

private fun ScannerButtonAltTexts.getString(state: ScannerButtonState) = when (state) {
    ScannerButtonState.Initializing -> ""
    ScannerButtonState.Done -> done
    ScannerButtonState.Ready -> ready
    is ScannerButtonState.Scanning -> scanning
}

sealed interface ScannerButtonState {
    data object Initializing : ScannerButtonState
    data object Ready : ScannerButtonState
    data class Scanning(val completionRatio: Float) : ScannerButtonState
    data object Done : ScannerButtonState
}

private class ScannerButtonPreviewParams : PreviewParameterProvider<ScannerButtonState> {
    override val values: Sequence<ScannerButtonState> = sequenceOf(
        ScannerButtonState.Ready,
        ScannerButtonState.Scanning(0.33f),
        ScannerButtonState.Done,
    )
}

@WalletComponentPreview
@Composable
private fun ScannerButtonPreview(
    @PreviewParameter(ScannerButtonPreviewParams::class) state: ScannerButtonState
) {
    WalletTheme {
        ScannerButton(
            onClick = {},
            state = state,
            stateTexts = ScannerButtonAltTexts("", "", "")
        )
    }
}
