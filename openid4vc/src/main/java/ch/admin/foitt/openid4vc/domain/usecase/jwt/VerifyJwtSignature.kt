package ch.admin.foitt.openid4vc.domain.usecase.jwt

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureError
import com.github.michaelbull.result.Result

interface VerifyJwtSignature {
    @CheckResult
    operator fun invoke(jwt: Jwt, publicKey: Jwk): Result<Unit, VerifyJwtSignatureError>
}
