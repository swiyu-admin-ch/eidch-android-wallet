package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.di.EidApplicationProcessEntryPoint
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdUiDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetDocumentType
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetDocumentTypeImpl @Inject constructor(
    private val destinationScopedComponentManager: DestinationScopedComponentManager,
) : GetDocumentType {
    override operator fun invoke(): StateFlow<EIdUiDocumentType> {
        val repository = destinationScopedComponentManager.getEntryPoint(
            entryPointClass = EidApplicationProcessEntryPoint::class.java,
            componentScope = ComponentScope.EidApplicationProcess,
        ).eidApplicationProcessRepository()

        return repository.documentType
    }
}
