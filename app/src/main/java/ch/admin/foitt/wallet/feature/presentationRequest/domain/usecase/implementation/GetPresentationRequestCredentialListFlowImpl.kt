package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.GetPresentationRequestCredentialListFlowError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationCredentialDisplayData
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.toGetPresentationRequestCredentialListFlowError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestCredentialListFlow
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialsWithDetailsFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialsWithDetailsFlow
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPresentationRequestCredentialListFlowImpl @Inject constructor(
    private val getCredentialsWithDetailsFlow: GetCredentialsWithDetailsFlow,
) : GetPresentationRequestCredentialListFlow {
    override fun invoke(
        compatibleCredentials: Set<CompatibleCredential>,
    ): Flow<Result<PresentationCredentialDisplayData, GetPresentationRequestCredentialListFlowError>> =
        getCredentialsWithDetailsFlow()
            .mapError(GetCredentialsWithDetailsFlowError::toGetPresentationRequestCredentialListFlowError)
            .andThen { credentialsWithDisplays ->
                coroutineBinding {
                    val compatibleCredentialIds = compatibleCredentials.map { it.credentialId }
                    val credentialList = credentialsWithDisplays.filter { it.credentialId in compatibleCredentialIds }
                    PresentationCredentialDisplayData(
                        credentials = credentialList
                    )
                }
            }
}
