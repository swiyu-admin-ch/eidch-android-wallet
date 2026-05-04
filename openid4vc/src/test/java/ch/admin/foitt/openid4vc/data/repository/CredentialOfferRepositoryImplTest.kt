package ch.admin.foitt.openid4vc.data.repository

import android.webkit.URLUtil
import ch.admin.foitt.openid4vc.data.CredentialOfferRepositoryImpl
import ch.admin.foitt.openid4vc.data.repository.mock.CredentialOfferRepoMocks.mockSoftwareKeyPair
import ch.admin.foitt.openid4vc.domain.model.CredentialRequestType
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.TokenResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerConfigurationResponse
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionKeyPair
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.usecase.jwe.DecryptJWE
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import ch.admin.foitt.openid4vc.util.assertTrue
import ch.admin.foitt.openid4vc.utils.ContentType.applicationJwt
import ch.admin.foitt.openid4vc.utils.content
import com.github.michaelbull.result.Ok
import com.nimbusds.jose.CompressionAlgorithm
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.ECDHEncrypter
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.URL
import java.security.KeyPair
import java.security.interfaces.ECPublicKey

class CredentialOfferRepositoryImplTest {

    private lateinit var handler: (suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData)

    @MockK
    private lateinit var mockDecryptJWE: DecryptJWE

    private val json = SafeJsonTestInstance.safeJson

