package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import ch.admin.foitt.wallet.platform.database.domain.model.DpopBindingEntity

@Dao
interface DpopBindingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(dpopBinding: DpopBindingEntity)
}
