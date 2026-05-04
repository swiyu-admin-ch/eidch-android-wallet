package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.FetchTypeMetadataError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.FetchVcSchemaError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.Rendering
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchema
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchTypeMetadata
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSchema
import ch.admin.foitt.openid4vc.utils.SafeGetUrlError
import ch.admin.foitt.openid4vc.utils.safeGetUrl
import ch.admin.foitt.wallet.platform.jsonSchema.domain.model.JsonSchemaError
import ch.admin.foitt.wallet.platform.jsonSchema.domain.usecase.JsonSchemaValidator
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchOcaBundleError
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.model.MetaDataIntegrity
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.RawOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.VcMetadata
import ch.admin.foitt.wallet.platform.oca.domain.model.toFetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchVcMetadataByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.ResolveMetaDataIntegrity
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject
import javax.inject.Named

class FetchVcMetadataByFormatImpl @Inject constructor(
    private val resolveMetaDataIntegrity: ResolveMetaDataIntegrity,
    private val fetchTypeMetadata: FetchTypeMetadata,
    private val fetchVcSchema: FetchVcSchema,
    private val fetchOcaBundle: FetchOcaBundle,
    @param:Named("VcSdJwtJsonSchemaValidator")
    private val vcSdJwtJsonSchemaValidator: JsonSchemaValidator,

) : FetchVcMetadataByFormat {
    override suspend fun invoke(
        anyCredential: AnyCredential
    ): Result<VcMetadata, FetchVcMetadataByFormatError> = coroutineBinding {
        when (anyCredential.format) {
            CredentialFormat.VC_SD_JWT -> fetchVcMetadataForVcSdJwt(anyCredential as VcSdJwtCredential).bind()
            else -> Err(OcaError.UnsupportedCredentialFormat).bind()
        }
    }

    private suspend fun fetchVcMetadataForVcSdJwt(
        credential: VcSdJwtCredential,
    ): Result<VcMetadata, FetchVcMetadataByFormatError> = coroutineBinding {
        var vcSchema: VcSchema? = null
        var rawOcaBundle: RawOcaBundle? = null

        val (typeMetadataUrl, typeMetadataUrlIntegrity) = resolveMetaDataIntegrity(MetaDataIntegrity.from(credential))
            .bind()
        typeMetadataUrl?.let { url ->
            val typeMetadata = fetchTypeMetadata(credentialVct = credential.vct, url = url, integrity = typeMetadataUrlIntegrity)
                .mapError(FetchTypeMetadataError::toFetchVcMetadataByFormatError)
                .bind()

            // ignore vc schema if not provided in type metadata, fetch for valid url, error for invalid url
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

            // validate vcSchema
            vcSchema?.let {
                vcSdJwtJsonSchemaValidator(credential.getClaimsForPresentation().toString(), it.schema)
                    .mapError(JsonSchemaError::toFetchVcMetadataByFormatError)
                    .bind()
            }

            // find first display that contains a valid oca rendering
            val vcSdJwtOcaRendering = typeMetadata.displays
                ?.flatMap { it.renderings ?: emptyList() }
                ?.firstOrNull { it is Rendering.VcSdJwtOcaRendering } as? Rendering.VcSdJwtOcaRendering

            rawOcaBundle = vcSdJwtOcaRendering?.uri?.let { uri ->
                fetchOcaBundle(uri = uri, integrity = vcSdJwtOcaRendering.uriIntegrity)
                    .mapError(FetchOcaBundleError::toFetchVcMetadataByFormatError)
                    .bind()
            }
        }

        VcMetadata(
            vcSchema = vcSchema,
            rawOcaBundle = rawOcaBundle,
        )
    }
}
