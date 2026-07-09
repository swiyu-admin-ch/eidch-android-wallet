package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.AdaptiveBottomButtonBar
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainAnimation
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdUiDocumentType
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun EIdDocumentRecordingInfoScreen(
    viewModel: EIdDocumentRecordingInfoViewModel,
) {
    EIdDocumentRecordingInfoScreenContent(
        documentType = viewModel.documentType.collectAsStateWithLifecycle().value,
        onContinue = viewModel::onContinue,
    )
}

@Composable
private fun EIdDocumentRecordingInfoScreenContent(
    documentType: EIdUiDocumentType,
    onContinue: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        val animationRes = when (documentType) {
            EIdUiDocumentType.IDENTITY_CARD, EIdUiDocumentType.RESIDENT_PERMIT -> R.raw.doc_record
            EIdUiDocumentType.PASSPORT -> R.raw.doc_record_pass
        }

        ScreenMainAnimation(
            animationRes = animationRes,
            fallbackImage = R.drawable.wallet_ic_docrec,
        )
    },
    stickyBottomContent = {
        AdaptiveBottomButtonBar(
            buttons = listOf(
                {
                    Buttons.FilledPrimary(
                        text = stringResource(R.string.tk_eidRequest_recordDocument_information_button_primary),
                        onClick = onContinue,
                    )
                },
            ),
        )
    }
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(R.string.tk_eidRequest_recordDocument_information_primary)
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(stringResource(R.string.tk_eidRequest_recordDocument_information_secondary))
}

@WalletAllScreenPreview
@Composable
private fun EIdDocumentRecordingInfoScreenPreview() {
    WalletTheme {
        EIdDocumentRecordingInfoScreenContent(
            documentType = EIdUiDocumentType.PASSPORT,
            onContinue = {},
        )
    }
}
