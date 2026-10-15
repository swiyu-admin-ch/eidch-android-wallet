package ch.admin.foitt.wallet.platform.passphrase.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashedData
import ch.admin.foitt.wallet.platform.passphrase.domain.model.HashPassphraseError
import com.github.michaelbull.result.Result

fun interface HashPassphrase {
    @CheckResult
    suspend operator fun invoke(
        passphrase: String,
        initializeSalt: Boolean,
    ): Result<HashedData, HashPassphraseError>
}
