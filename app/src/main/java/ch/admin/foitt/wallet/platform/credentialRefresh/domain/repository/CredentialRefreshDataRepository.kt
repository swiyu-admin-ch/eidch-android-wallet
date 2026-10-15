package ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.CredentialRefreshDataError
import ch.admin.foitt.wallet.platform.database.domain.model.BatchRefreshDataEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithAuthentication
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBatchDataAndAuthentication
import com.github.michaelbull.result.Result

interface CredentialRefreshDataRepository {
    suspend fun getCredentialRefreshDataById(credentialId: Long): Result<VerifiableCredentialWithAuthentication, CredentialRefreshDataError>
    suspend fun getAllCredentialRefreshData():
        Result<List<VerifiableCredentialWithAuthentication>, CredentialRefreshDataError>

    suspend fun getAllBatchCredentialRefreshData():
        Result<List<VerifiableCredentialWithBatchDataAndAuthentication>, CredentialRefreshDataError>

    suspend fun getBatchRefreshDataById(credentialId: Long): Result<BatchRefreshDataEntity?, CredentialRefreshDataError>

    suspend fun getCredentialAuthenticationById(credentialId: Long):
        Result<CredentialAuthenticationEntity?, CredentialRefreshDataError>

    suspend fun saveRefreshData(
        credentialId: Long,
        batchSize: BatchSize?,
        accessToken: String,
        refreshToken: String,
        dpopKeyBinding: KeyBinding?,
    ): Result<Long, CredentialRefreshDataError>

    suspend fun updateBatchSize(
        credentialId: Long,
        batchSize: BatchSize,
    ): Result<Int, CredentialRefreshDataError>
}
