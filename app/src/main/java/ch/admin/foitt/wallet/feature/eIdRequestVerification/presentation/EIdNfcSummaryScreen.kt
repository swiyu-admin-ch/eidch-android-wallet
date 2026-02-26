package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.clusterLazyListItem
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import coil.compose.AsyncImage

@Composable
internal fun EIdNfcSummaryScreen(viewModel: EIdNfcSummaryViewModel) {
    EIdNfcSummaryScreenContent(
        picture = viewModel.picture,
        givenName = viewModel.givenName,
        surname = viewModel.surname,
        documentId = viewModel.documentId,
        expiryDate = viewModel.expiryDate,
        onContinue = viewModel::onContinue,
    )
}

@Composable
private fun EIdNfcSummaryScreenContent(
    picture: ByteArray,
    givenName: String,
    surname: String,
    documentId: String,
    expiryDate: String,
    onContinue: () -> Unit,
) = Box(
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
            .widthIn(max = Sizes.maxClusterWidth)
            .fillMaxWidth()
            .padding(
                start = Sizes.s04,
                end = Sizes.s04,
            ),
        useTopInsets = true,
        useBottomInsets = false,
    ) {
        item {
            WalletLayouts.TopInsetSpacer(
                shouldScrollUnderTopBar = true,
                scaffoldPaddings = LocalScaffoldPaddings.current,
            )
        }
        item {
            Spacer(modifier = Modifier.height(Sizes.s01))
            WalletTexts.HeadlineSmall(
                text = stringResource(R.string.tk_eidRequest_nfcScan_summary_primary)
            )
            Spacer(modifier = Modifier.height(Sizes.s06))
        }
        documentImage(
            headerText = R.string.tk_eidRequest_nfcScan_summary_documentPicture,
            base64ImageData = picture,
        )
        documentItem(
            headerText = R.string.tk_eidRequest_nfcScan_summary_givenName,
            contentText = givenName,
        )
        documentItem(
            headerText = R.string.tk_eidRequest_nfcScan_summary_surname,
            contentText = surname,
        )
        documentItem(
            headerText = R.string.tk_eidRequest_nfcScan_summary_documentId,
            contentText = documentId,
        )
        documentItem(
            headerText = R.string.tk_eidRequest_nfcScan_summary_expiryDate,
            contentText = expiryDate,
            lastItem = true,
        )
        item {
            Spacer(modifier = Modifier.height(reportedBlockHeight))
        }
    }

    HeightReportingLayout(
        modifier = Modifier.align(Alignment.BottomCenter),
        onContentHeightMeasured = { height -> reportedBlockHeight = height },
    ) {
        Buttons.FilledPrimary(
            text = stringResource(R.string.tk_eidRequest_nfcScan_summary_button_continue),
            onClick = onContinue,
            modifier = Modifier
                .widthIn(max = Sizes.maxClusterWidth)
                .fillMaxWidth()
                .bottomSafeDrawing()
                .padding(
                    start = WalletLayouts.paddingStickyMedium,
                    end = WalletLayouts.paddingStickyMedium,
                    bottom = WalletLayouts.paddingStickyMedium
                ),

        )
    }
}

private fun LazyListScope.documentItem(
    @StringRes headerText: Int,
    contentText: String,
    lastItem: Boolean = false,
) = this.clusterLazyListItem(
    isFirstItem = false,
    isLastItem = lastItem,
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
        overlineContent = {
            WalletTexts.LabelMedium(
                text = stringResource(headerText)
            )
        },
        headlineContent = {
            WalletTexts.BodyLarge(
                text = contentText
            )
        },
        supportingContent = null,
    )
}

private fun LazyListScope.documentImage(
    @StringRes headerText: Int,
    base64ImageData: ByteArray,
) = this.clusterLazyListItem(
    isFirstItem = true,
    isLastItem = false,
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
        overlineContent = {
            WalletTexts.LabelMedium(
                text = stringResource(headerText),
            )
        },
        headlineContent = {
            AsyncImage(
                modifier = Modifier
                    .padding(top = Sizes.s02)
                    .heightIn(max = Sizes.claimImageMaxHeight)
                    .clip(RoundedCornerShape(Sizes.s02)),
                model = base64ImageData,
                alignment = Alignment.TopStart,
                contentScale = ContentScale.Fit,
                contentDescription = null,
                filterQuality = FilterQuality.High,
            )
        },
        supportingContent = null,
    )
}

@WalletAllScreenPreview
@Composable
private fun EIdNfcSummaryScreenPreview() {
    WalletTheme {
        EIdNfcSummaryScreenContent(
            picture = byteArrayOf(),
            givenName = "Max",
            surname = "Mustermann",
            documentId = "123456789",
            expiryDate = "01.01.2024",
            onContinue = {},
        )
    }
}
