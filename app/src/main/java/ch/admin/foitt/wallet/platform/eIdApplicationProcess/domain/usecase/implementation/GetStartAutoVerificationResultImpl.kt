package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.di.EidApplicationProcessEntryPoint
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AutoVerificationResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetStartAutoVerificationResult
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetStartAutoVerificationResultImpl @Inject constructor(
    private val destinationScopedComponentManager: DestinationScopedComponentManager,
) : GetStartAutoVerificationResult {
    override operator fun invoke(): StateFlow<AutoVerificationResponse?> {
        val repository = destinationScopedComponentManager.getEntryPoint(
            entryPointClass = EidApplicationProcessEntryPoint::class.java,
            componentScope = ComponentScope.EidOnlineSession,
        ).eidStartAutoVerificationRepository()

        return repository.autoVerificationResponse
    }
}
