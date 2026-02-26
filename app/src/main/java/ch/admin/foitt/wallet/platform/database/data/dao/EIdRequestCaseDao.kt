package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase

@Dao
interface EIdRequestCaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(case: EIdRequestCase)

    @Query("SELECT * FROM eidrequestcase WHERE id = :id")
    fun getEIdRequestCaseById(id: String): EIdRequestCase

    @Query("DELETE FROM eidrequestcase WHERE id = :id")
    fun deleteById(id: String)

    @Update
    fun update(state: EIdRequestCase): Int

    fun setRequestCredentialId(caseId: String, credentialId: Long): Int {
        val requestCase = getEIdRequestCaseById(caseId)
        return update(requestCase.copy(credentialId = credentialId))
    }
}
