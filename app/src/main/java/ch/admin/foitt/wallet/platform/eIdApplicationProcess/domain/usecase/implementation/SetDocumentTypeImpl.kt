package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.di.EidApplicationProcessEntryPoint
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdUiDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetDocumentType
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import javax.inject.Inject

class SetDocumentTypeImpl @Inject constructor(
    private val destinationScopedComponentManager: DestinationScopedComponentManager,
) : SetDocumentType {
    override operator fun invoke(eIdDocumentType: EIdUiDocumentType) {
        val repository = destinationScopedComponentManager.getEntryPoint(
            entryPointClass = EidApplicationProcessEntryPoint::class.java,
            componentScope = ComponentScope.EidApplicationProcess,
        ).eidApplicationProcessRepository()

        repository.setDocumentType(eIdDocumentType)
    }
}
