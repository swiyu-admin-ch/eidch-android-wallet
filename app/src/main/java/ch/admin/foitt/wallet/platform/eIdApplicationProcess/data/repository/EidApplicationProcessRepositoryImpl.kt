package ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdUiDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EidApplicationProcessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class EidApplicationProcessRepositoryImpl @Inject constructor() : EidApplicationProcessRepository {
    private val _hasLegalGuardian = MutableStateFlow(false)
    override val hasLegalGuardian = _hasLegalGuardian.asStateFlow()

    override fun setHasLegalGuardian(hasLegalGuardian: Boolean) {
        _hasLegalGuardian.value = hasLegalGuardian
    }

    private val _documentType = MutableStateFlow(EIdUiDocumentType.IDENTITY_CARD)
    override val documentType = _documentType.asStateFlow()

    override fun setDocumentType(eIdUiDocumentType: EIdUiDocumentType) {
        _documentType.value = eIdUiDocumentType
    }
}