    private val mockHttpClient by lazy {

        HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    handler(request)
                }
            }
        }
    }

    private lateinit var repo: CredentialOfferRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(URLUtil::class)
        every { URLUtil.isHttpsUrl(any()) } returns true

        repo = CredentialOfferRepositoryImpl(
            httpClient = mockHttpClient,
            safeJson = json,
            decryptJWE = mockDecryptJWE,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching issuer metadata correctly sets Accept-Language header`() = runTest {
        handler = { request ->
            when {
                request.isAcceptLanguageIssuerResponse() -> respond(
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.content),
                    content = createIssuerMetadataJson(ISSUER_ACCEPT_LANGUAGE)
                )
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val url = URI.create("$BASE_URL$ISSUER_ACCEPT_LANGUAGE").toURL()

        val result = repo.fetchRawAndParsedIssuerCredentialInformation(
            issuerEndpoint = url,
        ).assertOk()

        val expected = "$BASE_URL$CREDENTIAL_PATH$ISSUER_ACCEPT_LANGUAGE"

        assertEquals(expected, result.issuerCredentialInfo.credentialIssuer.toString())
    }

    @Test
    fun `Fetching credential issuer metadata uses OID4VCI url as first prio`() = runTest {
        handler = { request ->
            when {
                request.isMetadataOID4VCIIssuerResponse() -> respond(
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.content),
                    content = credentialIssuerMetadataOID4VCIResponse
                )
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val url = URI.create("$BASE_URL$ISSUER_OID4VCI").toURL()

        val result = repo.fetchRawAndParsedIssuerCredentialInformation(url).assertOk()

        val expected = "$BASE_URL$CREDENTIAL_PATH$ISSUER_OID4VCI"

        assertEquals(expected, result.issuerCredentialInfo.credentialIssuer.toString())
    }

    @Test
    fun `Fetching credential issuer metadata uses OIDC url as second prio`() = runTest {
        handler = { request ->
            when {
                request.isMetadataOID4VCIIssuerErrorResponse() -> throw IOException("network failure")

                request.isMetadataOIDCIssuerResponse() -> respond(
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.content),
                    content = credentialIssuerMetadataOIDCResponse
                )
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val url = URI.create("$BASE_URL$ISSUER_OIDC").toURL()

        val result = repo.fetchRawAndParsedIssuerCredentialInformation(url).assertOk()

        val expected = "$BASE_URL$CREDENTIAL_PATH$ISSUER_OIDC"

        assertEquals(expected, result.issuerCredentialInfo.credentialIssuer.toString())
    }

    @Test
    fun `Fetching credential issuer metadata returns error if both return an error`() = runTest {
        handler = { request ->
            when {
                request.isMetadataOID4VCIOtherIssuerErrorResponse() -> throw IOException("network failure")
                request.isMetadataOIDCOtherIssuerErrorResponse() -> throw IOException("network failure")
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val url = URI.create("$BASE_URL/otherIssuer").toURL()

        repo.fetchRawAndParsedIssuerCredentialInformation(url).assertErrorType(CredentialOfferError.NetworkInfoError::class)
    }

    @Test
    fun `Fetching credential issuer config uses OID4VCI url as first prio`() = runTest {
        handler = { request ->
            when {
                request.isConfigOID4VCIIssuerResponse() -> respond(
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.content),
                    content = credentialIssuerConfigOID4VCIResponse
                )
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val url = URI.create("$BASE_URL$ISSUER_OID4VCI").toURL()

        val result = repo.fetchIssuerConfiguration(url).assertOk() as IssuerConfigurationResponse.Plain

        val expected = "$BASE_URL$ISSUER_PATH$ISSUER_OID4VCI"

        assertEquals(expected, result.config.issuer.toString())
    }

    @Test
    fun `Fetching credential issuer config uses OIDC url as second prio`() = runTest {
        handler = { request ->
            when {
                request.isConfigOID4VCIIssuerErrorResponse() -> throw IOException("network failure")

                request.isConfigOIDCIssuerResponse() -> respond(
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.content),
                    content = credentialIssuerConfigOIDCResponse
                )
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val url = URI.create("$BASE_URL$ISSUER_OIDC").toURL()

        val result = repo.fetchIssuerConfiguration(url).assertOk() as IssuerConfigurationResponse.Plain

        val expected = "$BASE_URL$ISSUER_PATH$ISSUER_OIDC"

        assertEquals(expected, result.config.issuer.toString())
    }

    @Test
    fun `Fetching credential issuer config returns error if both return an error`() = runTest {
        handler = { request ->
            when {
                request.isConfigOID4VCIOtherIssuerErrorResponse() -> throw IOException("network failure")
                request.isConfigOIDCOtherIssuerErrorResponse() -> throw IOException("network failure")
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val url = URI.create("$BASE_URL/otherIssuer").toURL()

        repo.fetchIssuerConfiguration(url).assertErrorType(CredentialOfferError.NetworkInfoError::class)
    }

    @Test
    fun `Fetching verifiable credential returns verifiable credential result`() = runTest {
        handler = { request ->
            when {
                request.isVerifiableCredentialRequestWithJsonResponse() -> respond(
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.content),
                    content = verifiableCredentialResponseJson,
                )
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val result = repo.fetchCredential(
            issuerEndpoint = verifiableCredentialUrl,
            tokenResponse = tokenResponse,
            credentialRequestType = CredentialRequestType.Json("credentialRequest"),
            payloadEncryptionType = PayloadEncryptionType.None,
        ).assertOk()

        assertTrue(result is CredentialResponse.VerifiableCredential) { "response is not a verifiable credential" }
        val credential = (result as CredentialResponse.VerifiableCredential).credentials.first().credential
        assertEquals("credentialJwt", credential)
    }

    @Test
    fun `Fetching deferred credential returns deferred credential result`() = runTest {
        handler = { request ->
            when {
                request.isDeferredCredentialRequestWithJsonResponse() -> respond(
                    status = HttpStatusCode.Accepted,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.content),
                    content = deferredCredentialResponseJson,
                )
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val result = repo.fetchCredential(
            issuerEndpoint = deferredCredentialUrl,
            tokenResponse = tokenResponse,
            credentialRequestType = CredentialRequestType.Json("credentialRequest"),
            payloadEncryptionType = PayloadEncryptionType.None,
        ).assertOk()

        assertTrue(result is CredentialResponse.DeferredCredential) { "response is not a deferred credential" }
        val transactionId = (result as CredentialResponse.DeferredCredential).transactionId
        assertEquals("trxId", transactionId)
    }

    @Test
    fun `Fetching encrypted verifiable credential returns verifiable credential result`() = runTest {
        val keyPair = mockSoftwareKeyPair
        val httpResponseString = createCredentialJwe(keyPair)

        handler = { request ->
            when {
                request.isVerifiableCredentialRequestWithResponseEncryption() -> respond(
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, applicationJwt.content),
                    content = httpResponseString,
                )
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        coEvery { mockDecryptJWE(httpResponseString, keyPair.private) } returns Ok(verifiableCredentialResponseJson)

        val result = repo.fetchCredential(
            issuerEndpoint = verifiableCredentialUrl,
            tokenResponse = tokenResponse,
            credentialRequestType = CredentialRequestType.Jwt("credentialRequest"),
            payloadEncryptionType = createResponseEncryption(keyPair)
        ).assertOk()

        assertTrue(result is CredentialResponse.VerifiableCredential) { "response is not a verifiable credential" }
        val credential = (result as CredentialResponse.VerifiableCredential).credentials.first().credential
        assertEquals("credentialJwt", credential)
    }

    @Test
    fun `Fetching encrypted deferred credential returns deferred credential result`() = runTest {
        val keyPair = mockSoftwareKeyPair
        val httpResponseString = createCredentialJwe(keyPair)

        handler = { request ->
            when {
                request.isDeferredCredentialRequestWithResponseEncryption() -> respond(
                    status = HttpStatusCode.Accepted,
                    headers = headersOf(HttpHeaders.ContentType, applicationJwt.content),
                    content = httpResponseString,
                )
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        coEvery { mockDecryptJWE(httpResponseString, keyPair.private) } returns Ok(deferredCredentialResponseJson)

        val result = repo.fetchCredential(
            issuerEndpoint = deferredCredentialUrl,
            tokenResponse = tokenResponse,
            credentialRequestType = CredentialRequestType.Jwt("credentialRequest"),
            payloadEncryptionType = createResponseEncryption(keyPair)
        ).assertOk()

        assertTrue(result is CredentialResponse.DeferredCredential) { "response is not a deferred credential" }
        val transactionId = (result as CredentialResponse.DeferredCredential).transactionId
        assertEquals("trxId", transactionId)
    }

    @Test
    fun `Receiving an encrypted response without requesting it returns an error`() = runTest {
        handler = { request ->
            when {
                request.isVerifiableCredentialRequestWithJsonResponse() -> respond(
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, applicationJwt.content),
                    content = createCredentialJwe(mockSoftwareKeyPair),
                )
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val result = repo.fetchCredential(
            issuerEndpoint = verifiableCredentialUrl,
            tokenResponse = tokenResponse,
            credentialRequestType = CredentialRequestType.Json("credentialRequest"),
            payloadEncryptionType = PayloadEncryptionType.None
        ).assertErrorType(CredentialOfferError.Unexpected::class)

        assertEquals("Received encrypted response without asking for it", result.cause?.message)
    }

    @Test
    fun `Receiving a verifiable credential response with empty credential array returns an error`() = runTest {
        handler = { request ->
            when {
                request.isVerifiableCredentialRequestWithJsonResponse() -> respond(
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.content),
                    content = emptyCredentialResponseJson,
                )
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        repo.fetchCredential(
            issuerEndpoint = verifiableCredentialUrl,
            tokenResponse = tokenResponse,
            credentialRequestType = CredentialRequestType.Json("credentialRequest"),
            payloadEncryptionType = PayloadEncryptionType.None,
        ).assertErrorType(CredentialOfferError.Unexpected::class)
    }

    @Test
    fun `Receiving a credential with an invalid json structure returns an error`() = runTest {
        handler = { request ->
            when {
                request.isVerifiableCredentialRequestWithJsonResponse() -> respond(
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.content),
                    content = invalidCredentialResponseJson,
                )
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        repo.fetchCredential(
            issuerEndpoint = verifiableCredentialUrl,
            tokenResponse = tokenResponse,
            credentialRequestType = CredentialRequestType.Json("credentialRequest"),
            payloadEncryptionType = PayloadEncryptionType.None,
        ).assertErrorType(CredentialOfferError.Unexpected::class)
    }

    @Test
    fun `Network error during fetching credential returns an error`() = runTest {
        handler = { request ->
            when {
                request.isErrorResponse() -> throw IOException("network failure")
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        repo.fetchCredential(
            issuerEndpoint = errorUrl,
            tokenResponse = tokenResponse,
            credentialRequestType = CredentialRequestType.Json("credentialRequest"),
            payloadEncryptionType = PayloadEncryptionType.None,
        ).assertErrorType(CredentialOfferError.NetworkInfoError::class)
    }

    private fun createCredentialJwe(keyPair: KeyPair): String {
        val jweHeader = JWEHeader.Builder(JWEAlgorithm.ECDH_ES, EncryptionMethod.A128GCM)
            .compressionAlgorithm(CompressionAlgorithm.DEF)
            .build()
        val jwePayload = Payload(verifiableCredentialResponseJson)
        val jwe = JWEObject(jweHeader, jwePayload)
        jwe.encrypt(ECDHEncrypter(keyPair.public as ECPublicKey))
        return jwe.serialize()
    }

    private fun createResponseEncryption(keyPair: KeyPair): PayloadEncryptionType.Response = PayloadEncryptionType.Response(
        requestEncryption = mockk(),
        responseEncryption = mockk(),
        responseEncryptionKeyPair = PayloadEncryptionKeyPair(
            keyPair = JWSKeyPair(
                algorithm = SigningAlgorithm.ES256,
                keyPair = keyPair,
                keyId = "keyId",
                bindingType = KeyBindingType.SOFTWARE
            ),
            alg = "alg",
            enc = "enc",
            zip = "zip"
        )
    )

    private fun HttpRequestData.isAcceptLanguageIssuerResponse() =
        this.method == HttpMethod.Get && this.url.encodedPath == "$ISSUER_METADATA_PATH$ISSUER_ACCEPT_LANGUAGE" &&
            this.headers[HttpHeaders.AcceptLanguage] == "de-CH, en, fr-CH, it-CH, rm"

    private fun HttpRequestData.isMetadataOID4VCIIssuerResponse() = mockResponse(expectedPath = "$ISSUER_METADATA_PATH$ISSUER_OID4VCI")

    private fun HttpRequestData.isMetadataOID4VCIIssuerErrorResponse() = mockResponse(expectedPath = "$ISSUER_METADATA_PATH$ISSUER_OIDC")

    private fun HttpRequestData.isMetadataOIDCIssuerResponse() = mockResponse(expectedPath = "$ISSUER_OIDC$ISSUER_METADATA_PATH")

    private fun HttpRequestData.isMetadataOID4VCIOtherIssuerErrorResponse() = mockResponse(
        expectedPath = "$ISSUER_METADATA_PATH/otherIssuer"
    )

    private fun HttpRequestData.isMetadataOIDCOtherIssuerErrorResponse() = mockResponse(expectedPath = "/otherIssuer$ISSUER_METADATA_PATH")

    private fun HttpRequestData.isConfigOID4VCIIssuerResponse() = mockResponse(expectedPath = "$ISSUER_CONFIG_PATH$ISSUER_OID4VCI")

    private fun HttpRequestData.isConfigOID4VCIIssuerErrorResponse() = mockResponse(expectedPath = "$ISSUER_CONFIG_PATH$ISSUER_OIDC")

    private fun HttpRequestData.isConfigOIDCIssuerResponse() = mockResponse(expectedPath = "$ISSUER_OIDC$ISSUER_CONFIG_PATH")

    private fun HttpRequestData.isConfigOID4VCIOtherIssuerErrorResponse() = mockResponse(expectedPath = "$ISSUER_CONFIG_PATH/otherIssuer")

    private fun HttpRequestData.isConfigOIDCOtherIssuerErrorResponse() = mockResponse(expectedPath = "/otherIssuer$ISSUER_CONFIG_PATH")

    private fun HttpRequestData.isVerifiableCredentialRequestWithJsonResponse() = mockResponse(
        method = HttpMethod.Post,
        expectedPath = "$CREDENTIAL_PATH$VERIFIABLE_PATH",
        contentType = ContentType.Application.Json.content
    )

    private fun HttpRequestData.isDeferredCredentialRequestWithJsonResponse() = mockResponse(
        method = HttpMethod.Post,
        expectedPath = "$CREDENTIAL_PATH$DEFERRED_PATH",
        contentType = ContentType.Application.Json.content
    )

    private fun HttpRequestData.isVerifiableCredentialRequestWithResponseEncryption() = mockResponse(
        method = HttpMethod.Post,
        expectedPath = "$CREDENTIAL_PATH$VERIFIABLE_PATH",
        contentType = applicationJwt.content
    )

    private fun HttpRequestData.isDeferredCredentialRequestWithResponseEncryption() = mockResponse(
        method = HttpMethod.Post,
        expectedPath = "$CREDENTIAL_PATH$DEFERRED_PATH",
        contentType = applicationJwt.content
    )

    private fun HttpRequestData.isErrorResponse() = mockResponse(method = HttpMethod.Post, expectedPath = "$CREDENTIAL_PATH$ERROR_PATH")

    private fun HttpRequestData.mockResponse(
        method: HttpMethod = HttpMethod.Get,
        expectedPath: String,
        contentType: String? = null
    ): Boolean {
        val result = this.method == method && this.url.encodedPath == expectedPath

        return if (contentType != null) {
            result && this.body.contentType?.content == contentType
        } else {
            result
        }
    }

    private companion object {
        const val ISSUER_METADATA_PATH = "/.well-known/openid-credential-issuer"
        const val ISSUER_CONFIG_PATH = "/.well-known/oauth-authorization-server"
        const val ISSUER_ACCEPT_LANGUAGE = "/issuerAcceptLanguage"
        const val ISSUER_OID4VCI = "/issuerOID4VCI"
        const val ISSUER_OIDC = "/issuerOIDC"

        fun createIssuerMetadataJson(issuer: String) = """
            {
                "credential_endpoint": "https://example.com/credential/endpoint",
                "credential_issuer": "https://example.com/credential$issuer",
                "credential_configurations_supported": {
                    "identifier": {
                        "format": "vc+sd-jwt",
                        "vct": "vct",
                        "credential_signing_alg_values_supported": [
                            "ES256"
                        ],
                        "proof_types_supported": {
                            "jwt": {
                                "proof_signing_alg_values_supported": [
                                    "ES256"
                                ]
                            }
                        }
                    }
                }
            }
        """.trimIndent()
        val credentialIssuerMetadataOID4VCIResponse = createIssuerMetadataJson(ISSUER_OID4VCI)

        val credentialIssuerMetadataOIDCResponse = createIssuerMetadataJson(ISSUER_OIDC)

        val credentialIssuerConfigOID4VCIResponse = """
            {
                "issuer": "https://example.com/issuer$ISSUER_OID4VCI",
                "token_endpoint": "https://example.com/token/endpoint"
            }
        """.trimIndent()

        val credentialIssuerConfigOIDCResponse = """
            {
                "issuer": "https://example.com/issuer$ISSUER_OIDC",
                "token_endpoint": "https://example.com/token/endpoint"
            }
        """.trimIndent()

        const val BASE_URL = "https://example.com"
        const val ISSUER_PATH = "/issuer"
        const val CREDENTIAL_PATH = "/credential"
        const val VERIFIABLE_PATH = "/verifiable"
        const val DEFERRED_PATH = "/deferred"
        const val ERROR_PATH = "/error"
        val verifiableCredentialUrl = createUrl(VERIFIABLE_PATH)
        val deferredCredentialUrl = createUrl(DEFERRED_PATH)
        val errorUrl = createUrl(ERROR_PATH)
        val tokenResponse = TokenResponse(
            accessToken = "accessToken",
            tokenType = "bearer",
        )

        val verifiableCredentialResponseJson = """
            {
                "credentials": [
                    {
                        "credential": "credentialJwt"
                    }
                ]
            }
        """.trimIndent()

        val emptyCredentialResponseJson = """
            {
                "credentials": []
            }
        """.trimIndent()

        val deferredCredentialResponseJson = """
            {
                "transaction_id": "trxId",
                "interval": 100
            }
        """.trimIndent()

        val invalidCredentialResponseJson = """
            {
                "content": "invalid"
            }
        """.trimIndent()

        fun createUrl(endpoint: String): URL = URI.create("$BASE_URL$CREDENTIAL_PATH$endpoint").toURL()
    }
}
