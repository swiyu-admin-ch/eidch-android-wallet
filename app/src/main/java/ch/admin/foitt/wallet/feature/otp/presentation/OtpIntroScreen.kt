package ch.admin.foitt.wallet.feature.otp.presentation

import androidx.compose.foundation.layout.PaddingValues
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
internal fun OtpIntroScreen(
    viewModel: OtpIntroViewModel,
) {
    OtpIntroScreenContent(
        onContinue = viewModel::onContinue,
        onCancel = viewModel::onCancel
    )
}

@Composable
private fun OtpIntroScreenContent(
    onContinue: () -> Unit,
    onCancel: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_otp_intro,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerLow,
            fillMaxWidth = 0.75f,
            paddingValues = PaddingValues(vertical = Sizes.s04)
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        Buttons.FilledPrimary(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.tk_eidRequest_otp_intro_primaryButton),
            onClick = onContinue,
        )
        Buttons.TonalSecondary(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.tk_eidRequest_otp_intro_secondaryButton),
            onClick = onCancel,
        )
    }
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(R.string.tk_eidRequest_otp_intro_title)
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(stringResource(R.string.tk_eidRequest_otp_intro_body))
}

@WalletAllScreenPreview
@Composable
private fun OtpIntroScreenPreview() {
    WalletTheme {
        OtpIntroScreenContent(
            onContinue = {},
            onCancel = {}
        )
    }
}
