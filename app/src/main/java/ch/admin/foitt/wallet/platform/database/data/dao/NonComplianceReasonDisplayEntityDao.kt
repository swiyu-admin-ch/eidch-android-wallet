package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ch.admin.foitt.wallet.platform.database.domain.model.NonComplianceReasonDisplayEntity

@Dao
interface NonComplianceReasonDisplayEntityDao {
    @Insert
    fun insert(nonComplianceReasonDisplayEntity: NonComplianceReasonDisplayEntity): Long

    @Query("SELECT * FROM noncompliancereasondisplayentity WHERE id = :reasonId")
    fun getById(reasonId: Long): NonComplianceReasonDisplayEntity
}
