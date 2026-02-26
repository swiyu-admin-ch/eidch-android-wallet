package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.CredentialType
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateCredentialRequestError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchDeferredCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequest
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyVcSdJwtSignature
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchExistingIssuerCredentialInfoError
import ch.admin.foitt.wallet.platform.credential.domain.model.GenerateCredentialDisplaysError
import ch.admin.foitt.wallet.platform.credential.domain.model.RefreshDeferredCredentialsError
import ch.admin.foitt.wallet.platform.credential.domain.model.toKeyBinding
import ch.admin.foitt.wallet.platform.credential.domain.model.toRefreshDeferredCredentialsError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchExistingIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchTrustForIssuance
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshDeferredCredentials
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.database.domain.model.RawCredentialData
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchVcMetadataByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaBundler
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.GetPayloadEncryptionTypeError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryptionType
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeferredCredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialRepository
import ch.admin.foitt.wallet.platform.utils.compress
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getOr
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository as OIDCredentialOfferRepository

class RefreshDeferredCredentialsImpl @Inject constructor(
    private val deferredCredentialRepository: DeferredCredentialRepository,
    private val fetchExistingIssuerCredentialInfo: FetchExistingIssuerCredentialInfo,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val getPayloadEncryptionType: GetPayloadEncryptionType,
    private val createCredentialRequest: CreateCredentialRequest,
    private val oidCredentialOfferRepository: OIDCredentialOfferRepository,
    private val verifyVcSdJwtSignature: VerifyVcSdJwtSignature,
    private val fetchVcMetadataByFormat: FetchVcMetadataByFormat,
    private val fetchTrustForIssuance: FetchTrustForIssuance,
    private val ocaBundler: OcaBundler,
    private val generateAnyDisplays: GenerateAnyDisplays,
    private val credentialOfferRepository: CredentialOfferRepository,
) : RefreshDeferredCredentials {
    override suspend fun invoke(): Result<Unit, RefreshDeferredCredentialsError> = coroutineBinding {
        Timber.d("Deferred refresh: starting refresh for all entries")

        val deferredCredentials = deferredCredentialRepository.getAll()
            .mapError(DeferredCredentialRepositoryError::toRefreshDeferredCredentialsError).bind()

        deferredCredentials.forEach { deferredCredential ->
            val deferredEntity = deferredCredential.deferredCredential
            if (!deferredEntity.shouldRefresh) {
                Timber.d("Deferred refresh: no refresh for ${deferredEntity.credentialId}, pollinterval ${deferredEntity.pollInterval}")
                return@forEach
            }

            val rawAndParsedCredentialInfo = fetchExistingIssuerCredentialInfo(credentialId = deferredEntity.credentialId)
                .mapError(FetchExistingIssuerCredentialInfoError::toRefreshDeferredCredentialsError)
                .bind()

            val payloadEncryptionType = if (environmentSetupRepository.payloadEncryptionEnabled) {
                getPayloadEncryptionType(
                    requestEncryption = rawAndParsedCredentialInfo.issuerCredentialInfo.credentialRequestEncryption,
                    responseEncryption = rawAndParsedCredentialInfo.issuerCredentialInfo.credentialResponseEncryption,
                ).mapError(GetPayloadEncryptionTypeError::toRefreshDeferredCredentialsError)
                    .bind()
            } else {
                PayloadEncryptionType.None
            }

            val credentialRequestType = createCredentialRequest(
                payloadEncryptionType = payloadEncryptionType,
                credentialType = CredentialType.Deferred(transactionId = deferredEntity.transactionId)
            ).mapError(CreateCredentialRequestError::toRefreshDeferredCredentialsError)
                .bind()

            oidCredentialOfferRepository.fetchDeferredCredential(
                issuerEndpoint = deferredEntity.endpoint,
                accessToken = deferredEntity.accessToken,
                credentialRequestType = credentialRequestType,
                payloadEncryptionType = payloadEncryptionType,
            ).onFailure { error ->
                handleDeferredError(
                    error = error,
                    deferredCredential = deferredCredential,
                )
            }.onSuccess { credentialResponse ->
                when (credentialResponse) {
                    is CredentialResponse.DeferredCredential -> handleDeferredCredential(
                        deferredCredentialEntity = deferredCredential,
                        credentialResponse = credentialResponse,
                        rawAndParsedIssuerCredentialInfo = rawAndParsedCredentialInfo,
                    ).onFailure {
                        invalidateCredential(deferredCredentialEntity = deferredCredential)
                    }

                    is CredentialResponse.VerifiableCredential -> handleCredential(
                        deferredCredentialEntity = deferredCredential,
                        credentialResponse = credentialResponse,
                        rawAndParsedIssuerCredentialInfo = rawAndParsedCredentialInfo,
                    ).onFailure { error ->
                        handleCredentialError(
                            deferredCredentialEntity = deferredCredential,
                            error = error,
                        )
                    }
                }
            }
        }
    }

    private val DeferredCredentialEntity.shouldRefresh: Boolean
        get() = progressionState == DeferredProgressionState.IN_PROGRESS &&
            (polledAt ?: 0L) + pollInterval.toLong() <= Instant.now().epochSecond

    private suspend fun handleDeferredCredential(
        deferredCredentialEntity: DeferredCredentialWithKeyBinding,
        credentialResponse: CredentialResponse.DeferredCredential,
        rawAndParsedIssuerCredentialInfo: RawAndParsedIssuerCredentialInfo,
    ): Result<Long, RefreshDeferredCredentialsError> = coroutineBinding {
        Timber.d("Deferred refresh: handle update for ${deferredCredentialEntity.credential.id}")
        if (credentialResponse.transactionId != deferredCredentialEntity.deferredCredential.transactionId) {
            Err(CredentialError.InvalidCredentialOffer).bind()
        }

        val credentialConfig = rawAndParsedIssuerCredentialInfo.issuerCredentialInfo.credentialConfigurations.firstOrNull {
            it.identifier == deferredCredentialEntity.credential.selectedConfigurationId
        } ?: Err(CredentialError.InvalidIssuerCredentialInfo).bind()

        // generate new credential & issuer displays
        val displays = generateAnyDisplays(
            anyCredential = null,
            issuerInfo = rawAndParsedIssuerCredentialInfo.issuerCredentialInfo,
            trustStatement = null,
            metadata = credentialConfig,
            ocaBundle = null,
        ).mapError(GenerateCredentialDisplaysError::toRefreshDeferredCredentialsError)
            .bind()

        credentialOfferRepository.updateDeferredCredentialOffer(
            credentialId = deferredCredentialEntity.credential.id,
            progressionState = DeferredProgressionState.IN_PROGRESS,
            polledAt = Instant.now().epochSecond,
            pollInterval = credentialResponse.interval,
            issuerDisplays = displays.issuerDisplays,
            credentialDisplays = displays.credentialDisplays,
            rawMetadata = rawAndParsedIssuerCredentialInfo.rawIssuerCredentialInfo.toByteArray().compress(),
        ).mapError(CredentialOfferRepositoryError::toRefreshDeferredCredentialsError)
            .bind()
    }

    private suspend fun handleDeferredError(
        error: FetchDeferredCredentialError,
        deferredCredential: DeferredCredentialWithKeyBinding,
    ) = when (error) {
        is CredentialOfferError.CredentialRequestDenied,
        is CredentialOfferError.InvalidRequest,
        is CredentialOfferError.InvalidTransactionId -> invalidateCredential(deferredCredential)

        is CredentialOfferError.NetworkInfoError -> {}
        is CredentialOfferError.Unexpected -> {
            Timber.w(t = error.cause, message = "Deferred refresh: fetch deferred failed")
        }
    }

    private suspend fun invalidateCredential(deferredCredentialEntity: DeferredCredentialWithKeyBinding) =
        deferredCredentialRepository.updateStatus(
            credentialId = deferredCredentialEntity.credential.id,
            progressionState = DeferredProgressionState.INVALID,
            polledAt = Instant.now().epochSecond,
            pollInterval = deferredCredentialEntity.deferredCredential.pollInterval,
        )

    private suspend fun handleCredential(
        deferredCredentialEntity: DeferredCredentialWithKeyBinding,
        credentialResponse: CredentialResponse.VerifiableCredential,
        rawAndParsedIssuerCredentialInfo: RawAndParsedIssuerCredentialInfo,
    ): Result<Long, RefreshDeferredCredentialsError> = coroutineBinding {
        Timber.d("Deferred refresh: handle getting credential for ${deferredCredentialEntity.deferredCredential.credentialId}")

        val anyCredential: AnyCredential = when (deferredCredentialEntity.credential.format) {
            CredentialFormat.VC_SD_JWT -> {
                val keyBinding: KeyBinding? = deferredCredentialEntity.keyBinding?.toKeyBinding()?.getOr(null)
                verifyVcSdJwtSignature(
                    keyBinding = keyBinding,
                    payload = credentialResponse.firstCredential,
                ).mapError { error ->
                    error.toRefreshDeferredCredentialsError()
                }.bind()
            }

            CredentialFormat.UNKNOWN -> {
                Err(CredentialError.UnsupportedCredentialFormat).bind()
            }
        }

        val vcMetadata = fetchVcMetadataByFormat(anyCredential)
            .mapError(FetchVcMetadataByFormatError::toRefreshDeferredCredentialsError).bind()

        val trustCheckResult = fetchTrustForIssuance(
            issuerDid = anyCredential.issuer,
            vcSchemaId = anyCredential.vcSchemaId,
        )

        val rawOcaBundle = vcMetadata.rawOcaBundle?.rawOcaBundle
        val ocaBundle = rawOcaBundle?.let {
            ocaBundler(it).get()
        }

        val credentialConfig = rawAndParsedIssuerCredentialInfo.issuerCredentialInfo.credentialConfigurations.firstOrNull {
            it.identifier == deferredCredentialEntity.credential.selectedConfigurationId
        } ?: run {
            invalidateCredential(deferredCredentialEntity)
            Err(CredentialError.InvalidCredentialOffer)
        }.bind()

        val displays = generateAnyDisplays(
            anyCredential = anyCredential,
            issuerInfo = rawAndParsedIssuerCredentialInfo.issuerCredentialInfo,
            trustStatement = trustCheckResult.actorTrustStatement,
            metadata = credentialConfig,
            ocaBundle = ocaBundle,
        ).mapError(GenerateCredentialDisplaysError::toRefreshDeferredCredentialsError).bind()

        val rawCredentialData = RawCredentialData(
            credentialId = deferredCredentialEntity.credential.id,
            rawOcaBundle = rawOcaBundle?.toByteArray()?.compress(),
            rawOIDMetadata = rawAndParsedIssuerCredentialInfo.rawIssuerCredentialInfo.toByteArray().compress()
        )

        credentialOfferRepository.saveCredentialFromDeferred(
            credentialId = deferredCredentialEntity.credential.id,
            payloads = listOf(anyCredential.payload),
            validFrom = anyCredential.validFromInstant?.epochSecond,
            validUntil = anyCredential.validUntilInstant?.epochSecond,
            issuer = anyCredential.issuer,
            issuerDisplays = displays.issuerDisplays,
            credentialDisplays = displays.credentialDisplays,
            clusters = displays.clusters,
            rawCredentialData = rawCredentialData,
        ).mapError(CredentialOfferRepositoryError::toRefreshDeferredCredentialsError).bind()
    }

    private suspend fun handleCredentialError(
        deferredCredentialEntity: DeferredCredentialWithKeyBinding,
        error: RefreshDeferredCredentialsError,
    ) = when (error) {
        CredentialError.InvalidIssuerCredentialInfo,
        CredentialError.IntegrityCheckFailed,
        CredentialError.InvalidCredentialOffer,
        CredentialError.InvalidGenerateMetadataClaims,
        CredentialError.InvalidJsonScheme,
        CredentialError.UnknownIssuer,
        CredentialError.UnsupportedCredentialFormat,
        CredentialError.UnsupportedKeyStorageSecurityLevel,
        CredentialError.IncompatibleDeviceKeyStorage,
        is CredentialError.InvalidSignedMetadata -> invalidateCredential(deferredCredentialEntity)

        CredentialError.NetworkError -> {}
        is CredentialError.Unexpected -> {
            Timber.w(t = error.cause, message = "Deferred refresh: fetch credential failed")
        }
    }

    private val CredentialResponse.VerifiableCredential.firstCredential
        get() = this.credentials.first().credential
}
