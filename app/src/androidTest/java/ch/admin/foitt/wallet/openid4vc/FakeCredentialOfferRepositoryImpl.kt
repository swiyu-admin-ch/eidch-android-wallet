package ch.admin.foitt.wallet.openid4vc

import ch.admin.foitt.openid4vc.domain.model.CredentialRequestType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchDeferredCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.TokenResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerConfigurationResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.feature.credentialOffer.mock.CredentialOfferMocks.MOCK_ACCESS_TOKEN
import ch.admin.foitt.wallet.feature.credentialOffer.mock.CredentialOfferMocks.MOCK_CREDENTIAL_RESPONSE
import ch.admin.foitt.wallet.feature.credentialOffer.mock.CredentialOfferMocks.MOCK_C_NONCE
import ch.admin.foitt.wallet.feature.credentialOffer.mock.CredentialOfferMocks.MOCK_OPEN_ID_CONFIG
import ch.admin.foitt.wallet.feature.credentialOffer.mock.CredentialOfferMocks.MOCK_UETLIBERG_CREDENTIAL_METADATA
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import timber.log.Timber
import java.net.URL
import javax.inject.Inject

@OptIn(UnsafeResultValueAccess::class)
internal class FakeCredentialOfferRepositoryImpl @Inject constructor(
    private val safeJson: SafeJson,
) : CredentialOfferRepository {
    override suspend fun fetchRawAndParsedIssuerCredentialInformation(
        issuerEndpoint: URL
    ): Result<RawAndParsedIssuerCredentialInfo, FetchIssuerCredentialInfoError> {
        Timber.d("issuer credential information fake was used")
        return Ok(
            RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = safeJson.safeDecodeStringTo<IssuerCredentialInfo>(MOCK_UETLIBERG_CREDENTIAL_METADATA).value,
                rawIssuerCredentialInfo = MOCK_UETLIBERG_CREDENTIAL_METADATA
            )
        )
    }

    override suspend fun getIssuerCredentialInfo(
        issuerEndpoint: URL
    ): Result<IssuerCredentialInfo, FetchIssuerCredentialInfoError> {
        Timber.d("issuer credential information fake was used")
        return Ok(safeJson.safeDecodeStringTo<IssuerCredentialInfo>(MOCK_UETLIBERG_CREDENTIAL_METADATA).value)
    }

    override suspend fun fetchIssuerConfiguration(
        issuerEndpoint: URL
    ) = Ok(safeJson.safeDecodeStringTo<IssuerConfigurationResponse>(MOCK_OPEN_ID_CONFIG).value)

    override suspend fun fetchNonce(nonceEndpoint: URL) = Ok(MOCK_C_NONCE)

    override suspend fun fetchAccessToken(
        tokenEndpoint: URL,
        preAuthorizedCode: String
    ) = Ok(safeJson.safeDecodeStringTo<TokenResponse>(MOCK_ACCESS_TOKEN).value)

    override suspend fun fetchAccessTokenByRefreshToken(
        tokenEndpoint: URL,
        refreshToken: String
    ) = Ok(safeJson.safeDecodeStringTo<TokenResponse>(MOCK_ACCESS_TOKEN).value)

    override suspend fun fetchCredential(
        issuerEndpoint: URL,
        tokenResponse: TokenResponse,
        credentialRequestType: CredentialRequestType,
        payloadEncryptionType: PayloadEncryptionType
    ): Result<CredentialResponse, FetchVerifiableCredentialError> = Ok(
        safeJson.safeDecodeStringTo<CredentialResponse>(MOCK_CREDENTIAL_RESPONSE).value
    )

    override suspend fun fetchDeferredCredential(
        issuerEndpoint: String,
        accessToken: String,
        credentialRequestType: CredentialRequestType,
        payloadEncryptionType: PayloadEncryptionType
    ): Result<CredentialResponse, FetchDeferredCredentialError> = Ok(
        safeJson.safeDecodeStringTo<CredentialResponse>(MOCK_CREDENTIAL_RESPONSE).value
    )
}
