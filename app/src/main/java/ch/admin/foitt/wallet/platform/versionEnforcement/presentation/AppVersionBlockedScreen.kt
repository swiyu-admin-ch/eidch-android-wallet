package ch.admin.foitt.wallet.platform.versionEnforcement.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.AdaptiveButtonContainer
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.centerHorizontallyOnFullscreen
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithFullscreenGradient
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.EnforcementType
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun AppVersionBlockedScreen(viewModel: AppVersionBlockedViewModel) {
    val activity = LocalActivity.current
    BackHandler {
        activity.finish()
    }
    AppVersionBlockedScreenContent(
        title = viewModel.title,
        text = viewModel.text,
        enforcedType = viewModel.enforcedType,
        onPlayStore = viewModel::goToPlayStore,
        onContinue = viewModel::onContinue,
        onSettings = viewModel::onSettings,
        onClose = viewModel::onClose
    )
}

@Composable
private fun AppVersionBlockedScreenContent(
    title: String?,
    text: String?,
    enforcedType: EnforcementType,
    onPlayStore: () -> Unit,
    onContinue: () -> Unit,
    onSettings: () -> Unit,
    onClose: () -> Unit
) {
    WalletLayouts.ScrollableColumnWithFullscreenGradient(
        stickyBottomContent = {
            AdaptiveButtonContainer(
                buttons = listOf(
                    {
                        BottomButtons(
                            enforcedType = enforcedType,
                            onPlayStore = onPlayStore,
                            onContinue = onContinue,
                            onSettings = onSettings,
                            onClose = onClose,
                        )
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .bottomSafeDrawing()
                    .padding(Sizes.s04),
            )
        },
        scrollableContent = {
            Column(
                modifier = Modifier
                    .centerHorizontallyOnFullscreen(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                title?.let {
                    WalletTexts.TitleScreen(
                        modifier = Modifier.widthIn(max = Sizes.maxTextWidth),
                        text = title,
                        maxLines = Int.MAX_VALUE,
                        textAlign = TextAlign.Center,
                        color = WalletTheme.colorScheme.onGradientFixed,
                    )
                }
                Spacer(modifier = Modifier.height(Sizes.s02))
                text?.let {
                    WalletTexts.Body(
                        modifier = Modifier.widthIn(max = Sizes.maxTextWidth),
                        text = text,
                        textAlign = TextAlign.Center,
                        color = WalletTheme.colorScheme.onGradientFixed,
                    )
                }
            }
        },
    )
}

@Composable
private fun BottomButtons(
    enforcedType: EnforcementType,
    onPlayStore: () -> Unit,
    onContinue: () -> Unit,
    onSettings: () -> Unit,
    onClose: () -> Unit
) {
    when (enforcedType) {
        EnforcementType.OS_UPDATE -> {
            Buttons.FilledPrimaryFixed(
                text = stringResource(id = R.string.tk_versionEnforcement_systemUpdate_button),
                onClick = onSettings,
            )
        }

        EnforcementType.DEVICE_BLACKLIST -> {
            Buttons.FilledPrimaryFixed(
                text = stringResource(id = R.string.tk_versionEnforcement_blacklisted_button),
                onClick = onClose,
            )
        }

        EnforcementType.APP_BLOCKED -> {
            Buttons.FilledPrimaryFixed(
                text = stringResource(id = R.string.tk_versionEnforcement_button),
                onClick = onPlayStore,
            )
        }

        EnforcementType.UPDATE_SUGGESTED -> {
            Buttons.FilledPrimaryFixed(
                text = stringResource(id = R.string.tk_versionEnforcement_button),
                onClick = onPlayStore,
            )
            Buttons.FilledSecondaryFixed(
                text = stringResource(id = R.string.tk_versionEnforcement_later_button),
                onClick = onContinue,
            )
        }
    }
}

@WalletAllScreenPreview
@Composable
private fun AppVersionBlockedScreenPreview() {
    WalletTheme {
        AppVersionBlockedScreenContent(
            title = "App is too old",
            text = "You need to update the app",
            onPlayStore = {},
            enforcedType = EnforcementType.UPDATE_SUGGESTED,
            onContinue = {},
            onSettings = {},
            onClose = {},
        )
    }
}
