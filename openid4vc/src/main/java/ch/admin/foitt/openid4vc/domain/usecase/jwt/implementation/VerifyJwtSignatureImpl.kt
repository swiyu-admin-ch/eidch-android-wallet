package ch.admin.foitt.openid4vc.domain.usecase.jwt.implementation

import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureError
import ch.admin.foitt.openid4vc.domain.model.jwt.toVerifyJwtSignatureError
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignature
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.util.Base64URL
import javax.inject.Inject

class VerifyJwtSignatureImpl @Inject constructor() : VerifyJwtSignature {
    override fun invoke(jwt: Jwt, publicKey: Jwk): Result<Unit, VerifyJwtSignatureError> = binding {
        val valid = runSuspendCatching {
            val key = ECKey.Builder(
                Curve(publicKey.crv),
                Base64URL(publicKey.x),
                Base64URL(publicKey.y)
            ).build()
            val verifier = ECDSAVerifier(key)
            jwt.signedJwt.verify(verifier)
        }.mapError { throwable ->
            throwable.toVerifyJwtSignatureError("jwt signature validation failed")
        }.bind()

        if (!valid) {
            return@binding Err(JwtError.InvalidJwt).bind<Unit>()
        }
    }
}
