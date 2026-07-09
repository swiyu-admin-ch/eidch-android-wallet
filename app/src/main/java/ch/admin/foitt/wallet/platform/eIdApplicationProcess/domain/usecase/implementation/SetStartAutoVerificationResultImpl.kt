package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.di.EidApplicationProcessEntryPoint
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AutoVerificationResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetStartAutoVerificationResult
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import javax.inject.Inject

class SetStartAutoVerificationResultImpl @Inject constructor(
    private val destinationScopedComponentManager: DestinationScopedComponentManager,
) : SetStartAutoVerificationResult {
    override operator fun invoke(startAutoVerificationResult: AutoVerificationResponse) {
        val repository = destinationScopedComponentManager.getEntryPoint(
            entryPointClass = EidApplicationProcessEntryPoint::class.java,
            componentScope = ComponentScope.EidOnlineSession,
        ).eidStartAutoVerificationRepository()

        repository.setAutoVerificationResponse(startAutoVerificationResult)
    }
}
