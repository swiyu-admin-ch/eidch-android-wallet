package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.PresentableBatchItemCount
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import com.github.michaelbull.result.Result

interface BundleItemRepository {
    suspend fun deleteByIds(bundleItemIds: List<Long>): Result<Int, BundleItemRepositoryError>
    suspend fun getAllByCredentialId(credentialId: Long): Result<List<BundleItemEntity>, BundleItemRepositoryError>
    suspend fun getCountOfNeverPresented(): Result<List<PresentableBatchItemCount>, BundleItemRepositoryError>
    suspend fun updateStatusByCredentialId(credentialId: Long, status: CredentialStatus): Result<Int, BundleItemRepositoryError>
}
