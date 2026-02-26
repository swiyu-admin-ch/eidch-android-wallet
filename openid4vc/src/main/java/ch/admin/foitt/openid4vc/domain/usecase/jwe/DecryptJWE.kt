package ch.admin.foitt.openid4vc.domain.usecase.jwe

import ch.admin.foitt.openid4vc.domain.model.jwe.DecryptJWEError
import com.github.michaelbull.result.Result
import java.security.PrivateKey

interface DecryptJWE {
    operator fun invoke(
        jweString: String,
        privateKey: PrivateKey,
        jweMaxCompressedCipherTextLength: Int = JWE_MAX_COMPRESSED_CIPHER_TEXT_LENGTH,
    ): Result<String, DecryptJWEError>

    private companion object {
        // Nimbus JOSE Jwt library default is 100.000 which is not enough for our credentials containing large images etc.
        private const val JWE_MAX_COMPRESSED_CIPHER_TEXT_LENGTH = 600000
    }
}
