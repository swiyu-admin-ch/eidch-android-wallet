package ch.admin.foitt.wallet.platform.proximity.data.repository

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.swiyu.shared.proximity.ProximityPresentationController
import ch.admin.foitt.swiyu.shared.proximity.ProximityState
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProximitySubmissionError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.toProximitySubmissionError
import ch.admin.foitt.wallet.platform.proximity.domain.model.ProximityEngagementUpdate
import ch.admin.foitt.wallet.platform.proximity.domain.model.domain.repository.ProximityRepository
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProximityRepositoryImpl @Inject constructor(
    private val controller: ProximityPresentationController,
    private val safeJson: SafeJson,
) : ProximityRepository {

    override var isPresentationStarted = false

    override val state = controller.state

    override fun startEngagement(): Flow<Result<ProximityEngagementUpdate, ProximitySubmissionError>> = start {
        controller.startEngagement()
    }

    override fun startEngagementReverse(qrCode: String): Flow<Result<ProximityEngagementUpdate, ProximitySubmissionError>> = start {
        controller.startEngagementReverse(readerEngagement = qrCode)
    }

    override fun start(
        starter: () -> Unit
    ): Flow<Result<ProximityEngagementUpdate, ProximitySubmissionError>> = flow {
        isPresentationStarted = true
        starter()
        streamEngagementStates()
    }

    private suspend fun FlowCollector<Result<ProximityEngagementUpdate, ProximitySubmissionError>>.streamEngagementStates() {
        controller.state.collect { state ->
            when (state) {
                is ProximityState.Error -> emit(Err(ProximitySubmissionError.Failed(state.error.message)))
                is ProximityState.ReadyForEngagement -> emit(Ok(ProximityEngagementUpdate.QrCode(state.qrCodeData)))
                is ProximityState.RequestingDocuments -> emit(Ok(ProximityEngagementUpdate.Request(state.raw)))
                else -> {}
            }
        }
    }

    override suspend fun submit(
        authorizationResponseConfig: AuthorizationResponseConfig
    ): Result<Unit, ProximitySubmissionError> = coroutineBinding {
        val objectToEncode = authorizationResponseConfig.params.map {
            it.key.jsonName to safeJson.safeDecodeStringTo<Map<String, List<String>>>(it.value)
                .mapError(JsonParsingError::toProximitySubmissionError)
                .bind()
        }.toMap()
        safeJson.safeEncodeObjectToString(objectToEncode)
            .mapError(JsonParsingError::toProximitySubmissionError)
            .andThen { jsonString ->
                val data = jsonString.encodeToByteArray()

                controller.submitDocument(data = data)

                controller.state.first { state ->
                    state is ProximityState.PresentationCompleted ||
                        state is ProximityState.Error
                }.let { state ->
                    when (state) {
                        is ProximityState.PresentationCompleted -> {
                            Ok(Unit)
                        }

                        is ProximityState.Error -> Err(ProximitySubmissionError.Failed(state.error.message))
                        else -> Err(ProximitySubmissionError.Failed("Unexpected state: $state"))
                    }
                }
            }.bind()
    }

    override fun dispose() {
        controller.dispose()
        isPresentationStarted = false
    }

    override fun reset() {
        controller.reset()
        isPresentationStarted = false
    }

    override fun decline() {
        controller.decline()
        isPresentationStarted = false
    }
}
