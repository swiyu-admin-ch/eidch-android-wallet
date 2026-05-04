package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureFromDidError
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyVcSdJwtSignatureError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.toVerifyVcSdJwtSignatureError
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyVcSdJwtSignature
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class VerifyVcSdJwtSignatureImpl @Inject constructor(
    private val verifyJwtSignatureFromDid: VerifyJwtSignatureFromDid,
) : VerifyVcSdJwtSignature {
    override suspend operator fun invoke(
        keyBinding: KeyBinding?,
        payload: String,
    ): Result<VcSdJwtCredential, VerifyVcSdJwtSignatureError> = coroutineBinding {
        val credential = runSuspendCatching {
            VcSdJwtCredential(
                keyBinding = keyBinding,
                payload = payload,
            )
        }.mapError { throwable ->
            throwable.toVerifyVcSdJwtSignatureError()
        }.bind()

        val issuerDid = runSuspendCatching {
            credential.issuer
        }.mapError { throwable ->
            throwable.toVerifyVcSdJwtSignatureError()
        }.bind()

        val keyId = runSuspendCatching {
            credential.kid
        }.mapError { throwable ->
            throwable.toVerifyVcSdJwtSignatureError()
        }.bind()

        verifyJwtSignatureFromDid(
            did = issuerDid,
            kid = keyId,
            jwt = credential,
        ).mapError(VerifyJwtSignatureFromDidError::toVerifyVcSdJwtSignatureError).bind()

        credential
    }
}
