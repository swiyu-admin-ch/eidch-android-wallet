package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidIssuerDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.GenerateCredentialDisplaysError
import ch.admin.foitt.wallet.platform.credential.domain.model.GenerateMetadataDisplaysError
import ch.admin.foitt.wallet.platform.credential.domain.model.toAnyIssuerDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.toGenerateCredentialDisplaysError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataDisplays
import ch.admin.foitt.wallet.platform.credential.domain.util.addFallbackLanguage
import ch.admin.foitt.wallet.platform.credential.domain.util.entityNames
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayConst
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedCredentialInformationDisplay
import ch.admin.foitt.wallet.platform.oca.domain.model.GenerateOcaDisplaysError
import ch.admin.foitt.wallet.platform.oca.domain.model.MetaDisplays
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaDisplays
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.JsonObject
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
        credentialConfiguration: AnyCredentialConfiguration,
        ocaBundle: OcaBundle?,
    ): Result<AnyDisplays, GenerateCredentialDisplaysError> = coroutineBinding {
        val credentialDisplays = generateCredentialDisplays(
            ocaBundle = ocaBundle,
            anyCredential = anyCredential,
            credentialConfiguration = credentialConfiguration,
        ).bind()
        AnyDisplays(
            issuerDisplays = generateIssuerDisplays(trustStatement, issuerInfo),
            credentialDisplays = credentialDisplays.credentialDisplays,
            clusters = credentialDisplays.clusters,
        )
    }

    private suspend fun generateCredentialDisplays(
        ocaBundle: OcaBundle?,
        anyCredential: AnyCredential?,
        credentialConfiguration: AnyCredentialConfiguration,
    ): Result<MetaDisplays, GenerateCredentialDisplaysError> = coroutineBinding {
        val jsonObject = anyCredential?.let {
            getCredentialClaims(anyCredential).bind()
        }
        if (ocaBundle != null) {
            generateOcaDisplays(
                jsonObject = jsonObject,
                credentialFormat = anyCredential?.format?.format ?: credentialConfiguration.format.format,
                ocaBundle = ocaBundle,
            ).mapError(GenerateOcaDisplaysError::toGenerateCredentialDisplaysError)
                .bind()
        } else {
            generateMetadataDisplays(jsonObject, credentialConfiguration)
                .mapError(GenerateMetadataDisplaysError::toGenerateCredentialDisplaysError)
                .bind()
        }
    }

    private fun getCredentialClaims(
        anyCredential: AnyCredential,
    ): Result<JsonObject, GenerateCredentialDisplaysError> =
        runSuspendCatching {
            anyCredential.getClaimsToSave()
        }.mapError { throwable -> throwable.toGenerateCredentialDisplaysError("getClaimsToSave error") }

    private fun generateIssuerDisplays(trustStatement: TrustStatement?, issuerInfo: IssuerCredentialInfo): List<AnyIssuerDisplay> =
        if (trustStatement != null) {
            createTrustedIssuerDisplays(trustStatement, issuerInfo)
        } else {
            createMetadataIssuerDisplays(issuerInfo)
        }.addFallbackLanguage {
            AnyIssuerDisplay(name = DisplayConst.ISSUER_FALLBACK_NAME, locale = DisplayLanguage.FALLBACK)
        }

    private fun createTrustedIssuerDisplays(
        trustStatement: TrustStatement,
        issuerInfo: IssuerCredentialInfo
    ): List<AnyIssuerDisplay> = trustStatement.entityNames()?.map { (locale, entityName) ->
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

    private fun createMetadataIssuerDisplays(issuerInfo: IssuerCredentialInfo): List<AnyIssuerDisplay> =
        issuerInfo.display?.map(OidIssuerDisplay::toAnyIssuerDisplay).orEmpty()
}
