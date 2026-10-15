package ch.admin.foitt.wallet.platform.activityList.data.repository

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityRepositoryError
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.activityList.domain.model.toActivityRepositoryError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityRepository
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityActorDisplayEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityClaimEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialActivityEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.data.dao.ImageEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.NonComplianceReasonDisplayEntityDao
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityActorDisplayEntity
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityClaimEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialActivityEntity
import ch.admin.foitt.wallet.platform.database.domain.model.ImageEntity
import ch.admin.foitt.wallet.platform.database.domain.model.NonComplianceReasonDisplayEntity
import ch.admin.foitt.wallet.platform.database.domain.usecase.RunInTransaction
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
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
    private val getLocalizedDisplay: GetLocalizedDisplay,
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
                actorCompliance = actorDisplayData.actorComplianceState,
                nonComplianceData = nonComplianceData,
            )

            val activityId = runInTransaction {
                val activityId = activityDao().insert(activity)

                actorDisplayData.nonComplianceReason?.let {
                    it.forEach { (reason, locale) ->
                        reason?.let {
                            val nonComplianceReasonDisplay = NonComplianceReasonDisplayEntity(
                                activityId = activityId,
                                locale = locale,
                                reason = reason,
                            )
                            nonComplianceReasonDisplayDao().insert(nonComplianceReasonDisplay)
                        }
                    }
                }

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
                val imageDatas = actorDisplayData.imageData?.associateBy { it.locale } ?: emptyMap()
                val names = actorDisplayData.name?.associateBy { it.locale } ?: emptyMap()
                val locales = images.keys + imageDatas.keys + names.keys

                val messageDigest = MessageDigest.getInstance("SHA-256")

                if (locales.isEmpty()) {
                    val activityActorDisplay = ActivityActorDisplayEntity(
                        activityId = activityId,
                        locale = actorDisplayData.preferredLanguage ?: "",
                        name = actorFallbackName,
                        imageHash = null
                    )
                    activityActorDisplayDao().insert(activityActorDisplay)
                } else {
                    saveActivityActorDisplayFromLocales(
                        locales = locales,
                        actorDisplayData = actorDisplayData,
                        actorFallbackName = actorFallbackName,
                        activityId = activityId,
                        names = names,
                        messageDigest = messageDigest
                    )
                }

                activityId
            }

            activityId
        }.mapError { throwable ->
            throwable.toActivityRepositoryError("error when saving activity list entry")
        }
    }

    private suspend fun saveActivityActorDisplayFromLocales(
        locales: Set<String>,
        actorDisplayData: ActorDisplayData,
        actorFallbackName: String,
        activityId: Long,
        names: Map<String, ActorField<String>>,
        messageDigest: MessageDigest
    ) {
        locales.forEach { locale ->
            // The blob and URI locale sets routinely differ, so an exact map lookup
            // would leave name-only locales without a logo.
            val imageData = getLocalizedDisplay(
                displays = actorDisplayData.imageData.orEmpty(),
                preferredLocaleString = locale,
            )?.value
                ?: getLocalizedDisplay(
                    displays = actorDisplayData.image.orEmpty(),
                    preferredLocaleString = locale,
                )?.value?.let { imageUri ->
                    getImageDataFromUri(imageUri)
                }

            val imageHash = imageData?.let {
                val hash = messageDigest.digest(it).toHexString()
                val imageEntity = ImageEntity(
                    hash = hash,
                    image = it
                )
                imageDao().insert(imageEntity)
                hash
            }

            val activityActorDisplay = ActivityActorDisplayEntity(
                activityId = activityId,
                locale = locale,
                name = names[locale]?.value ?: actorFallbackName,
                imageHash = imageHash
            )

            activityActorDisplayDao().insert(activityActorDisplay)
        }
    }

    private suspend fun activityDao(): CredentialActivityEntityDao = suspendUntilNonNull { activityDaoFlow.value }
    private suspend fun nonComplianceReasonDisplayDao(): NonComplianceReasonDisplayEntityDao = suspendUntilNonNull {
        nonComplianceReasonDisplayDaoFlow.value
    }
    private suspend fun activityClaimDao(): ActivityClaimEntityDao = suspendUntilNonNull { activityClaimDaoFlow.value }
    private suspend fun activityActorDisplayDao(): ActivityActorDisplayEntityDao = suspendUntilNonNull {
        activityActorDisplayDaoFlow.value
    }
    private suspend fun imageDao(): ImageEntityDao = suspendUntilNonNull { imageDaoFlow.value }

    private val activityDaoFlow = daoProvider.credentialActivityEntityDao
    private val nonComplianceReasonDisplayDaoFlow = daoProvider.nonComplianceReasonDisplayEntityDao
    private val activityClaimDaoFlow = daoProvider.activityClaimEntityDao
    private val activityActorDisplayDaoFlow = daoProvider.activityActorDisplayEntityDao
    private val imageDaoFlow = daoProvider.imageEntityDao
}
