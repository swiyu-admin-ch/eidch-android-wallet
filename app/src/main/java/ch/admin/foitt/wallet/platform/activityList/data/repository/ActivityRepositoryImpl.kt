package ch.admin.foitt.wallet.platform.activityList.data.repository

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityRepositoryError
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.activityList.domain.model.toActivityRepositoryError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityRepository
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityActorDisplayEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityClaimEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialActivityEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.data.dao.ImageEntityDao
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityActorDisplayEntity
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityClaimEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialActivityEntity
import ch.admin.foitt.wallet.platform.database.domain.model.ImageEntity
import ch.admin.foitt.wallet.platform.database.domain.usecase.RunInTransaction
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.utils.domain.usecase.GetImageDataFromUri
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val runInTransaction: RunInTransaction,
    private val getImageDataFromUri: GetImageDataFromUri,
) : ActivityRepository {
    override suspend fun saveActivity(
        credentialId: Long,
        activityType: ActivityType,
        actorDisplayData: ActorDisplayData,
        actorFallbackName: String,
        claimIds: List<Long>?,
        nonComplianceData: String?,
    ): Result<Long?, ActivityRepositoryError> = withContext(ioDispatcher) {
        runSuspendCatching {
            val activity = CredentialActivityEntity(
                credentialId = credentialId,
                type = activityType,
                actorTrust = actorDisplayData.trustStatus,
                vcSchemaTrust = actorDisplayData.vcSchemaTrustStatus,
                nonComplianceData = nonComplianceData,
            )

            val activityId = runInTransaction {
                val activityId = activityDao().insert(activity)

                claimIds?.let {
                    it.forEach { claimId ->
                        val activityClaim = ActivityClaimEntity(
                            activityId = activityId,
                            claimId = claimId,
                        )
                        activityClaimDao().insert(activityClaim)
                    }
                }

                val images = actorDisplayData.image?.associateBy { it.locale } ?: emptyMap()
                val names = actorDisplayData.name?.associateBy { it.locale } ?: emptyMap()
                val locales = images.keys + names.keys

                val messageDigest = MessageDigest.getInstance("SHA-256")

                locales.forEach { locale ->
                    val imageHash = images[locale]?.value?.let { imageUri ->
                        val imageDataByteArray = getImageDataFromUri(imageUri)
                        imageDataByteArray?.let {
                            val hash = messageDigest.digest(imageDataByteArray).toHexString()
                            val imageEntity = ImageEntity(
                                hash = hash,
                                image = imageDataByteArray
                            )
                            imageDao().insert(imageEntity)
                            hash
                        }
                    }

                    val activityActorDisplay = ActivityActorDisplayEntity(
                        activityId = activityId,
                        locale = locale,
                        name = names[locale]?.value ?: actorFallbackName,
                        imageHash = imageHash
                    )

                    activityActorDisplayDao().insert(activityActorDisplay)
                }

                activityId
            }

            activityId
        }.mapError { throwable ->
            throwable.toActivityRepositoryError("error when saving activity list entry")
        }
    }

    private suspend fun activityDao(): CredentialActivityEntityDao = suspendUntilNonNull { activityDaoFlow.value }
    private suspend fun activityClaimDao(): ActivityClaimEntityDao = suspendUntilNonNull { activityClaimDaoFlow.value }
    private suspend fun activityActorDisplayDao(): ActivityActorDisplayEntityDao = suspendUntilNonNull {
        activityActorDisplayDaoFlow.value
    }
    private suspend fun imageDao(): ImageEntityDao = suspendUntilNonNull { imageDaoFlow.value }

    private val activityDaoFlow = daoProvider.credentialActivityEntityDao
    private val activityClaimDaoFlow = daoProvider.activityClaimEntityDao
    private val activityActorDisplayDaoFlow = daoProvider.activityActorDisplayEntityDao
    private val imageDaoFlow = daoProvider.imageEntityDao
}
