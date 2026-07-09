package ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.proximity.ProximityPresentationRequest
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchAndSaveCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProximityEngagementError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.toProcessInvitationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ProcessPresentationRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import ch.admin.foitt.wallet.platform.invitation.domain.model.ValidateInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.toProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ProcessInvitation
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ValidateInvitation
import ch.admin.foitt.wallet.platform.proximity.domain.model.ProximityEngagementEvent
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.ProximityEngagement
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

internal class ProcessInvitationImpl @Inject constructor(
    private val validateInvitation: ValidateInvitation,
    private val fetchAndSaveCredential: FetchAndSaveCredential,
    private val processPresentationRequest: ProcessPresentationRequest,
    private val proximityEngagement: ProximityEngagement,
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
                    is PresentationRequestWithRaw -> processPresentation(invitation)
                    is ProximityPresentationRequest -> processProximityEngagement(invitation)
                    else -> Err(InvitationError.Unexpected)
                }
            }

    private suspend fun processProximityEngagement(
        invitation: ProximityPresentationRequest
    ): Result<ProcessInvitationResult, ProcessInvitationError> {
        return proximityEngagement(invitation.rawQrData).map { result ->
            result.map { event ->
                when (event) {
                    is ProximityEngagementEvent.QrCode -> {
                        Err(InvitationError.Unexpected)
                    }
                    is ProximityEngagementEvent.Request -> {
                        Ok(event.processPresentationRequestResult.toProcessInvitationResult())
                    }
                }
            }.mapError(ProximityEngagementError::toProcessInvitationError)
                .andThen { it }
        }.first()
    }

    private suspend fun processPresentation(presentationRequestWithRaw: PresentationRequestWithRaw) =
        processPresentationRequest(presentationRequestWithRaw)
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
}
