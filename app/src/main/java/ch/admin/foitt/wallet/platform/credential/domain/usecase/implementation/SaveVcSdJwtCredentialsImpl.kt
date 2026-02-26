package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.CacheIssuerDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.GenerateCredentialDisplaysError
import ch.admin.foitt.wallet.platform.credential.domain.model.toFetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchTrustForIssuance
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveVcSdJwtCredentials
import ch.admin.foitt.wallet.platform.database.domain.model.RawCredentialData
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.FetchNonComplianceData
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchVcMetadataByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaBundler
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.utils.compress
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import java.net.URL
import javax.inject.Inject

class SaveVcSdJwtCredentialsImpl @Inject constructor(
    private val fetchNonComplianceData: FetchNonComplianceData,
    private val fetchVcMetadataByFormat: FetchVcMetadataByFormat,
    private val ocaBundler: OcaBundler,
    private val generateAnyDisplays: GenerateAnyDisplays,
    private val cacheIssuerDisplayData: CacheIssuerDisplayData,
    private val credentialOfferRepository: CredentialOfferRepository,
    private val fetchTrustForIssuance: FetchTrustForIssuance,
) : SaveVcSdJwtCredentials {
    override suspend fun invoke(
        credentialId: Long,
        issuerUrl: URL,
        vcSdJwtCredentials: List<VcSdJwtCredential>,
        rawAndParsedCredentialInfo: RawAndParsedIssuerCredentialInfo,
        credentialConfig: AnyCredentialConfiguration,
    ): Result<Long, FetchCredentialError> = coroutineBinding {
        val vcSdJwtCredential = vcSdJwtCredentials.first()
        val vcMetadata = fetchVcMetadataByFormat(vcSdJwtCredential)
            .mapError(FetchVcMetadataByFormatError::toFetchCredentialError)
            .bind()

        val trustCheckResult = fetchTrustForIssuance(
            issuerDid = vcSdJwtCredential.issuer,
            vcSchemaId = vcSdJwtCredential.vcSchemaId,
        )

        val rawOcaBundle = vcMetadata.rawOcaBundle?.rawOcaBundle
        val ocaBundle = rawOcaBundle?.let {
            ocaBundler(it).get()
        }

        val displays = generateAnyDisplays(
            anyCredential = vcSdJwtCredential,
            issuerInfo = rawAndParsedCredentialInfo.issuerCredentialInfo,
            trustStatement = trustCheckResult.actorTrustStatement,
            metadata = credentialConfig,
            ocaBundle = ocaBundle,
        ).mapError(GenerateCredentialDisplaysError::toFetchCredentialError).bind()

        val nonComplianceData = fetchNonComplianceData(actorDid = vcSdJwtCredential.issuer)

        cacheIssuerDisplayData(
            trustCheckResult = trustCheckResult,
            issuerDisplays = displays.issuerDisplays,
            nonComplianceData = nonComplianceData,
        )

        val rawCredentialData = RawCredentialData(
            credentialId = -1,
            rawOcaBundle = rawOcaBundle?.toByteArray()?.compress(),
            rawOIDMetadata = rawAndParsedCredentialInfo.rawIssuerCredentialInfo.toByteArray().compress()
        )

        credentialOfferRepository.saveCredentialOffer(
            credentialId = credentialId,
            keyBindings = vcSdJwtCredentials.map { it.keyBinding },
            payloads = vcSdJwtCredentials.map { it.payload },
            format = vcSdJwtCredential.format,
            validFrom = vcSdJwtCredential.validFromInstant?.epochSecond,
            validUntil = vcSdJwtCredential.validUntilInstant?.epochSecond,
            issuer = vcSdJwtCredential.issuer,
            issuerDisplays = displays.issuerDisplays,
            credentialDisplays = displays.credentialDisplays,
            clusters = displays.clusters,
            rawCredentialData = rawCredentialData,
            selectedConfigurationId = credentialConfig.identifier,
            issuerUrl = issuerUrl
        ).mapError(CredentialOfferRepositoryError::toFetchCredentialError).bind()
    }
}
