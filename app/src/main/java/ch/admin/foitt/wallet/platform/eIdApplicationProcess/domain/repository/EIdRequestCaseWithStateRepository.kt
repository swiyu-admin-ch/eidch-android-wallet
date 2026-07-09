package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithStateRepositoryError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface EIdRequestCaseWithStateRepository {
    fun getEIdRequestCasesWithStatesFlow(): Flow<Result<List<EIdRequestCaseWithState>, EIdRequestCaseWithStateRepositoryError>>
    suspend fun getEIdRequestCasesWithStates(): Result<List<EIdRequestCaseWithState>, EIdRequestCaseWithStateRepositoryError>
}
