package ch.admin.foitt.wallet.platform.permission.domain.model

import androidx.compose.runtime.Immutable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus

@ExperimentalPermissionsApi
@Immutable
class NoOpAccompanistPermissionState(
    override val permission: String,
    override val status: PermissionStatus,
    val onPermissionResult: (Boolean) -> Unit = {},
    val overridePermissionRequestResult: () -> Boolean = {
        status == PermissionStatus.Granted
    }
) : PermissionState {
    override fun launchPermissionRequest() {
        val permissionResult = overridePermissionRequestResult()
        onPermissionResult(permissionResult)
    }
}
