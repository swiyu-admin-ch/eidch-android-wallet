package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.GenerateDPoPKeyPairError
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredentialResult
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyDeferredCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialByConfigError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.GetSignedMetadataDidError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.GetVerifiableCredentialParamsError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.Grant
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.GetSignedMetadataDid
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.model.RefreshCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toFetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toRefreshCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.EvaluateBatchSize
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAllAnyCredentialsByCredentialId
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetCredentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshCredential
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.CredentialRefreshDataError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.GetBindingKeyPairError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.usecase.GetBindingKeyPair
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationWithDpopBinding
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithAuthentication
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.GenerateProofKeyPairError
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateDPoPKeyPair
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateProofKeyPairs
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.GetPayloadEncryptionTypeError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryption
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeleteBundleItemError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteBundleItems
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteKeyStoreEntry
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessIdentityTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityTrustStatement
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetSwissProfileStatus
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import kotlinx.coroutines.sync.Mutex
import timber.log.Timber
import java.net.URL
import java.time.Instant
import javax.inject.Inject

internal class RefreshCredentialImpl @Inject constructor(
    private val bundleItemRepository: BundleItemRepository,
    private val credentialRefreshDataRepository: CredentialRefreshDataRepository,
    private val getCredentialConfig: GetCredentialConfig,
    private val getPayloadEncryption: GetPayloadEncryption,
    private val fetchRawAndParsedIssuerCredentialInfo: FetchRawAndParsedIssuerCredentialInfo,
    private val getVerifiableCredentialParams: GetVerifiableCredentialParams,
    private val evaluateBatchSize: EvaluateBatchSize,
    private val getSignedMetadataDid: GetSignedMetadataDid,
    private val generateProofKeyPairs: GenerateProofKeyPairs,
    private val processIdentityTrustStatement: ProcessIdentityTrustStatement,
    private val fetchCredentialByConfig: FetchCredentialByConfig,
    private val getBindingKeyPair: GetBindingKeyPair,
    private val handleCredentialResult: HandleCredentialResult,
    private val handleBatchCredentialResult: HandleBatchCredentialResult,
    private val deleteBundleItems: DeleteBundleItems,
    private val generateDPoPKeyPair: GenerateDPoPKeyPair,
    private val deleteKeyStoreEntry: DeleteKeyStoreEntry,
    private val verifiableCredentialRepository: VerifiableCredentialRepository,
    private val getAllAnyCredentialsByCredentialId: GetAllAnyCredentialsByCredentialId,
    private val getSwissProfileStatus: GetSwissProfileStatus,
) : RefreshCredential {
    private val refreshMutex = Mutex()

    /**
     * Skips silently when a refresh is already running, so overlapping triggers (e.g. after login and on home
     * resume) cannot start a second refresh for the same credential. This requires the use case to be a shared
     * (ActivityRetainedScoped) instance so the mutex is shared across callers.
     */
    override suspend fun invoke(credentialId: Long): Result<Unit, RefreshCredentialError> {
        if (!refreshMutex.tryLock()) {
            Timber.d(message = "Credential refresh already in flight, skipping")
            return Ok(Unit)
        }
        return try {
            refresh(credentialId)
        } finally {
            refreshMutex.unlock()
        }
    }

    private suspend fun refresh(credentialId: Long): Result<Unit, RefreshCredentialError> = coroutineBinding {
        val credentialToRefresh = credentialRefreshDataRepository.getCredentialRefreshDataById(credentialId)
            .mapError(CredentialRefreshDataError::toRefreshCredentialError).bind()
        val credentialEntity = credentialToRefresh.credential

        val existingBundleItems = bundleItemRepository.getAllByCredentialId(credentialId)
            .mapError { it.toFetchCredentialError().toRefreshCredentialError() }.bind()
        val isRevoked = existingBundleItems.firstOrNull()?.status == CredentialStatus.REVOKED

        if (!canBeRefreshed(credentialToRefresh, isRevoked)) {
            Timber.d(message = "RefreshCredential: credential $credentialId cannot be refreshed, skipping")
            return@coroutineBinding
        }

        credentialToRefresh.authentication?.refreshToken?.let { refreshToken ->
            val credentialOffer = CredentialOffer(
                credentialIssuer = credentialEntity.issuerUrl,
                credentialConfigurationIds = credentialEntity.selectedConfigurationId?.let { listOf(it) } ?: listOf(),
                grants = Grant(
                    refreshToken = refreshToken
                )
            )

            refreshAndSaveCredential(
                credentialId = credentialId,
                authentication = credentialToRefresh.authentication,
                credentialOffer = credentialOffer,
                oldBundleItems = existingBundleItems,
            )
                .onOk { result -> if (result != null) recordRefreshAttempt(credentialId) }
                .onErr { recordRefreshAttempt(credentialId) }
                .mapError(FetchCredentialError::toRefreshCredentialError).bind()
        }
    }

    private fun canBeRefreshed(
        credential: VerifiableCredentialWithAuthentication,
        isRevoked: Boolean,
    ): Boolean =
        credential.verifiableCredential.progressionState == VerifiableProgressionState.ACCEPTED &&
            credential.authentication?.refreshToken != null &&
            !isRevoked

    private suspend fun refreshAndSaveCredential(
        credentialId: Long,
        authentication: CredentialAuthenticationWithDpopBinding,
        credentialOffer: CredentialOffer,
        oldBundleItems: List<BundleItemEntity>,
    ): Result<FetchCredentialResult?, FetchCredentialError> {
        val generatedKeyPairs = mutableListOf<BindingKeyPair>()
        return fetchAndSaveCredential(credentialId, authentication, credentialOffer, oldBundleItems, generatedKeyPairs)
            .onErr { deleteGeneratedHardwareKeys(generatedKeyPairs) }
    }

    private suspend fun fetchAndSaveCredential(
        credentialId: Long,
        authentication: CredentialAuthenticationWithDpopBinding,
        credentialOffer: CredentialOffer,
        oldBundleItems: List<BundleItemEntity>,
        generatedKeyPairs: MutableList<BindingKeyPair>,
    ): Result<FetchCredentialResult?, FetchCredentialError> = coroutineBinding {
        val rawAndParsedCredentialInfo = fetchRawAndParsedIssuerCredentialInfo(
            issuerEndpoint = credentialOffer.credentialIssuer,
            forceRefresh = true,
        ).mapError(FetchIssuerCredentialInfoError::toFetchCredentialError).bind()

        val issuerInfo = rawAndParsedCredentialInfo.issuerCredentialInfo
        val config = getCredentialConfig(
            credentials = credentialOffer.credentialConfigurationIds,
            credentialConfigurations = issuerInfo.credentialConfigurations
        ).bind()

        if (config.credentialRefreshDisabled) {
            Timber.d(message = "Refresh disabled for credential $credentialId, skipping")
            return@coroutineBinding null
        }

        val verifiableCredentialParams = getVerifiableCredentialParams(
            credentialConfiguration = config,
            credentialOffer = credentialOffer,
            issuerCredentialInfo = issuerInfo
        ).mapError(GetVerifiableCredentialParamsError::toFetchCredentialError).bind()

        val batchSize = if (verifiableCredentialParams.isBatch) {
            evaluateBatchSize(issuerInfo).bind()
        } else {
            1
        }

        val actorDid = getSignedMetadataDid(rawAndParsedCredentialInfo.rawIssuerCredentialInfo)
            .mapError(GetSignedMetadataDidError::toFetchCredentialError)
            .bind()

        val proofKeyPairs = verifiableCredentialParams.proofTypeConfig?.let { proofTypeConfig ->
            generateProofKeyPairs(
                amount = batchSize,
                proofTypeConfig = proofTypeConfig,
                actorDid = actorDid,
            ).mapError(GenerateProofKeyPairError::toFetchCredentialError)
                .onOk { generatedKeyPairs += it }
                .bind()
        }

        val payloadEncryption = getPayloadEncryption(
            requestEncryption = rawAndParsedCredentialInfo.issuerCredentialInfo.credentialRequestEncryption,
            responseEncryption = rawAndParsedCredentialInfo.issuerCredentialInfo.credentialResponseEncryption,
        ).mapError(GetPayloadEncryptionTypeError::toFetchCredentialError).bind()

        val dpopKeyPair = getBindingKeyPair(authentication)
            .onErr { error ->
                Timber.d(message = "Credential refresh: resolving the stored DPoP key pair failed: $error")
            }
            .mapError(GetBindingKeyPairError::toFetchCredentialError).bind()
            ?: generateDPoPKeyPair(verifiableCredentialParams, actorDid)
                .onErr { error ->
                    Timber.d(message = "Credential refresh: generating a DPoP key pair failed: $error")
                }
                .mapError(GenerateDPoPKeyPairError::toFetchCredentialError)
                .onOk { keyPair -> keyPair?.let { generatedKeyPairs += it } }
                .bind()

        val identityTrustStatement = processIdentityTrustStatement(issuerInfo.identityTrustStatement, actorDid)
            .mapError(ProcessIdentityTrustStatementError::toFetchCredentialError)
            .bind()

        val anyCredentialResult = fetchCredentialByConfig(
            verifiableCredentialParams = verifiableCredentialParams,
            bindingKeyPairs = proofKeyPairs,
            payloadEncryption = payloadEncryption,
            dpopKeyPair = dpopKeyPair,
        ).onErr { error ->
            Timber.d(message = "Credential refresh: fetching the credential failed: $error")
        }.mapError(FetchCredentialByConfigError::toFetchCredentialError).bind()

        if (!shouldKeepRefreshedCredential(credentialId, anyCredentialResult)) {
            Timber.w(
                message = "Discarding refreshed credential $credentialId because expired or swiss profile " +
                    "status is not newer than the current credential"
            )
            recordRefreshAttempt(credentialId)
            deleteGeneratedHardwareKeys(generatedKeyPairs)
            return@coroutineBinding null
        }

        val result = saveFetchedCredential(
            credentialId = credentialId,
            anyCredentialResult = anyCredentialResult,
            issuerUrl = credentialOffer.credentialIssuer,
            batchSize = batchSize,
            identityTrustStatement = identityTrustStatement,
            rawAndParsedCredentialInfo = rawAndParsedCredentialInfo,
            config = config,
        ).bind()

        generatedKeyPairs.clear()

        deleteBundleItems(oldBundleItems.map { it.id })
            .mapError(DeleteBundleItemError::toFetchCredentialError).bind()

        Timber.d(message = "Credential refreshed ${credentialOffer.credentialIssuer}")
        return@coroutineBinding result
    }

    private suspend fun shouldKeepRefreshedCredential(credentialId: Long, anyCredentialResult: AnyCredentialResult): Boolean {
        val newCredential = when (anyCredentialResult) {
            is AnyVerifiedCredential -> anyCredentialResult.vcSdJwtCredential
            is AnyVerifiedBatchCredential -> anyCredentialResult.vcSdJwtCredentials.firstOrNull()
            is AnyDeferredCredential -> null
        } ?: return true
        if (newCredential.validity is Validity.Expired) return false
        val currentCredential = getAllAnyCredentialsByCredentialId(credentialId).get()?.firstOrNull() ?: return true
        return isProfileStatusEqualOrNewer(newCredential.profileVersion, currentCredential.profileVersion)
    }

    private suspend fun isProfileStatusEqualOrNewer(newVersion: String?, currentVersion: String?): Boolean {
        val currentStatus = getSwissProfileStatus(currentVersion) ?: return true
        val newStatus = getSwissProfileStatus(newVersion) ?: return false
        return newStatus.freshness >= currentStatus.freshness
    }

    private suspend fun saveFetchedCredential(
        credentialId: Long,
        anyCredentialResult: AnyCredentialResult,
        issuerUrl: URL,
        batchSize: BatchSize,
        identityTrustStatement: IdentityV2TrustStatement?,
        rawAndParsedCredentialInfo: RawAndParsedIssuerCredentialInfo,
        config: AnyCredentialConfiguration,
    ): Result<FetchCredentialResult, FetchCredentialError> = coroutineBinding {
        when (anyCredentialResult) {
            is AnyVerifiedBatchCredential -> handleBatchCredentialResult(
                credentialId = credentialId,
                issuerUrl = issuerUrl,
                batchSize = batchSize,
                anyVerifiedBatchCredential = anyCredentialResult,
                identityTrustStatement = identityTrustStatement,
                rawAndParsedCredentialInfo = rawAndParsedCredentialInfo,
                credentialConfig = config,
            ).onErr { error ->
                Timber.d(message = "Credential refresh: saving the batch credential failed: $error")
            }.bind()

            is AnyDeferredCredential -> {
                val exception = IllegalStateException("RefreshCredential: unexpectedly received a deferred credential")
                Timber.e(t = exception)
                Err(CredentialError.Unexpected(exception)).bind()
            }

            is AnyVerifiedCredential -> handleCredentialResult(
                credentialId = credentialId,
                issuerUrl = issuerUrl,
                anyVerifiedCredential = anyCredentialResult,
                identityTrustStatement = identityTrustStatement,
                rawAndParsedCredentialInfo = rawAndParsedCredentialInfo,
                credentialConfig = config,
            ).onErr { error ->
                Timber.d(message = "Credential refresh: saving the credential failed: $error")
            }.bind()
        }
    }

    private suspend fun deleteGeneratedHardwareKeys(keyPairs: List<BindingKeyPair>) {
        keyPairs
            .filter { it.keyPair.bindingType == KeyBindingType.HARDWARE }
            .forEach { bindingKeyPair ->
                deleteKeyStoreEntry(bindingKeyPair.keyPair.keyId)
            }
    }

    private suspend fun recordRefreshAttempt(credentialId: Long) {
        verifiableCredentialRepository.updateLastRefreshAttemptAtByCredentialId(
            credentialId = credentialId,
            lastRefreshAttemptAt = Instant.now().epochSecond,
        )
    }
}
