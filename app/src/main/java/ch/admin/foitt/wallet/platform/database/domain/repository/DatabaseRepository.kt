package ch.admin.foitt.wallet.platform.database.domain.repository

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.database.domain.model.ChangeDatabasePassphraseError
import ch.admin.foitt.wallet.platform.database.domain.model.CreateAppDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseState
import ch.admin.foitt.wallet.platform.database.domain.model.OpenDatabaseError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.StateFlow

interface DatabaseRepository {
    val databaseState: StateFlow<DatabaseState>

    @CheckResult
    suspend fun createDatabase(passphrase: ByteArray): Result<Unit, CreateAppDatabaseError>

    suspend fun close()

    @CheckResult
    suspend fun open(passphrase: ByteArray): Result<Unit, OpenDatabaseError>

    @CheckResult
    suspend fun checkIfCorrectPassphrase(passphrase: ByteArray): Result<Unit, OpenDatabaseError>

    @CheckResult
    suspend fun changePassphrase(newPassphrase: ByteArray): Result<Unit, ChangeDatabasePassphraseError>

    @CheckResult
    fun isOpen(): Boolean

    suspend fun <V> runInTransaction(block: suspend () -> V): V?
}
