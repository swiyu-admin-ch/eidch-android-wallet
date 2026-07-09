package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.faceScanner

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.AdaptiveBottomButtonBar
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun FaceScannerErrorContent(
    @StringRes title: Int,
    @StringRes content: Int,
    @StringRes buttonText: Int,
    onButton: () -> Unit,
    onHelp: () -> Unit
) {
    FaceScannerContent(
        icon = R.drawable.wallet_ic_cross_circle_colored,
        primaryText = title,
        secondaryText = content,
        buttonText = buttonText,
        buttonHelp = R.string.tk_error_generic_helpLink_label,
        onRetry = onButton,
        onHelp = onHelp,
    )
}

@Composable
private fun FaceScannerContent(
    @DrawableRes icon: Int,
    @StringRes primaryText: Int,
    @StringRes secondaryText: Int?,
    @StringRes buttonText: Int?,
    @StringRes buttonHelp: Int,
    onRetry: () -> Unit = {},
    onHelp: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = icon,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
        )
    },
    stickyBottomContent = {
        buttonText?.let {
            AdaptiveBottomButtonBar(
                buttons = listOf(
                    {
                        Buttons.FilledPrimary(
                            text = stringResource(buttonText),
                            onClick = onRetry,
                        )
                    },
                ),
            )
        }
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(primaryText)
    )
    secondaryText?.let {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(secondaryText)
        )
    }
    Spacer(modifier = Modifier.height(Sizes.s06))
    Buttons.TextLink(
        text = stringResource(id = buttonHelp),
        onClick = onHelp,
        endIcon = painterResource(id = R.drawable.wallet_ic_external_link),
    )
}
