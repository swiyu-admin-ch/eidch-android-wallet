package ch.admin.foitt.wallet.platform.crypto.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.crypto.domain.model.CryptoError
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashConfiguration
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashDataWithSaltError
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashedData
import ch.admin.foitt.wallet.platform.crypto.domain.usecase.HashDataWithSalt
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

class HashDataWithSaltImpl @Inject constructor(
    private val hashConfiguration: HashConfiguration,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : HashDataWithSalt {
    override suspend fun invoke(
        data: String,
        salt: ByteArray?,
    ): Result<HashedData, HashDataWithSaltError> = withContext(ioDispatcher) {
        return@withContext runSuspendCatching {
            val currentSalt = salt ?: generateSalt(hashConfiguration.saltLength)

            val factory = SecretKeyFactory.getInstance(hashConfiguration.hashAlgorithm)
            val spec = PBEKeySpec(
                data.toCharArray(),
                currentSalt,
                hashConfiguration.hashIterations,
                hashConfiguration.hashKeyLength
            )
            val key = factory.generateSecret(spec)
            val hash = key.encoded

            HashedData(hash, currentSalt)
        }.mapError { throwable ->
            CryptoError.createGeneralError(throwable)
        }
    }

    private fun generateSalt(length: Int): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(length)
        random.nextBytes(salt)

        return salt
    }
}
