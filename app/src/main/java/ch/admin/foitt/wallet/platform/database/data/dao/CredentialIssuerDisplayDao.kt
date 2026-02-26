package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import kotlinx.coroutines.flow.Flow

@Dao
interface CredentialIssuerDisplayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(credentialIssuerDisplays: Collection<CredentialIssuerDisplay>)

    @Query("SELECT * FROM credentialissuerdisplay WHERE credentialId = :credentialId")
    fun getCredentialIssuerDisplaysById(credentialId: Long): List<CredentialIssuerDisplay>

    @Query("SELECT * FROM credentialissuerdisplay WHERE credentialId = :credentialId")
    fun getCredentialIssuerDisplaysFlowById(credentialId: Long): Flow<List<CredentialIssuerDisplay>>

    @Query("DELETE FROM credentialissuerdisplay WHERE credentialId = :credentialId")
    fun deleteByCredentialId(credentialId: Long): Int
}
