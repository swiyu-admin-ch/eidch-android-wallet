package ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestContainer
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchAndSaveCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestResult
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ProcessPresentationRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import ch.admin.foitt.wallet.platform.invitation.domain.model.ValidateInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.toProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ProcessInvitation
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ValidateInvitation
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import timber.log.Timber
import javax.inject.Inject

internal class ProcessInvitationImpl @Inject constructor(
    private val validateInvitation: ValidateInvitation,
    private val fetchAndSaveCredential: FetchAndSaveCredential,
    private val processPresentationRequest: ProcessPresentationRequest,
) : ProcessInvitation {
    override suspend fun invoke(
        invitationUri: String,
    ): Result<ProcessInvitationResult, ProcessInvitationError> =
        validateInvitation(invitationUri)
            .mapError(ValidateInvitationError::toProcessInvitationError)
            .andThen { invitation ->
                Timber.d("Found valid invitation with uri: $invitationUri")
                when (invitation) {
                    is CredentialOffer -> processCredentialOffer(credentialOffer = invitation)
                    is PresentationRequestContainer -> processPresentation(invitation)
                    else -> Err(InvitationError.Unexpected)
                }
            }

    private suspend fun processPresentation(requestContainer: PresentationRequestContainer) =
        processPresentationRequest(requestContainer)
            .map { processPresentationResult ->
                processPresentationResult.toProcessInvitationResult()
            }.mapError(ProcessPresentationRequestError::toProcessInvitationError)

    private suspend fun processCredentialOffer(credentialOffer: CredentialOffer): Result<ProcessInvitationResult, ProcessInvitationError> =
        fetchAndSaveCredential(credentialOffer)
            .map { fetchResult ->
                when (fetchResult) {
                    is FetchCredentialResult.Credential -> ProcessInvitationResult.CredentialOffer(fetchResult.credentialId)
                    is FetchCredentialResult.DeferredCredential -> ProcessInvitationResult.DeferredCredential(fetchResult.credentialId)
                }
            }.mapError(FetchCredentialError::toProcessInvitationError)

    private fun ProcessPresentationRequestResult.toProcessInvitationResult(): ProcessInvitationResult =
        when (this) {
            is ProcessPresentationRequestResult.Credential -> ProcessInvitationResult.PresentationRequest(
                credential = credential,
                request = presentationRequest,
                shouldCheckTrustStatement = shouldFetchTrustStatements,
            )
            is ProcessPresentationRequestResult.CredentialList ->
                ProcessInvitationResult.PresentationRequestCredentialList(
                    credentials = credentials,
                    request = presentationRequest,
                    shouldCheckTrustStatement = shouldFetchTrustStatements,
                )
        }
}
