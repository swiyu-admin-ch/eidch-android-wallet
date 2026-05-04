package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObject
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.VerifyRequestObjectSignature
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.mock.MockPresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.mock.MockPresentationRequest.CLIENT_ID
import ch.admin.foitt.wallet.platform.credentialPresentation.mock.MockPresentationRequest.JWT_CONTAINING_INVALID_AUTHORIZATION_REQUEST
import ch.admin.foitt.wallet.platform.credentialPresentation.mock.MockPresentationRequest.JWT_MISSING_CLIENT_ID
import ch.admin.foitt.wallet.platform.credentialPresentation.mock.MockPresentationRequest.JWT_MISSING_RESPONSE_URI
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class ValidatePresentationRequestImplTest {

    private val testSafeJson = SafeJsonTestInstance.safeJson

    @MockK
    private lateinit var mockRequestObject: RequestObject

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockVerifyRequestObjectSignature: VerifyRequestObjectSignature

    @SpyK
    private var mockPresentationJwt: Jwt = Jwt(MockPresentationRequest.VALID_JWT)

    private lateinit var useCase: ValidatePresentationRequest

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = ValidatePresentationRequestImpl(
            safeJson = testSafeJson,
            environmentSetupRepository = mockEnvironmentSetupRepository,
            verifyRequestObjectSignature = mockVerifyRequestObjectSignature,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A valid jwt presentation request returns Ok`() = runTest {
        useCase(mockRequestObject).assertOk()
    }

    @TestFactory
    fun `Request object jwt missing or containing invalid header typ returns an error`(): List<DynamicTest> {
        val input = listOf(null, "otherType")

        return input.map {
            DynamicTest.dynamicTest("Input: $it should return an unexpected error") {
                runTest {
                    every { mockPresentationJwt.type } returns it

                    useCase(mockRequestObject).assertErrorType(CredentialPresentationError.Unexpected::class)
                }
            }
        }
    }

    @Test
    fun `Request object jwt missing client_id returns an error`() = runTest {
        coEvery { mockRequestObject.jwt } returns Jwt(JWT_MISSING_CLIENT_ID)

        useCase(mockRequestObject).assertErrorType(CredentialPresentationError.Unexpected::class)
    }

    @Test
    fun `Request object clientId not matching jwt client_id returns an error`() = runTest {
        coEvery { mockRequestObject.clientId } returns "other client id"

        useCase(mockRequestObject).assertErrorType(CredentialPresentationError.Unexpected::class)
    }

    @Test
    fun `Request object jwt missing response_uri claim returns an error`() = runTest {
        coEvery { mockRequestObject.jwt } returns Jwt(JWT_MISSING_RESPONSE_URI)

        useCase(mockRequestObject).assertErrorType(CredentialPresentationError.Unexpected::class)
    }

    @Test
    fun `Request object jwt with an invalid jwt alg header returns an invalid presentation error`() = runTest {
        coEvery { mockPresentationJwt.algorithm } returns INVALID_JWT_ALGORITHM

        useCase(mockRequestObject).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
    }

    @Test
    fun `Request object jwt missing kid header returns an invalid presentation error`(): Unit = runTest {
        coEvery { mockPresentationJwt.keyId } returns null

        useCase(mockRequestObject).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
    }

    @Test
    fun `Request object jwt that is not yet valid returns an invalid presentation error`() = runTest {
        coEvery { mockRequestObject.jwt } returns Jwt(MockPresentationRequest.NOT_YET_VALID_JWT)

        useCase(mockRequestObject).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
    }

    @Test
    fun `Request object jwt that is expired returns an invalid presentation error`() = runTest {
        coEvery { mockRequestObject.jwt } returns Jwt(MockPresentationRequest.EXPIRED_JWT)

        useCase(mockRequestObject).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
    }

    @Test
    fun `ValidatePresentationRequest maps errors from verifying request object signature`(): Unit = runTest {
        coEvery {
            mockVerifyRequestObjectSignature(any(), any())
        } returns Err(VcSdJwtError.InvalidJwt)

        useCase(mockRequestObject).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
    }

    @Test
    fun `Request object jwt containing an invalid authorization request returns an error`() = runTest {
        coEvery { mockRequestObject.jwt } returns Jwt(JWT_CONTAINING_INVALID_AUTHORIZATION_REQUEST)

        useCase(mockRequestObject).assertErrorType(CredentialPresentationError.Unexpected::class)
    }

    // authorization request validation
    @Test
    fun `Authorization request with an invalid response_type (something else than 'vp_token') returns an invalid presentation error`() =
        runTest {
            val presentationJson = MockPresentationRequest.authorizationRequest
                .copy(responseType = INVALID_RESPONSE_TYPE)
                .toJsonObject()

            coEvery { mockPresentationJwt.payloadJson } returns presentationJson

            useCase(mockRequestObject).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Authorization request with an invalid response_mode returns an invalid presentation error`() =
        runTest {
            val presentationJson = MockPresentationRequest.authorizationRequest
                .copy(responseMode = INVALID_RESPONSE_MODE)
                .toJsonObject()
            coEvery { mockPresentationJwt.payloadJson } returns presentationJson

            useCase(mockRequestObject).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Authorization request with response_mode 'direct_post(dot)jwt' but missing client metadata returns an invalid presentation error`() =
        runTest {
            val presentationJson = MockPresentationRequest.authorizationRequest
                .copy(responseMode = DIRECT_POST_JWT)
                .toJsonObject()
            coEvery { mockPresentationJwt.payloadJson } returns presentationJson

            useCase(mockRequestObject).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Authorization request with response_mode 'direct_post(dot)jwt' and client metadata returns success`() =
        runTest {
            val presentationJson = MockPresentationRequest.authorizationRequest
                .copy(
                    responseMode = DIRECT_POST_JWT,
                    clientMetaData = ClientMetaData(
                        clientNameList = emptyList(),
                        logoUriList = emptyList(),
                    )
                )
                .toJsonObject()
            coEvery { mockPresentationJwt.payloadJson } returns presentationJson

            useCase(mockRequestObject).assertOk()
        }

    @Test
    fun `Authorization request with empty input descriptor fields returns an invalid presentation error`(): Unit = runTest {
        every {
            mockPresentationJwt.payloadJson
        } returns MockPresentationRequest.invalidPresentationRequestFields().toJsonObject()

        useCase(mockRequestObject).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
    }

    @Test
    fun `Authorization request with empty DCQL query claims returns an invalid presentation error`(): Unit = runTest {
        every {
            mockPresentationJwt.payloadJson
        } returns MockPresentationRequest.invalidPresentationRequestClaims().toJsonObject()

        useCase(mockRequestObject).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
    }

    @Test
    fun `Authorization request with missing presentation definition and missing DCQL query returns an invalid presentation error`(): Unit =
        runTest {
            every {
                mockPresentationJwt.payloadJson
            } returns MockPresentationRequest.invalidPresentationRequestPresentationRequestDCQL().toJsonObject()

            useCase(mockRequestObject).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @TestFactory
    fun `Invalid constrain paths should throw error`(): List<DynamicTest> {
        val invalidConstrainPaths = listOf(
            listOf("$..book[?(@.price <= $['expensive'])]"),
            listOf("$..book[ ?(@.price <= $['expensive'])]"),
            listOf("$..book[\t?(@.isbn)]"),
            listOf("$..book[        ?(@.isbn)]"),
            listOf("$..book[?(@.author =~ /.*REES/i)]"),
            listOf("valid.path", "$..book[?(@.isbn)]"),
            listOf("$..book[?(@.isbn)]", "valid.path"),
            listOf("valid.path", "$..book[?(@.isbn)]", "valid.path"),
        )
        return invalidConstrainPaths.map { paths ->
            DynamicTest.dynamicTest("$paths contains an invalid contrains path") {
                runTest {
                    every {
                        mockPresentationJwt.payloadJson
                    } returns MockPresentationRequest.invalidPresentationRequestPath(paths).toJsonObject()

                    useCase(mockRequestObject).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
                }
            }
        }
    }

    @Test
    @Disabled
    fun `Authorization request not using holder binding missing state returns an invalid presentation error`(): Unit =
        runTest {
            every {
                mockPresentationJwt.payloadJson
            } returns MockPresentationRequest.invalidPresentationRequestState().toJsonObject()

            useCase(mockRequestObject).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    private fun setupDefaultMocks() {
        coEvery { mockRequestObject.jwt } returns mockPresentationJwt
        coEvery { mockRequestObject.clientId } returns CLIENT_ID

        coEvery { mockEnvironmentSetupRepository.attestationsServiceTrustedDids } returns emptyList()

        coEvery { mockVerifyRequestObjectSignature(any(), any()) } returns Ok(Unit)
    }

    private fun AuthorizationRequest.toJsonObject(): JsonObject =
        testSafeJson.json.encodeToJsonElement(value = this).jsonObject

    private companion object {
        const val DIRECT_POST_JWT = "direct_post.jwt"
        const val INVALID_RESPONSE_TYPE = "invalid response_type"
        const val INVALID_RESPONSE_MODE = "invalid response_mode"
        const val INVALID_JWT_ALGORITHM = "HS256"
    }
}
