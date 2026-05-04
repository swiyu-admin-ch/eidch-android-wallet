package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.GenerateCredentialDisplaysError
import ch.admin.foitt.wallet.platform.credential.domain.model.UpdateDeferredCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toUpdateDeferredCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.UpdateDeferredCredential
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeferredCredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialRepository
import ch.admin.foitt.wallet.platform.utils.compress
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

class UpdateDeferredCredentialImpl @Inject constructor(
    private val deferredCredentialRepository: DeferredCredentialRepository,
    private val generateAnyDisplays: GenerateAnyDisplays,
    private val credentialOfferRepository: CredentialOfferRepository,
) : UpdateDeferredCredential {
    override suspend fun invoke(
        deferredCredentialEntity: DeferredCredentialWithKeyBinding,
        credentialResponse: CredentialResponse.DeferredCredential,
        rawAndParsedIssuerCredentialInfo: RawAndParsedIssuerCredentialInfo,
    ): Result<Unit, UpdateDeferredCredentialError> = coroutineBinding {
        Timber.d("Deferred refresh: handle update for ${deferredCredentialEntity.credential.id}")
        if (credentialResponse.transactionId != deferredCredentialEntity.deferredCredential.transactionId) {
            Err(CredentialError.Unexpected(IllegalStateException("Transaction ids do not match"))).bind()
        }

        deferredCredentialRepository.updateStatus(
            credentialId = deferredCredentialEntity.credential.id,
            progressionState = DeferredProgressionState.IN_PROGRESS,
            polledAt = Instant.now().epochSecond,
            pollInterval = credentialResponse.interval,
        ).mapError(DeferredCredentialRepositoryError::toUpdateDeferredCredentialError).bind()

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
        ).mapError(GenerateCredentialDisplaysError::toUpdateDeferredCredentialError)
            .bind()

        credentialOfferRepository.updateDeferredCredentialMetaData(
            credentialId = deferredCredentialEntity.credential.id,
            issuerDisplays = displays.issuerDisplays,
            credentialDisplays = displays.credentialDisplays,
            rawMetadata = rawAndParsedIssuerCredentialInfo.rawIssuerCredentialInfo.toByteArray().compress(),
        ).mapError(CredentialOfferRepositoryError::toUpdateDeferredCredentialError)
            .bind()
    }
}
