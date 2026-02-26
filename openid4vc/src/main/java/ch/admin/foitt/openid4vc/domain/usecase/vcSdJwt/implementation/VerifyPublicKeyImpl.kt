package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.eid.didresolver.did_sidekicks.Jwk
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyPublicKey
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.jwt.SignedJWT
import timber.log.Timber
import javax.inject.Inject

internal class VerifyPublicKeyImpl @Inject constructor() : VerifyPublicKey {
    override fun invoke(publicKey: Jwk, signedJWT: SignedJWT): Result<Unit, Unit> = runSuspendCatching {
        val key = ECKey.Builder(
            Curve(publicKey.crv),
            Base64URL(publicKey.x),
            Base64URL(publicKey.y)
        ).build()
        val verifier = ECDSAVerifier(key)
        signedJWT.verify(verifier)
    }.mapError { throwable ->
        Timber.w(t = throwable, message = "Jwt public key verification failed")
    }.andThen { isVerified ->
        if (isVerified) {
            Ok(Unit)
        } else {
            Err(Unit)
        }
    }
}
