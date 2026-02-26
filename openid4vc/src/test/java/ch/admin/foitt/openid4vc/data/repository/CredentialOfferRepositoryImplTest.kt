package ch.admin.foitt.openid4vc.data.repository

import ch.admin.foitt.openid4vc.data.CredentialOfferRepositoryImpl
import ch.admin.foitt.openid4vc.data.repository.mock.CredentialOfferRepoMocks.mockSoftwareKeyPair
import ch.admin.foitt.openid4vc.domain.model.CredentialRequestType
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.TokenResponse
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
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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

    @MockK
    private lateinit var mockDecryptJWE: DecryptJWE

    private val json = SafeJsonTestInstance.safeJson

    private var httpResponseString = ""

    private val mockHttpClient = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                when {
                    request.isVerifiableCredentialRequestWithJsonResponse() -> respond(
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.content),
                        content = httpResponseString,
                    )

                    request.isDeferredCredentialRequestWithJsonResponse() -> respond(
                        status = HttpStatusCode.Accepted,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.content),
                        content = httpResponseString,
                    )

                    request.isVerifiableCredentialRequestWithResponseEncryption() -> respond(
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, applicationJwt.content),
                        content = httpResponseString,
                    )

                    request.isDeferredCredentialRequestWithResponseEncryption() -> respond(
                        status = HttpStatusCode.Accepted,
                        headers = headersOf(HttpHeaders.ContentType, applicationJwt.content),
                        content = httpResponseString,
                    )

                    request.isErrorResponse() -> throw IOException("network failure")

                    else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
                }
            }
        }
    }

    private lateinit var repo: CredentialOfferRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

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
    fun `Fetching verifiable credential returns verifiable credential result`() = runTest {
        httpResponseString = verifiableCredentialResponseJson

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
        httpResponseString = deferredCredentialResponseJson

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
        httpResponseString = createCredentialJwe(keyPair)

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
        httpResponseString = createCredentialJwe(keyPair)

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
        val keyPair = mockSoftwareKeyPair
        httpResponseString = createCredentialJwe(keyPair)

        val result = repo.fetchCredential(
            issuerEndpoint = verifiableCredentialUrl,
            tokenResponse = tokenResponse,
            credentialRequestType = CredentialRequestType.Jwt("credentialRequest"),
            payloadEncryptionType = PayloadEncryptionType.None
        ).assertErrorType(CredentialOfferError.Unexpected::class)

        assertEquals("Received encrypted response without asking for it", result.cause?.message)
    }

    @Test
    fun `Receiving a verifiable credential response with empty credential array returns an error`() = runTest {
        httpResponseString = emptyCredentialResponseJson

        repo.fetchCredential(
            issuerEndpoint = verifiableCredentialUrl,
            tokenResponse = tokenResponse,
            credentialRequestType = CredentialRequestType.Json("credentialRequest"),
            payloadEncryptionType = PayloadEncryptionType.None,
        ).assertErrorType(CredentialOfferError.Unexpected::class)
    }

    @Test
    fun `Receiving a credential with an invalid json structure returns an error`() = runTest {
        httpResponseString = invalidCredentialResponseJson

        repo.fetchCredential(
            issuerEndpoint = verifiableCredentialUrl,
            tokenResponse = tokenResponse,
            credentialRequestType = CredentialRequestType.Json("credentialRequest"),
            payloadEncryptionType = PayloadEncryptionType.None,
        ).assertErrorType(CredentialOfferError.Unexpected::class)
    }

    @Test
    fun `Network error during fetching credential returns an error`() = runTest {
        httpResponseString = verifiableCredentialResponseJson

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

    private fun HttpRequestData.isVerifiableCredentialRequestWithJsonResponse() =
        this.method == HttpMethod.Post && this.url.encodedPath == "$CREDENTIAL_PATH$VERIFIABLE_PATH" && this.body.contentType?.content == ContentType.Application.Json.content

    private fun HttpRequestData.isDeferredCredentialRequestWithJsonResponse() =
        this.method == HttpMethod.Post && this.url.encodedPath == "$CREDENTIAL_PATH$DEFERRED_PATH" && this.body.contentType?.content == ContentType.Application.Json.content

    private fun HttpRequestData.isVerifiableCredentialRequestWithResponseEncryption() =
        this.method == HttpMethod.Post && this.url.encodedPath == "$CREDENTIAL_PATH$VERIFIABLE_PATH" && this.body.contentType?.content == applicationJwt.content

    private fun HttpRequestData.isDeferredCredentialRequestWithResponseEncryption() =
        this.method == HttpMethod.Post && this.url.encodedPath == "$CREDENTIAL_PATH$DEFERRED_PATH" && this.body.contentType?.content == applicationJwt.content

    private fun HttpRequestData.isErrorResponse() =
        this.method == HttpMethod.Post && this.url.encodedPath == "$CREDENTIAL_PATH$ERROR_PATH"

    private companion object {
        const val BASE_URL = "https://example.com"
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
