package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorMetaDataError
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.FetchAndCacheIssuerDisplayDataError
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.toFetchAndCacheIssuerDisplayDataError
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.CacheIssuerDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchAndCacheIssuerDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAllAnyCredentialsByCredentialIdError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchTrustForIssuance
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAllAnyCredentialsByCredentialId
import ch.admin.foitt.wallet.platform.credential.domain.util.entityNames
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.FetchNonComplianceData
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialIssuerDisplayRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialIssuerDisplayRepo
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class FetchAndCacheIssuerDisplayDataImpl @Inject constructor(
    private val getAllAnyCredentialsByCredentialId: GetAllAnyCredentialsByCredentialId,
    private val fetchTrustForIssuance: FetchTrustForIssuance,
    private val credentialIssuerDisplayRepo: CredentialIssuerDisplayRepo,
    private val getLocalizedDisplay: GetLocalizedDisplay,
    private val fetchNonComplianceData: FetchNonComplianceData,
    private val cacheIssuerDisplayData: CacheIssuerDisplayData,
) : FetchAndCacheIssuerDisplayData {
    override suspend operator fun invoke(
        credentialId: Long,
    ): Result<Unit, FetchAndCacheIssuerDisplayDataError> = coroutineBinding {
        val anyCredentials = getAllAnyCredentialsByCredentialId(credentialId)
            .mapError(GetAllAnyCredentialsByCredentialIdError::toFetchAndCacheIssuerDisplayDataError)
            .bind()

        val anyCredential = runSuspendCatching {
            anyCredentials.first()
        }.mapError { ActorMetaDataError.Unexpected(it) }.bind()

        val trustCheckResult = fetchTrustForIssuance(
            issuerDid = anyCredential.issuer,
            vcSchemaId = anyCredential.vcSchemaId,
        )
        val actorTrustStatement = trustCheckResult.actorTrustStatement
        val savedIssuerDisplays = credentialIssuerDisplayRepo.getIssuerDisplays(credentialId)
            .mapError(CredentialIssuerDisplayRepositoryError::toFetchAndCacheIssuerDisplayDataError)
            .bind()

        val localizedIssuerDisplays: List<AnyIssuerDisplay> = if (actorTrustStatement != null) {
            // if trust statement is available only use information from there (no fallback to metadata)
            actorTrustStatement.entityNames()?.map { (locale, entityName) ->
                val savedDisplay = getLocalizedDisplay(
                    displays = savedIssuerDisplays,
                    preferredLocaleString = locale,
                )

                AnyIssuerDisplay(
                    locale = locale,
                    name = entityName,
                    logo = savedDisplay?.image, // exception: use logo from metadata
                    logoAltText = savedDisplay?.imageAltText, // exception: use logo alt text from metadata
                )
            }.orEmpty()
        } else {
            // if trust statement is not available use metadata
            savedIssuerDisplays.map { display ->
                AnyIssuerDisplay(
                    locale = display.locale,
                    name = display.name,
                    logo = display.image,
                    logoAltText = display.imageAltText,
                )
            }
        }

        val nonComplianceData = fetchNonComplianceData(actorDid = anyCredential.issuer)

        cacheIssuerDisplayData(
            trustCheckResult = trustCheckResult,
            issuerDisplays = localizedIssuerDisplays,
            nonComplianceData = nonComplianceData,
        )
    }
}
