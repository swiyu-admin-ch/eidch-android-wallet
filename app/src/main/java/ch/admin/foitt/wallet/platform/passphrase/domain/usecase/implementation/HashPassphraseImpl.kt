package ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashDataWithSaltError
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashedData
import ch.admin.foitt.wallet.platform.crypto.domain.usecase.HashDataWithSalt
import ch.admin.foitt.wallet.platform.passphrase.domain.model.HashPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.toHashPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.SaltRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class HashPassphraseImpl @Inject constructor(
    private val hashDataWithSalt: HashDataWithSalt,
    private val saltRepository: SaltRepository,
) : HashPassphrase {
    @CheckResult
    override suspend fun invoke(
        passphrase: String,
        initializeSalt: Boolean,
    ): Result<HashedData, HashPassphraseError> = coroutineBinding {
        val salt = if (initializeSalt) {
            null
        } else {
            saltRepository.get()
        }

        hashDataWithSalt(data = passphrase, salt = salt)
            .mapError(HashDataWithSaltError::toHashPassphraseError).bind()
    }
}
