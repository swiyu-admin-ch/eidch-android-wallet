package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.ProcessDataUiState
import ch.admin.foitt.wallet.platform.composables.AdaptiveBottomButtonBar
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.WalletLinearProgressIndicator
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun EIdProcessDataScreen(
    viewModel: EIdProcessDataViewModel,
) {
    EIdProcessDataScreenContent(
        processDataState = viewModel.state.collectAsStateWithLifecycle().value,
    )
}

@Composable
private fun EIdProcessDataScreenContent(
    processDataState: ProcessDataUiState,
) = when (processDataState) {
    is ProcessDataUiState.Processing -> LoadingContent(processDataState.progress)

    is ProcessDataUiState.Declined -> DeclinedErrorContent(
        onClose = processDataState.onClose,
        onHelp = processDataState.onHelp,
    )

    is ProcessDataUiState.GenericError -> GenericErrorContent(
        onClose = processDataState.onClose,
        onRetry = processDataState.onRetry,
        onHelp = processDataState.onHelp,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadingContent(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing,
        ),
        label = "upload_progress",
    )

    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(WalletTheme.shapes.extraLarge)
                    .background(WalletTheme.colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .padding(Sizes.s06)
                        .widthIn(max = 240.dp),
                ) {
                    WalletLinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Sizes.s02),
                    )
                    Spacer(modifier = Modifier.height(Sizes.s02))
                    WalletTexts.BodyMedium(
                        text = "${(animatedProgress * 100).toInt()}%",
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { hideFromAccessibility() },
                        textAlign = TextAlign.End,
                        color = WalletTheme.colorScheme.progressTrackColor,
                    )
                }
            }
        },
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(id = R.string.tk_eidRequest_submitDocuments_primary),
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.tk_eidRequest_submitDocuments_secondary),
        )
    }
}

@Composable
private fun DeclinedErrorContent(
    onClose: () -> Unit,
    onHelp: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_cross_circle_colored,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
        )
    },
    stickyBottomContent = {
        AdaptiveBottomButtonBar(
            buttons = listOf(
                {
                    Buttons.FilledPrimary(
                        text = stringResource(R.string.tk_eidRequest_dataProcess_declined_primaryButton),
                        onClick = onClose,
                    )
                },
            ),
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_eidRequest_dataProcess_declined_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_eidRequest_dataProcess_declined_secondary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    Buttons.TextLink(
        text = stringResource(id = R.string.tk_eidRequest_dataProcess_link_textButton),
        onClick = onHelp,
        endIcon = painterResource(id = R.drawable.wallet_ic_chevron),
    )
}

@Composable
private fun GenericErrorContent(
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onHelp: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_cross_circle_colored,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
        )
    },
    stickyBottomContent = {
        AdaptiveBottomButtonBar(
            buttons = listOf(
                {
                    Buttons.FilledPrimary(
                        text = stringResource(R.string.tk_eidRequest_dataProcess_error_primaryButton),
                        onClick = onRetry,
                    )
                },
                {
                    Buttons.TonalSecondary(
                        text = stringResource(R.string.tk_eidRequest_dataProcess_error_secondaryButton),
                        onClick = onClose,
                    )
                },
            ),
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_eidRequest_dataProcess_error_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_eidRequest_dataProcess_error_secondary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    Buttons.TextLink(
        text = stringResource(id = R.string.tk_eidRequest_dataProcess_link_textButton),
        onClick = onHelp,
        endIcon = painterResource(id = R.drawable.wallet_ic_chevron),
    )
}

private class EIdProcessDataPreviewParams : PreviewParameterProvider<ProcessDataUiState> {
    override val values: Sequence<ProcessDataUiState> = sequenceOf(
        ProcessDataUiState.Processing(0.5f),
        ProcessDataUiState.Processing(1f),
        ProcessDataUiState.Declined({}, {}),
        ProcessDataUiState.GenericError({}, {}, {}),
    )
}

@WalletAllScreenPreview
@Composable
private fun EIdProcessDataScreenPreview(
    @PreviewParameter(EIdProcessDataPreviewParams::class) state: ProcessDataUiState,
) {
    WalletTheme {
        EIdProcessDataScreenContent(
            processDataState = state,
        )
    }
}
