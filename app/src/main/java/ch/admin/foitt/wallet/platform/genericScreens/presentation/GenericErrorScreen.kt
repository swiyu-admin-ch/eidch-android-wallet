package ch.admin.foitt.wallet.platform.genericScreens.presentation

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
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun GenericErrorScreen(
    viewModel: GenericErrorViewModel,
) {
    GenericErrorScreenContent(
        title = viewModel.title,
        subtitle = viewModel.subtitle,
        errorText = viewModel.errorText,
        errorDescription = viewModel.errorDescription,
        onBack = viewModel::onBack,
    )
}

@Composable
private fun GenericErrorScreenContent(
    title: Int,
    subtitle: Int,
    errorText: String?,
    errorDescription: Int?,
    onBack: () -> Unit,
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                iconRes = R.drawable.wallet_ic_cross_circle_colored,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow,
            )
        },
        stickyBottomBackgroundColor = Color.Transparent,
        stickyBottomContent = {
            Buttons.FilledPrimary(
                text = stringResource(R.string.global_error_backToHome_button),
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(title)
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            text = stringResource(subtitle)
        )
        Spacer(modifier = Modifier.height(Sizes.s06))

        errorText?.let {
            WalletTexts.BodyLarge(
                text = it
            )
        }
        errorDescription?.let {
            WalletTexts.BodyLarge(
                text = stringResource(it),
            )
        }
    }
}

@WalletAllScreenPreview
@Composable
private fun GenericErrorScreenPreview() {
    WalletTheme {
        GenericErrorScreenContent(
            title = R.string.presentation_error_title,
            subtitle = R.string.presentation_error_message,
            errorText = GenericErrorScreenState.INVALID_REQUEST.name.lowercase(),
            errorDescription = R.string.tk_credentialOffer_error_invalidRequest_description,
            onBack = {},
        )
    }
}
