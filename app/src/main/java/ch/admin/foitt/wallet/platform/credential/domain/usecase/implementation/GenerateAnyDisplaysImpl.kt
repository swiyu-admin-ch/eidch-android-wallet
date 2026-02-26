package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.GenerateCredentialDisplaysError
import ch.admin.foitt.wallet.platform.credential.domain.model.GenerateMetadataDisplaysError
import ch.admin.foitt.wallet.platform.credential.domain.model.toGenerateCredentialDisplaysError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataDisplays
import ch.admin.foitt.wallet.platform.credential.domain.util.addFallbackLanguageIfNecessary
import ch.admin.foitt.wallet.platform.credential.domain.util.entityNames
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayConst
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedCredentialInformationDisplay
import ch.admin.foitt.wallet.platform.oca.domain.model.GenerateOcaDisplaysError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaDisplays
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath.using
import com.jayway.jsonpath.Option
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class GenerateAnyDisplaysImpl @Inject constructor(
    private val getLocalizedCredentialInformationDisplay: GetLocalizedCredentialInformationDisplay,
    private val generateOcaDisplays: GenerateOcaDisplays,
    private val generateMetadataDisplays: GenerateMetadataDisplays,
) : GenerateAnyDisplays {
    override suspend fun invoke(
        anyCredential: AnyCredential?,
        issuerInfo: IssuerCredentialInfo,
        trustStatement: TrustStatement?,
        metadata: AnyCredentialConfiguration,
        ocaBundle: OcaBundle?,
    ): Result<AnyDisplays, GenerateCredentialDisplaysError> = coroutineBinding {
        val useTrustStatement = trustStatement != null

        val localizedIssuerDisplays: List<AnyIssuerDisplay> = if (useTrustStatement) {
            // if trust statement is available only use information from there (no fallback to metadata)
            trustStatement.entityNames()?.map { (locale, entityName) ->
                val metadataDisplay = issuerInfo.display?.let {
                    getLocalizedCredentialInformationDisplay(
                        displays = it,
                        preferredLocaleString = locale,
                    )
                }

                AnyIssuerDisplay(
                    locale = locale,
                    name = entityName,
                    logo = metadataDisplay?.logo?.uri, // exception: use logo from metadata
                    logoAltText = metadataDisplay?.logo?.altText, // exception: use logo alt text from metadata
                )
            }.orEmpty()
        } else {
            // if trust statement is not available use metadata
            issuerInfo.display?.map { oidIssuerDisplay ->
                AnyIssuerDisplay(
                    locale = oidIssuerDisplay.locale,
                    name = oidIssuerDisplay.name,
                    logo = oidIssuerDisplay.logo?.uri,
                    logoAltText = oidIssuerDisplay.logo?.altText,
                )
            }.orEmpty()
        }.addFallbackLanguageIfNecessary {
            AnyIssuerDisplay(name = DisplayConst.ISSUER_FALLBACK_NAME, locale = DisplayLanguage.FALLBACK)
        }

        val credentialClaims = anyCredential?.let {
            getCredentialClaims(anyCredential).bind()
        } ?: mapOf()

        val displays = if (ocaBundle != null) {
            generateOcaDisplays(credentialClaims, ocaBundle)
                .mapError(GenerateOcaDisplaysError::toGenerateCredentialDisplaysError)
                .bind()
        } else {
            generateMetadataDisplays(credentialClaims, metadata)
                .mapError(GenerateMetadataDisplaysError::toGenerateCredentialDisplaysError)
                .bind()
        }

        AnyDisplays(
            issuerDisplays = localizedIssuerDisplays,
            credentialDisplays = displays.credentialDisplays,
            clusters = displays.clusters,
        )
    }

    private suspend fun getCredentialClaims(anyCredential: AnyCredential) = coroutineBinding {
        val credentialJson = runSuspendCatching { anyCredential.getClaimsToSave() }
            .mapError { throwable -> throwable.toGenerateCredentialDisplaysError("getClaimsToSave error") }
            .bind()
        val conf: Configuration = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST).build()
        // Map<JsonPath, value>
        val credentialClaims: Map<String, String?> = using(conf)
            .parse(credentialJson)
            .read<List<Map<String, JsonElement>>>(anyCredential.claimsPath)
            .firstOrNull()
            ?.mapValues {
                when (it.value) {
                    is JsonPrimitive -> it.value.jsonPrimitive.contentOrNull
                    is JsonArray -> it.value.jsonArray.toString()
                    is JsonObject -> it.value.jsonObject.toString()
                }
            } ?: emptyMap()

        credentialClaims
    }
}
