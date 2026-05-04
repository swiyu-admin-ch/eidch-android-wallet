package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
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
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.DocumentScannerErrorType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdDocumentType
import ch.admin.foitt.wallet.platform.preview.WalletDefaultPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun DocumentScannerErrorContent(
    type: DocumentScannerErrorType,
    onRetry: () -> Unit,
    onHelp: (() -> Unit)? = null,
) {
    val (title, body, buttonText) = when (type) {
        DocumentScannerErrorType.GENERIC -> Triple(
            R.string.tk_error_generic_primary,
            R.string.tk_error_generic_secondary,
            R.string.tk_error_generic_button_primary
        )

        DocumentScannerErrorType.UNEQUAL_DOCUMENTS -> Triple(
            R.string.tk_eidRequest_documentScan_wrongDocument_primary,
            R.string.tk_eidRequest_documentScan_wrongDocument_secondary,
            R.string.tk_eidRequest_documentScan_wrongDocument_button
        )
    }

    DocumentScannerContent(
        icon = R.drawable.wallet_ic_cross_circle_colored,
        primaryText = title,
        secondaryText = body,
        buttonText = buttonText,
        onButtonClick = onRetry,
        onHelp = if (type == DocumentScannerErrorType.GENERIC) onHelp else null,
        helpButtonText = R.string.tk_error_generic_helpLink_label,
    )
}

@Composable
internal fun DocumentScannerBacksideInfoContent(
    documentType: EIdDocumentType,
    onButtonClick: () -> Unit,
) {
    val (primaryText, secondaryText) = when (documentType) {
        EIdDocumentType.IDENTITY_CARD, EIdDocumentType.RESIDENT_PERMIT -> Pair(
            R.string.tk_eidRequest_scanDocument_secondPage_idCard_primary,
            R.string.tk_eidRequest_scanDocument_secondPage_idCard_secondary,
        )

        EIdDocumentType.PASSPORT -> Pair(
            R.string.tk_eidRequest_scanDocument_secondPage_passport_primary,
            R.string.tk_eidRequest_scanDocument_secondPage_passport_secondary,
        )
    }

    DocumentScannerContent(
        icon = R.drawable.wallet_ic_scan_number_block,
        primaryText = primaryText,
        secondaryText = secondaryText,
        buttonText = R.string.tk_global_continue,
        modifier = Modifier.background(WalletTheme.colorScheme.background),
        onButtonClick = onButtonClick,
    )
}

@Composable
private fun DocumentScannerContent(
    @DrawableRes icon: Int,
    @StringRes primaryText: Int,
    @StringRes secondaryText: Int?,
    @StringRes buttonText: Int?,
    modifier: Modifier = Modifier,
    onButtonClick: () -> Unit = {},
    onHelp: (() -> Unit)? = null,
    @StringRes helpButtonText: Int? = null,
) = WalletLayouts.ScrollableColumnWithPicture(
    modifier = modifier,
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
            type = DocumentScannerErrorType.GENERIC,
            onRetry = {},
            onHelp = {},
        )
    }
}

@WalletDefaultPreview
@Composable
private fun DocumentScannerBacksideInfoContentPreview() {
    WalletTheme {
        DocumentScannerBacksideInfoContent(
            documentType = EIdDocumentType.IDENTITY_CARD,
            onButtonClick = {},
        )
    }
}
