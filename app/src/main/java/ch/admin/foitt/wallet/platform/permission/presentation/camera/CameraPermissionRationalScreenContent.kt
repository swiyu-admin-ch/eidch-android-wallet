package ch.admin.foitt.wallet.platform.permission.presentation.camera

import androidx.compose.runtime.Composable
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.permission.presentation.PermissionScreenContent
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun CameraPermissionRationalScreenContent(
    onOpenSettings: () -> Unit,
) = PermissionScreenContent(
    icon = R.drawable.wallet_ic_camera_colored,
    primaryButton = R.string.tk_global_tothesettings,
    title = R.string.tk_receive_cameraaccessneeded3_title,
    message = R.string.tk_receive_cameraaccessneeded3_body,
    onAllow = onOpenSettings,
)

@WalletComponentPreview
@Composable
private fun CameraPermissionRationalScreenContentPreview() {
    WalletTheme {
        CameraPermissionRationalScreenContent(
            onOpenSettings = {},
        )
    }
}
