package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFile

@Dao
interface EIdRequestFileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(file: EIdRequestFile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(files: List<EIdRequestFile>): List<Long>

    @Transaction
    @Query("SELECT * FROM eidrequestfile WHERE eIdRequestCaseId = :caseId")
    fun getAllFilesByCaseId(caseId: String): List<EIdRequestFile>

    @Transaction
    @Query("SELECT * FROM eidrequestfile ORDER BY createdAt ASC")
    fun getAllFiles(): List<EIdRequestFile>

    @Transaction
    @Query("SELECT * FROM eidrequestfile WHERE eIdRequestCaseId = :caseId AND fileName = :fileName ORDER BY createdAt DESC LIMIT 1")
    fun getFile(caseId: String, fileName: String): EIdRequestFile
}
