package ch.admin.foitt.wallet.platform.database.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.database.domain.model.CreateAppDatabaseError
import com.github.michaelbull.result.Result

interface CreateAppDatabase {

    @CheckResult
    suspend operator fun invoke(
        passphrase: ByteArray
    ): Result<Unit, CreateAppDatabaseError>
}
