package ch.admin.foitt.wallet.platform.permission.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.platform.composables.AdaptiveBottomButtonBar
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.nonFocusableAccessibilityAnchor
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun PermissionScreenContent(
    @DrawableRes icon: Int,
    @StringRes primaryButton: Int,
    @StringRes title: Int,
    @StringRes message: Int,
    onAllow: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            modifier = Modifier.testTag(TestTags.PERMISSION_INTRO_ICON.name),
            iconRes = icon,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerHigh,
        )
    },
    stickyBottomContent = {
        AdaptiveBottomButtonBar(
            buttons = listOf(
                {
                    Buttons.FilledPrimary(
                        modifier = Modifier.testTag(TestTags.ACCEPT_BUTTON.name),
                        text = stringResource(id = primaryButton),
                        onClick = onAllow,
                    )
                },
            ),
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        modifier = Modifier.nonFocusableAccessibilityAnchor(),
        text = stringResource(id = title)
    )
    Spacer(modifier = Modifier.height(Sizes.s05))
    WalletTexts.BodyLarge(
        text = stringResource(id = message),
        modifier = Modifier.fillMaxWidth(),
    )
}
