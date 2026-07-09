package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
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

internal class VerifyVcSdJwtSignatureImpl @Inject constructor(
    private val verifyJwtSignatureFromDid: VerifyJwtSignatureFromDid,
) : VerifyVcSdJwtSignature {
    override suspend operator fun invoke(
        keyBinding: KeyBinding?,
        payload: String,
        format: CredentialFormat,
    ): Result<VcSdJwtCredential, VerifyVcSdJwtSignatureError> = coroutineBinding {
        runSuspendCatching {
            val credential = VcSdJwtCredential(
                keyBinding = keyBinding,
                payload = payload,
                format = format,
            )
            val keyId = credential.kid

            verifyJwtSignatureFromDid(
                kid = keyId,
                jwt = credential,
            ).mapError(VerifyJwtSignatureFromDidError::toVerifyVcSdJwtSignatureError).bind()

            credential
        }.mapError { throwable ->
            throwable.toVerifyVcSdJwtSignatureError()
        }.bind()
    }
}
