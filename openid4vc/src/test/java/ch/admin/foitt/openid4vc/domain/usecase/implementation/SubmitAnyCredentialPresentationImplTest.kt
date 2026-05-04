package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponse
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseParam
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseType
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.Constraints
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.DescriptorMap
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationSubmission
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyDescriptorMapByPresentationDefinition
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyVerifiablePresentation
import ch.admin.foitt.openid4vc.domain.usecase.GetAuthorizationResponseConfig
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.util.UUID

class SubmitAnyCredentialPresentationImplTest {

    @MockK
    private lateinit var mockCreateAnyVerifiablePresentation: CreateAnyVerifiablePresentation

    @MockK
    private lateinit var mockCreateAnyDescriptorMapByPresentationDefinition:
        CreateAnyDescriptorMapByPresentationDefinition

    @MockK
    private lateinit var mockGetAuthorizationResponseConfig: GetAuthorizationResponseConfig

    @MockK
    private lateinit var mockPresentationRequestRepository: PresentationRequestRepository

    @MockK
    private lateinit var mockAnyCredential: AnyCredential

    @MockK
    private lateinit var mockAuthorizationRequest: AuthorizationRequest

    private lateinit var useCase: SubmitAnyCredentialPresentationImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = SubmitAnyCredentialPresentationImpl(
            createAnyVerifiablePresentation = mockCreateAnyVerifiablePresentation,
            createAnyDescriptorMapByPresentationDefinition = mockCreateAnyDescriptorMapByPresentationDefinition,
            getAuthorizationResponseConfig = mockGetAuthorizationResponseConfig,
            presentationRequestRepository = mockPresentationRequestRepository,
        )

