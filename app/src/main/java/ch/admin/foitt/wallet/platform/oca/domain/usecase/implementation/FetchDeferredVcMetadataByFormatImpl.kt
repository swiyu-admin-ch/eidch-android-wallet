package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.FetchTypeMetadataError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.FetchVcSchemaError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.Rendering
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchema
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchTypeMetadata
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSchema
import ch.admin.foitt.openid4vc.utils.SafeGetUrlError
import ch.admin.foitt.openid4vc.utils.safeGetUrl
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchOcaBundleError
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.model.MetaDataIntegrity
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.RawOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.VcMetadata
import ch.admin.foitt.wallet.platform.oca.domain.model.toFetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchDeferredVcMetadataByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.usecase.ResolveMetaDataIntegrity
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class FetchDeferredVcMetadataByFormatImpl @Inject constructor(
    private val resolveMetaDataIntegrity: ResolveMetaDataIntegrity,
    private val fetchTypeMetadata: FetchTypeMetadata,
    private val fetchVcSchema: FetchVcSchema,
    private val fetchOcaBundle: FetchOcaBundle

) : FetchDeferredVcMetadataByFormat {
    override suspend fun invoke(
        credentialConfig: AnyCredentialConfiguration,
    ): Result<VcMetadata, FetchVcMetadataByFormatError> = coroutineBinding {
        when (credentialConfig.format) {
            CredentialFormat.VC_SD_JWT -> fetchVcMetadataForVcSdJwt(
                credentialConfig as VcSdJwtCredentialConfiguration
            ).bind()
            else -> Err(OcaError.UnsupportedCredentialFormat).bind()
        }
    }

    private suspend fun fetchVcMetadataForVcSdJwt(
        credentialConfig: VcSdJwtCredentialConfiguration,
    ): Result<VcMetadata, FetchVcMetadataByFormatError> = coroutineBinding {
        var vcSchema: VcSchema? = null
        var rawOcaBundle: RawOcaBundle? = null

        val (typeMetadataUrl, typeMetadataUrlIntegrity) = resolveMetaDataIntegrity(
            MetaDataIntegrity.from(credentialConfig)
        ).bind()
        typeMetadataUrl?.let { url ->
            val typeMetadata = fetchTypeMetadata(
                credentialVct = credentialConfig.vct,
                url = url,
                integrity = typeMetadataUrlIntegrity
            ).mapError(FetchTypeMetadataError::toFetchVcMetadataByFormatError).bind()

            typeMetadata.schemaUri?.let {
                val schemaUri = safeGetUrl(typeMetadata.schemaUri)
                    .mapError(SafeGetUrlError::toFetchVcMetadataByFormatError)
                    .bind()

                vcSchema = fetchVcSchema(
                    schemaUrl = schemaUri,
                    schemaUriIntegrity = typeMetadata.schemaUriIntegrity,
                ).mapError(FetchVcSchemaError::toFetchVcMetadataByFormatError)
                    .bind()
            }

            val vcSdJwtOcaRendering = typeMetadata.displays
                ?.flatMap { it.renderings ?: emptyList() }
                ?.firstNotNullOfOrNull { it as? Rendering.VcSdJwtOcaRendering }

            rawOcaBundle = vcSdJwtOcaRendering?.uri?.let { uri ->
                fetchOcaBundle(uri = uri, integrity = vcSdJwtOcaRendering.uriIntegrity)
                    .mapError(FetchOcaBundleError::toFetchVcMetadataByFormatError).bind()
            }
        }

        VcMetadata(vcSchema, rawOcaBundle)
    }
}
