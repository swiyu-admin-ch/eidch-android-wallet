package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.wallet.platform.database.domain.model.BatchRefreshDataEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBatchDataAndAuthentication

@Dao
interface BatchRefreshDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(batchRefreshDataEntity: BatchRefreshDataEntity): Long

    @Query("UPDATE BatchRefreshDataEntity SET batchSize = :batchSize WHERE credentialId = :credentialId")
    fun updateBatchSize(credentialId: Long, batchSize: BatchSize): Int

    @Transaction
    @Query(
        """
            SELECT * FROM VerifiableCredentialEntity
            WHERE credentialId IN (SELECT credentialId FROM BatchRefreshDataEntity)
            AND credentialId IN (SELECT credentialId FROM CredentialAuthenticationEntity)
        """
    )
    fun getAll(): List<VerifiableCredentialWithBatchDataAndAuthentication>
}
