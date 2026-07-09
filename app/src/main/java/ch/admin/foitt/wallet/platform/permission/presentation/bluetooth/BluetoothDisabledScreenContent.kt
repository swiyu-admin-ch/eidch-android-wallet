package ch.admin.foitt.wallet.platform.permission.presentation.bluetooth

import androidx.compose.runtime.Composable
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.permission.presentation.PermissionScreenContent
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun BluetoothDisabledScreenContent(
    onEnableBluetooth: () -> Unit,
) = PermissionScreenContent(
    icon = R.drawable.wallet_ic_bluetooth,
    primaryButton = R.string.verification_bluetooth_enable,
    title = R.string.verification_bluetooth_permission_title,
    message = R.string.verification_bluetooth_permission_description,
    onAllow = onEnableBluetooth,
)

@WalletComponentPreview
@Composable
private fun BluetoothDisabledScreenContentPreview() {
    WalletTheme {
        BluetoothDisabledScreenContent(
            onEnableBluetooth = {},
        )
    }
}
