package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
fun EIdPrivacyPolicyScreen(
    viewModel: EIdPrivacyPolicyViewModel,
) {
    val activity = LocalActivity.current

    EIdPrivacyPolicyScreenContent(
        onEIdPrivacyPolicy = viewModel::onEIdPrivacyPolicy,
        onNext = { activity.let { viewModel.onNext(it) } },
    )
}

@Composable
private fun EIdPrivacyPolicyScreenContent(
    onEIdPrivacyPolicy: () -> Unit,
    onNext: () -> Unit,
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                iconRes = R.drawable.wallet_ic_shield_person_colored,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
            )
        },
        stickyBottomBackgroundColor = Color.Transparent,
        stickyBottomContent = {
            Buttons.FilledPrimary(
                text = stringResource(R.string.tk_getEid_dataPrivacy_primaryButton),
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(id = R.string.tk_getEid_dataPrivacy_title),
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.tk_getEid_dataPrivacy_body),
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        Buttons.TextLink(
            text = stringResource(id = R.string.tk_getEid_dataPrivacy_link_text),
            onClick = onEIdPrivacyPolicy,
            endIcon = painterResource(id = R.drawable.wallet_ic_chevron),
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun EIdPrivacyPolicyScreenPreview() {
    WalletTheme {
        EIdPrivacyPolicyScreenContent(
            onEIdPrivacyPolicy = {},
            onNext = {},
        )
    }
}
