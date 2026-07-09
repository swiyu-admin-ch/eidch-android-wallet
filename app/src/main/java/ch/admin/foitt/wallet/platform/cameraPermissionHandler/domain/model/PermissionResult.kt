package ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model

data class PermissionResult(
    val permissionsAreGranted: Boolean,
    val rationaleShouldBeShown: Boolean,
    val introPromptWasAccepted: Boolean,
    val autoPromptWasTriggered: Boolean,
    val manualPromptWasTriggered: Boolean,
    val rationaleWasShown: Boolean,
)
