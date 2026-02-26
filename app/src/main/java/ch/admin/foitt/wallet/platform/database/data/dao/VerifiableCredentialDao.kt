package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState

@Dao
interface VerifiableCredentialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(verifiableCredential: VerifiableCredentialEntity): Long

    @Query("UPDATE VerifiableCredentialEntity SET progressionState = :progressionState WHERE credentialId = :id")
    fun updateProgressStateByCredentialId(id: Long, progressionState: VerifiableProgressionState): Int

    @Query("DELETE FROM VerifiableCredentialEntity WHERE credentialId = :id")
    fun deleteById(id: Long)

    @Query("SELECT * FROM VerifiableCredentialEntity WHERE credentialId = :id")
    fun getById(id: Long): VerifiableCredentialEntity

    @Query("SELECT credentialId FROM VerifiableCredentialEntity")
    fun getAllIds(): List<Long>

    @Query("SELECT * FROM VerifiableCredentialEntity")
    fun getAll(): List<VerifiableCredentialEntity>

    @Query("UPDATE VerifiableCredentialEntity SET updatedAt = :updatedAt WHERE credentialId = :id")
    fun updatedAt(id: Long, updatedAt: Long): Int
}
