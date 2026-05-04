package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObject
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyRequestObjectSignatureError
import com.github.michaelbull.result.Result

interface VerifyRequestObjectSignature {
    suspend operator fun invoke(
        requestObject: RequestObject,
        trustedAttestationDids: List<String>,
    ): Result<Unit, VerifyRequestObjectSignatureError>
}
