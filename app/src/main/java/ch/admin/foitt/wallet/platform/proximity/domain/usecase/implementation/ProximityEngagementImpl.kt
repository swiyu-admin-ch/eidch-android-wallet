package ch.admin.foitt.wallet.platform.proximity.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObject
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProximityEngagementError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProximitySubmissionError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ValidatePresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.VerificationProcessType
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.toProximityEngagementError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ProcessPresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.platform.proximity.domain.model.ProximityEngagementEvent
import ch.admin.foitt.wallet.platform.proximity.domain.model.ProximityEngagementUpdate
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.GetProximityRepositoryForScope
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.ProximityEngagement
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProximityEngagementImpl @Inject constructor(
    private val validatePresentationRequest: ValidatePresentationRequest,
    private val processPresentationRequest: ProcessPresentationRequest,
    private val getProximityRepositoryForScope: GetProximityRepositoryForScope,
) : ProximityEngagement {

    override suspend fun invoke(qrCode: String?): Flow<Result<ProximityEngagementEvent, ProximityEngagementError>> {
        return if (qrCode != null) {
            getProximityRepositoryForScope().startEngagementReverse(qrCode)
        } else {
            getProximityRepositoryForScope().startEngagement()
        }.map { event ->
            coroutineBinding {
                val updateEvent = event.mapError(ProximitySubmissionError::toProximityEngagementError).bind()
                mapToEngagementEvent(updateEvent).bind()
            }
        }
    }

    private suspend fun mapToEngagementEvent(
        update: ProximityEngagementUpdate
    ): Result<ProximityEngagementEvent, ProximityEngagementError> = coroutineBinding {
        when (update) {
            is ProximityEngagementUpdate.QrCode -> {
                ProximityEngagementEvent.QrCode(update.qrCode)
            }

            is ProximityEngagementUpdate.Request -> {
                val requestObject = validatePresentationRequest(
                    verificationProcessType = VerificationProcessType.PROXIMITY,
                    requestObject = RequestObject(Jwt(update.request), null, null)
                ).mapError(ValidatePresentationRequestError::toProximityEngagementError)
                    .bind()
                val presentationRequest = processPresentationRequest(requestObject)
                    .mapError(ProcessPresentationRequestError::toProximityEngagementError)
                    .bind()
                ProximityEngagementEvent.Request(presentationRequest)
            }
        }
    }
}
