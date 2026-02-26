package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityWithDetailsDao {
    @Transaction
    @Query("SELECT * FROM credentialactivityentity WHERE id = :activityId")
    fun getNullableByIdFlow(activityId: Long): Flow<ActivityWithDetails?>

    @Transaction
    @Query("SELECT * FROM credentialactivityentity WHERE id = :activityId")
    fun getByIdFlow(activityId: Long): Flow<ActivityWithDetails>
}
