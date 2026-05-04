package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.ResolvePublicKeyError
import com.github.michaelbull.result.Result

interface ResolvePublicKey {
    suspend operator fun invoke(did: String, kid: String): Result<Jwk, ResolvePublicKeyError>
}
