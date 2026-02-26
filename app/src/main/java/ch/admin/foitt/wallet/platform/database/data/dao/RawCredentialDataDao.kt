package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.admin.foitt.wallet.platform.database.domain.model.RawCredentialData

@Dao
interface RawCredentialDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rawCredentialData: RawCredentialData): Long

    @Query("SELECT * FROM RawCredentialData WHERE id = :id")
    fun getById(id: Long): RawCredentialData

    @Query("SELECT * FROM RawCredentialData WHERE credentialId = :credentialId")
    fun getRawCredentialDataByCredentialId(credentialId: Long): List<RawCredentialData>

    @Query("DELETE FROM RawCredentialData WHERE credentialId = :credentialId")
    fun deleteByCredentialId(credentialId: Long): Int

    @Query("UPDATE RawCredentialData SET rawOIDMetadata = :metadata WHERE credentialId = :credentialId")
    fun updateMetadataByCredentialId(credentialId: Long, metadata: ByteArray): Int
}
