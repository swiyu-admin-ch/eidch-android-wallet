package ch.admin.foitt.openid4vc.domain.repository

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.CredentialRequestType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchDeferredCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerConfigurationError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchNonceError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.TokenResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerConfigurationResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import com.github.michaelbull.result.Result
import java.net.URL

interface CredentialOfferRepository {

    @CheckResult
    suspend fun fetchRawAndParsedIssuerCredentialInformation(
        issuerEndpoint: URL
    ): Result<RawAndParsedIssuerCredentialInfo, FetchIssuerCredentialInfoError>

    @CheckResult
    suspend fun getIssuerCredentialInfo(
        issuerEndpoint: URL
    ): Result<IssuerCredentialInfo, FetchIssuerCredentialInfoError>

    @CheckResult
    suspend fun fetchIssuerConfiguration(
        issuerEndpoint: URL
    ): Result<IssuerConfigurationResponse, FetchIssuerConfigurationError>

    @CheckResult
    suspend fun fetchNonce(
        nonceEndpoint: URL
    ): Result<String, FetchNonceError>

    @CheckResult
    suspend fun fetchAccessToken(
        tokenEndpoint: URL,
        preAuthorizedCode: String
    ): Result<TokenResponse, FetchVerifiableCredentialError>

    @CheckResult
    suspend fun fetchAccessTokenByRefreshToken(
        tokenEndpoint: URL,
        refreshToken: String
    ): Result<TokenResponse, FetchVerifiableCredentialError>

    @CheckResult
    suspend fun fetchCredential(
        issuerEndpoint: URL,
        tokenResponse: TokenResponse,
        credentialRequestType: CredentialRequestType,
        payloadEncryptionType: PayloadEncryptionType,
    ): Result<CredentialResponse, FetchVerifiableCredentialError>

    suspend fun fetchDeferredCredential(
        issuerEndpoint: String,
        accessToken: String,
        credentialRequestType: CredentialRequestType,
        payloadEncryptionType: PayloadEncryptionType,
    ): Result<CredentialResponse, FetchDeferredCredentialError>
}
