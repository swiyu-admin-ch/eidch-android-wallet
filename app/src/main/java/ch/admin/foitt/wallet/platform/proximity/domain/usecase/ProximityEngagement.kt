package ch.admin.foitt.wallet.platform.proximity.domain.usecase

import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProximityEngagementError
import ch.admin.foitt.wallet.platform.proximity.domain.model.ProximityEngagementEvent
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface ProximityEngagement {
    suspend operator fun invoke(qrCode: String? = null): Flow<Result<ProximityEngagementEvent, ProximityEngagementError>>
}
