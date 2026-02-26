package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.database.data.dao.BundleItemEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimClusterDisplayEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimClusterEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialIssuerDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialKeyBindingEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.data.dao.DeferredCredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.RawCredentialDataDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialDao
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.ClusterDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialKeyBindingEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayConst
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.database.domain.model.RawCredentialData
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.toCredentialClaimClusterDisplayEntity
import ch.admin.foitt.wallet.platform.database.domain.model.toCredentialClaimClusterEntity
import ch.admin.foitt.wallet.platform.database.domain.usecase.RunInTransaction
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.keybindingMatching.domain.usecase.MatchKeyBindingToPayloadCnf
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toCredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOrThrow
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject

class CredentialOfferRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val runInTransaction: RunInTransaction,
    private val matchKeyBindingToPayloadCnf: MatchKeyBindingToPayloadCnf,
) : CredentialOfferRepository {

    override suspend fun saveCredentialOffer(
        credentialId: Long,
        keyBindings: List<KeyBinding?>,
        payloads: List<String>,
        format: CredentialFormat,
        selectedConfigurationId: String,
        validFrom: Long?,
        validUntil: Long?,
        issuer: String?,
        issuerDisplays: List<AnyIssuerDisplay>,
        credentialDisplays: List<AnyCredentialDisplay>,
        clusters: List<Cluster>,
        rawCredentialData: RawCredentialData,
        issuerUrl: URL,
    ): Result<Long, CredentialOfferRepositoryError> = coroutineBinding {
        saveCredentialOfferTransaction(
            credentialId = credentialId,
            format = format,
            issuerUrl = issuerUrl,
            selectedConfigurationId = selectedConfigurationId,
            issuerDisplays = issuerDisplays,
            credentialDisplays = credentialDisplays,
            rawCredentialData = rawCredentialData,
        ) { credentialId ->
            val verifiableCredential = createVerifiableCredential(
                credentialId = credentialId,
                validFrom = validFrom,
                validUntil = validUntil,
                issuer = issuer,
            )
            verifiableCredentialDao().insert(verifiableCredential)

            payloads.forEach { payload ->
                val bundleItemId: Long = bundleItemEntityDao().insert(createBundleItem(credentialId, payload))

                val keyBinding = matchKeyBindingToPayloadCnf(
                    keyBindings = keyBindings,
                    payload = payload
                ).getOrThrow { IllegalStateException("No payload found for keyBinding") }

                createCredentialKeyBinding(credentialId, keyBinding, bundleItemId)?.let {
                    credentialKeyBindingDao().insert(it)
                }
            }

            clusters.forEach { cluster ->
                saveCluster(
                    cluster = cluster,
                    credentialId = credentialId,
                    parentClusterId = null
                )
            }
        }.mapError { throwable ->
            throwable.toCredentialOfferRepositoryError("saveCredentialOffer error")
        }.bind()
    }

    override suspend fun saveDeferredCredentialOffer(
        transactionId: String,
        accessToken: String,
        endpoint: URL,
        pollInterval: Int,
        keyBindings: List<KeyBinding>?,
        format: CredentialFormat,
        issuerUrl: URL,
        selectedConfigurationId: String,
        issuerDisplays: List<AnyIssuerDisplay>,
        credentialDisplays: List<AnyCredentialDisplay>,
        rawCredentialData: RawCredentialData
    ): Result<Long, CredentialOfferRepositoryError> = saveCredentialOfferTransaction(
        format = format,
        issuerUrl = issuerUrl,
        selectedConfigurationId = selectedConfigurationId,
        issuerDisplays = issuerDisplays,
        credentialDisplays = credentialDisplays,
        rawCredentialData = rawCredentialData,
    ) { credentialId ->
        keyBindings?.forEach { keyBinding ->
            createCredentialKeyBinding(credentialId, keyBinding)?.let {
                credentialKeyBindingDao().insert(it)
            }
        }
        deferredCredentialDao().insert(
            DeferredCredentialEntity(
                credentialId = credentialId,
                transactionId = transactionId,
                accessToken = accessToken,
                endpoint = endpoint.toExternalForm(),
                pollInterval = pollInterval,
            )
        )
    }.mapError { throwable ->
        throwable.toCredentialOfferRepositoryError("saveDeferredCredentialOffer error")
    }

    override suspend fun updateDeferredCredentialOffer(
        credentialId: Long,
        progressionState: DeferredProgressionState,
        polledAt: Long,
        pollInterval: Int,
        issuerDisplays: List<AnyIssuerDisplay>,
        credentialDisplays: List<AnyCredentialDisplay>,
        rawMetadata: ByteArray
    ): Result<Long, CredentialOfferRepositoryError> = withContext(ioDispatcher) {
        runSuspendCatching {
            runInTransaction {
                val updatedCredential = deferredCredentialDao().updateStatusByCredentialId(
                    credentialId = credentialId,
                    progressionState = progressionState,
                    polledAt = polledAt,
                    pollInterval = pollInterval,
                )

                credentialIssuerDisplayDao().deleteByCredentialId(credentialId)
                val issuerDisplays = createCredentialIssuerDisplays(issuerDisplays, credentialId)
                credentialIssuerDisplayDao().insertAll(issuerDisplays)

                credentialDisplayDao().deleteByCredentialId(credentialId)
                val credDisplays = createCredentialDisplays(credentialDisplays, credentialId)
                credentialDisplayDao().insertAll(credDisplays)

                rawCredentialDataDao().updateMetadataByCredentialId(credentialId, rawMetadata)

                updatedCredential.toLong()
            } ?: error("updateDeferredCredentialOffer: transaction failed")
        }.mapError { throwable ->
            throwable.toCredentialOfferRepositoryError("updateDeferredCredentialOffer error")
        }
    }

    override suspend fun saveCredentialFromDeferred(
        credentialId: Long,
        payloads: List<String>,
        validFrom: Long?,
        validUntil: Long?,
        issuer: String?,
        issuerDisplays: List<AnyIssuerDisplay>,
        credentialDisplays: List<AnyCredentialDisplay>,
        clusters: List<Cluster>,
        rawCredentialData: RawCredentialData,
    ): Result<Long, CredentialOfferRepositoryError> = withContext(ioDispatcher) {
        runSuspendCatching {
            runInTransaction {
                credentialIssuerDisplayDao().deleteByCredentialId(credentialId)
                val issuerDisplays = createCredentialIssuerDisplays(issuerDisplays, credentialId)
                credentialIssuerDisplayDao().insertAll(issuerDisplays)

                credentialDisplayDao().deleteByCredentialId(credentialId)
                val credDisplays = createCredentialDisplays(credentialDisplays, credentialId)
                credentialDisplayDao().insertAll(credDisplays)

                rawCredentialDataDao().deleteByCredentialId(credentialId)
                rawCredentialDataDao().insert(rawCredentialData = rawCredentialData)

                deferredCredentialDao().deleteById(credentialId)

                val verifiableCredential = createVerifiableCredential(
                    credentialId = credentialId,
                    validFrom = validFrom,
                    validUntil = validUntil,
                    issuer = issuer,
                )
                verifiableCredentialDao().insert(verifiableCredential)

                val keyBindings = credentialKeyBindingDao().getByCredentialId(credentialId).map {
                    it.toKeyBinding()
                }

                payloads.forEach { payload ->
                    val bundleItemId: Long = bundleItemEntityDao().insert(createBundleItem(credentialId, payload))

                    val keyBinding = matchKeyBindingToPayloadCnf(
                        keyBindings = keyBindings,
                        payload = payload,
                    ).getOrThrow { IllegalStateException("No payload found for keyBinding") }

                    createCredentialKeyBinding(credentialId, keyBinding, bundleItemId)?.let { keyBindingEntity ->
                        credentialKeyBindingDao().update(keyBindingEntity)
                    }
                }

                clusters.forEach { cluster ->
                    saveCluster(cluster, credentialId, null)
                }

                credentialId
            } ?: error("saveCredentialFromDeferred: transaction failed")
        }.mapError { throwable ->
            throwable.toCredentialOfferRepositoryError("saveCredentialFromDeferred error")
        }
    }

    private suspend fun saveCredentialOfferTransaction(
        credentialId: Long = 0L,
        format: CredentialFormat,
        selectedConfigurationId: String,
        issuerDisplays: List<AnyIssuerDisplay>,
        credentialDisplays: List<AnyCredentialDisplay>,
        rawCredentialData: RawCredentialData,
        issuerUrl: URL,
        additionalTask: suspend (credentialId: Long) -> Unit,
    ): Result<Long, Throwable> = withContext(ioDispatcher) {
        runSuspendCatching {
            runInTransaction {
                val credentialId = credentialDao().insert(
                    Credential(
                        id = credentialId,
                        format = format,
                        issuerUrl = issuerUrl,
                        selectedConfigurationId = selectedConfigurationId,
                    )
                )

                val issuerDisplays = createCredentialIssuerDisplays(issuerDisplays, credentialId)
                credentialIssuerDisplayDao().insertAll(issuerDisplays)

                val credDisplays = createCredentialDisplays(credentialDisplays, credentialId)
                credentialDisplayDao().insertAll(credDisplays)

                rawCredentialDataDao().insert(rawCredentialData = rawCredentialData.copy(credentialId = credentialId))

                additionalTask(credentialId)

                credentialId
            } ?: error("credential id == null")
        }
    }

    private fun createVerifiableCredential(
        credentialId: Long,
        validFrom: Long?,
        validUntil: Long?,
        issuer: String?,
    ) = VerifiableCredentialEntity(
        issuer = issuer,
        validFrom = validFrom,
        validUntil = validUntil,
        credentialId = credentialId,
    )

    private fun createBundleItem(
        credentialId: Long,
        payload: String,
    ) = BundleItemEntity(
        credentialId = credentialId,
        payload = payload,
    )

    private fun createCredentialKeyBinding(
        credentialId: Long,
        keyBinding: KeyBinding?,
        bundleItemId: Long? = null
    ) = keyBinding?.let {
        CredentialKeyBindingEntity(
            id = it.identifier,
            credentialId = credentialId,
            bundleItemId = bundleItemId,
            algorithm = it.algorithm.stdName,
            bindingType = it.bindingType,
            publicKey = it.publicKey,
            privateKey = it.privateKey,
        )
    }

    private fun CredentialKeyBindingEntity.toKeyBinding(): KeyBinding = KeyBinding(
        identifier = id,
        algorithm = SigningAlgorithm.valueOf(algorithm),
        bindingType = bindingType,
        publicKey = publicKey,
        privateKey = privateKey
    )

    private fun createCredentialIssuerDisplays(
        issuerDisplays: List<AnyIssuerDisplay>,
        credentialId: Long,
    ) = issuerDisplays.map { display ->
        CredentialIssuerDisplay(
            credentialId = credentialId,
            name = display.name ?: DisplayConst.ISSUER_FALLBACK_NAME,
            image = display.logo,
            imageAltText = display.logoAltText,
            locale = display.locale ?: DisplayLanguage.UNKNOWN,
        )
    }

    private fun createCredentialDisplays(
        credentialDisplays: List<AnyCredentialDisplay>,
        credentialId: Long,
    ) = credentialDisplays.map { display ->
        CredentialDisplay(
            credentialId = credentialId,
            locale = display.locale ?: DisplayLanguage.UNKNOWN,
            name = display.name,
            description = display.description,
            logoUri = display.logo,
            logoAltText = display.logoAltText,
            backgroundColor = display.backgroundColor,
            theme = display.theme
        )
    }

    private fun createClusterDisplayEntities(
        clusterDisplays: List<ClusterDisplay>,
        clusterId: Long
    ) = clusterDisplays.map { clusterDisplay ->
        clusterDisplay.toCredentialClaimClusterDisplayEntity(clusterId)
    }

    private fun createCredentialClaims(
        claims: Map<CredentialClaim, List<AnyClaimDisplay>>
    ): Map<CredentialClaim, List<CredentialClaimDisplay>> = claims.map { (claim, claimDisplays) ->
        claim to claimDisplays.map { display ->
            CredentialClaimDisplay(
                // at this point we do not have the claim id yet, since it will only be known after the claim was inserted in the db
                claimId = -1,
                name = display.name,
                locale = display.locale ?: DisplayLanguage.UNKNOWN,
                value = display.value,
            )
        }
    }.toMap()

    private suspend fun saveCluster(cluster: Cluster, credentialId: Long, parentClusterId: Long?) {
        val clusterId = credentialClaimClusterEntityDao().insert(cluster.toCredentialClaimClusterEntity(credentialId, parentClusterId))

        val clusterDisplays = createClusterDisplayEntities(cluster.clusterDisplays, clusterId)
        credentialClaimClusterDisplayEntityDao().insertAll(clusterDisplays)

        val claims = createCredentialClaims(cluster.claims)
        claims.forEach { (claim, claimDisplays) ->
            val claimToSave = claim.copy(clusterId = clusterId)
            val claimId = credentialClaimDao().insert(claimToSave)
            val displays = claimDisplays.map { it.copy(claimId = claimId) }
            credentialClaimDisplayDao().insertAll(displays)
        }

        cluster.childClusters.forEach { childCluster ->
            saveCluster(cluster = childCluster, credentialId = credentialId, parentClusterId = clusterId)
        }
    }

    private val credentialDaoFlow = daoProvider.credentialDaoFlow
    private suspend fun credentialDao(): CredentialDao = suspendUntilNonNull {
        credentialDaoFlow.value
    }

    private val verifiableCredentialDaoFlow = daoProvider.verifiableCredentialDaoFlow
    private suspend fun verifiableCredentialDao(): VerifiableCredentialDao = suspendUntilNonNull {
        verifiableCredentialDaoFlow.value
    }

    private val deferredCredentialDaoFlow = daoProvider.deferredCredentialDao
    private suspend fun deferredCredentialDao(): DeferredCredentialDao = suspendUntilNonNull {
        deferredCredentialDaoFlow.value
    }

    private val bundleItemEntityDaoFlow = daoProvider.bundleItemEntityDaoFlow
    private suspend fun bundleItemEntityDao(): BundleItemEntityDao = suspendUntilNonNull {
        bundleItemEntityDaoFlow.value
    }

    private val credentialKeyBindingEntityDaoFlow = daoProvider.credentialKeyBindingEntityDaoFlow
    private suspend fun credentialKeyBindingDao(): CredentialKeyBindingEntityDao = suspendUntilNonNull {
        credentialKeyBindingEntityDaoFlow.value
    }

    private val credentialDisplayDaoFlow = daoProvider.credentialDisplayDaoFlow
    private suspend fun credentialDisplayDao(): CredentialDisplayDao = suspendUntilNonNull {
        credentialDisplayDaoFlow.value
    }

    private val rawCredentialDataDao = daoProvider.rawCredentialDataDao
    private suspend fun rawCredentialDataDao(): RawCredentialDataDao = suspendUntilNonNull { rawCredentialDataDao.value }

    private val credentialIssuerDisplayDaoFlow = daoProvider.credentialIssuerDisplayDaoFlow
    private suspend fun credentialIssuerDisplayDao(): CredentialIssuerDisplayDao = suspendUntilNonNull {
        credentialIssuerDisplayDaoFlow.value
    }

    private val credentialClaimDaoFlow = daoProvider.credentialClaimDaoFlow
    private suspend fun credentialClaimDao(): CredentialClaimDao = suspendUntilNonNull { credentialClaimDaoFlow.value }

    private val credentialClaimDisplayDaoFlow = daoProvider.credentialClaimDisplayDaoFlow
    private suspend fun credentialClaimDisplayDao(): CredentialClaimDisplayDao = suspendUntilNonNull {
        credentialClaimDisplayDaoFlow.value
    }

    private val credentialClaimClusterEntityDaoFlow = daoProvider.credentialClaimClusterEntityDao
    private suspend fun credentialClaimClusterEntityDao(): CredentialClaimClusterEntityDao = suspendUntilNonNull {
        credentialClaimClusterEntityDaoFlow.value
    }

    private val credentialClaimClusterDisplayEntityDaoFlow = daoProvider.credentialClaimClusterDisplayEntityDao
    private suspend fun credentialClaimClusterDisplayEntityDao(): CredentialClaimClusterDisplayEntityDao = suspendUntilNonNull {
        credentialClaimClusterDisplayEntityDaoFlow.value
    }
}
