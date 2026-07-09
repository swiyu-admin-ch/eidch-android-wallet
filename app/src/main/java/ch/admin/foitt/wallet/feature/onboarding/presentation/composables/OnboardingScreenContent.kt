package ch.admin.foitt.wallet.feature.onboarding.presentation.composables

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts

@Composable
fun OnboardingScreenContent(
    title: String,
    subtitle: String,
    details: String? = null,
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = title
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        text = subtitle,
        modifier = Modifier
            .fillMaxWidth()
    )
    details?.let {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.Body(
            text = details,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}
