package ch.admin.foitt.wallet.platform.cameraPermissionHandler.infra.implementation

import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.PermissionState
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.repository.CameraIntroRepository
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.CheckCameraPermission
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.infra.PermissionStateHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class CameraPermissionStateHandlerImpl @Inject constructor(
    private val checkCameraPermission: CheckCameraPermission,
    private val cameraIntroRepository: CameraIntroRepository,
) : PermissionStateHandler {

    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.Initial)
    override val permissionState = _permissionState.asStateFlow()

    private var autoPromptWasTriggered = false
    private var manualPromptWasTriggered = false
    private var rationaleWasShown = false

    override suspend fun updateState(
        hasPermission: Boolean,
        shouldShowRationale: Boolean,
        isActivePrompt: Boolean,
    ) {
        if (permissionState.value is PermissionState.Intro && hasPermission) {
            cameraIntroRepository.setPermissionPromptWasTriggered(true)
        }

        when (permissionState.value) {
            PermissionState.AutoPrompt -> if (isActivePrompt) autoPromptWasTriggered = true
            PermissionState.ManualPrompt, PermissionState.Intro -> if (isActivePrompt) manualPromptWasTriggered = true
            PermissionState.Rationale -> rationaleWasShown = true
            PermissionState.Blocked -> if (!isActivePrompt) {
                autoPromptWasTriggered = false
                manualPromptWasTriggered = false
                rationaleWasShown = false
            }
            else -> {}
        }

        val cameraIntroPromptTriggered = cameraIntroRepository.getPermissionPromptWasTriggered()

        _permissionState.value = checkCameraPermission(
            permissionsAreGranted = hasPermission,
            rationaleShouldBeShown = shouldShowRationale,
            introPromptWasAccepted = cameraIntroPromptTriggered,
            autoPromptWasTriggered = autoPromptWasTriggered,
            manualPromptWasTriggered = manualPromptWasTriggered,
            rationaleWasShown = rationaleWasShown,
        )
    }
}
