package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.GetCompatibleCredentialsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestResult
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.toProcessPresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetCompatibleCredentials
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ProcessPresentationRequest
import ch.admin.foitt.wallet.platform.ssi.domain.model.VerifiableCredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class ProcessPresentationRequestImpl @Inject constructor(
    private val getCompatibleCredentials: GetCompatibleCredentials,
    private val verifiableCredentialRepository: VerifiableCredentialRepository,
) : ProcessPresentationRequest {
    override suspend fun invoke(
        presentationRequestWithRaw: PresentationRequestWithRaw,
    ): Result<ProcessPresentationRequestResult, ProcessPresentationRequestError> = coroutineBinding {
        checkIfWalletIsEmpty(presentationRequestWithRaw.authorizationRequest.responseUri).bind()

        val compatibleCredentials = findCompatibleCredentials(presentationRequestWithRaw.authorizationRequest).bind()

        when {
            compatibleCredentials.isEmpty() -> Err(
                CredentialPresentationError.NoCompatibleCredential(presentationRequestWithRaw.authorizationRequest.responseUri)
            ).bind<ProcessPresentationRequestResult>()
            compatibleCredentials.size == 1 -> ProcessPresentationRequestResult.Credential(
                credential = compatibleCredentials.first(),
                presentationRequest = presentationRequestWithRaw,
            )
            else -> ProcessPresentationRequestResult.CredentialList(
                credentials = compatibleCredentials,
                presentationRequest = presentationRequestWithRaw,
            )
        }
    }

    private suspend fun checkIfWalletIsEmpty(uri: String): Result<Unit, ProcessPresentationRequestError> = coroutineBinding {
        val credentials = verifiableCredentialRepository.getAllIds()
            .mapError(VerifiableCredentialRepositoryError::toProcessPresentationRequestError)
            .bind()

        if (credentials.isEmpty()) {
            Err(CredentialPresentationError.EmptyWallet(responseUri = uri)).bind<Unit>()
        }
    }

    private suspend fun findCompatibleCredentials(
        authorizationRequest: AuthorizationRequest
    ): Result<Set<CompatibleCredential>, ProcessPresentationRequestError> =
        getCompatibleCredentials(authorizationRequest)
            .mapError(GetCompatibleCredentialsError::toProcessPresentationRequestError)
}