        setupMockResponses()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Submitting dif presentation for any credential just runs`() = runTest {
        useCase(
            anyCredential = mockAnyCredential,
            requestedFields = requestedFields,
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = null,
        ).assertOk()
    }

    @Test
    fun `Submitting dcql presentation for any credential just runs`() = runTest {
        useCase(
            anyCredential = mockAnyCredential,
            requestedFields = requestedFields,
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = DCQL_QUERY_ID,
        ).assertOk()
    }

    @Test
    fun `Submitting a presentation where the repository returns VerificationError returns an error`() = runTest {
        mockAuthorizationRequest(
            inputDescriptorFormats = listOf(
                InputDescriptorFormat.VcSdJwt(
                    sdJwtAlgorithms = emptyList(),
                    kbJwtAlgorithms = emptyList(),
                )
            )
        )

        useCase(
            anyCredential = mockAnyCredential,
            requestedFields = requestedFields,
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = null,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `Submitting a presentation for multiple input descriptor with same format just runs`() = runTest {
        mockAuthorizationRequest(
            inputDescriptorFormats = listOf(validVcSdJwt, validVcSdJwt)
        )

        useCase(
            anyCredential = mockAnyCredential,
            requestedFields = requestedFields,
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = null,
        ).assertOk()
    }

    @Test
    fun `Submitting a presentation with an invalid response uri returns an error`() = runTest {
        every { mockAuthorizationRequest.responseUri } returns "invalid"
        coEvery {
            mockPresentationRequestRepository.submitPresentation(
                url = any(),
                authorizationResponseConfig = AuthorizationResponseConfig(
                    type = AuthorizationResponseType.DIF,
                    params = mapOf(
                        AuthorizationResponseParam.VP_TOKEN to VP_TOKEN,
                        AuthorizationResponseParam.PRESENTATION_SUBMISSION to "",
                    )
                )
            )
        } returns Ok(Unit)

        useCase(
            anyCredential = mockAnyCredential,
            requestedFields = requestedFields,
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = null,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `Submitting a presentation for any credential maps errors from creating any verifiable presentation`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockCreateAnyVerifiablePresentation(any(), any(), any())
        } returns Err(PresentationRequestError.Unexpected(exception))

        val result = useCase(
            anyCredential = mockAnyCredential,
            requestedFields = requestedFields,
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = null,
        )

        val error = result.assertErrorType(PresentationRequestError.Unexpected::class)
        assertEquals(exception, error.throwable)
    }

    @Test
    fun `Submitting a presentation for any credential maps errors from invalid response uri`() = runTest {
        every { mockAuthorizationRequest.responseUri } returns "invalid uri"

        useCase(
            anyCredential = mockAnyCredential,
            requestedFields = requestedFields,
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = null,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `Submitting a presentation for any credential maps errors from getting presentation request type`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockGetAuthorizationResponseConfig(any(), any(), any())
        } returns Err(PresentationRequestError.Unexpected(exception))

        val result = useCase(
            anyCredential = mockAnyCredential,
            requestedFields = requestedFields,
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = null,
        )

        val error = result.assertErrorType(PresentationRequestError.Unexpected::class)
        assertEquals(exception, error.throwable)
    }

    @Test
    fun `Submitting a presentation for any credential maps errors from submitting presentation`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockPresentationRequestRepository.submitPresentation(any(), any())
        } returns Err(PresentationRequestError.Unexpected(exception))

        val result = useCase(
            anyCredential = mockAnyCredential,
            requestedFields = requestedFields,
            authorizationRequest = mockAuthorizationRequest,
            usePayloadEncryption = true,
            dcqlQueryId = null,
        )

        val error = result.assertErrorType(PresentationRequestError.Unexpected::class)
        assertEquals(exception, error.throwable)
    }

    private fun setupMockResponses() {
        mockAuthorizationRequest(listOf(validVcSdJwt))
        coEvery {
            mockCreateAnyVerifiablePresentation(
                anyCredential = mockAnyCredential,
                requestedFields = requestedFields,
                authorizationRequest = mockAuthorizationRequest,
            )
        } returns Ok(VP_TOKEN)

        coEvery {
            mockCreateAnyDescriptorMapByPresentationDefinition(any())
        } returns mockDescriptorMaps

        coEvery {
            mockGetAuthorizationResponseConfig(
                authorizationRequest = mockAuthorizationRequest,
                authorizationResponse = authorizationResponseDif,
                usePayloadEncryption = true,
            )
        } returns Ok(authorizationResponseConfigDif)

        coEvery {
            mockGetAuthorizationResponseConfig(
                authorizationRequest = mockAuthorizationRequest,
                authorizationResponse = authorizationResponseDCQL,
                usePayloadEncryption = true,
            )
        } returns Ok(authorizationResponseConfigDcql)

        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns PRESENTATION_SUBMISSION_ID

        coEvery {
            mockPresentationRequestRepository.submitPresentation(
                url = URL(RESPONSE_URI),
                authorizationResponseConfig = authorizationResponseConfigDif,
            )
        } returns Ok(Unit)

        coEvery {
            mockPresentationRequestRepository.submitPresentation(
                url = URL(RESPONSE_URI),
                authorizationResponseConfig = authorizationResponseConfigDcql,
            )
        } returns Ok(Unit)
    }

    private fun mockAuthorizationRequest(inputDescriptorFormats: List<InputDescriptorFormat>) {
        every { mockAuthorizationRequest.presentationDefinition } returns mockk {
            every { inputDescriptors } returns inputDescriptorFormats.map { format ->
                createInputDescriptor(format)
            }
            every { id } returns PRESENTATION_DEFINITION_ID
        }
        every { mockAuthorizationRequest.responseUri } returns RESPONSE_URI
        every { mockAuthorizationRequest.state } returns STATE
    }

    private fun createInputDescriptor(format: InputDescriptorFormat) = InputDescriptor(
        constraints = Constraints(listOf()),
        formats = listOf(format),
        id = "id",
        name = "name",
        purpose = "purpose",
    )

    private val authorizationResponseConfigDif = AuthorizationResponseConfig(
        type = AuthorizationResponseType.DIF,
        params = mapOf(
            AuthorizationResponseParam.RESPONSE to AUTHORIZATION_RESPONSE_JWE,
        )
    )

    private val authorizationResponseConfigDcql = AuthorizationResponseConfig(
        type = AuthorizationResponseType.DCQL,
        params = mapOf(
            AuthorizationResponseParam.RESPONSE to AUTHORIZATION_RESPONSE_JWE,
        )
    )

    private companion object {
        const val STATE = "state"
        const val RESPONSE_URI = "https://example.com"
        const val FIELD = "field"
        val requestedFields = listOf(FIELD)
        val validVcSdJwt = InputDescriptorFormat.VcSdJwt(
            sdJwtAlgorithms = listOf(SigningAlgorithm.ES512),
            kbJwtAlgorithms = emptyList(),
        )

        const val PRESENTATION_DEFINITION_ID = "presentationDefinitionId"
        const val VP_TOKEN = "vpToken"
        const val DCQL_QUERY_ID = "dcql query id"
        const val PRESENTATION_SUBMISSION_ID = "presentationSubmissionId"
        val mockDescriptorMaps = listOf(mockk<DescriptorMap>())
        val presentationSubmission = PresentationSubmission(
            definitionId = PRESENTATION_DEFINITION_ID,
            descriptorMap = mockDescriptorMaps,
            id = PRESENTATION_SUBMISSION_ID,
        )
        val authorizationResponseDif = AuthorizationResponse.Dif(
            vpToken = VP_TOKEN,
            presentationSubmission = presentationSubmission,
            state = STATE
        )
        val authorizationResponseDCQL = AuthorizationResponse.Dcql(
            vpToken = mapOf(DCQL_QUERY_ID to listOf(VP_TOKEN)),
            state = STATE
        )

        const val AUTHORIZATION_RESPONSE_JWE = "jwe"
    }
}
