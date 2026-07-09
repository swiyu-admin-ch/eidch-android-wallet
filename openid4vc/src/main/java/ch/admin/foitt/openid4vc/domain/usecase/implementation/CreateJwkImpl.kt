package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.CreateJwkError
import ch.admin.foitt.openid4vc.domain.model.JwkError
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.toCurve
import ch.admin.foitt.openid4vc.domain.usecase.CreateJwk
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.toErrorIfNull
import com.nimbusds.jose.jwk.ECKey
import java.security.KeyPair
import java.security.interfaces.ECPublicKey
import javax.inject.Inject

internal class CreateJwkImpl @Inject constructor() : CreateJwk {
    override suspend fun invoke(
        keyPair: KeyPair,
        algorithm: SigningAlgorithm,
    ): Result<String, CreateJwkError> =
        keyPair.toPublicECKey(algorithm).map { publicKey ->
            publicKey.toJSONString()
        }

    private fun KeyPair.toPublicECKey(algorithm: SigningAlgorithm): Result<ECKey, CreateJwkError> = runSuspendCatching {
        when (val pub = public) {
            is ECPublicKey -> pub.toPublicECKey(algorithm)
            else -> null
        }
    }.mapError { throwable ->
        JwkError.Unexpected(throwable)
    }.toErrorIfNull {
        JwkError.UnsupportedCryptographicSuite
    }

    private fun ECPublicKey.toPublicECKey(
        signingAlgorithm: SigningAlgorithm
    ): ECKey = ECKey.Builder(signingAlgorithm.toCurve(), this).build()
}
