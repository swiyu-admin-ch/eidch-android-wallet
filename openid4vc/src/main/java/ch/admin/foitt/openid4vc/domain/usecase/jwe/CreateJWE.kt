package ch.admin.foitt.openid4vc.domain.usecase.jwe

import ch.admin.foitt.openid4vc.domain.model.jwe.CreateJWEError
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import com.github.michaelbull.result.Result

interface CreateJWE {
    operator fun invoke(
        algorithm: String,
        encryptionMethod: String,
        compressionAlgorithm: String? = null,
        payload: String,
        encryptionKey: Jwk,
    ): Result<String, CreateJWEError>
}
