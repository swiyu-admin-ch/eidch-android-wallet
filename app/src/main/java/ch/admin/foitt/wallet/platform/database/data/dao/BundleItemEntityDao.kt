package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.PresentableBatchItemCount

@Dao
interface BundleItemEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bundleItemEntity: BundleItemEntity): Long

    @Query("DELETE FROM BundleItemEntity WHERE id IN (:ids)")
    fun deleteByIds(ids: List<Long>): Int

    @Transaction
    @Query("SELECT * FROM BundleItemEntity")
    fun getAll(): List<BundleItemEntity>

    @Transaction
    @Query("SELECT credentialId, COUNT(*) AS count FROM BundleItemEntity GROUP BY credentialId HAVING COUNT(*) > 1")
    fun getCountOfNeverPresented(): List<PresentableBatchItemCount>

    @Transaction
    @Query("SELECT * FROM BundleItemEntity WHERE credentialId = :credentialId")
    fun getAllByCredentialId(credentialId: Long): List<BundleItemEntity>

    @Query("UPDATE BundleItemEntity SET status = :status WHERE credentialId = :id")
    fun updateStatusByCredentialId(id: Long, status: CredentialStatus): Int
}
