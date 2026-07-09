package ch.admin.foitt.wallet.platform.permission.presentation.camera

import androidx.compose.runtime.Composable
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.permission.presentation.PermissionScreenContent
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun CameraPermissionNotGrantedScreenContent(
    onAllow: () -> Unit,
) = PermissionScreenContent(
    icon = R.drawable.wallet_ic_camera_colored,
    primaryButton = R.string.tk_global_continue_button,
    title = R.string.tk_receive_cameraaccessneeded1_title,
    message = R.string.tk_receive_cameraaccessneeded1_body,
    onAllow = onAllow,
)

@WalletComponentPreview
@Composable
private fun CameraPermissionNotGrantedScreenContentPreview() {
    WalletTheme {
        CameraPermissionNotGrantedScreenContent(
            onAllow = {},
        )
    }
}
