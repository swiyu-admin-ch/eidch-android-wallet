package ch.admin.foitt.openid4vc.domain.usecase.jwt.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureError
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureFromDidError
import ch.admin.foitt.openid4vc.domain.model.jwt.toVerifyJwtSignatureFromDidError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.ResolvePublicKeyError
import ch.admin.foitt.openid4vc.domain.usecase.ResolvePublicKey
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class VerifyJwtSignatureFromDidImpl @Inject constructor(
    private val resolvePublicKey: ResolvePublicKey,
    private val verifyJwtSignature: VerifyJwtSignature,
) : VerifyJwtSignatureFromDid {
    override suspend fun invoke(did: String, kid: String, jwt: Jwt): Result<Unit, VerifyJwtSignatureFromDidError> = coroutineBinding {
        val publicKey = resolvePublicKey(
            did = did,
            kid = kid,
        ).mapError(ResolvePublicKeyError::toVerifyJwtSignatureFromDidError)
            .bind()

        verifyJwtSignature(
            jwt = jwt,
            publicKey = publicKey,
        ).mapError(VerifyJwtSignatureError::toVerifyJwtSignatureFromDidError)
            .bind()
    }
}
