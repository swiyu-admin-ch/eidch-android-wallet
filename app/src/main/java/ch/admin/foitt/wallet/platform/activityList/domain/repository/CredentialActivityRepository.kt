package ch.admin.foitt.wallet.platform.activityList.domain.repository

import ch.admin.foitt.wallet.platform.activityList.domain.model.CredentialActivityRepositoryError
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialActivityEntity
import com.github.michaelbull.result.Result

interface CredentialActivityRepository {
    suspend fun insert(credentialActivityEntity: CredentialActivityEntity): Result<Long, CredentialActivityRepositoryError>
    suspend fun getById(activityId: Long): Result<CredentialActivityEntity, CredentialActivityRepositoryError>
    suspend fun deleteById(activityId: Long): Result<Unit, CredentialActivityRepositoryError>
    suspend fun deleteAllActivities(): Result<Unit, CredentialActivityRepositoryError>
}
