package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner.DocumentScannerErrorContent
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner.EIdDocumentScanSummaryUiState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.clusterLazyListItem
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.DocumentScannerErrorType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdDocumentType
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun EIdDocumentScanSummaryScreen(viewModel: EIdDocumentScanSummaryViewModel) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    BackHandler(enabled = true, viewModel::onClose)

    when (uiState) {
        is EIdDocumentScanSummaryUiState.Initial -> {
            LoadingOverlay(true)
        }

        is EIdDocumentScanSummaryUiState.Error -> {
            DocumentScannerErrorContent(
                type = DocumentScannerErrorType.GENERIC,
                onRetry = viewModel::onRetry,
                onHelp = viewModel::onHelp,
            )
        }

        is EIdDocumentScanSummaryUiState.Ready -> {
            val frontsidePainter = remember(uiState.frontsideImage) {
                val bitmap = BitmapFactory.decodeByteArray(uiState.frontsideImage, 0, uiState.frontsideImage.size).asImageBitmap()
                BitmapPainter(bitmap)
            }

            val backsidePainter = remember(uiState.backsideImage) {
                val bitmap = BitmapFactory.decodeByteArray(uiState.backsideImage, 0, uiState.backsideImage.size).asImageBitmap()
                BitmapPainter(bitmap)
            }

            EIdDocumentScanSummaryScreenContent(
                documentType = uiState.documentType,
                frontsidePainter = frontsidePainter,
                backsidePainter = backsidePainter,
                onContinue = viewModel::onContinue,
                onRetry = viewModel::onRetry,
            )
        }
    }
}

@Composable
private fun EIdDocumentScanSummaryScreenContent(
    documentType: EIdDocumentType,
    frontsidePainter: Painter,
    backsidePainter: Painter,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
) {
    val documentTypeText = stringResource(documentType.stringRes())
    val firstScanAltText = stringResource(
        R.string.tk_eidRequest_scanDocumentSubmit_firstScanImageAlt,
        documentTypeText
    )
    val secondScanAltText = stringResource(
        R.string.tk_eidRequest_scanDocumentSubmit_secondScanImageAlt,
        documentTypeText
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = WalletTheme.colorScheme.surfaceContainerLow)
    ) {
        var reportedBlockHeight by remember {
            mutableStateOf(0.dp)
        }

        WalletLayouts.LazyColumn(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = Sizes.contentMaxWidth)
                .fillMaxWidth()
                .padding(
                    start = Sizes.s04,
                    end = Sizes.s04,
                ),
            useTopInsets = false,
            useBottomInsets = false,
        ) {
            item {
                WalletLayouts.TopInsetSpacer(
                    shouldScrollUnderTopBar = true,
                    scaffoldPaddings = LocalScaffoldPaddings.current,
                )
            }

            documentImage(
                headerText = R.string.tk_eidRequest_scanDocumentSubmit_firstScanImageTitle,
                contentDescription = firstScanAltText,
                painter = frontsidePainter,
                isFirst = true,
            )

            documentImage(
                headerText = R.string.tk_eidRequest_scanDocumentSubmit_secondScanImageTitle,
                contentDescription = secondScanAltText,
                painter = backsidePainter,
                isLast = true,
            )

            item {
                Spacer(Modifier.height(reportedBlockHeight))
            }
        }

        HeightReportingLayout(
            modifier = Modifier.align(Alignment.BottomCenter),
            onContentHeightMeasured = { height ->
                reportedBlockHeight = height
            },
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = Sizes.maxClusterWidth)
                    .fillMaxWidth()
                    .bottomSafeDrawing()
                    .padding(
                        start = WalletLayouts.paddingStickyMedium,
                        end = WalletLayouts.paddingStickyMedium,
                        bottom = WalletLayouts.paddingStickyMedium
                    )
            ) {
                Buttons.FilledPrimary(
                    text = stringResource(R.string.tk_global_continue),
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                )
                Buttons.TonalSecondary(
                    text = stringResource(R.string.tk_eidRequest_scanDocumentSubmit_secondaryButton),
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun LazyListScope.documentImage(
    @StringRes headerText: Int,
    painter: Painter,
    contentDescription: String,
    isFirst: Boolean = false,
    isLast: Boolean = false,
) = this.clusterLazyListItem(
    isFirstItem = isFirst,
    isLastItem = isLast,
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
        overlineContent = { WalletTexts.LabelMedium(text = stringResource(headerText)) },
        headlineContent = {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier
                    .padding(top = Sizes.s02)
                    .fillMaxWidth()
                    .heightIn(max = Sizes.claimImageMaxHeight)
                    .clip(RoundedCornerShape(Sizes.s02)),
                contentScale = ContentScale.Crop,
            )
        },
    )
}

@StringRes
private fun EIdDocumentType.stringRes(): Int {
    return when (this) {
        EIdDocumentType.IDENTITY_CARD -> R.string.tk_eidRequest_documentSelection_idCard
        EIdDocumentType.PASSPORT -> R.string.tk_eidRequest_documentSelection_passport
        EIdDocumentType.RESIDENT_PERMIT -> R.string.tk_eidRequest_documentSelection_residentPermit
    }
}

@WalletAllScreenPreview
@Composable
private fun EIdDocumentScanSummaryScreenContentPreview() {
    WalletTheme {
        EIdDocumentScanSummaryScreenContent(
            documentType = EIdDocumentType.IDENTITY_CARD,
            frontsidePainter = ColorPainter(Color.Cyan),
            backsidePainter = ColorPainter(Color.Cyan),
            onContinue = {},
            onRetry = {},
        )
    }
}
