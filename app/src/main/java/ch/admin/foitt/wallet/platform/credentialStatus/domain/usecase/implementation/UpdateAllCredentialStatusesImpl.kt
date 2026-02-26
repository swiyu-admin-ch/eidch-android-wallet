package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateAllCredentialStatuses
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateCredentialStatus
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import timber.log.Timber
import javax.inject.Inject

class UpdateAllCredentialStatusesImpl @Inject constructor(
    private val verifiableCredentialRepository: VerifiableCredentialRepository,
    private val updateCredentialStatus: UpdateCredentialStatus,
) : UpdateAllCredentialStatuses {
    override suspend fun invoke() {
        verifiableCredentialRepository.getAllIds()
            .onSuccess { credentialIds ->
                credentialIds.forEach { id ->
                    updateCredentialStatus(id).onFailure { error ->
                        val exception = (error as? CredentialStatusError.Unexpected)?.cause
                        Timber.e(exception, "Could not update credential status for credential")
                    }
                }
            }.onFailure { error ->
                val exception = (error as? SsiError.Unexpected)?.cause
                Timber.e(exception, "Could not get credentials for credential status update")
            } // silently fail
    }
}
