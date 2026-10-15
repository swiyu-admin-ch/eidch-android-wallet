package ch.admin.foitt.wallet.platform.crypto.domain.usecase

import ch.admin.foitt.wallet.platform.crypto.domain.model.HashDataWithSaltError
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashedData
import com.github.michaelbull.result.Result

interface HashDataWithSalt {
    suspend operator fun invoke(data: String, salt: ByteArray?): Result<HashedData, HashDataWithSaltError>
}
