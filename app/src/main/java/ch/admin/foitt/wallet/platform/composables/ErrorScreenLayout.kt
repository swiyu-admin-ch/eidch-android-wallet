package ch.admin.foitt.wallet.platform.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts

@Composable
fun WalletLayouts.StandardErrorScreen(
    @StringRes primaryText: Int,
    @StringRes secondaryText: Int?,
    modifier: Modifier = Modifier,
    @StringRes primaryActionText: Int?,
    @StringRes secondaryActionText: Int? = null,
    @StringRes tertiaryActionText: Int? = null,
    primaryAction: (() -> Unit)?,
    secondaryAction: (() -> Unit)? = null,
    tertiaryAction: (() -> Unit)? = null,
    @DrawableRes mainImage: Int = R.drawable.wallet_ic_cross_circle_colored,
) = WalletLayouts.ScrollableColumnWithPicture(
    modifier = modifier,
    stickyStartContent = {
        ScreenMainImage(
            iconRes = mainImage,
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        if (primaryActionText != null && primaryAction != null) {
            Buttons.FilledPrimary(
                text = stringResource(primaryActionText),
                onClick = primaryAction,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (secondaryActionText != null && secondaryAction != null) {
            Buttons.TonalSecondary(
                text = stringResource(secondaryActionText),
                onClick = secondaryAction,
                modifier = Modifier.fillMaxWidth()
            )
        }
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(id = primaryText),
    )
    secondaryText?.let {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = secondaryText),
        )
    }

    tertiaryActionText?.let {
        Spacer(modifier = Modifier.height(Sizes.s04))
        Buttons.TextLink(
            text = stringResource(tertiaryActionText),
            onClick = tertiaryAction ?: {},
            endIcon = painterResource(id = R.drawable.wallet_ic_external_link)
        )
    }
}

@Composable
internal fun WalletLayouts.UnexpectedErrorContent(
    onCloseError: () -> Unit,
) = WalletLayouts.StandardErrorScreen(
    primaryText = R.string.tk_global_error_unexpected_title,
    secondaryText = R.string.tk_global_error_unexpected_message,
    primaryActionText = R.string.tk_global_close,
    primaryAction = onCloseError,
)

@Composable
internal fun WalletLayouts.NetworkErrorContent(
    onCloseError: () -> Unit,
    onRetry: (() -> Unit)? = null,
) = WalletLayouts.StandardErrorScreen(
    primaryText = R.string.tk_global_error_network_title,
    secondaryText = R.string.tk_global_error_network_message,
    primaryActionText = R.string.tk_global_retry,
    primaryAction = onRetry,
    secondaryActionText = R.string.tk_global_error_close,
    secondaryAction = onCloseError,
)
