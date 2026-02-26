package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDp
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
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.utils.contentDescription
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletButtonColors
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun ScannerButton(
    onClick: () -> Unit,
    state: ScannerButtonState,
    modifier: Modifier = Modifier,
    buttonSize: Dp = Sizes.s20,
    @StringRes altTextRes: Int = when (state) {
        ScannerButtonState.Done -> R.string.tk_eidRequest_documentScan_controlButton_done_alt
        ScannerButtonState.Ready -> R.string.tk_eidRequest_documentScan_controlButton_start_alt
        ScannerButtonState.Scanning -> R.string.tk_eidRequest_documentScan_controlButton_stop_alt
    }
) = Button(
    onClick = onClick,
    enabled = state != ScannerButtonState.Done,
    shape = CircleShape,
    colors = WalletButtonColors.brandRed(),
    contentPadding = PaddingValues(),
    modifier = modifier
        .size(buttonSize)
        .contentDescription(stringResource(altTextRes))
) {
    val transition = updateTransition(state)

    val animatedCornerRadius by transition.animateDp { state ->
        when (state) {
            ScannerButtonState.Done -> buttonSize / 2f
            ScannerButtonState.Ready -> buttonSize / 2f
            ScannerButtonState.Scanning -> Sizes.s01
        }
    }

    val animatedPadding by transition.animateDp { state ->
        when (state) {
            ScannerButtonState.Done -> Sizes.s05
            ScannerButtonState.Ready -> Sizes.s02
            ScannerButtonState.Scanning -> Sizes.s05
        }
    }

    when (state) {
        ScannerButtonState.Done -> {
            Icon(
                painter = painterResource(R.drawable.wallet_ic_checkmark_big),
                contentDescription = null,
                modifier = Modifier
                    .padding(animatedPadding)
                    .fillMaxSize(),
            )
        }
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
        ScannerButtonState.Scanning -> {
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

sealed interface ScannerButtonState {
    data object Ready : ScannerButtonState
    data object Scanning : ScannerButtonState
    data object Done : ScannerButtonState
}

private class ScannerButtonPreviewParams : PreviewParameterProvider<ScannerButtonState> {
    override val values: Sequence<ScannerButtonState> = sequenceOf(
        ScannerButtonState.Ready,
        ScannerButtonState.Scanning,
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
        )
    }
}
