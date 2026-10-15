package ch.admin.foitt.openid4vc.domain.usecase.jwt

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureError
import com.github.michaelbull.result.Result
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.util.Base64URL

fun interface VerifyJwtSignature {
    @CheckResult
    operator fun invoke(jwt: Jwt, publicKey: Jwk): Result<Unit, VerifyJwtSignatureError> = invoke(
        header = jwt.signedJwt.header,
        signingInput = jwt.signedJwt.signingInput,
        signature = jwt.signedJwt.signature,
        publicKey = publicKey,
    )

    @CheckResult
    operator fun invoke(
        header: JWSHeader,
        signingInput: ByteArray,
        signature: Base64URL,
        publicKey: Jwk
    ): Result<Unit, VerifyJwtSignatureError>
}
