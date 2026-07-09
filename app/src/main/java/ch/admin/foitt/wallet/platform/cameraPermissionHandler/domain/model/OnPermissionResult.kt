package ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model

fun interface OnPermissionResult {
    operator fun invoke(
        permissionGranted: Boolean,
        shouldShowRationale: Boolean,
        isActivePrompt: Boolean,
    )
}
