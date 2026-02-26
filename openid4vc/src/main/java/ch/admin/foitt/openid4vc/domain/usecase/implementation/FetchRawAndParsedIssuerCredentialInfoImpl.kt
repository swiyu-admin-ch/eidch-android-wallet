package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.ValidateIssuerMetadataJwtError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.ValidateIssuerMetadataJwt
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import java.net.URL
import javax.inject.Inject

internal class FetchRawAndParsedIssuerCredentialInfoImpl @Inject constructor(
    private val credentialOfferRepository: CredentialOfferRepository,
    private val validateIssuerMetadataJwt: ValidateIssuerMetadataJwt,
) : FetchRawAndParsedIssuerCredentialInfo {
    override suspend fun invoke(issuerEndpoint: URL): Result<RawAndParsedIssuerCredentialInfo, FetchIssuerCredentialInfoError> =
        coroutineBinding {
            val rawAndParsedInfo = credentialOfferRepository.fetchRawAndParsedIssuerCredentialInformation(issuerEndpoint).bind()
            // it was already parsed in the repo, so we can assume that we get the JWT if available
            val jwt = runSuspendCatching { Jwt(rawAndParsedInfo.rawIssuerCredentialInfo) }.get()
            jwt?.let {
                validateIssuerMetadataJwt(
                    credentialIssuerIdentifier = issuerEndpoint.toString(),
                    jwt = jwt,
                    type = "openidvci-issuer-metadata+jwt"
                ).mapError(ValidateIssuerMetadataJwtError::toFetchIssuerCredentialInfoError)
                    .bind()
            }
            rawAndParsedInfo
        }
}
