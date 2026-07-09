package ch.admin.foitt.wallet.platform.permission.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.AdaptiveBottomButtonBar
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.nonFocusableAccessibilityAnchor
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun PermissionBlockedScreenContent(
    onOpenSettings: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_camera_colored,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerHigh,
        )
    },
    stickyBottomContent = {
        AdaptiveBottomButtonBar(
            buttons = listOf(
                {
                    Buttons.FilledPrimary(
                        text = stringResource(id = R.string.tk_global_tothesettings),
                        onClick = onOpenSettings,
                    )
                },
            ),
        )
    }
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        modifier = Modifier.nonFocusableAccessibilityAnchor(),
        text = stringResource(id = R.string.tk_receive_cameraaccessneeded3_title)
    )
    Spacer(modifier = Modifier.height(Sizes.s05))
    WalletTexts.BodyLarge(
        text = stringResource(id = R.string.tk_receive_cameraaccessneeded3_body),
        modifier = Modifier.fillMaxWidth(),
    )
}

@WalletComponentPreview
@Composable
private fun PermissionBlockedScreenContentPreview() {
    WalletTheme {
        PermissionBlockedScreenContent(
            onOpenSettings = {},
        )
    }
}
