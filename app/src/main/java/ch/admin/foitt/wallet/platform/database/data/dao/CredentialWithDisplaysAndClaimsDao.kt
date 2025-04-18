package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplaysAndClaims
import kotlinx.coroutines.flow.Flow

@Dao
interface CredentialWithDisplaysAndClaimsDao {
    @Transaction
    @Query("SELECT * FROM credential WHERE id = :id")
    fun getCredentialWithDisplaysAndClaimsFlowById(id: Long): Flow<CredentialWithDisplaysAndClaims>

    @Transaction
    @Query("SELECT * FROM credential WHERE id = :id")
    fun getNullableCredentialWithDisplaysAndClaimsFlowById(id: Long): Flow<CredentialWithDisplaysAndClaims?>
}
