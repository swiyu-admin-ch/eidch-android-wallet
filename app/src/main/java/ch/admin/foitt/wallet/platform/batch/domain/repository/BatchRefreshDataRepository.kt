@file:Suppress("LongParameterList")

package ch.admin.foitt.wallet.platform.batch.domain.repository

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.wallet.platform.batch.domain.error.BatchRefreshDataRepositoryError
import ch.admin.foitt.wallet.platform.database.domain.model.BatchRefreshDataEntity
import com.github.michaelbull.result.Result

interface BatchRefreshDataRepository {

    suspend fun saveBatchRefreshData(
        credentialId: Long,
        batchSize: BatchSize,
        refreshToken: String
    ): Result<Long, BatchRefreshDataRepositoryError>

    suspend fun getAll(): Result<List<BatchRefreshDataEntity>, BatchRefreshDataRepositoryError>
}
