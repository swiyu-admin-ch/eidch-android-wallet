package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.pointsAtSetOf
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.GenerateMetadataDisplaysError
import ch.admin.foitt.wallet.platform.credential.domain.model.MetadataDisplays
import ch.admin.foitt.wallet.platform.credential.domain.model.toAnyCredentialDisplay
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataClaimDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataDisplays
import ch.admin.foitt.wallet.platform.credential.domain.util.addFallbackLanguageIfNecessary
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject

class GenerateMetadataDisplaysImpl @Inject constructor(
    private val generateMetadataClaimDisplays: GenerateMetadataClaimDisplays,
) : GenerateMetadataDisplays {
    override suspend fun invoke(
        claimsWithPointers: Map<ClaimsPathPointer, JsonElement>,
        metadata: AnyCredentialConfiguration,
    ): Result<MetadataDisplays, GenerateMetadataDisplaysError> = coroutineBinding {
        val localizedCredentialDisplays = metadata.credentialMetadata?.display?.map {
            it.toAnyCredentialDisplay()
        }.addFallbackLanguageIfNecessary {
            AnyCredentialDisplay(name = metadata.identifier, locale = DisplayLanguage.FALLBACK)
        }

        val localizedClaims = createLocalizedCredentialClaims(
            credentialConfiguration = metadata,
            claimsWithPointers = claimsWithPointers,
        ).bind()

        val cluster = Cluster(
            order = -1,
            claims = localizedClaims
        )

        MetadataDisplays(
            credentialDisplays = localizedCredentialDisplays,
            clusters = listOf(cluster)
        )
    }

    private suspend fun createLocalizedCredentialClaims(
        credentialConfiguration: AnyCredentialConfiguration,
        claimsWithPointers: Map<ClaimsPathPointer, JsonElement>,
    ): Result<Map<CredentialClaim, List<AnyClaimDisplay>>, GenerateMetadataDisplaysError> = coroutineBinding {
        val metadataClaims = credentialConfiguration.credentialMetadata?.claims

        claimsWithPointers.map { (claimPathPointer, claimValueJson) ->
            val metadataClaim = metadataClaims?.find {
                it.path.pointsAtSetOf(claimPathPointer)
            }

            generateMetadataClaimDisplays(
                claimsPathPointer = claimPathPointer,
                claimValueJson = claimValueJson,
                metadataClaim = metadataClaim,
                order = metadataClaims?.indexOf(metadataClaim) ?: -1
            ).bind()
        }.toMap()
    }
}
