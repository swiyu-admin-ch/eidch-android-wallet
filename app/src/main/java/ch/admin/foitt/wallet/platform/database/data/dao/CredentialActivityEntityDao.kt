package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialActivityEntity

@Dao
interface CredentialActivityEntityDao {
    @Insert
    fun insert(credentialActivityEntity: CredentialActivityEntity): Long

    @Query("SELECT * FROM credentialactivityentity WHERE id = :activityId")
    fun getById(activityId: Long): CredentialActivityEntity

    @Query("DELETE FROM credentialactivityentity WHERE id = :activityId")
    fun deleteById(activityId: Long): Unit

    @Query("DELETE FROM credentialactivityentity")
    fun deleteAllActivities(): Unit
}
