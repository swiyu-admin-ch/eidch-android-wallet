package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateDPoPProofJwtError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import com.github.michaelbull.result.Result
import java.net.URL

fun interface CreateDPoPProofJwt {
    suspend operator fun invoke(
        method: String,
        url: URL,
        keyPair: JWSKeyPair,
        nonce: String?,
        accessToken: String?,
        keyAttestationJwt: Jwt?,
    ): Result<String, CreateDPoPProofJwtError>
}
