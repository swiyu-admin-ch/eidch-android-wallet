package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.nfcScanner

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import ch.admin.foitt.wallet.platform.composables.presentation.LoadingIndicator
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun NfcScannerInfoContent(
    onStart: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_nfc_info,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerLow,
            paddingValues = PaddingValues(vertical = Sizes.s04)
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        Buttons.FilledPrimary(
            text = stringResource(R.string.tk_eidRequest_nfcScan_intro_primaryButton),
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
        )
    },
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(R.string.tk_eidRequest_nfcScan_intro_primary)
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.tk_eidRequest_nfcScan_intro_secondary)
    )
}

@Composable
internal fun NfcScannerChipDetectionContent(
    onStop: () -> Unit,
) = NfcScannerContent(
    icon = R.drawable.wallet_ic_spinner_colored,
    primaryText = R.string.tk_eidRequest_nfcScan_chipDetection_primary,
    secondaryText = R.string.tk_eidRequest_nfcScan_chipDetection_secondary,
    buttonText = R.string.tk_eidRequest_nfcScan_chipDetection_button_scanning,
    isButtonActive = true,
    onButtonClick = onStop,
)

@Composable
internal fun NfcScannerChipDataReadingContent(
    onStop: () -> Unit,
) = NfcScannerContent(
    icon = R.drawable.wallet_ic_queue_colored,
    primaryText = R.string.tk_eidRequest_nfcScan_chipDataReading_primary,
    secondaryText = R.string.tk_eidRequest_nfcScan_chipDataReading_secondary,
    buttonText = R.string.tk_eidRequest_nfcScan_chipDataReading_button_scanning,
    isButtonActive = true,
    onButtonClick = onStop,
)

@Composable
internal fun NfcScannerSuccessContent() = NfcScannerContent(
    icon = R.drawable.wallet_ic_check_circle_colored,
    primaryText = R.string.tk_eidRequest_nfcScan_success_primary,
    secondaryText = R.string.tk_eidRequest_nfcScan_success_secondary,
    buttonText = null,
    isButtonActive = false,
    onButtonClick = { },
)

@Composable
internal fun NfcScannerErrorContent(
    onRetry: () -> Unit,
) = NfcScannerContent(
    icon = R.drawable.wallet_ic_cross_circle_colored,
    primaryText = R.string.tk_eidRequest_nfcScan_error_primary,
    secondaryText = R.string.tk_eidRequest_nfcScan_error_secondary,
    buttonText = R.string.tk_eidRequest_nfcScan_error_button_retry,
    isButtonActive = false,
    onButtonClick = onRetry,
)

@Composable
internal fun NfcScannerFailureContent(
    onContinue: () -> Unit,
    onRetry: () -> Unit,
) = NfcScannerContent(
    icon = R.drawable.wallet_ic_cross_circle_colored,
    primaryText = R.string.tk_eidRequest_nfcScan_failure_primary,
    secondaryText = R.string.tk_eidRequest_nfcScan_failure_secondary,
    buttonText = R.string.tk_eidRequest_nfcScan_failure_button_continue,
    isButtonActive = false,
    onButtonClick = onContinue,
    button2Text = R.string.tk_eidRequest_nfcScan_failure_button_retry,
    onButton2Click = onRetry,
)

@Composable
internal fun NfcScannerNfcDisabledContent(
    onEnableNfc: () -> Unit,
) = NfcScannerContent(
    icon = R.drawable.wallet_ic_nfc_disabled_colored,
    primaryText = R.string.tk_eidRequest_nfcScan_nfcDisabled_primary,
    secondaryText = R.string.tk_eidRequest_nfcScan_nfcDisabled_secondary,
    buttonText = R.string.tk_eidRequest_nfcScan_nfcDisabled_button_enable,
    isButtonActive = false,
    onButtonClick = onEnableNfc,
)

@Composable
internal fun NfcScannerLoadingContent() = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        LoadingIndicator()
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = null,
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleScreen(
        text = stringResource(R.string.tk_eidRequest_nfcScan_loading_primary),
    )
}

@Composable
private fun NfcScannerContent(
    @DrawableRes icon: Int,
    @StringRes primaryText: Int,
    @StringRes secondaryText: Int?,
    @StringRes buttonText: Int?,
    onButtonClick: () -> Unit = {},
    isButtonActive: Boolean,
    @StringRes button2Text: Int? = null,
    onButton2Click: () -> Unit = {},
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = icon,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
        )
    },
    stickyBottomBackgroundColor = Color.Transparent,
    stickyBottomContent = {
        buttonText?.let {
            Buttons.FilledPrimary(
                text = stringResource(buttonText),
                onClick = onButtonClick,
                isActive = isButtonActive,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        button2Text?.let {
            Buttons.TonalSecondary(
                text = stringResource(button2Text),
                onClick = onButton2Click,
                modifier = Modifier.fillMaxWidth(),
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
}
