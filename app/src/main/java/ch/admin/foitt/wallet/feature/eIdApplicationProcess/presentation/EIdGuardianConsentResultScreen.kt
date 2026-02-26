package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GuardianConsentResultState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun EIdGuardianConsentResultScreen(
    viewModel: EIdGuardianConsentResultViewModel,
) {
    EIdGuardianConsentResultScreenContent(
        screenState = viewModel.screenState,
        deadlineText = viewModel.formattedDate,
        onNext = viewModel::onNext,
    )
}

@Composable
private fun EIdGuardianConsentResultScreenContent(
    screenState: GuardianConsentResultState,
    deadlineText: String?,
    onNext: () -> Unit,
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                iconRes = getMainImage(screenState),
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
            )
        },
        stickyBottomBackgroundColor = Color.Transparent,
        stickyBottomContent = {
            Buttons.FilledPrimary(
                text = getButtonText(screenState),
                onClick = onNext,
            )
        }
    ) {
        when (screenState) {
            GuardianConsentResultState.AV_READY_LEGAL_CONSENT_PENDING -> LegalConsentPendingAvReadyContent(deadlineText)
            GuardianConsentResultState.QUEUEING_LEGAL_CONSENT_OK -> LegalConsentOkAvQueueingContent(deadlineText)
            GuardianConsentResultState.QUEUEING_LEGAL_CONSENT_PENDING -> LegalConsentPendingAvQueueingContent()
            GuardianConsentResultState.AV_EXPIRED_LEGAL_CONSENT_PENDING -> LegalExpiredContent()
        }
    }
}

@Composable
private fun LegalConsentPendingAvReadyContent(deadlineText: String?) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(R.string.tk_getEid_consentPending_avReady_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_getEid_consentPending_avReady_secondary, deadlineText ?: ""),
    )
}

@Composable
private fun LegalConsentOkAvQueueingContent(deadlineText: String?) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_getEid_consentOk_avQueue_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_getEid_consentOk_avQueue_secondary),
    )
    deadlineText?.let {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.tk_getEid_queuing_body2_android),
        )
        WalletTexts.TitleMedium(
            modifier = Modifier.fillMaxWidth(),
            text = deadlineText,
        )
    }
}

@Composable
private fun LegalConsentPendingAvQueueingContent() {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_getEid_consentPending_avQueue_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_getEid_consentPending_avQueue_secondary),
    )
}

@Composable
private fun LegalExpiredContent() {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = R.string.tk_getEid_expired_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = R.string.tk_getEid_expired_secondary),
    )
}

@Composable
private fun getMainImage(screenState: GuardianConsentResultState) = when (screenState) {
    GuardianConsentResultState.AV_READY_LEGAL_CONSENT_PENDING,
    GuardianConsentResultState.QUEUEING_LEGAL_CONSENT_OK,
    GuardianConsentResultState.QUEUEING_LEGAL_CONSENT_PENDING -> R.drawable.wallet_ic_queue_colored
    GuardianConsentResultState.AV_EXPIRED_LEGAL_CONSENT_PENDING -> R.drawable.wallet_ic_cross_circle_colored
}

@Composable
private fun getButtonText(screenState: GuardianConsentResultState) = stringResource(
    when (screenState) {
        GuardianConsentResultState.AV_READY_LEGAL_CONSENT_PENDING,
        GuardianConsentResultState.QUEUEING_LEGAL_CONSENT_OK,
        GuardianConsentResultState.QUEUEING_LEGAL_CONSENT_PENDING,
        GuardianConsentResultState.AV_EXPIRED_LEGAL_CONSENT_PENDING -> R.string.tk_global_close
    }
)

private class EIdStatusResultPreviewParams : PreviewParameterProvider<Pair<GuardianConsentResultState, String?>> {
    override val values = sequenceOf(
        Pair(GuardianConsentResultState.AV_READY_LEGAL_CONSENT_PENDING, "10. Januar 2025 10:07"),
        Pair(GuardianConsentResultState.QUEUEING_LEGAL_CONSENT_OK, "10. Januar 2025"),
        Pair(GuardianConsentResultState.QUEUEING_LEGAL_CONSENT_PENDING, null),
        Pair(GuardianConsentResultState.AV_EXPIRED_LEGAL_CONSENT_PENDING, null),
    )
}

@WalletAllScreenPreview
@Composable
private fun EIdGuardianConsentResultScreenPreview(
    @PreviewParameter(EIdStatusResultPreviewParams::class) previewParams: Pair<GuardianConsentResultState, String?>
) {
    WalletTheme {
        EIdGuardianConsentResultScreenContent(
            screenState = previewParams.first,
            deadlineText = previewParams.second,
            onNext = {},
        )
    }
}
