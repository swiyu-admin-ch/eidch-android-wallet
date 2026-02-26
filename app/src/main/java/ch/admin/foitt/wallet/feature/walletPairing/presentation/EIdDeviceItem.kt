package ch.admin.foitt.wallet.feature.walletPairing.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.walletPairing.presentation.model.PairingMainWalletUiState
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun EIdDeviceItem(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    dateAddedText: String? = null,
    mainWalletUiState: PairingMainWalletUiState = PairingMainWalletUiState.Initial,
) {
    val isClickable = onClick != null
    Row(
        modifier = modifier
            .then(
                if (onClick != null && mainWalletUiState == PairingMainWalletUiState.Initial) {
                    Modifier
                        .clickable(onClick = onClick)
                        .spaceBarKeyClickable(onClick)
                } else {
                    Modifier
                }
            )
            .padding(
                start = Sizes.s04,
                top = if (isClickable) Sizes.s03 else Sizes.s05,
                end = Sizes.s06,
                bottom = if (isClickable) Sizes.s03 else Sizes.s05
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (mainWalletUiState) {
            PairingMainWalletUiState.Initial -> {
                Icon(
                    painter = painterResource(id = R.drawable.wallet_ic_paring_device_add),
                    contentDescription = null,
                    tint = WalletTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.width(Sizes.s04))

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
                    if (subtitle != null) {
                        WalletTexts.BodyLarge(
                            text = subtitle,
                            color = WalletTheme.colorScheme.secondary,
                        )
                    }
                }
            }

            PairingMainWalletUiState.SyncMainWallet -> {
                CircularProgressIndicator(
                    color = WalletTheme.colorScheme.primary,
                    trackColor = WalletTheme.colorScheme.secondaryContainer,
                    strokeWidth = Sizes.s01,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .size(Sizes.s05)
                        .clearAndSetSemantics {}
                )
                Spacer(modifier = Modifier.width(Sizes.s04))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    WalletTexts.BodyLarge(
                        text = stringResource(R.string.tk_eidRequest_walletPairing_currentDevice_loadingTitle),
                        color = WalletTheme.colorScheme.onSurface
                    )
                }
            }

            PairingMainWalletUiState.MainWalletAdded -> {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    WalletTexts.BodyLarge(
                        text = stringResource(R.string.tk_eidRequest_walletPairing_currentDevice_sectionTitle_sync),
                        color = WalletTheme.colorScheme.onSurface
                    )
                    if (subtitle != null) {
                        WalletTexts.BodyLarge(
                            text = subtitle,
                            color = WalletTheme.colorScheme.secondary,
                        )
                    }
                }
                if (dateAddedText != null) {
                    Spacer(modifier = Modifier.width(Sizes.s04))
                    WalletTexts.BodySmall(
                        text = dateAddedText,
                        color = WalletTheme.colorScheme.onSurface,
                    )
                }
                Icon(
                    modifier = Modifier.padding(start = Sizes.s02),
                    painter = painterResource(id = R.drawable.wallet_ic_paring_device_check),
                    contentDescription = null,
                    tint = WalletTheme.colorScheme.tertiary,
                )
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = Sizes.s04)
    )
}

@WalletComponentPreview
@Composable
private fun EIdDeviceItemPreview() {
    WalletTheme {
        EIdDeviceItem(
            title = "Add this device",
            subtitle = "Android Pixel 10",
            dateAddedText = "22.07.2025 15:36",
            onClick = null,
            mainWalletUiState = PairingMainWalletUiState.MainWalletAdded
        )
    }
}
