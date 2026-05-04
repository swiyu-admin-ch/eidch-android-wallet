package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitAnyCredentialPresentationError
import ch.admin.foitt.openid4vc.domain.usecase.SubmitAnyCredentialPresentation
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.SubmitPresentationError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.toSubmitPresentationError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.SubmitPresentation
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAllAnyCredentialsByCredentialIdError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAllAnyCredentialsByCredentialId
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class SubmitPresentationImpl @Inject constructor(
    private val getAllAnyCredentialsByCredentialId: GetAllAnyCredentialsByCredentialId,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val submitAnyCredentialPresentation: SubmitAnyCredentialPresentation,
) : SubmitPresentation {
    override suspend fun invoke(
        authorizationRequest: AuthorizationRequest,
        compatibleCredential: CompatibleCredential,
    ): Result<Unit, SubmitPresentationError> =
        getAllAnyCredentialsByCredentialId(compatibleCredential.credentialId)
            .mapError(GetAllAnyCredentialsByCredentialIdError::toSubmitPresentationError)
            .andThen { anyCredentials ->
                submitAnyCredentialPresentation(
                    anyCredential = anyCredentials.first(),
                    requestedFields = compatibleCredential.requestedFields.map {
                        if (it.path.size == 1) {
                            it.path.toPointerString().removeSurrounding("[\"", "\"]")
                        } else {
                            it.path.toPointerString()
                        }
                    },
                    authorizationRequest = authorizationRequest,
                    usePayloadEncryption = environmentSetupRepository.payloadEncryptionEnabled,
                    dcqlQueryId = compatibleCredential.dcqlQueryId,
                ).mapError(SubmitAnyCredentialPresentationError::toSubmitPresentationError)
            }
}
