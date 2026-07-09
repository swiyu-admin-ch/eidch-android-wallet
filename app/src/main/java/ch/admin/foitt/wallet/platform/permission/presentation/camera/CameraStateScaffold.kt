package ch.admin.foitt.wallet.platform.permission.presentation.camera

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import ch.admin.foitt.wallet.platform.permission.presentation.PermissionScaffold
import ch.admin.foitt.wallet.platform.utils.openAppDetailsSettings

@SuppressLint("InlinedApi")
@Composable
fun CameraStateScaffold(
    modifier: Modifier = Modifier,
    onCameraPermissionChanged: () -> Unit,
    updateContentShown: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        // Update camera state when the screen is first composed, which is earlier than the LifecycleResumeEffect (this prevents flickering)
        onCameraPermissionChanged()
    }

    LifecycleResumeEffect(Unit) {
        // Update camera state whenever the screen is resumed, to react to permission or enabled state changes
        onCameraPermissionChanged()
        onPauseOrDispose {}
    }

    PermissionScaffold(
        modifier = modifier,
        permissions = listOf(
            Manifest.permission.CAMERA,
        ),
        permissionRationaleContent = { onHandled ->
            updateContentShown(false)
            CameraPermissionRationalScreenContent {
                context.openAppDetailsSettings()
                onHandled()
            }
        },
        permissionNotGrantedContent = { handler ->
            updateContentShown(false)
            CameraPermissionNotGrantedScreenContent {
                handler.requestPermission()
            }
        },
        permissionGrantedContent = {
            updateContentShown(true)
            content()
        }
    )
}
