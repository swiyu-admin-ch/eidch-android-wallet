package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialKeyBindingEntity

@Dao
interface CredentialKeyBindingEntityDao {
    @Insert
    fun insert(keyBinding: CredentialKeyBindingEntity): Long

    @Update
    fun update(keyBinding: CredentialKeyBindingEntity): Int

    @Query("SELECT * FROM credentialkeybindingentity WHERE credentialId = :credentialId")
    fun getByCredentialId(credentialId: Long): List<CredentialKeyBindingEntity>
}
