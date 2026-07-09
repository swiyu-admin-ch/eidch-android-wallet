package ch.admin.foitt.wallet.platform.cameraPermissionHandler.mock

import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.repository.CameraIntroRepository

class InMemoryCameraIntroRepository : CameraIntroRepository {
    var value = false
    override suspend fun getPermissionPromptWasTriggered() = value

    override suspend fun setPermissionPromptWasTriggered(introWasPassed: Boolean) {
        value = introWasPassed
    }
}
