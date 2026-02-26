package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.eid.didresolver.did_sidekicks.DidDoc
import ch.admin.eid.didresolver.did_sidekicks.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.toVerifyJwtError
import ch.admin.foitt.openid4vc.domain.usecase.ResolveDid
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyPublicKey
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class VerifyJwtSignatureImpl @Inject constructor(
    private val resolveDid: ResolveDid,
    private val verifyPublicKey: VerifyPublicKey,
) : VerifyJwtSignature {
    override suspend fun invoke(did: String, kid: String, jwt: Jwt): Result<Unit, VerifyJwtError> = coroutineBinding {
        val didDoc = resolveDid(did)
            .mapError { error -> error.toVerifyJwtError() }
            .bind()

        if (didDoc.getDeactivated()) {
            Err(VcSdJwtError.DidDocumentDeactivated).bind()
        }

        val publicKey = didDoc.getPublicKey(keyIdentifier = kid).bind()

        verifyPublicKey(publicKey, jwt.signedJwt)
            .mapError { VcSdJwtError.InvalidJwt }
            .bind()
    }

    private fun DidDoc.getPublicKey(keyIdentifier: String): Result<Jwk, VcSdJwtError.InvalidJwt> {
        val publicKey = getVerificationMethod()
            .firstOrNull { it.id.contentEquals(keyIdentifier) }?.publicKeyJwk

        return publicKey?.let {
            Ok(it)
        } ?: Err(VcSdJwtError.InvalidJwt)
    }
}
