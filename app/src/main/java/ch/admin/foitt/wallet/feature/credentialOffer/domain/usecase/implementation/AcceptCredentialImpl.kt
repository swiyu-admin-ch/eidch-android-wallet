package ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.toAcceptCredentialError
import ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.AcceptCredential
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.ssi.domain.model.VerifiableCredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class AcceptCredentialImpl @Inject constructor(
    private val verifiableCredentialRepository: VerifiableCredentialRepository,
) : AcceptCredential {
    override suspend operator fun invoke(credentialId: Long) = coroutineBinding {
        verifiableCredentialRepository.updateProgressionStateByCredentialId(
            credentialId = credentialId,
            progressionState = VerifiableProgressionState.ACCEPTED,
        ).mapError(VerifiableCredentialRepositoryError::toAcceptCredentialError).bind()
    }
}
