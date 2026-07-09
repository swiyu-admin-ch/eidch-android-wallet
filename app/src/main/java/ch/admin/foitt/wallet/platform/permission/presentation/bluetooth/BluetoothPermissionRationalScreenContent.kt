package ch.admin.foitt.wallet.platform.permission.presentation.bluetooth

import androidx.compose.runtime.Composable
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.permission.presentation.PermissionScreenContent
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun BluetoothPermissionRationalScreenContent(
    onOpenSettings: () -> Unit,
) = PermissionScreenContent(
    icon = R.drawable.wallet_ic_bluetooth,
    primaryButton = R.string.tk_global_tothesettings,
    title = R.string.verification_bluetooth_permission_title,
    message = R.string.verification_bluetooth_permission_description,
    onAllow = onOpenSettings,
)

@WalletComponentPreview
@Composable
private fun BluetoothPermissionRationalScreenContentPreview() {
    WalletTheme {
        BluetoothPermissionRationalScreenContent(
            onOpenSettings = {},
        )
    }
}
