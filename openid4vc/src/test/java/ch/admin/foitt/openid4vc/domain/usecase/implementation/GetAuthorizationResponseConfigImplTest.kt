@file:OptIn(UnsafeResultValueAccess::class)

package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwe.JWEError
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwks
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponse
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseParam
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseType
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationSubmission
import ch.admin.foitt.openid4vc.domain.usecase.GetAuthorizationResponseConfig
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

class GetAuthorizationResponseConfigImplTest {

    private val safeJson = SafeJsonTestInstance.safeJsonWithDiscriminator

    @MockK
    private lateinit var mockCreateJWE: CreateJWE

    @MockK
    private lateinit var mockAuthorizationRequest: AuthorizationRequest

    @MockK
    private lateinit var mockAuthorizationResponseDIF: AuthorizationResponse.Dif

    @MockK
    private lateinit var mockAuthorizationResponseDCQL: AuthorizationResponse.Dcql

    @MockK
    private lateinit var mockClientMetadata: ClientMetaData

    private lateinit var useCase: GetAuthorizationResponseConfig

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        setupDefaultMocks()

        useCase = GetAuthorizationResponseConfigImpl(
            safeJson = safeJson,
            createJWE = mockCreateJWE,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `With payload encryption disabled and a dif body return a dif presentation type Json`() = runTest {
        val result = useCase(
            authorizationRequest = mockAuthorizationRequest,
            authorizationResponse = mockAuthorizationResponseDIF,
            usePayloadEncryption = false,
        ).assertOk()

        val expected = AuthorizationResponseConfig(
            type = AuthorizationResponseType.DIF,
            params = mapOf(
                AuthorizationResponseParam.VP_TOKEN to VP_TOKEN,
                AuthorizationResponseParam.PRESENTATION_SUBMISSION to safeJson.safeEncodeObjectToString(presentationSubmission).value,
                AuthorizationResponseParam.STATE to STATE,
            ),
        )

        assertEquals(expected, result)
    }

    @Test
    fun `With payload encryption disabled and a dcql body return a dcql presentation type Json`() = runTest {
        val result = useCase(
            authorizationRequest = mockAuthorizationRequest,
            authorizationResponse = mockAuthorizationResponseDCQL,
            usePayloadEncryption = false,
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
    fun `For response mode 'direct_post' returns a presentation type Json`() = runTest {
        val result = useCase(
            authorizationRequest = mockAuthorizationRequest,
            authorizationResponse = mockAuthorizationResponseDIF,
            usePayloadEncryption = true,
        ).assertOk()

        val expected = AuthorizationResponseConfig(
            type = AuthorizationResponseType.DIF,
            params = mapOf(
                AuthorizationResponseParam.VP_TOKEN to VP_TOKEN,
                AuthorizationResponseParam.PRESENTATION_SUBMISSION to safeJson.safeEncodeObjectToString(presentationSubmission).value,
                AuthorizationResponseParam.STATE to STATE,
            ),
        )

        assertEquals(expected, result)
    }

    @Test
    fun `For response mode 'direct_post' with DIF, state parameter is not set if not in presentation request`() = runTest {
        every { mockAuthorizationResponseDIF.state } returns null

        val result = useCase(
            authorizationRequest = mockAuthorizationRequest,
            authorizationResponse = mockAuthorizationResponseDIF,
            usePayloadEncryption = true,
        ).assertOk()

        val expected = AuthorizationResponseConfig(
            type = AuthorizationResponseType.DIF,
            params = mapOf(
                AuthorizationResponseParam.VP_TOKEN to VP_TOKEN,
                AuthorizationResponseParam.PRESENTATION_SUBMISSION to safeJson.safeEncodeObjectToString(presentationSubmission).value,
            ),
        )

        assertEquals(expected, result)
    }

    @Test
    fun `For response mode 'direct_post' with DCQL, state parameter is not set if not in presentation request`() = runTest {
        every { mockAuthorizationResponseDCQL.state } returns null

        val result = useCase(
            authorizationRequest = mockAuthorizationRequest,
            authorizationResponse = mockAuthorizationResponseDCQL,
            usePayloadEncryption = true,
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
            authorizationRequest = mockAuthorizationRequest,
            authorizationResponse = mockAuthorizationResponseDIF,
            usePayloadEncryption = true,
        ).assertOk()

        val expected = AuthorizationResponseConfig(
            type = AuthorizationResponseType.DIF,
            params = mapOf(
                AuthorizationResponseParam.RESPONSE to "presentation request jwe",
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' where no jwk is supported returns an error`() = runTest {
        every { mockAuthorizationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        every { mockClientMetadata.jwks } returns Jwks(keys = listOf(jwk.copy(kty = "unsupported key type")))

        useCase(
            authorizationRequest = mockAuthorizationRequest,
            authorizationResponse = mockAuthorizationResponseDIF,
            usePayloadEncryption = true,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' where no jwk is provided returns an error`() = runTest {
        every { mockAuthorizationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        every { mockClientMetadata.jwks } returns Jwks(keys = emptyList())

        useCase(
            authorizationRequest = mockAuthorizationRequest,
            authorizationResponse = mockAuthorizationResponseDIF,
            usePayloadEncryption = true,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' where no encryption value is supported returns an error`() = runTest {
        every { mockAuthorizationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        every { mockClientMetadata.encryptedResponseEncValuesSupported } returns listOf("unsupported value")

        useCase(
            authorizationRequest = mockAuthorizationRequest,
            authorizationResponse = mockAuthorizationResponseDIF,
            usePayloadEncryption = true,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `For response mode 'direct_post(dot)jwt' where no encryption value is provided uses default value`() = runTest {
        every { mockAuthorizationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        every { mockClientMetadata.encryptedResponseEncValuesSupported } returns null

        useCase(
            authorizationRequest = mockAuthorizationRequest,
            authorizationResponse = mockAuthorizationResponseDIF,
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
        every { mockAuthorizationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST_JWT
        coEvery {
            mockCreateJWE(any(), any(), any(), any(), any())
        } returns Err(JWEError.Unexpected(IllegalStateException("jwe error")))

        useCase(
            authorizationRequest = mockAuthorizationRequest,
            authorizationResponse = mockAuthorizationResponseDIF,
            usePayloadEncryption = true,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `For any other response mode returns and error`() = runTest {
        every { mockAuthorizationRequest.responseMode } returns "other"

        useCase(
            authorizationRequest = mockAuthorizationRequest,
            authorizationResponse = mockAuthorizationResponseDIF,
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
        every { mockAuthorizationRequest.responseMode } returns RESPONSE_MODE_DIRECT_POST
        every { mockAuthorizationRequest.clientMetaData } returns mockClientMetadata
        every { mockAuthorizationRequest.state } returns STATE

        every { mockAuthorizationResponseDIF.vpToken } returns VP_TOKEN
        every { mockAuthorizationResponseDIF.presentationSubmission } returns presentationSubmission
        every { mockAuthorizationResponseDIF.state } returns STATE

        every { mockAuthorizationResponseDCQL.vpToken } returns mapOf(DCQL_QUERY_ID to listOf(VP_TOKEN))
        every { mockAuthorizationResponseDCQL.state } returns STATE

        every { mockClientMetadata.jwks } returns jwks
        every { mockClientMetadata.encryptedResponseEncValuesSupported } returns listOf(ENCRYPTION_VALUE)

        val payloadJsonDif = buildJsonObject {
            put("vp_token", VP_TOKEN)
            put("presentation_submission", safeJson.safeEncodeObjectToString(mockAuthorizationResponseDIF.presentationSubmission).value)
            put("state", STATE)
        }

        val payloadJsonDcql = buildJsonObject {
            put("vp_token", safeJson.safeEncodeObjectToString(mockAuthorizationResponseDCQL.vpToken).value)
            put("state", STATE)
        }

        coEvery {
            mockCreateJWE(
                algorithm = ALG_VALUE,
                encryptionMethod = ENCRYPTION_VALUE,
                payload = safeJson.safeEncodeObjectToString(payloadJsonDif).value,
                encryptionKey = jwk,
            )
        } returns Ok("presentation request jwe")

        coEvery {
            mockCreateJWE(
                algorithm = ALG_VALUE,
                encryptionMethod = ENCRYPTION_VALUE,
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
        const val ENCRYPTION_VALUE = "A128GCM"
        const val VP_TOKEN = "vp token"
        const val DCQL_QUERY_ID = "dcql query id"
        const val STATE = "state"
    }
}
