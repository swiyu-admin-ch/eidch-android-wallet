package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithState
import kotlinx.coroutines.flow.Flow

@Dao
interface EIdRequestCaseWithStateDao {
    @Transaction
    @Query("SELECT * FROM eidrequestcase")
    fun getEIdCasesWithStatesFlow(): Flow<List<EIdRequestCaseWithState>>

    @Transaction
    @Query("SELECT * FROM eidrequestcase")
    fun getEIdCasesWithStates(): List<EIdRequestCaseWithState>
}
