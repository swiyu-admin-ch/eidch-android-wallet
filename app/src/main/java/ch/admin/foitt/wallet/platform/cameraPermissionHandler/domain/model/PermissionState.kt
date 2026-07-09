package ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model

sealed interface PermissionState {
    data object Initial : PermissionState
    data object Granted : PermissionState
    data object Blocked : PermissionState
    data object Intro : PermissionState
    data object Rationale : PermissionState
    data object AutoPrompt : PermissionState
    data object ManualPrompt : PermissionState
}
