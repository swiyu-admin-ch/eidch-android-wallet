package ch.admin.foitt.wallet.platform.activityList.data.repository

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityListError
import ch.admin.foitt.wallet.platform.activityList.domain.model.CredentialActivityRepositoryError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.CredentialActivityRepository
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialActivityEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.data.dao.ImageEntityDao
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialActivityEntity
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class CredentialActivityRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CredentialActivityRepository {
    override suspend fun insert(
        credentialActivityEntity: CredentialActivityEntity
    ): Result<Long, CredentialActivityRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            credentialActivityDao().insert(credentialActivityEntity)
        }
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Error when inserting activity entity")
        ActivityListError.Unexpected(throwable)
    }

    override suspend fun getById(
        activityId: Long
    ): Result<CredentialActivityEntity, CredentialActivityRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            credentialActivityDao().getById(activityId)
        }
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Error when getting activity entity by id")
        ActivityListError.Unexpected(throwable)
    }

    override suspend fun deleteById(activityId: Long): Result<Unit, CredentialActivityRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            credentialActivityDao().deleteById(activityId)
            // also clean up images without children
            imageDao().deleteImagesWithoutChildren()
        }
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Error when deleting activity entity")
        ActivityListError.Unexpected(throwable)
    }

    override suspend fun deleteAllActivities(): Result<Unit, CredentialActivityRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            credentialActivityDao().deleteAllActivities()
            // also clean up images without children
            imageDao().deleteImagesWithoutChildren()
        }
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "Error when deleting all activity entities")
        ActivityListError.Unexpected(throwable)
    }

    private suspend fun credentialActivityDao(): CredentialActivityEntityDao = suspendUntilNonNull {
        credentialActivityDaoFlow.value
    }
    private val credentialActivityDaoFlow = daoProvider.credentialActivityEntityDao

    private suspend fun imageDao(): ImageEntityDao = suspendUntilNonNull { imageDaoFlow.value }
    private val imageDaoFlow = daoProvider.imageEntityDao
}
