package ch.admin.foitt.openid4vc.domain.usecase.jwe.implementation

import ch.admin.foitt.openid4vc.domain.model.jwe.DecryptJWEError
import ch.admin.foitt.openid4vc.domain.model.jwe.JWEError
import ch.admin.foitt.openid4vc.domain.usecase.jwe.DecryptJWE
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.nimbusds.jose.JWEDecrypterOption
import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.crypto.ECDHDecrypter
import com.nimbusds.jose.crypto.opts.MaxCompressedCipherTextLength
import com.nimbusds.jose.jwk.ECKey
import timber.log.Timber
import java.security.PrivateKey
import javax.inject.Inject

class DecryptJWEImpl @Inject constructor() : DecryptJWE {
    override fun invoke(
        jweString: String,
        privateKey: PrivateKey,
        jweMaxCompressedCipherTextLength: Int,
    ): Result<String, DecryptJWEError> = runSuspendCatching {
        val jwe = JWEObject.parse(jweString)
        val providedPublicKey = jwe.header.ephemeralPublicKey.toECKey()

        val ecKey = ECKey.Builder(providedPublicKey)
            .privateKey(privateKey)
            .build()

        jwe.decrypt(
            ECDHDecrypter(ecKey),
            setOf<JWEDecrypterOption>(MaxCompressedCipherTextLength(jweMaxCompressedCipherTextLength))
        )

        jwe.payload.toString()
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Error during JWE decryption")
        JWEError.Unexpected(throwable)
    }
}
