package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
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
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainAnimation
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.DocumentScannerErrorType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdUiDocumentType
import ch.admin.foitt.wallet.platform.preview.WalletDefaultPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun DocumentScannerErrorContent(
    type: DocumentScannerErrorType,
    @StringRes title: Int,
    @StringRes content: Int,
    @StringRes buttonText: Int,
    onButton: () -> Unit,
    onHelp: (() -> Unit)? = null,
) {
    DocumentScannerContent(
        icon = R.drawable.wallet_ic_cross_circle_colored,
        primaryText = title,
        secondaryText = content,
        buttonText = buttonText,
        onButtonClick = onButton,
        onHelp = if (type == DocumentScannerErrorType.Generic) onHelp else null,
        helpButtonText = R.string.tk_error_generic_helpLink_label,
    )
}

@Composable
internal fun DocumentScannerBacksideInfoContent(
    documentType: EIdUiDocumentType,
    onButtonClick: () -> Unit,
) {
    when (documentType) {
        EIdUiDocumentType.IDENTITY_CARD, EIdUiDocumentType.RESIDENT_PERMIT -> DocumentScannerContent(
            animation = R.raw.doc_scan_back,
            icon = R.drawable.wallet_ic_scan_number_block,
            primaryText = R.string.tk_eidRequest_scanDocument_secondPage_idCard_primary,
            secondaryText = R.string.tk_eidRequest_scanDocument_secondPage_idCard_secondary,
            buttonText = R.string.tk_global_continue,
            modifier = Modifier.background(WalletTheme.colorScheme.background),
            onButtonClick = onButtonClick,
        )
        EIdUiDocumentType.PASSPORT -> DocumentScannerContent(
            animation = R.raw.doc_scan_pass_2,
            icon = R.drawable.wallet_ic_scan_second_side,
            primaryText = R.string.tk_eidRequest_scanDocument_secondPage_passport_primary,
            secondaryText = R.string.tk_eidRequest_scanDocument_secondPage_passport_secondary,
            buttonText = R.string.tk_global_continue,
            modifier = Modifier.background(WalletTheme.colorScheme.background),
            onButtonClick = onButtonClick,
        )
    }
}

@Composable
private fun DocumentScannerContent(
    @DrawableRes icon: Int,
    @StringRes primaryText: Int,
    @StringRes secondaryText: Int?,
    @StringRes buttonText: Int?,
    modifier: Modifier = Modifier,
    @RawRes animation: Int? = null,
    onButtonClick: () -> Unit = {},
    onHelp: (() -> Unit)? = null,
    @StringRes helpButtonText: Int? = null,
) = WalletLayouts.ScrollableColumnWithPicture(
    modifier = modifier,
    stickyStartContent = {
        animation?.let {
            ScreenMainAnimation(
                animationRes = animation,
                fallbackImage = icon,
            )
        } ?: ScreenMainImage(iconRes = icon)
    },
    stickyBottomContent = {
        buttonText?.let {
            AdaptiveBottomButtonBar(
                buttons = listOf(
                    {
                        Buttons.FilledPrimary(
                            text = stringResource(buttonText),
                            onClick = onButtonClick,
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
    if (onHelp != null && helpButtonText != null) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        Buttons.TextLink(
            text = stringResource(id = helpButtonText),
            onClick = onHelp,
            endIcon = painterResource(id = R.drawable.wallet_ic_external_link),
        )
    }
}

@WalletDefaultPreview
@Composable
private fun DocumentScannerErrorContentPreview() {
    WalletTheme {
        DocumentScannerErrorContent(
            type = DocumentScannerErrorType.Generic,
            title = R.string.tk_error_generic_primary,
            content = R.string.tk_error_generic_secondary,
            buttonText = R.string.tk_error_generic_button_primary,
            onButton = {},
            onHelp = {},
        )
    }
}

@WalletDefaultPreview
@Composable
private fun DocumentScannerBacksideInfoContentPreview() {
    WalletTheme {
        DocumentScannerBacksideInfoContent(
            documentType = EIdUiDocumentType.IDENTITY_CARD,
            onButtonClick = {},
        )
    }
}
