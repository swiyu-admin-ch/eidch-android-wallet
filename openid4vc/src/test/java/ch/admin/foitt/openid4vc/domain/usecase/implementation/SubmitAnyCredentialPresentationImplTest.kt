package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.Constraints
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.DescriptorMap
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestBody
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestType
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationSubmission
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyDescriptorMaps
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyVerifiablePresentation
import ch.admin.foitt.openid4vc.domain.usecase.GetPresentationRequestType
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
    private lateinit var mockCreateAnyDescriptorMaps: CreateAnyDescriptorMaps

    @MockK
    private lateinit var mockGetPresentationRequestType: GetPresentationRequestType

    @MockK
    private lateinit var mockPresentationRequestRepository: PresentationRequestRepository

    @MockK
    private lateinit var mockAnyCredential: AnyCredential

    @MockK
    private lateinit var mockPresentationRequest: PresentationRequest

    private lateinit var useCase: SubmitAnyCredentialPresentationImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = SubmitAnyCredentialPresentationImpl(
            createAnyVerifiablePresentation = mockCreateAnyVerifiablePresentation,
            createAnyDescriptorMaps = mockCreateAnyDescriptorMaps,
            getPresentationRequestType = mockGetPresentationRequestType,
            presentationRequestRepository = mockPresentationRequestRepository,
        )

        success()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Submitting presentation for any credential just runs`() = runTest {
        useCase(
            anyCredential = mockAnyCredential,
            requestedFields = requestedFields,
            presentationRequest = mockPresentationRequest,
            usePayloadEncryption = true,
        ).assertOk()
    }

    @Test
    fun `Submitting a presentation where the repository returns VerificationError returns an error`() = runTest {
        mockPresentationRequest(
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
            presentationRequest = mockPresentationRequest,
            usePayloadEncryption = true,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `Submitting a presentation for multiple input descriptor with same format just runs`() = runTest {
        mockPresentationRequest(
            inputDescriptorFormats = listOf(validVcSdJwt, validVcSdJwt)
        )

        useCase(
            anyCredential = mockAnyCredential,
            requestedFields = requestedFields,
            presentationRequest = mockPresentationRequest,
            usePayloadEncryption = true,
        ).assertOk()
    }

    @Test
    fun `Submitting a presentation with an invalid response uri returns an error`() = runTest {
        every { mockPresentationRequest.responseUri } returns "invalid"
        coEvery {
            mockPresentationRequestRepository.submitPresentation(
                url = any(),
                presentationRequestType = PresentationRequestType.Json(vpToken = VP_TOKEN, presentationSubmission = ""),
            )
        } returns Ok(Unit)

        useCase(
            anyCredential = mockAnyCredential,
            requestedFields = requestedFields,
            presentationRequest = mockPresentationRequest,
            usePayloadEncryption = true,
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
            presentationRequest = mockPresentationRequest,
            usePayloadEncryption = true,
        )

        val error = result.assertErrorType(PresentationRequestError.Unexpected::class)
        assertEquals(exception, error.throwable)
    }

    @Test
    fun `Submitting a presentation for any credential maps errors from invalid response uri`() = runTest {
        every { mockPresentationRequest.responseUri } returns "invalid uri"

        useCase(
            anyCredential = mockAnyCredential,
            requestedFields = requestedFields,
            presentationRequest = mockPresentationRequest,
            usePayloadEncryption = true,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `Submitting a presentation for any credential maps errors from getting presentation request type`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockGetPresentationRequestType(any(), any(), any())
        } returns Err(PresentationRequestError.Unexpected(exception))

        val result = useCase(
            anyCredential = mockAnyCredential,
            requestedFields = requestedFields,
            presentationRequest = mockPresentationRequest,
            usePayloadEncryption = true,
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
            presentationRequest = mockPresentationRequest,
            usePayloadEncryption = true,
        )

        val error = result.assertErrorType(PresentationRequestError.Unexpected::class)
        assertEquals(exception, error.throwable)
    }

    private fun success() {
        mockPresentationRequest(listOf(validVcSdJwt))
        coEvery {
            mockCreateAnyVerifiablePresentation(
                anyCredential = mockAnyCredential,
                requestedFields = requestedFields,
                presentationRequest = mockPresentationRequest,
            )
        } returns Ok(VP_TOKEN)

        coEvery { mockCreateAnyDescriptorMaps(mockPresentationRequest) } returns mockDescriptorMaps

        coEvery {
            mockGetPresentationRequestType(
                presentationRequest = mockPresentationRequest,
                presentationRequestBody = presentationRequestBody,
                usePayloadEncryption = true,
            )
        } returns Ok(presentationRequestType)

        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns PRESENTATION_SUBMISSION_ID

        coEvery {
            mockPresentationRequestRepository.submitPresentation(
                url = URL(RESPONSE_URI),
                presentationRequestType = presentationRequestType,
            )
        } returns Ok(Unit)
    }

    private fun mockPresentationRequest(inputDescriptorFormats: List<InputDescriptorFormat>) {
        every { mockPresentationRequest.presentationDefinition } returns mockk {
            every { inputDescriptors } returns inputDescriptorFormats.map { format ->
                createInputDescriptor(format)
            }
            every { id } returns PRESENTATION_DEFINITION_ID
        }
        every { mockPresentationRequest.responseUri } returns RESPONSE_URI
    }

    private fun createInputDescriptor(format: InputDescriptorFormat) = InputDescriptor(
        constraints = Constraints(listOf()),
        formats = listOf(format),
        id = "id",
        name = "name",
        purpose = "purpose",
    )

    private val presentationRequestType = PresentationRequestType.Jwt(
        response = PRESENTATION_REQUEST_TYPE_JWE
    )

    private companion object {
        const val RESPONSE_URI = "https://example.com"
        const val FIELD = "field"
        val requestedFields = listOf(FIELD)
        val validVcSdJwt = InputDescriptorFormat.VcSdJwt(
            sdJwtAlgorithms = listOf(SigningAlgorithm.ES512),
            kbJwtAlgorithms = emptyList(),
        )

        const val PRESENTATION_DEFINITION_ID = "presentationDefinitionId"
        const val VP_TOKEN = "vpToken"
        const val PRESENTATION_SUBMISSION_ID = "presentationSubmissionId"
        val mockDescriptorMaps = listOf(mockk<DescriptorMap>())
        val presentationSubmission = PresentationSubmission(
            definitionId = PRESENTATION_DEFINITION_ID,
            descriptorMap = mockDescriptorMaps,
            id = PRESENTATION_SUBMISSION_ID,
        )
        val presentationRequestBody = PresentationRequestBody(VP_TOKEN, presentationSubmission)

        const val PRESENTATION_REQUEST_TYPE_JWE = "jwe"
    }
}
