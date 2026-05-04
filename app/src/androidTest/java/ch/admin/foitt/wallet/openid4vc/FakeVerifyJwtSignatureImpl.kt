package ch.admin.foitt.wallet.openid4vc

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignature
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import javax.inject.Inject

class FakeVerifyJwtSignatureImpl @Inject constructor(
) : VerifyJwtSignature {
    override suspend fun invoke(did: String, kid: String, jwt: Jwt): Result<Unit, VerifyJwtError> {
        return Ok(Unit)
    }
}
