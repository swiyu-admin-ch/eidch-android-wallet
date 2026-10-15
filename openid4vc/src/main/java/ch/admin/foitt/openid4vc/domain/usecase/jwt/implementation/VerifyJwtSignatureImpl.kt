package ch.admin.foitt.openid4vc.domain.usecase.jwt.implementation

import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureError
import ch.admin.foitt.openid4vc.domain.model.jwt.toVerifyJwtSignatureError
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignature
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.Ed25519Verifier
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.KeyType
import com.nimbusds.jose.jwk.OctetKeyPair
import com.nimbusds.jose.util.Base64URL
import javax.inject.Inject

internal class VerifyJwtSignatureImpl @Inject constructor() : VerifyJwtSignature {
    override fun invoke(
        header: JWSHeader,
        signingInput: ByteArray,
        signature: Base64URL,
        publicKey: Jwk
    ): Result<Unit, VerifyJwtSignatureError> = binding {
        val valid = runSuspendCatching {
            val verifier = createVerifier(publicKey)
            verifier.verify(header, signingInput, signature)
        }.mapError { throwable ->
            throwable.toVerifyJwtSignatureError("jws signature validation failed")
        }.bind()

        if (!valid) {
            return@binding Err(JwtError.InvalidJwt).bind<Unit>()
        }
    }

    private fun createVerifier(publicKey: Jwk): JWSVerifier = when (publicKey.kty) {
        KeyType.EC.value -> {
            val key = ECKey.Builder(
                Curve(publicKey.crv),
                Base64URL(publicKey.x),
                Base64URL(checkNotNull(publicKey.y) { "EC public key is missing the y coordinate" }),
            ).build()
            ECDSAVerifier(key)
        }
        KeyType.OKP.value -> {
            val key = OctetKeyPair.Builder(
                Curve(publicKey.crv),
                Base64URL(publicKey.x),
            ).build()
            Ed25519Verifier(key)
        }
        else -> error("unsupported key type '${publicKey.kty}' for jws signature verification")
    }
}
