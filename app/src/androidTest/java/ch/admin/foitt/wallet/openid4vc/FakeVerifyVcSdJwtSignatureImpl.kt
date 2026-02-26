package ch.admin.foitt.wallet.openid4vc

import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyVcSdJwtSignatureError
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyVcSdJwtSignature
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.mockk.mockk
import javax.inject.Inject

class FakeVerifyVcSdJwtSignatureImpl @Inject constructor(
) : VerifyVcSdJwtSignature {
    override suspend fun invoke(keyBinding: KeyBinding?, payload: String): Result<VcSdJwtCredential, VerifyVcSdJwtSignatureError> {
        return Ok(mockk<VcSdJwtCredential>())
    }

}
