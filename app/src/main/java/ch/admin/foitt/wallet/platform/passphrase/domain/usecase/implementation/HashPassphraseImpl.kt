package ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashDataError
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashedData
import ch.admin.foitt.wallet.platform.crypto.domain.usecase.HashDataWithSalt
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.SaltRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.onFailure
import timber.log.Timber
import javax.inject.Inject

class HashPassphraseImpl @Inject constructor(
    private val hashDataWithSalt: HashDataWithSalt,
    private val saltRepository: SaltRepository,
) : HashPassphrase {
    @CheckResult
    override suspend fun invoke(
        pin: String,
        initializeSalt: Boolean,
    ): Result<HashedData, HashDataError> {
        return coroutineBinding {
            if (initializeSalt) {
                val hashData = hashDataWithSalt(
                    data = pin,
                    salt = null,
                ).bind()
                hashData
            } else {
                val hashData = hashDataWithSalt(data = pin, salt = saltRepository.get()).bind()
                hashData
            }
        }.onFailure { error ->
            Timber.e(t = error.throwable, message = "Error while trying to hash PIN")
        }
    }
}
