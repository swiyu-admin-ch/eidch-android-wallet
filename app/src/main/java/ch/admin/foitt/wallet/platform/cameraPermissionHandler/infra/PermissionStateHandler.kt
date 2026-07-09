package ch.admin.foitt.wallet.platform.cameraPermissionHandler.infra

import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.PermissionState
import kotlinx.coroutines.flow.StateFlow

interface PermissionStateHandler {
    val permissionState: StateFlow<PermissionState>

    suspend fun updateState(
        hasPermission: Boolean,
        shouldShowRationale: Boolean,
        isActivePrompt: Boolean,
    )
}
