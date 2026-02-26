package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.wallet.platform.credential.domain.model.toAnyCredentials
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestField
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetRequestedFields
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBundleItemsWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithBundleItemsWithKeyBindingRepository
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class GetCompatibleCredentialsImplTest {

    @MockK
    private lateinit var verifiableCredentialWithBundleItemsWithKeyBindingRepository:
        VerifiableCredentialWithBundleItemsWithKeyBindingRepository

    @MockK
    private lateinit var mockGetRequestedFields: GetRequestedFields

    @MockK
    private lateinit var mockCredential: Credential

    @MockK
    private lateinit var mockAnyCredential: AnyCredential

    @MockK
    private lateinit var mockDbCredential: VerifiableCredentialWithBundleItemsWithKeyBinding

    @MockK
    private lateinit var mockKeyBinding: KeyBinding

    @MockK
    private lateinit var mockCredential2: Credential

    @MockK
    private lateinit var mockAnyCredential2: AnyCredential

    @MockK
    private lateinit var mockDbCredential2: VerifiableCredentialWithBundleItemsWithKeyBinding

    @MockK
    private lateinit var mockKeyBinding2: KeyBinding

    private lateinit var useCase: GetCompatibleCredentialsImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetCompatibleCredentialsImpl(verifiableCredentialWithBundleItemsWithKeyBindingRepository, mockGetRequestedFields)

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting compatible credentials with no credential returns empty list`() = runTest {
        setupDefaultMocks(credentials = emptyList())

        val result = useCase(inputDescriptors).assertOk()

        assertEquals(0, result.size)
    }

    @Test
    fun `Getting compatible credentials with two matching credentials returns both ids and their fields`() = runTest {
        setupDefaultMocks(
            credentials = listOf(mockDbCredential, mockDbCredential2),
            requestedFields = requestedFields,
            requestedFields2 = requestedFields2,
        )

        val result = useCase(inputDescriptors).assertOk()
        val credential1 = result.find { it.credentialId == CREDENTIAL_ID }
        val credential2 = result.find { it.credentialId == CREDENTIAL_ID_2 }

        assertEquals(2, result.size)
        assertEquals(CREDENTIAL_ID, credential1?.credentialId)
        assertEquals(requestedFields, credential1?.requestedFields)

        assertEquals(CREDENTIAL_ID_2, credential2?.credentialId)
        assertEquals(requestedFields2, credential2?.requestedFields)
    }

    @Test
    fun `Getting compatible credentials with two credentials where one matches returns the matched credential id and the fields`() = runTest {
        setupDefaultMocks(
            credentials = listOf(mockDbCredential, mockDbCredential2),
            requestedFields = emptyList(),
            requestedFields2 = requestedFields2,
        )

        val result = useCase(inputDescriptors).assertOk()
        val credential2 = result.find { it.credentialId == CREDENTIAL_ID_2 }

        assertEquals(1, result.size)
        assertEquals(CREDENTIAL_ID_2, credential2?.credentialId)
        assertEquals(requestedFields2, credential2?.requestedFields)
    }

    @Test
    fun `Getting compatible credentials where the credential has a non-matching format returns an empty list`() = runTest {
        every { mockAnyCredential.format } returns CredentialFormat.UNKNOWN

        val result = useCase(inputDescriptors).assertOk()
        assertEquals(0, result.size)
    }

    @Test
    fun `Getting compatible credentials with two credentials where none matches returns empty list`() = runTest {
        setupDefaultMocks(
            credentials = listOf(mockDbCredential, mockDbCredential2),
            requestedFields = emptyList(),
            requestedFields2 = emptyList(),
        )

        val result = useCase(inputDescriptors).assertOk()

        assertEquals(0, result.size)
    }

    @Test
    fun `Getting compatible credentials maps errors from the credential repository`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            verifiableCredentialWithBundleItemsWithKeyBindingRepository.getAll()
        } returns Err(SsiError.Unexpected(exception))

        val result = useCase(inputDescriptors)

        val error = result.assertErrorType(CredentialPresentationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Getting compatible credentials maps errors from getting requested fields`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockGetRequestedFields(any(), any()) } returns Err(CredentialPresentationError.Unexpected(exception))

        val result = useCase(inputDescriptors)

        val error = result.assertErrorType(CredentialPresentationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Getting compatible credentials maps errors from json parsing`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockAnyCredential.getClaimsForPresentation().toString() } throws exception

        val result = useCase(inputDescriptors)

        val error = result.assertErrorType(CredentialPresentationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @ParameterizedTest
    @MethodSource("generateCredentialStates")
    fun `Getting compatible credentials only returns accepted credentials`(
        states: TestCredentialStates
    ) = runTest {
        setupDefaultMocks(
            credentials = listOf(mockDbCredential, mockDbCredential2),
            requestedFields = requestedFields,
            requestedFields2 = requestedFields2,
        )
        coEvery { mockDbCredential.verifiableCredential.progressionState } returns states.credentialState1
        coEvery { mockDbCredential2.verifiableCredential.progressionState } returns states.credentialState2

        val result = useCase(inputDescriptors).assertOk()

        assertEquals(states.expectedIds, result.map { it.credentialId })
    }

    @Test
    fun `Getting compatible credentials without key binding with two matching credentials returns both ids and their fields`() = runTest {
        setupDefaultMocks(
            credentials = listOf(mockDbCredential, mockDbCredential2),
            requestedFields = requestedFields,
            requestedFields2 = requestedFields2,
        )

        every { mockAnyCredential.keyBinding } returns null
        every { mockAnyCredential2.keyBinding } returns null
        every { inputDescriptor.formats } returns listOf(inputDescriptorFormatVcSdJwtEmpty)

        val result = useCase(inputDescriptors).assertOk()
        val credential1 = result.find { it.credentialId == CREDENTIAL_ID }
        val credential2 = result.find { it.credentialId == CREDENTIAL_ID_2 }

        assertEquals(2, result.size)
        assertEquals(CREDENTIAL_ID, credential1?.credentialId)
        assertEquals(requestedFields, credential1?.requestedFields)

        assertEquals(CREDENTIAL_ID_2, credential2?.credentialId)
        assertEquals(requestedFields2, credential2?.requestedFields)
    }

    @Test
    fun `Getting compatible key bound credentials returns the credential with matching key binding algorithm`() = runTest {
        setupDefaultMocks(
            credentials = listOf(mockDbCredential, mockDbCredential2),
            requestedFields = requestedFields,
            requestedFields2 = requestedFields2,
        )

        every { mockAnyCredential2.keyBinding } returns mockKeyBinding2
        every { mockKeyBinding2.algorithm } returns SigningAlgorithm.ES512

        val result = useCase(inputDescriptors).assertOk()
        val credential1 = result.find { it.credentialId == CREDENTIAL_ID }

        assertEquals(1, result.size)

        assertEquals(CREDENTIAL_ID, credential1?.credentialId)
        assertEquals(requestedFields, credential1?.requestedFields)
    }

    @Test
    fun `Getting compatible key bound credentials returns the credential with key binding`() = runTest {
        setupDefaultMocks(
            credentials = listOf(mockDbCredential, mockDbCredential2),
            requestedFields = requestedFields,
            requestedFields2 = requestedFields2,
        )

        every { mockAnyCredential.keyBinding } returns null

        val result = useCase(inputDescriptors).assertOk()
        val credential2 = result.find { it.credentialId == CREDENTIAL_ID_2 }

        assertEquals(1, result.size)

        assertEquals(CREDENTIAL_ID_2, credential2?.credentialId)
        assertEquals(requestedFields2, credential2?.requestedFields)
    }

    private fun setupDefaultMocks(
        credentials: List<VerifiableCredentialWithBundleItemsWithKeyBinding> = listOf(mockDbCredential),
        requestedFields: List<PresentationRequestField> = emptyList(),
        requestedFields2: List<PresentationRequestField> = emptyList(),
    ) {
        mockkStatic(VerifiableCredentialWithBundleItemsWithKeyBinding::toAnyCredentials)

        every { mockAnyCredential.id } returns CREDENTIAL_ID
        every { mockAnyCredential.getClaimsForPresentation().toString() } returns CREDENTIAL_JSON
        every { mockAnyCredential.format } returns CredentialFormat.VC_SD_JWT
        every { mockAnyCredential.payload } returns CREDENTIAL_PAYLOAD
        every { mockAnyCredential.keyBinding } returns mockKeyBinding
        every { mockKeyBinding.algorithm } returns SigningAlgorithm.ES256

        every { mockDbCredential.verifiableCredential.progressionState } returns VerifiableProgressionState.ACCEPTED
        every { mockDbCredential.toAnyCredentials() } returns Ok(listOf(mockAnyCredential))
        every { mockDbCredential.credential } returns mockCredential
        every { mockCredential.format } returns CredentialFormat.VC_SD_JWT
        every { mockCredential.id } returns CREDENTIAL_ID

        every { mockAnyCredential2.id } returns CREDENTIAL_ID_2
        every { mockAnyCredential2.getClaimsForPresentation().toString() } returns CREDENTIAL_JSON_2
        every { mockAnyCredential2.format } returns CredentialFormat.VC_SD_JWT
        every { mockAnyCredential2.payload } returns CREDENTIAL_PAYLOAD
        every { mockAnyCredential2.keyBinding } returns mockKeyBinding2
        every { mockKeyBinding2.algorithm } returns SigningAlgorithm.ES256

        every { mockDbCredential2.verifiableCredential.progressionState } returns VerifiableProgressionState.ACCEPTED
        every { mockDbCredential2.toAnyCredentials() } returns Ok(listOf(mockAnyCredential2))
        every { mockDbCredential2.credential } returns mockCredential2
        every { mockCredential2.format } returns CredentialFormat.VC_SD_JWT
        every { mockCredential2.id } returns CREDENTIAL_ID_2

        every { inputDescriptor.formats } returns listOf(inputDescriptorFormatVcSdJwt)
        coEvery { verifiableCredentialWithBundleItemsWithKeyBindingRepository.getAll() } returns Ok(credentials)
        coEvery { mockGetRequestedFields(CREDENTIAL_JSON, inputDescriptors) } returns Ok(requestedFields)
        coEvery { mockGetRequestedFields(CREDENTIAL_JSON_2, inputDescriptors) } returns Ok(requestedFields2)
    }

    data class TestCredentialStates(
        val credentialState1: VerifiableProgressionState,
        val credentialState2: VerifiableProgressionState,
        val expectedIds: List<Long>,
    )

    private companion object {
        const val CREDENTIAL_ID = 1L
        const val CREDENTIAL_ID_2 = 2L
        const val CREDENTIAL_JSON = "credentialJson"
        const val CREDENTIAL_JSON_2 = "credentialJson2"

        val inputDescriptor: InputDescriptor = mockk()
        val inputDescriptorFormatVcSdJwt = InputDescriptorFormat.VcSdJwt(
            sdJwtAlgorithms = listOf(SigningAlgorithm.ES256),
            kbJwtAlgorithms = listOf(SigningAlgorithm.ES256),
        )
        val inputDescriptorFormatVcSdJwtEmpty = InputDescriptorFormat.VcSdJwt(
            sdJwtAlgorithms = listOf(),
            kbJwtAlgorithms = listOf(),
        )
        val inputDescriptors: List<InputDescriptor> = listOf(inputDescriptor)
        val requestedFields: List<PresentationRequestField> = listOf(mockk())
        val requestedFields2: List<PresentationRequestField> = listOf(mockk())

        const val CREDENTIAL_PAYLOAD = """{"proof":{"cryptosuite":"ES256"}}"""

        @JvmStatic
        fun generateCredentialStates() = listOf(
            TestCredentialStates(
                VerifiableProgressionState.ACCEPTED,
                VerifiableProgressionState.ACCEPTED,
                listOf(CREDENTIAL_ID, CREDENTIAL_ID_2),
            ),
            TestCredentialStates(
                VerifiableProgressionState.ACCEPTED,
                VerifiableProgressionState.UNACCEPTED,
                listOf(CREDENTIAL_ID),
            ),
            TestCredentialStates(
                VerifiableProgressionState.UNACCEPTED,
                VerifiableProgressionState.ACCEPTED,
                listOf(CREDENTIAL_ID_2),
            ),
            TestCredentialStates(
                VerifiableProgressionState.UNACCEPTED,
                VerifiableProgressionState.UNACCEPTED,
                emptyList(),
            ),
        )
    }
}
