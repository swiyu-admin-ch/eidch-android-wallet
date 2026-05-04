package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.FetchTypeMetadataError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadata
import com.github.michaelbull.result.Result
import java.net.URL

interface FetchTypeMetadata {
    suspend operator fun invoke(
        credentialVct: String,
        url: URL,
        integrity: String?
    ): Result<TypeMetadata, FetchTypeMetadataError>
}
