@file:Suppress("LongParameterList")

package ch.admin.foitt.wallet.platform.batch.domain.repository

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.wallet.platform.batch.domain.error.BatchRefreshDataRepositoryError
import com.github.michaelbull.result.Result

interface BatchRefreshDataRepository {

    suspend fun saveBatchRefreshData(
        credentialId: Long,
        batchSize: BatchSize,
        accessToken: String,
        refreshToken: String,
        dpopKeyBinding: KeyBinding?,
    ): Result<Long, BatchRefreshDataRepositoryError>

    suspend fun updateBatchSize(
        credentialId: Long,
        batchSize: BatchSize,
    ): Result<Int, BatchRefreshDataRepositoryError>
}
