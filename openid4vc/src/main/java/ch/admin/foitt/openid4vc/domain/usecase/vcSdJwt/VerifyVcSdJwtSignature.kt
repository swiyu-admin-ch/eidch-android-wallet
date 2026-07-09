package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyVcSdJwtSignatureError
import com.github.michaelbull.result.Result

interface VerifyVcSdJwtSignature {
    suspend operator fun invoke(
        keyBinding: KeyBinding?,
        payload: String,
        format: CredentialFormat,
    ): Result<VcSdJwtCredential, VerifyVcSdJwtSignatureError>
}
