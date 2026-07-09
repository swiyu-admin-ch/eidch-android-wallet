package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdUiDocumentType
import kotlinx.coroutines.flow.StateFlow

interface EidApplicationProcessRepository {
    val hasLegalGuardian: StateFlow<Boolean>
    fun setHasLegalGuardian(hasLegalGuardian: Boolean)

    val documentType: StateFlow<EIdUiDocumentType>
    fun setDocumentType(eIdUiDocumentType: EIdUiDocumentType)
}
