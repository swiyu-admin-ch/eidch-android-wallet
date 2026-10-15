package ch.admin.foitt.wallet.platform.database.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.CreateAppDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.repository.DatabaseRepository
import ch.admin.foitt.wallet.platform.database.domain.usecase.CreateAppDatabase
import com.github.michaelbull.result.Result
import javax.inject.Inject

class CreateAppDatabaseImpl @Inject constructor(
    private val databaseRepository: DatabaseRepository,
) : CreateAppDatabase {

    override suspend fun invoke(passphrase: ByteArray): Result<Unit, CreateAppDatabaseError> = databaseRepository.createDatabase(passphrase)
}
