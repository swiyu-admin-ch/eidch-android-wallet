package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.utils.replaceContentDescription
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun DemoBadge() = CredentialBadge(
    text = stringResource(id = R.string.tk_credential_status_demo),
    altText = stringResource(id = R.string.tk_credential_status_demo_alt),
    contentColor = WalletTheme.colorScheme.onLightPrimary,
    backgroundColor = WalletTheme.colorScheme.lightPrimary,
    icon = null,
)

@Composable
internal fun ReadyBadge() = CredentialBadge(
    text = stringResource(id = R.string.tk_credential_status_ready),
    altText = stringResource(id = R.string.tk_credential_status_ready_alt),
    contentColor = WalletTheme.colorScheme.onLightTertiary,
    backgroundColor = WalletTheme.colorScheme.lightTertiary,
    icon = painterResource(R.drawable.wallet_ic_alarm_checkmark),
)

@Composable
private fun CredentialBadge(
    text: String,
    altText: String,
    contentColor: Color,
    backgroundColor: Color,
    icon: Painter?,
) {
    val labelTextHeight = with(LocalDensity.current) {
        WalletTheme.typography.labelMedium.lineHeight.toDp()
    }
    Row(
        modifier = Modifier
            .heightIn(min = Sizes.labelHeight)
            .replaceContentDescription(altText)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(Sizes.s04)
            )
            .padding(start = Sizes.s03, end = Sizes.s03),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Sizes.s01)
    ) {
        icon?.let {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.sizeIn(
                    maxWidth = labelTextHeight,
                    maxHeight = labelTextHeight,
                )
            )
        }
        WalletTexts.LabelMedium(
            text = text,
            color = contentColor,
        )
    }
}

private class CredentialBadgePreviewParams : PreviewParameterProvider<ComposableWrapper<Unit>> {
    override val values = sequenceOf(
        ComposableWrapper { DemoBadge() },
        ComposableWrapper { ReadyBadge() },
    )
}

@Composable
@WalletComponentPreview
private fun CredentialStatusBadgePreview(
    @PreviewParameter(CredentialBadgePreviewParams::class) label: ComposableWrapper<Unit>,
) {
    WalletTheme {
        label.value()
    }
}
