package ch.admin.foitt.wallet.platform.proximity.domain.model.domain.repository

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.swiyu.shared.proximity.ProximityState
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProximitySubmissionError
import ch.admin.foitt.wallet.platform.proximity.domain.model.ProximityEngagementUpdate
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ProximityRepository {
    var isPresentationStarted: Boolean
    val state: StateFlow<ProximityState>
    fun startEngagement(): Flow<Result<ProximityEngagementUpdate, ProximitySubmissionError>>
    fun startEngagementReverse(qrCode: String): Flow<Result<ProximityEngagementUpdate, ProximitySubmissionError>>
    suspend fun submit(authorizationResponseConfig: AuthorizationResponseConfig): Result<Unit, ProximitySubmissionError>
    fun dispose()
    fun reset()
    fun decline()
    fun start(starter: () -> Unit): Flow<Result<ProximityEngagementUpdate, ProximitySubmissionError>>
}
