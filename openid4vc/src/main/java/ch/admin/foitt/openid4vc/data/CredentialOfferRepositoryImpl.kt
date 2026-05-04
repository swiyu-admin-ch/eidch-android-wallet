package ch.admin.foitt.openid4vc.data

import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.openid4vc.domain.model.CredentialRequestType
import ch.admin.foitt.openid4vc.domain.model.HttpErrorBody
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchAccessTokenError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchDeferredCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerConfigurationError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchNonceError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.NonceResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.TokenResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerConfigurationResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchDeferredCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchIssuerConfigurationError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.jwe.DecryptJWEError
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.usecase.jwe.DecryptJWE
import ch.admin.foitt.openid4vc.utils.Constants
import ch.admin.foitt.openid4vc.utils.ContentType.applicationJwt
import ch.admin.foitt.openid4vc.utils.JsonError
import ch.admin.foitt.openid4vc.utils.JsonParsingError
import ch.admin.foitt.openid4vc.utils.SafeJson
import ch.admin.foitt.openid4vc.utils.acceptLanguageHeader
import ch.admin.foitt.openid4vc.utils.content
import ch.admin.foitt.openid4vc.utils.toContentType
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onSuccess
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import io.ktor.http.path
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import timber.log.Timber
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

internal class CredentialOfferRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
    private val safeJson: SafeJson,
    private val decryptJWE: DecryptJWE,
) : CredentialOfferRepository {

    private val latestIssuerCredentialInfoMutex = Mutex()
    private var latestIssuerCredentialInfo: IssuerCredentialInfo? = null

    override suspend fun fetchRawAndParsedIssuerCredentialInformation(
        issuerEndpoint: URL,
    ): Result<RawAndParsedIssuerCredentialInfo, FetchIssuerCredentialInfoError> = latestIssuerCredentialInfoMutex.withLock {
        fetchRawAndParsedIssuerCredentialInfo(
            issuerEndpoint = issuerEndpoint,
        ).onSuccess {
            latestIssuerCredentialInfo = it.issuerCredentialInfo
        }
    }

    private suspend fun fetchRawAndParsedIssuerCredentialInfo(
        issuerEndpoint: URL,
    ): Result<RawAndParsedIssuerCredentialInfo, FetchIssuerCredentialInfoError> = coroutineBinding {
        val response = fetchIssuerCredentialInfo(issuerEndpoint).bind()
        val rawString = response.getRawMetadataString().bind()
        val info = safeJson.safeDecodeStringTo<IssuerCredentialInfo>(rawString)
            .mapError(JsonParsingError::toFetchIssuerCredentialInfoError).bind()
        RawAndParsedIssuerCredentialInfo(
            issuerCredentialInfo = info,
            rawIssuerCredentialInfo = rawString
        )
    }

    private suspend fun fetchIssuerCredentialInfo(
        issuerEndpoint: URL
    ): Result<HttpResponse, FetchIssuerCredentialInfoError> = coroutineBinding {
        // First prio is OID4VCI/IETF style (f. ex. https://example.com/issuer1 -> https://example.com/.well-known/openid-credential-issuer/issuer1
        runSuspendCatching {
            val builder = URLBuilder(issuerEndpoint.toString())
            builder.path("/.well-known/openid-credential-issuer${issuerEndpoint.path}")

            httpClient.get(builder.buildString()) {
                accept(applicationJwt)
                acceptLanguageHeader()
            }
        }.getOrElse {
            runSuspendCatching {
                // Second prio is OID Connect style (f. ex. https://example.com/issuer1 -> https://example.com/issuer1/.well-known/openid-credential-issuer
                httpClient.get("$issuerEndpoint/.well-known/openid-credential-issuer") {
                    accept(applicationJwt)
                    acceptLanguageHeader()
                }
            }.mapError(Throwable::toFetchIssuerCredentialInfoError).bind()
        }
    }

    private suspend fun HttpResponse.getRawMetadataString(): Result<String, CredentialOfferError.InvalidSignedMetadata> {
        val rawData = body<String>()
        return if (contentType()?.content == applicationJwt.content) {
            runSuspendCatching { Jwt(rawData) }.mapError { error ->
                val message = "issuer credential info jwt parsing failed"
                Timber.e(t = error, message = message)
                CredentialOfferError.InvalidSignedMetadata(error.localizedMessage ?: message)
            }.map { it.payloadString }
        } else {
            Ok(rawData)
        }
    }

    override suspend fun getIssuerCredentialInfo(
        issuerEndpoint: URL
    ): Result<IssuerCredentialInfo, FetchIssuerCredentialInfoError> = latestIssuerCredentialInfo?.let { Ok(it) } ?: Err(
        CredentialOfferError.Unexpected(
            null
        )
    )

    override suspend fun fetchIssuerConfiguration(issuerEndpoint: URL) = coroutineBinding {
        // First prio is OID4VCI/IETF style (f. ex. https://example.com/issuer1 -> https://example.com/.well-known/oauth-authorization-server/issuer1
        val response = runSuspendCatching {
            val builder = URLBuilder(issuerEndpoint.toString())
            builder.path("/.well-known/oauth-authorization-server${issuerEndpoint.path}")

            httpClient.get(builder.buildString()) {
                accept(applicationJwt)
            }
        }.getOrElse {
            runSuspendCatching {
                // Second prio is OID Connect style (f. ex. https://example.com/issuer1 -> https://example.com/issuer1/.well-known/oauth-authorization-server
                httpClient.get("$issuerEndpoint/.well-known/oauth-authorization-server") {
                    accept(applicationJwt)
                }
            }.mapError(Throwable::toFetchIssuerConfigurationError).bind()
        }

        parseIssuerConfiguration(response).bind()
    }

    private suspend fun parseIssuerConfiguration(response: HttpResponse) = coroutineBinding {
        val rawData = response.body<String>()
        if (response.contentType()?.content == applicationJwt.content) {
            val jwt = runSuspendCatching { Jwt(rawData) }
                .mapError { error ->
                    val message = "issuer configuration jwt parsing failed"
                    Timber.e(t = error, message = message)
                    CredentialOfferError.InvalidSignedMetadata(error.localizedMessage ?: message)
                }.bind()
            val info = parseIssuerConfiguration(jwt.payloadString).bind()
            IssuerConfigurationResponse.Signed(jwt, info)
        } else {
            IssuerConfigurationResponse.Plain(parseIssuerConfiguration(rawData).bind())
        }
    }

    private fun parseIssuerConfiguration(rawData: String): Result<IssuerConfiguration, FetchIssuerConfigurationError> {
        return safeJson.safeDecodeStringTo<IssuerConfiguration>(
            rawData
        ).mapError(JsonParsingError::toFetchIssuerConfigurationError)
    }

    override suspend fun fetchNonce(
        nonceEndpoint: URL
    ) = runSuspendCatching<String> {
        val response = httpClient.post(nonceEndpoint).body<NonceResponse>()
        response.cNonce
    }.mapError(Throwable::toFetchNonceError)

    override suspend fun fetchAccessToken(
        tokenEndpoint: URL,
        preAuthorizedCode: String
    ) = runSuspendCatching<TokenResponse> {
        val formParameters = Parameters.build {
            append("grant_type", PRE_AUTHORIZED_KEY)
            append("pre-authorized_code", preAuthorizedCode)
        }.formUrlEncode()

        httpClient.post(tokenEndpoint) {
            header(Constants.SWIYU_API_VERSION_HEADER, Constants.SWIYU_API_VERSION_2)
            setBody(
                // Using directly FormDataContent automatically adds a charset in the header, which makes the API fails.
                TextContent(formParameters, ContentType.Application.FormUrlEncoded)
            )
        }.body()
    }.mapError { throwable ->
        throwable.toFetchAccessTokenError()
    }

    private suspend fun Throwable.toFetchAccessTokenError(): FetchAccessTokenError = when (this) {
        is ClientRequestException -> {
            when (response.status) {
                HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized -> {
                    val errorBodyString = this.response.bodyAsText()
                    val errorBodyResult = safeJson.safeDecodeStringTo<HttpErrorBody>(errorBodyString)
                    errorBodyResult.mapBoth(
                        success = { errorBody ->
                            handleAccessTokenErrorBody(errorBody)
                        },
                        failure = { jsonParsingError ->
                            when (jsonParsingError) {
                                is JsonError.Unexpected -> CredentialOfferError.Unexpected(jsonParsingError.throwable)
                            }
                        }
                    )
                }

                else -> CredentialOfferError.Unexpected(this)
            }
        }

        is IOException -> CredentialOfferError.NetworkInfoError
        else -> CredentialOfferError.Unexpected(this)
    }

    private fun handleAccessTokenErrorBody(
        errorBody: HttpErrorBody,
    ): FetchAccessTokenError = when (errorBody.error.lowercase()) {
        // see https://www.rfc-editor.org/rfc/rfc6749.html#section-5.2
        // see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#section-6.3
        "invalid_request" -> CredentialOfferError.InvalidRequest
        "invalid_grant" -> CredentialOfferError.InvalidGrant
        "invalid_client" -> CredentialOfferError.InvalidClient
        "unauthorized_client" -> CredentialOfferError.UnauthorizedClient
        "unauthorized_grant_type" -> CredentialOfferError.UnauthorizedGrantType
        else -> CredentialOfferError.InvalidCredentialOffer
    }

    override suspend fun fetchAccessTokenByRefreshToken(tokenEndpoint: URL, refreshToken: String) = runSuspendCatching<TokenResponse> {
        val formParameters = Parameters.build {
            append("grant_type", REFRESH_TOKEN_KEY)
            append("refresh_token", refreshToken)
        }.formUrlEncode()

        httpClient.post(tokenEndpoint) {
            header(Constants.SWIYU_API_VERSION_HEADER, Constants.SWIYU_API_VERSION_2)
            setBody(
                TextContent(formParameters, ContentType.Application.FormUrlEncoded)
            )
        }.body()
    }.mapError { throwable ->
        throwable.toFetchAccessTokenError()
    }

    override suspend fun fetchCredential(
        issuerEndpoint: URL,
        tokenResponse: TokenResponse,
        credentialRequestType: CredentialRequestType,
        payloadEncryptionType: PayloadEncryptionType,
    ): Result<CredentialResponse, FetchVerifiableCredentialError> = coroutineBinding {
        val httpResponse = runSuspendCatching {
            httpClient.post(issuerEndpoint) {
                contentType(credentialRequestType.toContentType())
                addAuthorizationHeader(tokenResponse)
                header(Constants.SWIYU_API_VERSION_HEADER, Constants.SWIYU_API_VERSION_2)
                accept(ContentType.Application.Json) // without response encryption it's JSON
                accept(applicationJwt) // with response encryption it's JWE
                setBody(credentialRequestType.request)
            }
        }.mapError { throwable ->
            throwable.toFetchVerifiableCredentialError()
        }.bind()

        val responseBody = httpResponse.body<String>()

        val jsonObjectString = when (httpResponse.headers[HttpHeaders.ContentType]) {
            applicationJwt.content -> {
                // handle jwt content type
                if (payloadEncryptionType is PayloadEncryptionType.Response) {
                    decryptJWE(
                        jweString = responseBody,
                        privateKey = payloadEncryptionType.responseEncryptionKeyPair.keyPair.keyPair.private,
                    ).mapError(DecryptJWEError::toFetchVerifiableCredentialError)
                        .bind()
                } else {
                    Err(
                        CredentialOfferError.Unexpected(
                            IllegalStateException("Received encrypted response without asking for it")
                        )
                    ).bind<String>()
                }
            }

            else -> responseBody // handle json content type
        }

        runSuspendCatching {
            when (httpResponse.status) {
                HttpStatusCode.OK -> decodeVerifiableCredential(jsonObjectString)
                HttpStatusCode.Accepted -> decodeDeferredCredential(jsonObjectString)
                else -> error("Unhandled credential response status: ${httpResponse.status}")
            }
        }.mapError { throwable ->
            Timber.e(t = throwable, message = "Error during fetch verifiable credential")
            CredentialOfferError.Unexpected(throwable)
        }.bind()
    }

    private fun HttpRequestBuilder.addAuthorizationHeader(tokenResponse: TokenResponse) {
        val value = when (tokenResponse.tokenType.uppercase()) {
            "BEARER" -> "Bearer ${tokenResponse.accessToken}"
            else -> error("Unsupported access token type: ${tokenResponse.tokenType}")
        }
        header("Authorization", value)
    }

    private suspend fun Throwable.toFetchVerifiableCredentialError(): FetchVerifiableCredentialError = when (this) {
        is ClientRequestException -> {
            when (response.status) {
                HttpStatusCode.BadRequest -> {
                    val errorBodyString = this.response.bodyAsText()
                    val errorBodyResult = safeJson.safeDecodeStringTo<HttpErrorBody>(errorBodyString)
                    errorBodyResult.mapBoth(
                        success = { errorBody ->
                            handleVerifiableCredentialErrorBody(errorBody)
                        },
                        failure = { jsonParsingError ->
                            when (jsonParsingError) {
                                is JsonError.Unexpected -> CredentialOfferError.Unexpected(jsonParsingError.throwable)
                            }
                        }
                    )
                }

                else -> CredentialOfferError.Unexpected(this)
            }
        }

        is IOException -> CredentialOfferError.NetworkInfoError
        else -> CredentialOfferError.Unexpected(this)
    }

    private fun decodeVerifiableCredential(jsonObjectString: String): CredentialResponse {
        val response = safeJson.safeDecodeStringTo<CredentialResponse.VerifiableCredential>(
            jsonObjectString
        ).getOrElse { error -> error("Deserialisation to VerifiableCredential failed: $error") }
        check(response.credentials.isNotEmpty()) { "No credentials found" }
        return response
    }

    private fun decodeDeferredCredential(jsonObjectString: String): CredentialResponse {
        return safeJson.safeDecodeStringTo<CredentialResponse.DeferredCredential>(
            jsonObjectString
        ).getOrElse { error -> error("Deserialization to DeferredCredential failed: $error") }
    }

    private fun handleVerifiableCredentialErrorBody(
        errorBody: HttpErrorBody,
    ): FetchVerifiableCredentialError = when (errorBody.error.lowercase()) {
        // see https://www.rfc-editor.org/rfc/rfc6750.html#section-3.1
        "invalid_request" -> CredentialOfferError.InvalidRequestBearerToken
        "invalid_token" -> CredentialOfferError.InvalidToken
        "insufficient_scope" -> CredentialOfferError.InsufficientScope
        // see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#section-8.3.1.2
        "invalid_credential_request" -> CredentialOfferError.InvalidCredentialRequest
        "unknown_credential_configuration" -> CredentialOfferError.UnknownCredentialConfiguration
        "unknown_credential_identifier" -> CredentialOfferError.UnknownCredentialIdentifier
        "invalid_proof" -> CredentialOfferError.InvalidProof
        "invalid_nonce" -> CredentialOfferError.InvalidNonce
        "invalid_encryption_parameters" -> CredentialOfferError.InvalidEncryptionParameters
        "credential_request_denied" -> CredentialOfferError.CredentialRequestDenied
        else -> CredentialOfferError.InvalidCredentialOffer
    }

    override suspend fun fetchDeferredCredential(
        issuerEndpoint: String,
        accessToken: String,
        credentialRequestType: CredentialRequestType,
        payloadEncryptionType: PayloadEncryptionType,
    ): Result<CredentialResponse, FetchDeferredCredentialError> = coroutineBinding {
        val httpResponse = runSuspendCatching {
            httpClient.post(issuerEndpoint) {
                contentType(credentialRequestType.toContentType())
                header("Authorization", "BEARER $accessToken")
                header(Constants.SWIYU_API_VERSION_HEADER, Constants.SWIYU_API_VERSION_2)
                accept(ContentType.Application.Json) // without response encryption it's JSON
                accept(applicationJwt) // with response encryption it's JWE
                setBody(credentialRequestType.request)
            }
        }.mapError { throwable ->
            throwable.toFetchDeferredCredentialError()
        }.bind()

        val responseBody = httpResponse.body<String>()

        val jsonObjectString = when (httpResponse.headers[HttpHeaders.ContentType]) {
            applicationJwt.content -> {
                // handle jwt content type
                if (payloadEncryptionType is PayloadEncryptionType.Response) {
                    decryptJWE(
                        jweString = responseBody,
                        privateKey = payloadEncryptionType.responseEncryptionKeyPair.keyPair.keyPair.private,
                    ).mapError(DecryptJWEError::toFetchDeferredCredentialError)
                        .bind()
                } else {
                    Err(
                        CredentialOfferError.Unexpected(
                            IllegalStateException("Received encrypted response without asking for it")
                        )
                    ).bind<String>()
                }
            }

            else -> responseBody // handle json content type
        }

        runSuspendCatching {
            when (httpResponse.status) {
                HttpStatusCode.OK -> decodeVerifiableCredential(jsonObjectString)
                HttpStatusCode.Accepted -> decodeDeferredCredential(jsonObjectString)
                else -> error("Unhandled credential response status: ${httpResponse.status}")
            }
        }.mapError { throwable ->
            Timber.e(t = throwable, message = "Error during fetch verifiable credential")
            CredentialOfferError.Unexpected(throwable)
        }.bind()
    }

    private suspend fun Throwable.toFetchDeferredCredentialError(): FetchDeferredCredentialError {
        Timber.e(t = this, message = "fetch deferred credential failed")
        return when (this) {
            is ClientRequestException -> {
                when (response.status) {
                    HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> {
                        val errorBodyString = this.response.bodyAsText()
                        val errorBodyResult = safeJson.safeDecodeStringTo<HttpErrorBody>(errorBodyString)
                        errorBodyResult.mapBoth(
                            success = { errorBody ->
                                handleDeferredCredentialErrorBody(errorBody)
                            },
                            failure = { jsonParsingError ->
                                when (jsonParsingError) {
                                    is JsonError.Unexpected -> CredentialOfferError.Unexpected(jsonParsingError.throwable)
                                }
                            }
                        )
                    }

                    else -> CredentialOfferError.Unexpected(this)
                }
            }

            is IOException -> CredentialOfferError.NetworkInfoError
            else -> CredentialOfferError.Unexpected(this)
        }
    }

    private fun handleDeferredCredentialErrorBody(
        errorBody: HttpErrorBody,
    ): FetchDeferredCredentialError = when (errorBody.error.lowercase()) {
        // see https://www.rfc-editor.org/rfc/rfc6750.html#section-3.1
        "invalid_request" -> CredentialOfferError.InvalidRequestBearerToken
        "invalid_token" -> CredentialOfferError.InvalidToken
        "insufficient_scope" -> CredentialOfferError.InsufficientScope
        // see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#section-8.3.1.2
        "invalid_credential_request" -> CredentialOfferError.InvalidCredentialRequest
        "unknown_credential_configuration" -> CredentialOfferError.UnknownCredentialConfiguration
        "unknown_credential_identifier" -> CredentialOfferError.UnknownCredentialIdentifier
        "invalid_proof" -> CredentialOfferError.InvalidProof
        "invalid_nonce" -> CredentialOfferError.InvalidNonce
        "invalid_encryption_parameters" -> CredentialOfferError.InvalidEncryptionParameters
        "credential_request_denied" -> CredentialOfferError.CredentialRequestDenied
        // see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#section-9.3
        "invalid_transaction_id" -> CredentialOfferError.InvalidTransactionId
        else -> CredentialOfferError.InvalidCredentialOffer
    }

    companion object {
        private const val PRE_AUTHORIZED_KEY = "urn:ietf:params:oauth:grant-type:pre-authorized_code"
        private const val REFRESH_TOKEN_KEY = "urn:ietf:params:oauth:grant-type:refresh_token"
    }
}
private fun Throwable.toFetchNonceError(): FetchNonceError = when (this) {
    is IOException -> CredentialOfferError.NetworkInfoError
    else -> CredentialOfferError.Unexpected(this)
}

private fun Throwable.toFetchIssuerConfigurationError(): FetchIssuerConfigurationError = when (this) {
    is IOException -> CredentialOfferError.NetworkInfoError
    else -> CredentialOfferError.Unexpected(this)
}

private fun Throwable.toFetchIssuerCredentialInfoError(): FetchIssuerCredentialInfoError {
    Timber.e(t = this, message = "fetch issuer credential info failed")
    return when (this) {
        is IOException -> CredentialOfferError.NetworkInfoError
        else -> CredentialOfferError.Unexpected(this)
    }
}
