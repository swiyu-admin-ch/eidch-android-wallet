@file:OptIn(UnsafeResultValueAccess::class)

package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.jwe.JWEError
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwks
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.EncryptionAlgorithm
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseParam
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseType
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyVerifiablePresentation
import ch.admin.foitt.openid4vc.domain.usecase.GetAuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.usecase.jwe.CreateJWE
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import ch.admin.foitt.openid4vc.util.assertErr
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetAuthorizationResponseConfigImplTest {

    private val safeJson = SafeJsonTestInstance.safeJsonWithDiscriminator

    @MockK
    private lateinit var mockCreateAnyVerifiablePresentation: CreateAnyVerifiablePresentation

    @MockK
    private lateinit var mockCreateJWE: CreateJWE

    @MockK
    private lateinit var mockAuthorizationRequest: AuthorizationRequest

    @MockK
    private lateinit var mockClientMetadata: ClientMetaData

    @MockK
    private lateinit var mockAnyCredential: AnyCredential

    private lateinit var useCase: GetAuthorizationResponseConfig

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        setupDefaultMocks()

        useCase = GetAuthorizationResponseConfigImpl(
            createAnyVerifiablePresentation = mockCreateAnyVerifiablePresentation,
            safeJson = safeJson,
            createJWE = mockCreateJWE,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `With payload encryption disabled and a dcql body return a dcql presentation type Json`() = runTest {
        val result = useCase(
            anyCredential = mockAnyCredential,
            presentationPaths = emptyList(),
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = false,
            dcqlQueryId = DCQL_QUERY_ID,
        ).assertOk()

        val expected = AuthorizationResponseConfig(
            type = AuthorizationResponseType.DCQL,
            params = mapOf(
                AuthorizationResponseParam.VP_TOKEN to safeJson.safeEncodeObjectToString(mapOf(DCQL_QUERY_ID to listOf(VP_TOKEN))).value,
                AuthorizationResponseParam.STATE to STATE,
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `For response mode 'direct_post' returns a dcql presentation type Json`() = runTest {
        val result = useCase(
            anyCredential = mockAnyCredential,
            presentationPaths = emptyList(),
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = DCQL_QUERY_ID,
        ).assertOk()

        val expected = AuthorizationResponseConfig(
            type = AuthorizationResponseType.DCQL,
            params = mapOf(
                AuthorizationResponseParam.VP_TOKEN to safeJson.safeEncodeObjectToString(mapOf(DCQL_QUERY_ID to listOf(VP_TOKEN))).value,
                AuthorizationResponseParam.STATE to STATE,
            ),
        )

        assertEquals(expected, result)
    }

    @Test
    fun `For response mode 'direct_post' with DCQL, state parameter is not set if not in presentation request`() = runTest {
        every { mockAuthorizationRequest.state } returns null

        val result = useCase(
            anyCredential = mockAnyCredential,
            presentationPaths = emptyList(),
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = DCQL_QUERY_ID,
        ).assertOk()

        val expected = AuthorizationResponseConfig(
            type = AuthorizationResponseType.DCQL,
            params = mapOf(
                AuthorizationResponseParam.VP_TOKEN to safeJson.safeEncodeObjectToString(mapOf(DCQL_QUERY_ID to listOf(VP_TOKEN))).value,
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' returns a presentation type JWT`() = runTest {
        every { mockAuthorizationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT

        val result = useCase(
            anyCredential = mockAnyCredential,
            presentationPaths = emptyList(),
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = DCQL_QUERY_ID,
        ).assertOk()

        val expected = AuthorizationResponseConfig(
            type = AuthorizationResponseType.DCQL,
            params = mapOf(
                AuthorizationResponseParam.RESPONSE to "presentation request jwe",
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' where no jwk kty is supported returns an error`() = runTest {
        every { mockAuthorizationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        every { mockClientMetadata.jwks } returns Jwks(keys = listOf(jwk.copy(kty = "unsupported key type")))

        useCase(
            anyCredential = mockAnyCredential,
            presentationPaths = emptyList(),
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = DCQL_QUERY_ID,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' where no jwk alg is supported returns an error`() = runTest {
        every { mockAuthorizationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        every { mockClientMetadata.jwks } returns Jwks(keys = listOf(jwk.copy(alg = "unsupported key type")))

        useCase(
            anyCredential = mockAnyCredential,
            presentationPaths = emptyList(),
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = DCQL_QUERY_ID,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' where no jwk crv is supported returns an error`() = runTest {
        every { mockAuthorizationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        every { mockClientMetadata.jwks } returns Jwks(keys = listOf(jwk.copy(crv = "unsupported key type")))

        useCase(
            anyCredential = mockAnyCredential,
            presentationPaths = emptyList(),
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = DCQL_QUERY_ID,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' where no jwk is provided returns an error`() = runTest {
        every { mockAuthorizationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        every { mockClientMetadata.jwks } returns Jwks(keys = emptyList())

        useCase(
            anyCredential = mockAnyCredential,
            presentationPaths = emptyList(),
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = DCQL_QUERY_ID,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' where no encryption value is supported returns an error`() = runTest {
        every { mockAuthorizationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        every { mockClientMetadata.encryptedResponseEncValuesSupported } returns listOf("unsupported value")

        useCase(
            anyCredential = mockAnyCredential,
            presentationPaths = emptyList(),
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = DCQL_QUERY_ID,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' where no encryption value is provided uses default value`() = runTest {
        every { mockAuthorizationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        every { mockClientMetadata.encryptedResponseEncValuesSupported } returns null

        useCase(
            anyCredential = mockAnyCredential,
            presentationPaths = emptyList(),
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = DCQL_QUERY_ID,
        ).assertOk()

        coVerify {
            mockCreateJWE(
                algorithm = any(),
                encryptionMethod = "A256GCM",
                compressionAlgorithm = any(),
                payload = any(),
                encryptionKey = any()
            )
        }
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' maps errors from JWE creation`() = runTest {
        every { mockAuthorizationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        coEvery {
            mockCreateJWE(any(), any(), any(), any(), any())
        } returns Err(JWEError.Unexpected(IllegalStateException("jwe error")))

        useCase(
            anyCredential = mockAnyCredential,
            presentationPaths = emptyList(),
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = DCQL_QUERY_ID,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `Maps errors from verifiable presentation creation`() = runTest {
        val expectedThrowable = IllegalStateException("vp creation error")
        coEvery {
            mockCreateAnyVerifiablePresentation(any(), any(), any())
        } returns Err(PresentationRequestError.Unexpected(expectedThrowable))

        val error = useCase(
            anyCredential = mockAnyCredential,
            presentationPaths = emptyList(),
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = DCQL_QUERY_ID,
        ).assertErr()

        assertSame(expectedThrowable, (error as PresentationRequestError.Unexpected).throwable)
        coVerify(exactly = 0) { mockCreateJWE(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `For any other response mode returns and error`() = runTest {
        every { mockAuthorizationRequest.responseMode } returns "other"

        useCase(
            anyCredential = mockAnyCredential,
            presentationPaths = emptyList(),
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = DCQL_QUERY_ID,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    private val jwk = Jwk(
        x = X_VALUE,
        y = Y_VALUE,
        crv = CURVE,
        kty = KEY_TYPE,
        use = KEY_USE,
        alg = ALG_VALUE,
    )

    private val jwks = Jwks(
        keys = listOf(jwk)
    )

    private fun setupDefaultMocks() {
        every { mockAuthorizationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST
        every { mockAuthorizationRequest.clientMetaData } returns mockClientMetadata
        every { mockAuthorizationRequest.state } returns STATE

        coEvery {
            mockCreateAnyVerifiablePresentation(any(), any(), any())
        } returns Ok(VP_TOKEN)

        every { mockClientMetadata.jwks } returns jwks
        every { mockClientMetadata.encryptedResponseEncValuesSupported } returns listOf(ENCRYPTION_VALUE_2, ENCRYPTION_VALUE_1)

        val payloadJsonDcql = buildJsonObject {
            put("vp_token", safeJson.safeEncodeObjectToString(mapOf(DCQL_QUERY_ID to listOf(VP_TOKEN))).value)
            put("state", STATE)
        }

        coEvery {
            mockCreateJWE(
                algorithm = ALG_VALUE,
                encryptionMethod = ENCRYPTION_VALUE_2,
                compressionAlgorithm = any(),
                payload = safeJson.safeEncodeObjectToString(payloadJsonDcql).value,
                encryptionKey = jwk,
            )
        } returns Ok("presentation request jwe")
    }

    private companion object Companion {
        const val RESPONSE_MODE_DIRECT_POST = "direct_post"
        const val RESPONSE_MODE_DIRECT_POST_JWT = "direct_post.jwt"
        const val X_VALUE = "x value"
        const val Y_VALUE = "y value"
        const val CURVE = "P-256"
        const val KEY_TYPE = "EC"
        const val KEY_USE = "enc"
        const val ALG_VALUE = "ECDH-ES"
        val ENCRYPTION_VALUE_1 = EncryptionAlgorithm.A128GCM.name
        val ENCRYPTION_VALUE_2 = EncryptionAlgorithm.A256GCM.name
        const val VP_TOKEN = "vp token"
        const val DCQL_QUERY_ID = "dcql query id"
        const val STATE = "state"
    }
}
