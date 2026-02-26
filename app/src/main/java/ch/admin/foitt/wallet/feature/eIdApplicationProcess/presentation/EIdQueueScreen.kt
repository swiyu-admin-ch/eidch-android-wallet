package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun EIdQueueScreen(
    viewModel: EIdQueueViewModel,
) {
    EIdQueueScreenContent(
        deadlineText = viewModel.formattedDate,
        onNext = viewModel::onNext,
    )
}

@Composable
private fun EIdQueueScreenContent(
    deadlineText: String?,
    onNext: () -> Unit,
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                iconRes = R.drawable.wallet_ic_queue_colored,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
            )
        },
        stickyBottomBackgroundColor = Color.Transparent,
        stickyBottomContent = {
            Buttons.FilledPrimary(
                text = stringResource(R.string.tk_global_continue),
                onClick = onNext,
            )
        }
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(id = R.string.tk_getEid_queuing_title),
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.tk_getEid_queuing_body),
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
}

@WalletAllScreenPreview
@Composable
private fun EIdQueueScreenPreview() {
    WalletTheme {
        EIdQueueScreenContent(
            deadlineText = "10. Januar 2025",
            onNext = {},
        )
    }
}
