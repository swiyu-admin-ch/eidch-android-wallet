package ch.admin.foitt.openid4vc.domain.usecase.jwt

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureFromDidError
import com.github.michaelbull.result.Result

interface VerifyJwtSignatureFromDid {
    suspend operator fun invoke(did: String, kid: String, jwt: Jwt): Result<Unit, VerifyJwtSignatureFromDidError>
}
