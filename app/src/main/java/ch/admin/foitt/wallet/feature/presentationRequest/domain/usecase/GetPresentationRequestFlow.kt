package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.GetPresentationRequestFlowError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestDisplayData
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface GetPresentationRequestFlow {
    operator fun invoke(
        id: Long,
        presentationPaths: List<ClaimsPathPointer>,
    ): Flow<Result<PresentationRequestDisplayData, GetPresentationRequestFlowError>>
}
