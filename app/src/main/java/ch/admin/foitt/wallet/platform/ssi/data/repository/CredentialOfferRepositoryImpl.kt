package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.TokenType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.database.data.dao.BundleItemEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialActivityEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialAuthenticationDao
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
import ch.admin.foitt.wallet.platform.database.data.dao.DpopBindingDao
import ch.admin.foitt.wallet.platform.database.data.dao.ImageEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.RawCredentialDataDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialDao
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.ClusterDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplayEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplayEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialKeyBindingEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.database.domain.model.DpopBindingEntity
import ch.admin.foitt.wallet.platform.database.domain.model.ImageEntity
import ch.admin.foitt.wallet.platform.database.domain.model.RawCredentialData
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.database.domain.model.toCredentialClaimClusterDisplayEntity
import ch.admin.foitt.wallet.platform.database.domain.model.toCredentialClaimClusterEntity
import ch.admin.foitt.wallet.platform.database.domain.usecase.RunInTransaction
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.keybindingMatching.domain.usecase.MatchKeyBindingToPayloadCnf
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedCredentialInformationDisplay
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundlerError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaBundler
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toCredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.utils.SafeJson
import ch.admin.foitt.wallet.platform.utils.decompress
import ch.admin.foitt.wallet.platform.utils.domain.usecase.GetImageDataFromUri
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.getOrThrow
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URL
import java.security.MessageDigest
import java.time.Instant
import javax.inject.Inject

class CredentialOfferRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val getLocalizedCredentialInformationDisplay: GetLocalizedCredentialInformationDisplay,
    private val runInTransaction: RunInTransaction,
    private val matchKeyBindingToPayloadCnf: MatchKeyBindingToPayloadCnf,
    private val getImageDataFromUri: GetImageDataFromUri,
    private val safeJson: SafeJson,
    private val ocaBundler: OcaBundler,
) : CredentialOfferRepository {

    override suspend fun saveCredentialOffer(
        credentialId: Long,
        keyBindings: List<KeyBinding>?,
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
        Timber.d("Save credential offer: Starting for credential $credentialId")
        saveCredentialOfferTransaction(
            credentialId = credentialId,
            format = format,
            issuerUrl = issuerUrl,
            selectedConfigurationId = selectedConfigurationId,
            issuerDisplays = issuerDisplays,
            credentialDisplays = credentialDisplays,
            rawCredentialData = rawCredentialData,
        ) { credentialId ->
            val existing = verifiableCredentialDao().getByIdOrNull(credentialId)

            val verifiableCredential = VerifiableCredentialEntity(
                credentialId = credentialId,
                validFrom = validFrom,
                validUntil = validUntil,
                issuer = issuer,
                progressionState = existing?.progressionState ?: VerifiableProgressionState.UNACCEPTED,
                createdAt = existing?.createdAt ?: Instant.now().epochSecond,
                refreshedAt = if (existing != null) Instant.now().epochSecond else null,
                nextPresentableBundleItemId = -1
            )

            verifiableCredentialDao().upsert(verifiableCredential)

            Timber.d("Save credential offer: saved credential:\n$verifiableCredential")

            val previousStatus = bundleItemEntityDao().getAllByCredentialId(credentialId).firstOrNull()?.status ?: CredentialStatus.UNKNOWN

            payloads.forEachIndexed { index, payload ->
                val bundleItemId: Long = bundleItemEntityDao().insert(createBundleItem(credentialId, payload, previousStatus))

                if (index == 0) {
                    verifiableCredentialDao().updateNextBundleIdByCredentialId(
                        credentialId = credentialId,
                        nextPresentableBundleItemId = bundleItemId,
                    )
                }

                keyBindings?.let {
                    val keyBindingEntity = getKeyBindingEntity(
                        keyBindings = it,
                        payload = payload,
                        credentialId = credentialId,
                        bundleItemId = bundleItemId,
                    )
                    credentialKeyBindingDao().insert(keyBindingEntity)
                }
            }

            val deletedActivities = credentialActivityEntityDao().deleteByCredentialId(credentialId)
            val deletedClusters = credentialClaimClusterEntityDao().deleteByCredentialId(credentialId)

            Timber.d("Save credential offer: deleted $deletedClusters existing clusters and $deletedActivities existing activities")
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

    private suspend fun getKeyBindingEntity(
        keyBindings: List<KeyBinding>,
        payload: String,
        credentialId: Long,
        bundleItemId: Long,
    ): CredentialKeyBindingEntity {
        val keyBinding = matchKeyBindingToPayloadCnf(
            keyBindings = keyBindings,
            payload = payload
        ).getOrThrow { IllegalStateException("No payload found for keyBinding") }

        return createCredentialKeyBinding(credentialId, keyBinding, bundleItemId)
    }

    override suspend fun saveDeferredCredentialOffer(
        transactionId: String,
        accessToken: String,
        tokenType: TokenType,
        refreshToken: String?,
        endpoint: URL,
        pollInterval: Int,
        keyBindings: List<KeyBinding>?,
        dpopKeyBinding: KeyBinding?,
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
            val keyBindingEntity = createCredentialKeyBinding(credentialId, keyBinding)
            credentialKeyBindingDao().insert(keyBindingEntity)
        }
        deferredCredentialDao().insert(
            DeferredCredentialEntity(
                credentialId = credentialId,
                transactionId = transactionId,
                endpoint = endpoint.toExternalForm(),
                pollInterval = pollInterval,
            )
        )
        val credentialAuthenticationId = credentialAuthenticationDao().insert(
            CredentialAuthenticationEntity(
                credentialId = credentialId,
                tokenType = tokenType,
                accessToken = accessToken,
                refreshToken = refreshToken,
            )
        )
        dpopKeyBinding?.let { keyBinding ->
            dpopBindingDao().insert(
                DpopBindingEntity(
                    id = keyBinding.identifier,
                    credentialAuthenticationId = credentialAuthenticationId,
                    algorithm = keyBinding.algorithm.name,
                    bindingType = keyBinding.bindingType,
                    publicKey = keyBinding.publicKey,
                    privateKey = keyBinding.privateKey,
                )
            )
        }
    }.mapError { throwable ->
        throwable.toCredentialOfferRepositoryError("saveDeferredCredentialOffer error")
    }

    override suspend fun updateDeferredCredentialMetaData(
        credentialId: Long,
        issuerDisplays: List<AnyIssuerDisplay>,
        credentialDisplays: List<AnyCredentialDisplay>,
        rawMetadata: ByteArray
    ): Result<Unit, CredentialOfferRepositoryError> = withContext(ioDispatcher) {
        runSuspendCatching {
            val uriToImageData = prefetchImages(issuerDisplays, credentialDisplays)

            runInTransaction {
                val uriToHash = insertImagesAndGetHashes(uriToImageData)

                credentialIssuerDisplayDao().deleteByCredentialId(credentialId)
                val issuerDisplayEntities = createCredentialIssuerDisplays(issuerDisplays, credentialId, uriToHash)
                credentialIssuerDisplayDao().insertAll(issuerDisplayEntities)

                credentialDisplayDao().deleteByCredentialId(credentialId)
                val credDisplayEntities = createCredentialDisplays(credentialDisplays, credentialId, uriToHash)
                credentialDisplayDao().insertAll(credDisplayEntities)

                rawCredentialDataDao().updateMetadataByCredentialId(credentialId, rawMetadata)
                Unit
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
            val uriToImageData = prefetchImages(issuerDisplays, credentialDisplays)

            runInTransaction {
                val uriToHash = insertImagesAndGetHashes(uriToImageData)

                credentialIssuerDisplayDao().deleteByCredentialId(credentialId)
                val issuerDisplayEntities = createCredentialIssuerDisplays(issuerDisplays, credentialId, uriToHash)
                credentialIssuerDisplayDao().insertAll(issuerDisplayEntities)

                credentialDisplayDao().deleteByCredentialId(credentialId)
                val credDisplayEntities = createCredentialDisplays(credentialDisplays, credentialId, uriToHash)
                credentialDisplayDao().insertAll(credDisplayEntities)

                rawCredentialDataDao().deleteByCredentialId(credentialId)
                rawCredentialDataDao().insert(rawCredentialData = rawCredentialData)

                deferredCredentialDao().deleteById(credentialId)

                // if this is empty (and therefore null) it was a deferred credential without holder binding
                val keyBindings = credentialKeyBindingDao().getByCredentialId(credentialId).map {
                    it.toKeyBinding()
                }.ifEmpty { null }

                val verifiableCredential = VerifiableCredentialEntity(
                    credentialId = credentialId,
                    validFrom = validFrom,
                    validUntil = validUntil,
                    issuer = issuer,
                    progressionState = VerifiableProgressionState.UNACCEPTED,
                    nextPresentableBundleItemId = -1,
                )
                verifiableCredentialDao().upsert(verifiableCredential)

                payloads.forEachIndexed { index, payload ->
                    val bundleItemId: Long = bundleItemEntityDao().insert(createBundleItem(credentialId, payload))

                    if (index == 0) {
                        verifiableCredentialDao().updateNextBundleIdByCredentialId(
                            credentialId = credentialId,
                            nextPresentableBundleItemId = bundleItemId,
                        )
                    }

                    keyBindings?.let {
                        val keyBindingEntity = getKeyBindingEntity(
                            keyBindings = it,
                            payload = payload,
                            credentialId = credentialId,
                            bundleItemId = bundleItemId,
                        )
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

    override suspend fun refreshCachedImages(credentialId: Long): Result<Unit, Throwable> = withContext(ioDispatcher) {
        runSuspendCatching {
            val issuerDisplaysInDb = credentialIssuerDisplayDao().getCredentialIssuerDisplaysById(credentialId)
            val credDisplaysInDb = credentialDisplayDao().getByCredentialId(credentialId)

            val isAnythingMissing = issuerDisplaysInDb.any { it.image == null && it.locale != DisplayLanguage.FALLBACK } ||
                credDisplaysInDb.any { it.logo == null && it.locale != DisplayLanguage.FALLBACK }
            if (!isAnythingMissing) return@runSuspendCatching

            val rawData = rawCredentialDataDao().getRawCredentialDataByCredentialId(credentialId).firstOrNull()
                ?: error("No raw data found for credential $credentialId")
            val issuerInfo = parseIssuerInfo(rawData) ?: error("Failed to parse issuer info")

            val missingIssuerImages = getMissingIssuerImages(issuerDisplaysInDb, issuerInfo)
            // Mirrors the save path: a bundle that cannot be read falls back to the metadata
            // displays, so the metadata logos are the correct source for them here too.
            val ocaBundle = parseOcaBundle(rawData).getOrElse { error ->
                Timber.w("OCA bundle unreadable for credential $credentialId, using metadata images: $error")
                null
            }
            val missingCredImages = getMissingCredentialImages(credentialId, credDisplaysInDb, issuerInfo, ocaBundle)

            if (missingIssuerImages.isEmpty() && missingCredImages.isEmpty()) return@runSuspendCatching

            val allUris = (missingIssuerImages.map { it.second } + missingCredImages.map { it.second }).distinct()
            val uriToImageData = allUris.mapNotNull { uri ->
                getImageDataFromUri(uri)?.let { uri to it }
            }.toMap()

            updateDisplayHashes(uriToImageData, missingIssuerImages, missingCredImages)
        }
    }

    private fun getMissingIssuerImages(
        issuerDisplaysInDb: List<CredentialIssuerDisplay>,
        issuerInfo: IssuerCredentialInfo
    ): List<Pair<Long, String>> {
        return issuerDisplaysInDb
            .filter { it.image == null && it.locale != DisplayLanguage.FALLBACK }
            .mapNotNull { display ->
                val uri = getLocalizedCredentialInformationDisplay(
                    displays = issuerInfo.display ?: emptyList(),
                    preferredLocaleString = display.locale
                )?.logo?.uri
                if (uri != null) display.entity.id to uri else null
            }
    }

    private suspend fun getMissingCredentialImages(
        credentialId: Long,
        credDisplaysInDb: List<CredentialDisplay>,
        issuerInfo: IssuerCredentialInfo,
        ocaBundle: OcaBundle?
    ): List<Pair<Long, String>> {
        val credentialInDb = credentialDao().getById(credentialId)
        val config = issuerInfo.credentialConfigurations.find { it.identifier == credentialInDb.selectedConfigurationId }
            ?: issuerInfo.credentialConfigurations.find { it.format == credentialInDb.format }
            ?: issuerInfo.credentialConfigurations.firstOrNull()

        return credDisplaysInDb.filter { it.logo == null && it.locale != DisplayLanguage.FALLBACK }.mapNotNull { display ->
            val uri = if (ocaBundle != null) {
                ocaBundle.ocaCredentialData.firstOrNull {
                    it.locale == display.locale && it.theme == display.entity.theme
                }?.logoData
            } else {
                config?.credentialMetadata?.display?.find { it.locale == display.locale }?.logo?.uri
            }

            if (uri != null) display.entity.id to uri else null
        }
    }

    private suspend fun updateDisplayHashes(
        uriToImageData: Map<String, ByteArray>,
        missingIssuerImages: List<Pair<Long, String>>,
        missingCredImages: List<Pair<Long, String>>
    ) {
        runInTransaction {
            val uriToHash = insertImagesAndGetHashes(uriToImageData)

            missingIssuerImages.forEach { (dbId, uri) ->
                uriToHash[uri]?.let { hash ->
                    credentialIssuerDisplayDao().updateImageHash(dbId, hash)
                }
            }

            missingCredImages.forEach { (dbId, uri) ->
                uriToHash[uri]?.let { hash ->
                    credentialDisplayDao().updateLogoHash(dbId, hash)
                }
            }
        }
    }

    private fun parseIssuerInfo(rawData: RawCredentialData): IssuerCredentialInfo? {
        val rawIssuerCredentialInfo = rawData.rawOIDMetadata?.decompress()?.decodeToString()
            ?: return null

        return safeJson.safeDecodeStringTo<IssuerCredentialInfo>(rawIssuerCredentialInfo).onErr {
            Timber.w("Failed to parse issuer info from raw credential data")
        }.getOrElse { null }
    }

    private suspend fun parseOcaBundle(rawData: RawCredentialData): Result<OcaBundle?, OcaBundlerError> {
        val rawOcaBundle = rawData.rawOcaBundle?.decompress()?.decodeToString()
            ?: return Ok(null) // No bundle: the credential displays legitimately come from metadata

        return ocaBundler(rawOcaBundle)
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
            val uriToImageData = prefetchImages(issuerDisplays, credentialDisplays)

            runInTransaction {
                val uriToHash = insertImagesAndGetHashes(uriToImageData)

                val credentialId = if (credentialId == 0L) {
                    credentialDao().insert(
                        Credential(
                            format = format,
                            issuerUrl = issuerUrl,
                            selectedConfigurationId = selectedConfigurationId,
                        )
                    )
                } else {
                    // Updating an existing credential must not reset its creation date
                    credentialDao().update(
                        credentialDao().getById(credentialId).copy(
                            format = format,
                            issuerUrl = issuerUrl,
                            selectedConfigurationId = selectedConfigurationId,
                        )
                    )
                    credentialId
                }

                credentialIssuerDisplayDao().deleteByCredentialId(credentialId)
                val issuerDisplayEntities = createCredentialIssuerDisplays(issuerDisplays, credentialId, uriToHash)
                credentialIssuerDisplayDao().insertAll(issuerDisplayEntities)

                credentialDisplayDao().deleteByCredentialId(credentialId)
                val credDisplayEntities = createCredentialDisplays(credentialDisplays, credentialId, uriToHash)
                credentialDisplayDao().insertAll(credDisplayEntities)

                rawCredentialDataDao().deleteByCredentialId(credentialId)
                rawCredentialDataDao().insert(rawCredentialData = rawCredentialData.copy(credentialId = credentialId))

                additionalTask(credentialId)

                credentialId
            } ?: error("credential id == null")
        }
    }

    private suspend fun prefetchImages(
        issuerDisplays: List<AnyIssuerDisplay>,
        credentialDisplays: List<AnyCredentialDisplay>,
    ): Map<String, ByteArray> {
        val allUris = (issuerDisplays.mapNotNull { it.logo } + credentialDisplays.mapNotNull { it.logo }).distinct()
        return allUris.mapNotNull { uri ->
            getImageDataFromUri(uri)?.let { uri to it }
        }.toMap()
    }

    private suspend fun insertImagesAndGetHashes(
        uriToImageData: Map<String, ByteArray>,
    ): Map<String, String> {
        val uriToHash = mutableMapOf<String, String>()
        val newImageEntities = mutableListOf<ImageEntity>()

        uriToImageData.forEach { (uri, imageData) ->
            val hash = MessageDigest.getInstance("SHA-256").digest(imageData).toHexString()
            uriToHash[uri] = hash
            newImageEntities.add(ImageEntity(hash = hash, image = imageData))
        }

        imageDao().insertAll(newImageEntities)
        return uriToHash
    }

    private fun createCredentialIssuerDisplays(
        issuerDisplays: List<AnyIssuerDisplay>,
        credentialId: Long,
        uriToHash: Map<String, String>,
    ) = issuerDisplays.map { display ->
        CredentialIssuerDisplayEntity(
            credentialId = credentialId,
            name = display.name,
            imageHash = uriToHash[display.logo],
            imageAltText = display.logoAltText,
            locale = display.locale ?: DisplayLanguage.UNKNOWN,
        )
    }

    private fun createCredentialDisplays(
        credentialDisplays: List<AnyCredentialDisplay>,
        credentialId: Long,
        uriToHash: Map<String, String>,
    ) = credentialDisplays.map { display ->
        CredentialDisplayEntity(
            credentialId = credentialId,
            locale = display.locale ?: DisplayLanguage.UNKNOWN,
            name = display.name,
            description = display.description,
            logoHash = uriToHash[display.logo],
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

    private fun createBundleItem(
        credentialId: Long,
        payload: String,
        status: CredentialStatus = CredentialStatus.UNKNOWN,
    ) = BundleItemEntity(
        credentialId = credentialId,
        payload = payload,
        status = status,
    )

    private fun createCredentialKeyBinding(
        credentialId: Long,
        keyBinding: KeyBinding,
        bundleItemId: Long? = null
    ) = CredentialKeyBindingEntity(
        id = keyBinding.identifier,
        credentialId = credentialId,
        bundleItemId = bundleItemId,
        algorithm = keyBinding.algorithm.stdName,
        bindingType = keyBinding.bindingType,
        publicKey = keyBinding.publicKey,
        privateKey = keyBinding.privateKey,
    )

    private fun CredentialKeyBindingEntity.toKeyBinding(): KeyBinding = KeyBinding(
        identifier = id,
        algorithm = SigningAlgorithm.valueOf(algorithm),
        bindingType = bindingType,
        publicKey = publicKey,
        privateKey = privateKey
    )

    private val credentialDaoFlow = daoProvider.credentialDaoFlow
    private suspend fun credentialDao(): CredentialDao = suspendUntilNonNull { credentialDaoFlow.value }

    private val verifiableCredentialDaoFlow = daoProvider.verifiableCredentialDaoFlow
    private suspend fun verifiableCredentialDao(): VerifiableCredentialDao = suspendUntilNonNull {
        verifiableCredentialDaoFlow.value
    }

    private val deferredCredentialDaoFlow = daoProvider.deferredCredentialDao
    private suspend fun deferredCredentialDao(): DeferredCredentialDao = suspendUntilNonNull {
        deferredCredentialDaoFlow.value
    }

    private val credentialAuthenticationDaoFlow = daoProvider.credentialAuthenticationDaoFlow
    private suspend fun credentialAuthenticationDao(): CredentialAuthenticationDao = suspendUntilNonNull {
        credentialAuthenticationDaoFlow.value
    }

    private val dpopBindingDaoFlow = daoProvider.dpopBindingDaoFlow
    private suspend fun dpopBindingDao(): DpopBindingDao = suspendUntilNonNull {
        dpopBindingDaoFlow.value
    }

    private val bundleItemEntityDaoFlow = daoProvider.bundleItemEntityDaoFlow
    private suspend fun bundleItemEntityDao(): BundleItemEntityDao = suspendUntilNonNull { bundleItemEntityDaoFlow.value }

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

    private val credentialActivityEntitiyDaoFlow = daoProvider.credentialActivityEntityDao
    private suspend fun credentialActivityEntityDao(): CredentialActivityEntityDao = suspendUntilNonNull {
        credentialActivityEntitiyDaoFlow.value
    }

    private val imageEntityDaoFlow = daoProvider.imageEntityDao
    private suspend fun imageDao(): ImageEntityDao = suspendUntilNonNull { imageEntityDaoFlow.value }
}
