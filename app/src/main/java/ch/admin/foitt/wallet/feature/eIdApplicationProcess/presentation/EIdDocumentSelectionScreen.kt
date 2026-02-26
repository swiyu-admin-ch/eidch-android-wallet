package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.addTopScaffoldPadding
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdDocumentType
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun EIdDocumentSelectionScreen(
    viewModel: EIdDocumentSelectionViewModel,
) {
    EIdDocumentSelectionScreenContent(
        showEIdMockMrzButton = viewModel.showEIdMockMrzButton,
        onDocumentSelected = viewModel::onDocumentSelected,
        onClickMock = viewModel::onClickMock
    )
}

@Composable
private fun EIdDocumentSelectionScreenContent(
    showEIdMockMrzButton: Boolean,
    onDocumentSelected: (EIdDocumentType) -> Unit,
    onClickMock: () -> Unit
) {
    CompactDocumentList(
        modifier = Modifier
            .fillMaxSize(),
        showEIdMockMrzButton = showEIdMockMrzButton,
        contentPadding = PaddingValues(bottom = Sizes.s06),
        onDocumentSelected = onDocumentSelected,
        onClickMock = onClickMock
    )
}

@Composable
private fun ListHeader() {
    Column(
        modifier = Modifier
            .addTopScaffoldPadding()
            .padding(
                start = Sizes.s04,
                end = Sizes.s04,
            )
    ) {
        WalletTexts.TitleScreen(
            text = stringResource(id = R.string.tk_eidRequest_documentSelection_primary)
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            text = stringResource(id = R.string.tk_eidRequest_documentSelection_secondary)
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
    }
}

@Composable
private fun CompactDocumentList(
    modifier: Modifier = Modifier,
    showEIdMockMrzButton: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onDocumentSelected: (EIdDocumentType) -> Unit,
    onClickMock: () -> Unit,
) {
    WalletLayouts.LazyColumn(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        contentPadding = contentPadding,
    ) {
        item {
            ListHeader()
        }
        item {
            EIdDocumentItem(
                onClick = { onDocumentSelected(EIdDocumentType.IDENTITY_CARD) },
                imageResource = R.drawable.wallet_id,
                stringResource = R.string.tk_eidRequest_documentSelection_idCard,
            )
        }
        item {
            EIdDocumentItem(
                onClick = { onDocumentSelected(EIdDocumentType.PASSPORT) },
                imageResource = R.drawable.wallet_passport,
                stringResource = R.string.tk_eidRequest_documentSelection_passport,
            )
        }
        item {
            EIdDocumentItem(
                onClick = { onDocumentSelected(EIdDocumentType.RESIDENT_PERMIT) },
                imageResource = R.drawable.wallet_resident_permit,
                stringResource = R.string.tk_eidRequest_documentSelection_residentPermit,
            )
        }
        if (showEIdMockMrzButton) {
            item {
                EIdDocumentItem(
                    onClick = onClickMock,
                    imageResource = R.drawable.wallet_ic_eid,
                    stringResource = R.string.tk_global_moreoptions_alt,
                )
            }
        }
    }
}

@WalletAllScreenPreview
@Composable
private fun EIdDocumentSelectionScreenPreview() {
    WalletTheme {
        EIdDocumentSelectionScreenContent(
            showEIdMockMrzButton = false,
            onDocumentSelected = {},
            onClickMock = {}
        )
    }
}
