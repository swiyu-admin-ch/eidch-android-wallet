package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.admin.foitt.wallet.platform.database.domain.model.BatchRefreshDataEntity

@Dao
interface BatchRefreshDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(batchRefreshDataEntity: BatchRefreshDataEntity): Long

    @Query("SELECT * FROM BatchRefreshDataEntity")
    fun getAll(): List<BatchRefreshDataEntity>
}
