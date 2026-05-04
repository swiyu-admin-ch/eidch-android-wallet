package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.CredentialRequestType
import ch.admin.foitt.openid4vc.domain.model.CredentialType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateCredentialRequestError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchAccessTokenError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchDeferredCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerConfigurationError
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequest
import ch.admin.foitt.openid4vc.domain.usecase.FetchIssuerConfiguration
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchAndUpdateDeferredCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchExistingIssuerCredentialInfoError
import ch.admin.foitt.wallet.platform.credential.domain.model.SaveCredentialFromDeferredError
import ch.admin.foitt.wallet.platform.credential.domain.model.UpdateDeferredCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toFetchAndUpdateDeferredCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toUpdateDeferredCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchAndUpdateDeferredCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchExistingIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveCredentialFromDeferred
import ch.admin.foitt.wallet.platform.credential.domain.usecase.UpdateDeferredCredential
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.GetPayloadEncryptionTypeError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryptionType
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeferredCredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThenRecover
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository as OIDCredentialOfferRepository

internal class FetchAndUpdateDeferredCredentialImpl @Inject constructor(
    private val deferredCredentialRepository: DeferredCredentialRepository,
    private val fetchExistingIssuerCredentialInfo: FetchExistingIssuerCredentialInfo,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val getPayloadEncryptionType: GetPayloadEncryptionType,
    private val createCredentialRequest: CreateCredentialRequest,
    private val oidCredentialOfferRepository: OIDCredentialOfferRepository,
    private val saveCredentialFromDeferred: SaveCredentialFromDeferred,
    private val updateDeferredCredential: UpdateDeferredCredential,
    private val fetchIssuerConfiguration: FetchIssuerConfiguration,
) : FetchAndUpdateDeferredCredential {
    override suspend operator fun invoke(
        deferredCredentialEntity: DeferredCredentialWithKeyBinding,
    ): Result<Unit, FetchAndUpdateDeferredCredentialError> = coroutineBinding {
        val deferredEntity = deferredCredentialEntity.deferredCredential
        val rawAndParsedIssuerCredentialInfo = fetchExistingIssuerCredentialInfo(credentialId = deferredEntity.credentialId)
            .mapError(FetchExistingIssuerCredentialInfoError::toUpdateDeferredCredentialError)
            .bind()

        val payloadEncryptionType = if (environmentSetupRepository.payloadEncryptionEnabled) {
            getPayloadEncryptionType(
                requestEncryption = rawAndParsedIssuerCredentialInfo.issuerCredentialInfo.credentialRequestEncryption,
                responseEncryption = rawAndParsedIssuerCredentialInfo.issuerCredentialInfo.credentialResponseEncryption,
            ).mapError(GetPayloadEncryptionTypeError::toUpdateDeferredCredentialError)
                .bind()
        } else {
            PayloadEncryptionType.None
        }

        val credentialRequestType = createCredentialRequest(
            payloadEncryptionType = payloadEncryptionType,
            credentialType = CredentialType.Deferred(transactionId = deferredEntity.transactionId)
        ).mapError(CreateCredentialRequestError::toUpdateDeferredCredentialError)
            .bind()

        val credentialResponse = tryFetchDeferredCredentialWithRefreshToken(
            deferredCredentialEntity = deferredCredentialEntity,
            credentialRequestType = credentialRequestType,
            payloadEncryptionType = payloadEncryptionType,
        ).onFailure { error ->
            handleDeferredError(
                error = error,
                deferredCredential = deferredCredentialEntity,
            )
        }.mapError(UpdateDeferredCredentialError::toFetchAndUpdateDeferredCredentialError).bind()

        when (credentialResponse) {
            is CredentialResponse.DeferredCredential -> updateDeferredCredential(
                deferredCredentialEntity = deferredCredentialEntity,
                credentialResponse = credentialResponse,
                rawAndParsedIssuerCredentialInfo = rawAndParsedIssuerCredentialInfo,
            ).onFailure {
                Timber.e(message = "Deferred refresh: update deferred failed")
            }

            is CredentialResponse.VerifiableCredential -> saveCredentialFromDeferred(
                deferredCredentialEntity = deferredCredentialEntity,
                credentialResponse = credentialResponse,
                rawAndParsedIssuerCredentialInfo = rawAndParsedIssuerCredentialInfo,
            ).onFailure { error ->
                handleCredentialError(
                    deferredCredentialEntity = deferredCredentialEntity,
                    error = error,
                )
            }
        }
    }

    private suspend fun tryFetchDeferredCredentialWithRefreshToken(
        deferredCredentialEntity: DeferredCredentialWithKeyBinding,
        credentialRequestType: CredentialRequestType,
        payloadEncryptionType: PayloadEncryptionType,
    ): Result<CredentialResponse, UpdateDeferredCredentialError> = coroutineBinding {
        val deferredEntity = deferredCredentialEntity.deferredCredential

        suspend fun fetchDeferredCredential(
            accessToken: String
        ) = oidCredentialOfferRepository.fetchDeferredCredential(
            issuerEndpoint = deferredEntity.endpoint,
            accessToken = accessToken,
            credentialRequestType = credentialRequestType,
            payloadEncryptionType = payloadEncryptionType,
        )

        fetchDeferredCredential(deferredEntity.accessToken)
            .andThenRecover { error ->
                if (
                    error is CredentialOfferError.InvalidToken &&
                    deferredEntity.refreshToken != null
                ) {
                    val issuerConfiguration = fetchIssuerConfiguration(deferredCredentialEntity.credential.issuerUrl)
                        .mapError(FetchIssuerConfigurationError::toUpdateDeferredCredentialError).bind()

                    val tokenResponse = oidCredentialOfferRepository.fetchAccessTokenByRefreshToken(
                        tokenEndpoint = issuerConfiguration.tokenEndpoint,
                        refreshToken = deferredEntity.refreshToken,
                    ).mapError(FetchAccessTokenError::toUpdateDeferredCredentialError).bind()

                    deferredCredentialRepository.updateTokens(
                        credentialId = deferredEntity.credentialId,
                        tokenResponse = tokenResponse,
                    ).mapError(DeferredCredentialRepositoryError::toUpdateDeferredCredentialError).bind()

                    fetchDeferredCredential(tokenResponse.accessToken)
                } else {
                    Err(error)
                }
            }.mapError(FetchDeferredCredentialError::toUpdateDeferredCredentialError).bind()
    }

    private suspend fun invalidateCredential(deferredCredentialEntity: DeferredCredentialWithKeyBinding) =
        deferredCredentialRepository.updateStatus(
            credentialId = deferredCredentialEntity.credential.id,
            progressionState = DeferredProgressionState.INVALID,
            polledAt = Instant.now().epochSecond,
            pollInterval = deferredCredentialEntity.deferredCredential.pollInterval,
        )

    private suspend fun handleDeferredError(
        error: UpdateDeferredCredentialError,
        deferredCredential: DeferredCredentialWithKeyBinding,
    ) {
        when (error) {
            CredentialError.InvalidCredentialOffer -> invalidateCredential(deferredCredential)
            CredentialError.IncompatibleDeviceKeyStorage,
            CredentialError.InvalidGenerateMetadataClaims,
            CredentialError.InvalidIssuerCredentialInfo,
            CredentialError.UnsupportedKeyStorageSecurityLevel,
            is CredentialError.InvalidSignedMetadata,
            CredentialError.NetworkError,
            CredentialError.UnsupportedImageFormat -> {
                Timber.d(message = "Deferred refresh: fetch deferred failed with error $error")
            }
            is CredentialError.Unexpected -> {
                Timber.w(t = error.cause, message = "Deferred refresh: fetch deferred failed")
            }
        }
    }

    private suspend fun handleCredentialError(
        deferredCredentialEntity: DeferredCredentialWithKeyBinding,
        error: SaveCredentialFromDeferredError,
    ) = when (error) {
        CredentialError.InvalidIssuerCredentialInfo,
        CredentialError.IntegrityCheckFailed,
        CredentialError.InvalidCredentialOffer,
        CredentialError.InvalidGenerateMetadataClaims,
        CredentialError.InvalidJsonScheme,
        CredentialError.UnknownIssuer,
        CredentialError.UnsupportedCredentialFormat,
        CredentialError.UnsupportedKeyStorageSecurityLevel,
        CredentialError.IncompatibleDeviceKeyStorage -> invalidateCredential(deferredCredentialEntity)
        CredentialError.NetworkError,
        CredentialError.UnsupportedImageFormat -> {
            Timber.d(message = "Deferred refresh: fetch deferred failed with error $error")
        }
        is CredentialError.Unexpected -> {
            Timber.w(t = error.cause, message = "Deferred refresh: fetch credential failed")
        }
    }
}
