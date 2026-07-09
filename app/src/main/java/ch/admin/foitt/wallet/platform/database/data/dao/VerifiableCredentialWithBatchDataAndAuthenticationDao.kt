package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBatchDataAndAuthentication

@Dao
interface VerifiableCredentialWithBatchDataAndAuthenticationDao {
    @Transaction
    @Query("SELECT * FROM verifiableCredentialEntity")
    fun getAll(): List<VerifiableCredentialWithBatchDataAndAuthentication>
}
