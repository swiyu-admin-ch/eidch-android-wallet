package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.eid.didresolver.did_sidekicks.DidDoc
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.ResolvePublicKeyError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.toResolvePublicKeyError
import ch.admin.foitt.openid4vc.domain.usecase.ResolveDid
import ch.admin.foitt.openid4vc.domain.usecase.ResolvePublicKey
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class ResolvePublicKeyImpl @Inject constructor(
    private val resolveDid: ResolveDid,
) : ResolvePublicKey {
    override suspend fun invoke(did: String, kid: String): Result<Jwk, ResolvePublicKeyError> = coroutineBinding {
        val didDoc = resolveDid(did)
            .mapError { error -> error.toResolvePublicKeyError() }
            .bind()

        if (didDoc.getDeactivated()) {
            Err(VcSdJwtError.DidDocumentDeactivated).bind()
        }

        didDoc.getPublicKey(keyIdentifier = kid).bind()
    }

    private fun DidDoc.getPublicKey(keyIdentifier: String): Result<Jwk, VcSdJwtError.InvalidJwt> = binding {
        val publicKey = getVerificationMethod()
            .firstOrNull { it.id.contentEquals(keyIdentifier) }?.publicKeyJwk

        val jwk = runSuspendCatching {
            checkNotNull(publicKey)
            val x = checkNotNull(publicKey.x)
            val y = checkNotNull(publicKey.y)
            val crv = checkNotNull(publicKey.crv)
            val kty = checkNotNull(publicKey.kty)

            Jwk(x = x, y = y, crv = crv, kty = kty)
        }.mapError { _ -> VcSdJwtError.InvalidJwt }.bind()

        jwk
    }
}
