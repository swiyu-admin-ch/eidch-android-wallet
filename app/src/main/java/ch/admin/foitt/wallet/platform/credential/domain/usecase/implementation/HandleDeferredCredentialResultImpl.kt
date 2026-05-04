package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyDeferredCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.model.GenerateCredentialDisplaysError
import ch.admin.foitt.wallet.platform.credential.domain.model.toFetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleDeferredCredentialResult
import ch.admin.foitt.wallet.platform.database.domain.model.RawCredentialData
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchDeferredVcMetadataByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaBundler
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.utils.compress
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.get
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import java.net.URL
import javax.inject.Inject

class HandleDeferredCredentialResultImpl @Inject constructor(
    private val fetchDeferredVcMetadataByFormat: FetchDeferredVcMetadataByFormat,
    private val ocaBundler: OcaBundler,
    private val generateAnyDisplays: GenerateAnyDisplays,
    private val credentialOfferRepository: CredentialOfferRepository,
) : HandleDeferredCredentialResult {
    override suspend fun invoke(
        issuerUrl: URL,
        deferredCredential: AnyDeferredCredential,
        rawAndParsedCredentialInfo: RawAndParsedIssuerCredentialInfo,
        credentialConfig: AnyCredentialConfiguration,
    ): Result<FetchCredentialResult, FetchCredentialError> = coroutineBinding {
        val vcMetadata = fetchDeferredVcMetadataByFormat(credentialConfig)
            .mapError(FetchVcMetadataByFormatError::toFetchCredentialError)
            .bind()

        val rawOcaBundle = vcMetadata.rawOcaBundle?.rawOcaBundle
        val ocaBundle = rawOcaBundle?.let {
            ocaBundler(it).get()
        }

        val displays = generateAnyDisplays(
            anyCredential = null,
            issuerInfo = rawAndParsedCredentialInfo.issuerCredentialInfo,
            trustStatement = null,
            metadata = credentialConfig,
            ocaBundle = ocaBundle,
        ).mapError(GenerateCredentialDisplaysError::toFetchCredentialError).bind()

        val rawCredentialData = RawCredentialData(
            credentialId = -1,
            rawOcaBundle = rawOcaBundle?.toByteArray()?.compress(),
            rawOIDMetadata = rawAndParsedCredentialInfo.rawIssuerCredentialInfo.toByteArray().compress()
        )

        // fixme: save expires and refreshToken
        credentialOfferRepository.saveDeferredCredentialOffer(
            transactionId = deferredCredential.transactionId,
            accessToken = deferredCredential.accessToken,
            refreshToken = deferredCredential.refreshToken,
            endpoint = deferredCredential.endpoint,
            pollInterval = deferredCredential.pollInterval,
            keyBindings = deferredCredential.keyBindings,
            format = deferredCredential.format,
            issuerDisplays = displays.issuerDisplays,
            credentialDisplays = displays.credentialDisplays,
            rawCredentialData = rawCredentialData,
            selectedConfigurationId = credentialConfig.identifier,
            issuerUrl = issuerUrl
        ).map {
            FetchCredentialResult.DeferredCredential(it)
        }.mapError(CredentialOfferRepositoryError::toFetchCredentialError).bind()
    }
}
