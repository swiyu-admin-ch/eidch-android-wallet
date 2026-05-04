package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithDisplays
import kotlinx.coroutines.flow.Flow

@Dao
interface DeferredCredentialWithDisplaysDao {
    @Transaction
    @Query("SELECT * FROM DeferredCredentialEntity ORDER BY createdAt DESC")
    fun getAll(): Flow<List<DeferredCredentialWithDisplays>>

    @Transaction
    @Query("SELECT * FROM DeferredCredentialEntity WHERE credentialId = :id")
    fun getById(id: Long): Flow<DeferredCredentialWithDisplays>
}
