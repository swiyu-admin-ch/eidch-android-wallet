package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState

@Dao
interface DeferredCredentialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(deferredCredential: DeferredCredentialEntity): Long

    @Transaction
    @Query("DELETE FROM DeferredCredentialEntity WHERE credentialId = :id")
    fun deleteById(id: Long): Int

    @Transaction
    @Query("SELECT * FROM DeferredCredentialEntity WHERE credentialId = :id")
    fun getById(id: Long): DeferredCredentialWithKeyBinding

    @Transaction
    @Query("SELECT * FROM DeferredCredentialEntity")
    fun getAll(): List<DeferredCredentialWithKeyBinding>

    @Transaction
    @Query(
        """
            UPDATE DeferredCredentialEntity 
            SET progressionState = :progressionState,
            polledAt = :polledAt, 
            pollInterval = :pollInterval 
            WHERE credentialId = :credentialId
        """
    )
    fun updateStatusByCredentialId(
        credentialId: Long,
        progressionState: DeferredProgressionState,
        polledAt: Long,
        pollInterval: Int,
    ): Int

    @Transaction
    @Query(
        """
            UPDATE DeferredCredentialEntity 
            SET accessToken = :accessToken, 
            refreshToken = COALESCE(:refreshToken, refreshToken)
            WHERE credentialId = :credentialId 
        """
    )
    fun updateTokensByCredentialId(
        credentialId: Long,
        accessToken: String,
        refreshToken: String?,
    ): Int
}
