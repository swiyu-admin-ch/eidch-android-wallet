package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase

import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.GetPresentationRequestCredentialListFlowError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationCredentialDisplayData
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface GetPresentationRequestCredentialListFlow {
    operator fun invoke(
        compatibleCredentials: Set<CompatibleCredential>,
    ): Flow<Result<PresentationCredentialDisplayData, GetPresentationRequestCredentialListFlowError>>
}
