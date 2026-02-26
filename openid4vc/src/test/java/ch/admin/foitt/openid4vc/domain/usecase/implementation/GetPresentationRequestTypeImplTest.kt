@file:OptIn(UnsafeResultValueAccess::class)

package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwe.JWEError
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwks
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestBody
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestType
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationSubmission
import ch.admin.foitt.openid4vc.domain.usecase.GetPresentationRequestType
import ch.admin.foitt.openid4vc.domain.usecase.jwe.CreateJWE
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetPresentationRequestTypeImplTest {

    private val safeJson = SafeJsonTestInstance.safeJsonWithDiscriminator

    @MockK
    private lateinit var mockCreateJWE: CreateJWE

    @MockK
    private lateinit var mockPresentationRequest: PresentationRequest

    @MockK
    private lateinit var mockPresentationRequestBody: PresentationRequestBody

    @MockK
    private lateinit var mockClientMetadata: ClientMetaData

    private lateinit var useCase: GetPresentationRequestType

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        setupDefaultMocks()

        useCase = GetPresentationRequestTypeImpl(
            safeJson = safeJson,
            createJWE = mockCreateJWE,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `With payload encryption disabled return a presentation type Json`() = runTest {
        val result = useCase(
            presentationRequest = mockPresentationRequest,
            presentationRequestBody = mockPresentationRequestBody,
            usePayloadEncryption = false,
        ).assertOk()

        val expected = PresentationRequestType.Json(
            vpToken = VP_TOKEN,
            presentationSubmission = safeJson.safeEncodeObjectToString(presentationSubmission).value,
        )

        assertEquals(expected, result)
    }

    @Test
    fun `For response mode 'direct_post' returns a presentation type Json`() = runTest {
        val result = useCase(
            presentationRequest = mockPresentationRequest,
            presentationRequestBody = mockPresentationRequestBody,
            usePayloadEncryption = true,
        ).assertOk()

        val expected = PresentationRequestType.Json(
            vpToken = VP_TOKEN,
            presentationSubmission = safeJson.safeEncodeObjectToString(presentationSubmission).value,
        )

        assertEquals(expected, result)
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' returns a presentation type JWT`() = runTest {
        every { mockPresentationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT

        val result = useCase(
            presentationRequest = mockPresentationRequest,
            presentationRequestBody = mockPresentationRequestBody,
            usePayloadEncryption = true,
        ).assertOk()

        val expected = PresentationRequestType.Jwt("presentation request jwe")

        assertEquals(expected, result)
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' where no jwk is supported returns an error`() = runTest {
        every { mockPresentationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        every { mockClientMetadata.jwks } returns Jwks(keys = listOf(jwk.copy(kty = "unsupported key type")))

        useCase(
            presentationRequest = mockPresentationRequest,
            presentationRequestBody = mockPresentationRequestBody,
            usePayloadEncryption = true,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' where no jwk is provided returns an error`() = runTest {
        every { mockPresentationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        every { mockClientMetadata.jwks } returns Jwks(keys = emptyList())

        useCase(
            presentationRequest = mockPresentationRequest,
            presentationRequestBody = mockPresentationRequestBody,
            usePayloadEncryption = true,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' where no encryption value is supported returns an error`() = runTest {
        every { mockPresentationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        every { mockClientMetadata.encryptedResponseEncValuesSupported } returns listOf("unsupported value")

        useCase(
            presentationRequest = mockPresentationRequest,
            presentationRequestBody = mockPresentationRequestBody,
            usePayloadEncryption = true,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' where no encryption value is provided uses default value`() = runTest {
        every { mockPresentationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        every { mockClientMetadata.encryptedResponseEncValuesSupported } returns null

        useCase(
            presentationRequest = mockPresentationRequest,
            presentationRequestBody = mockPresentationRequestBody,
            usePayloadEncryption = true,
        ).assertOk()

        coVerify {
            mockCreateJWE(
                algorithm = any(),
                encryptionMethod = "A128GCM",
                compressionAlgorithm = any(),
                payload = any(),
                encryptionKey = any()
            )
        }
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' maps errors from JWE creation`() = runTest {
        every { mockPresentationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        coEvery {
            mockCreateJWE(any(), any(), any(), any(), any())
        } returns Err(JWEError.Unexpected(IllegalStateException("jwe error")))

        useCase(
            presentationRequest = mockPresentationRequest,
            presentationRequestBody = mockPresentationRequestBody,
            usePayloadEncryption = true,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `For any other response mode returns and error`() = runTest {
        every { mockPresentationRequest.responseMode } returns "other"

        useCase(
            presentationRequest = mockPresentationRequest,
            presentationRequestBody = mockPresentationRequestBody,
            usePayloadEncryption = true,
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

    private val presentationSubmission = PresentationSubmission(
        definitionId = "defId",
        descriptorMap = emptyList(),
        id = "id",
    )

    private fun setupDefaultMocks() {
        every { mockPresentationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST
        every { mockPresentationRequest.clientMetaData } returns mockClientMetadata

        every { mockPresentationRequestBody.vpToken } returns VP_TOKEN
        every { mockPresentationRequestBody.presentationSubmission } returns presentationSubmission

        every { mockClientMetadata.jwks } returns jwks
        every { mockClientMetadata.encryptedResponseEncValuesSupported } returns listOf(ENCRYPTION_VALUE)

        val payloadJson = buildJsonObject {
            put("vp_token", VP_TOKEN)
            put("presentation_submission", safeJson.safeEncodeObjectToString(mockPresentationRequestBody.presentationSubmission).value)
        }

        coEvery {
            mockCreateJWE(
                algorithm = ALG_VALUE,
                encryptionMethod = ENCRYPTION_VALUE,
                payload = safeJson.safeEncodeObjectToString(payloadJson).value,
                encryptionKey = jwk,
            )
        } returns Ok("presentation request jwe")
    }

    private companion object {
        const val RESPONSE_MODE_DIRECT_POST = "direct_post"
        const val RESPONSE_MODE_DIRECT_POST_JWT = "direct_post.jwt"
        const val X_VALUE = "x value"
        const val Y_VALUE = "y value"
        const val CURVE = "P-256"
        const val KEY_TYPE = "EC"
        const val KEY_USE = "enc"
        const val ALG_VALUE = "ECDH-ES"
        const val ENCRYPTION_VALUE = "A128GCM"
        const val VP_TOKEN = "vp token"
    }
}
