package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun OnboardingErrorScreen(
    @Suppress("UNUSED_PARAMETER") viewModel: OnboardingErrorViewModel
) {
    val currentActivity = LocalActivity.current
    BackHandler {
        currentActivity.finish()
    }

    OnboardingErrorScreenContent(
        onClose = currentActivity::finish,
    )
}

@Composable
private fun OnboardingErrorScreenContent(
    onClose: () -> Unit,
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                iconRes = R.drawable.wallet_ic_cross_circle_colored,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow,
            )
        },
        stickyBottomContent = {
            Buttons.FilledPrimary(
                text = stringResource(id = R.string.tk_global_continue),
                onClick = onClose,
            )
        },
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(R.string.tk_onboarding_biometricsPermissionLater_primary)
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            text = stringResource(id = R.string.tk_onboarding_biometricsPermissionLater_secondary)
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun OnboardingErrorPreview() {
    WalletTheme {
        OnboardingErrorScreenContent(onClose = {})
    }
}
