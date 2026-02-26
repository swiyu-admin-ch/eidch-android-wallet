package ch.admin.foitt.wallet.feature.walletPairing.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun EIdOtherDeviceItem(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    title: String,
) {
    val isClickable = onClick != null
    Row(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .clickable(onClick = onClick)
                        .spaceBarKeyClickable(onClick)
                } else {
                    Modifier
                }
            )
            .padding(
                start = Sizes.s04,
                top = Sizes.s03,
                end = Sizes.s06,
                bottom = if (isClickable) Sizes.s03 else Sizes.s06
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isClickable) {
            Icon(
                painter = painterResource(id = R.drawable.wallet_ic_paring_device_add),
                contentDescription = null,
                tint = WalletTheme.colorScheme.tertiary,
            )
            Spacer(modifier = Modifier.width(Sizes.s04))
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            WalletTexts.BodyLarge(
                text = title,
                color = if (isClickable) {
                    WalletTheme.colorScheme.tertiary
                } else {
                    WalletTheme.colorScheme.onSurface
                },
            )
        }
        if (!isClickable) {
            Icon(
                modifier = Modifier.padding(start = Sizes.s02),
                painter = painterResource(id = R.drawable.wallet_ic_paring_device_check),
                contentDescription = null,
                tint = WalletTheme.colorScheme.tertiary,
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = Sizes.s04)
    )
}

@WalletComponentPreview
@Composable
private fun EIdOtherDeviceItemPreview() {
    WalletTheme {
        EIdOtherDeviceItem(
            title = "Add another device",
            onClick = null,
        )
    }
}
