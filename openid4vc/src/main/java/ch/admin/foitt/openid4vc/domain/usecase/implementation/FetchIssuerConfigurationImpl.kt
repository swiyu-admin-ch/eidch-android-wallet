package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.ValidateIssuerMetadataJwtError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerConfigurationResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchIssuerConfigurationError
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.usecase.FetchIssuerConfiguration
import ch.admin.foitt.openid4vc.domain.usecase.ValidateIssuerMetadataJwt
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import java.net.URL
import javax.inject.Inject

internal class FetchIssuerConfigurationImpl @Inject constructor(
    private val credentialOfferRepository: CredentialOfferRepository,
    private val validateIssuerMetadataJwt: ValidateIssuerMetadataJwt,
) : FetchIssuerConfiguration {
    override suspend fun invoke(issuerEndpoint: URL) = coroutineBinding {
        when (val issuerConfig = credentialOfferRepository.fetchIssuerConfiguration(issuerEndpoint).bind()) {
            is IssuerConfigurationResponse.Plain -> issuerConfig.config
            is IssuerConfigurationResponse.Signed -> {
                validateIssuerMetadataJwt(
                    credentialIssuerIdentifier = issuerEndpoint.toString(),
                    jwt = issuerConfig.jwt,
                    type = null // currently undefined :(
                ).mapError(ValidateIssuerMetadataJwtError::toFetchIssuerConfigurationError)
                    .bind()
                issuerConfig.config
            }
        }
    }
}
