package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityActorDisplayWithImage
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityActorDisplayWithImageDao {
    @Transaction
    @Query("SELECT * FROM activityactordisplayentity WHERE activityId = :activityId")
    fun getActorDisplaysByActivityId(activityId: Long): Flow<List<ActivityActorDisplayWithImage>>
}
