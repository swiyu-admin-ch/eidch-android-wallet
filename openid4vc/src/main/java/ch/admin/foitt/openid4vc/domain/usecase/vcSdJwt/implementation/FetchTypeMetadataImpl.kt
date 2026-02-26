package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.FetchTypeMetadataError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadata
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadataError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadataRepositoryError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.toFetchTypeMetadataByFormatError
import ch.admin.foitt.openid4vc.domain.repository.TypeMetadataRepository
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchTypeMetadata
import ch.admin.foitt.openid4vc.utils.JsonParsingError
import ch.admin.foitt.openid4vc.utils.SafeJson
import ch.admin.foitt.sriValidator.domain.SRIValidator
import ch.admin.foitt.sriValidator.domain.model.SRIError
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import java.net.URL
import javax.inject.Inject

internal class FetchTypeMetadataImpl @Inject constructor(
    private val typeMetadataRepository: TypeMetadataRepository,
    private val safeJson: SafeJson,
    private val sriValidator: SRIValidator,
) : FetchTypeMetadata {
    override suspend fun invoke(
        credentialVct: String,
        url: URL,
        integrity: String?,
        expectIntegrity: Boolean,
    ): Result<TypeMetadata, FetchTypeMetadataError> = coroutineBinding {
        val typeMetadataString = typeMetadataRepository.fetchTypeMetadata(url)
            .mapError(TypeMetadataRepositoryError::toFetchTypeMetadataByFormatError)
            .bind()

        // according to https://www.ietf.org/archive/id/draft-ietf-oauth-sd-jwt-vc-05.html#name-type-metadata sections 6, 8 and 9

        val typeMetadata = safeJson.safeDecodeStringTo<TypeMetadata>(typeMetadataString)
            .mapError(JsonParsingError::toFetchTypeMetadataByFormatError)
            .bind()

        if (expectIntegrity) {
            if (integrity == null) {
                Err(TypeMetadataError.InvalidData).bind<FetchTypeMetadataError>()
            } else {
                sriValidator(typeMetadataString.encodeToByteArray(), integrity)
                    .mapError(SRIError::toFetchTypeMetadataByFormatError)
                    .bind()
            }
        }

        if (typeMetadata.vct != credentialVct) {
            Err(TypeMetadataError.InvalidData).bind<FetchTypeMetadataError>()
        }

        typeMetadata
    }
}
