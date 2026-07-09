package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.swiyu.shared.dcql.DcqlSupport
import ch.admin.foitt.swiyu.shared.dcql.SwissProfileTrustedDidAuthority
import ch.admin.foitt.swiyu.shared.dcql.model.DcqlCredentialMatch
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toAnyCredentials
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithBundleItemsWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithBundleItemsWithKeyBindingRepository
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import ch.admin.foitt.wallet.util.assertTrue
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uniffi.heidi_dcql_rust.DcqlQuery
import java.time.Instant

class GetCompatibleCredentialsImplTest {

    @MockK
    private lateinit var verifiableCredentialWithBundleItemsWithKeyBindingRepository:
        VerifiableCredentialWithBundleItemsWithKeyBindingRepository

    @MockK
    private lateinit var mockCredential: Credential

    @MockK
    private lateinit var mockAnyCredential: AnyCredential

    @MockK
    private lateinit var mockDbCredential: VerifiableCredentialWithBundleItemsWithKeyBinding

    @MockK
    private lateinit var mockCredential2: Credential

    @MockK
    private lateinit var mockAnyCredential2: AnyCredential

    @MockK
    private lateinit var mockDbCredential2: VerifiableCredentialWithBundleItemsWithKeyBinding

    @MockK
    private lateinit var mockAuthorizationRequest: AuthorizationRequest

    @MockK
    private lateinit var mockDcqlQuery: DcqlQuery

    private lateinit var useCase: GetCompatibleCredentialsImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetCompatibleCredentialsImpl(
            verifiableCredentialWithBundleItemsWithKeyBindingRepository = verifiableCredentialWithBundleItemsWithKeyBindingRepository,
        )
        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting compatible credentials with no credential returns empty set`() = runTest {
        setupDefaultMocks(credentials = emptyList())

        val result = useCase(mockAuthorizationRequest).assertOk()

        assertEquals(0, result.size)
    }

    @Test
    fun `Getting compatible credentials with null dcql query returns empty set`() = runTest {
        every { mockAuthorizationRequest.dcqlQuery } returns null

        val result = useCase(mockAuthorizationRequest).assertOk()

        assertTrue(result.isEmpty()) { "Result should be empty when dcqlQuery is null" }
    }

    @Test
    fun `Expired Verifiable Credential is never part of compatible credentials`() = runTest {
        setupDefaultMocks()
        every { mockDbCredential.verifiableCredential.validUntil } returns Instant.now().minusSeconds(60).epochSecond

        val result = useCase(mockAuthorizationRequest).assertOk()

        assertTrue(result.isEmpty()) { "Result should be empty" }
    }

    @Test
    fun `Not yet valid Verifiable Credential is never part of compatible credentials`() = runTest {
        setupDefaultMocks()
        every { mockDbCredential.verifiableCredential.validUntil } returns Instant.now().plusSeconds(600).epochSecond
        every { mockDbCredential.verifiableCredential.validFrom } returns Instant.now().plusSeconds(60).epochSecond

        val result = useCase(mockAuthorizationRequest).assertOk()

        assertTrue(result.isEmpty()) { "Result should be empty" }
    }

    @Test
    fun `Verifiable Credential with revoked status is not part of compatible credentials`() = runTest {
        setupDefaultMocks()
        every { mockDbCredential.nextBundleItemToPresent } returns BundleItemEntity(
            credentialId = CREDENTIAL_ID,
            payload = CREDENTIAL_PAYLOAD_1,
            status = CredentialStatus.REVOKED,
        )

        val result = useCase(mockAuthorizationRequest).assertOk()

        assertTrue(result.isEmpty()) { "Result should be empty" }
    }

    @TestFactory
    fun `Verifiable credential with specific status is considered compatible`(): List<DynamicTest> {
        val statuses = listOf(
            CredentialStatus.SUSPENDED,
            CredentialStatus.UNSUPPORTED,
            CredentialStatus.UNKNOWN,
            CredentialStatus.VALID,
        )
        return statuses.map { status ->
            DynamicTest.dynamicTest("Verifiable credential with status $status is considered compatible") {
                runTest {
                    setupDefaultMocks()
                    every { mockDbCredential.nextBundleItemToPresent } returns BundleItemEntity(
                        credentialId = CREDENTIAL_ID,
                        payload = CREDENTIAL_PAYLOAD_1,
                        status = status,
                    )

                    val result = useCase(mockAuthorizationRequest).assertOk()

                    assertNotNull(result.find { it.credentialId == CREDENTIAL_ID })
                }
            }
        }
    }

    @Test
    fun `Getting compatible credentials with two matching credentials returns both`() = runTest {
        setupDefaultMocks(credentials = listOf(mockDbCredential, mockDbCredential2))

        val result = useCase(mockAuthorizationRequest).assertOk()

        assertEquals(2, result.size)
        assertNotNull(result.find { it.credentialId == CREDENTIAL_ID })
        assertNotNull(result.find { it.credentialId == CREDENTIAL_ID_2 })
    }

    @Test
    fun `Getting compatible credentials with two credentials where one matches returns the matched credential`() = runTest {
        setupDefaultMocks(credentials = listOf(mockDbCredential, mockDbCredential2))
        every { DcqlSupport.matchDcqlCredentials(mockDcqlQuery, any()) } returns listOf(
            DcqlCredentialMatch(DCQL_QUERY_ID_2, CREDENTIAL_PAYLOAD_2, emptyList()),
        )

        val result = useCase(mockAuthorizationRequest).assertOk()

        assertEquals(1, result.size)
        assertNotNull(result.find { it.credentialId == CREDENTIAL_ID_2 })
    }

    @Test
    fun `Getting compatible credentials with two credentials where none matches returns empty set`() = runTest {
        setupDefaultMocks(credentials = listOf(mockDbCredential, mockDbCredential2))
        every { DcqlSupport.matchDcqlCredentials(mockDcqlQuery, any()) } returns emptyList()

        val result = useCase(mockAuthorizationRequest).assertOk()

        assertEquals(0, result.size)
    }

    @Test
    fun `Getting compatible credentials sets dcqlQueryId on the result`() = runTest {
        setupDefaultMocks()
        every { DcqlSupport.matchDcqlCredentials(mockDcqlQuery, any()) } returns listOf(
            DcqlCredentialMatch(DCQL_QUERY_ID_1, CREDENTIAL_PAYLOAD_1, emptyList()),
        )

        val result = useCase(mockAuthorizationRequest).assertOk()

        assertEquals(DCQL_QUERY_ID_1, result.first().dcqlQueryId)
    }

    @Test
    fun `Getting compatible credentials maps errors from the credential repository`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            verifiableCredentialWithBundleItemsWithKeyBindingRepository.getAll()
        } returns Err(SsiError.Unexpected(exception))

        val error = useCase(mockAuthorizationRequest).assertErrorType(CredentialPresentationError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    @Test
    fun `Getting compatible credentials maps errors from toAnyCredentials`() = runTest {
        setupDefaultMocks()
        val exception = IllegalStateException()
        every { mockDbCredential.toAnyCredentials() } returns Err(CredentialError.Unexpected(exception))

        val error = useCase(mockAuthorizationRequest).assertErrorType(CredentialPresentationError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    @ParameterizedTest
    @MethodSource("generateCredentialStates")
    fun `Getting compatible credentials only returns accepted credentials`(
        states: TestCredentialStates,
    ) = runTest {
        setupDefaultMocks(credentials = listOf(mockDbCredential, mockDbCredential2))
        every { mockDbCredential.verifiableCredential.progressionState } returns states.credentialState1
        every { mockDbCredential2.verifiableCredential.progressionState } returns states.credentialState2

        val result = useCase(mockAuthorizationRequest).assertOk()

        assertEquals(states.expectedIds.toSet(), result.map { it.credentialId }.toSet())
    }

    private fun setupDefaultMocks(
        credentials: List<VerifiableCredentialWithBundleItemsWithKeyBinding> = listOf(mockDbCredential),
    ) {
        mockkStatic(VerifiableCredentialWithBundleItemsWithKeyBinding::toAnyCredentials)
        mockkObject(SwissProfileTrustedDidAuthority)
        every { SwissProfileTrustedDidAuthority.register() } returns Unit
        mockkObject(DcqlSupport)

        every { mockAuthorizationRequest.dcqlQuery } returns mockDcqlQuery
        every { DcqlSupport.matchDcqlCredentials(mockDcqlQuery, any()) } returns listOf(
            DcqlCredentialMatch(DCQL_QUERY_ID_1, CREDENTIAL_PAYLOAD_1, emptyList()),
            DcqlCredentialMatch(DCQL_QUERY_ID_2, CREDENTIAL_PAYLOAD_2, emptyList()),
        )

        every { mockDbCredential.verifiableCredential.progressionState } returns VerifiableProgressionState.ACCEPTED
        every { mockDbCredential.verifiableCredential.validFrom } returns Instant.now().minusSeconds(5).epochSecond
        every { mockDbCredential.verifiableCredential.validUntil } returns Instant.now().plusSeconds(5).epochSecond
        every { mockDbCredential.nextBundleItemToPresent } returns BundleItemEntity(
            credentialId = CREDENTIAL_ID,
            payload = CREDENTIAL_PAYLOAD_1,
        )
        every { mockDbCredential.credential } returns mockCredential
        every { mockDbCredential.toAnyCredentials() } returns Ok(listOf(mockAnyCredential))
        every { mockCredential.id } returns CREDENTIAL_ID
        every { mockAnyCredential.getPathsForPresentation(any()) } returns emptySet()

        every { mockDbCredential2.verifiableCredential.progressionState } returns VerifiableProgressionState.ACCEPTED
        every { mockDbCredential2.verifiableCredential.validFrom } returns Instant.now().minusSeconds(5).epochSecond
        every { mockDbCredential2.verifiableCredential.validUntil } returns Instant.now().plusSeconds(5).epochSecond
        every { mockDbCredential2.nextBundleItemToPresent } returns BundleItemEntity(
            credentialId = CREDENTIAL_ID_2,
            payload = CREDENTIAL_PAYLOAD_2,
        )
        every { mockDbCredential2.credential } returns mockCredential2
        every { mockDbCredential2.toAnyCredentials() } returns Ok(listOf(mockAnyCredential2))
        every { mockCredential2.id } returns CREDENTIAL_ID_2
        every { mockAnyCredential2.getPathsForPresentation(any()) } returns emptySet()

        coEvery { verifiableCredentialWithBundleItemsWithKeyBindingRepository.getAll() } returns Ok(credentials)
    }

    data class TestCredentialStates(
        val credentialState1: VerifiableProgressionState,
        val credentialState2: VerifiableProgressionState,
        val expectedIds: List<Long>,
    )

    private companion object {
        const val CREDENTIAL_ID = 1L
        const val CREDENTIAL_ID_2 = 2L
        const val CREDENTIAL_PAYLOAD_1 = "payload1"
        const val CREDENTIAL_PAYLOAD_2 = "payload2"
        const val DCQL_QUERY_ID_1 = "queryId1"
        const val DCQL_QUERY_ID_2 = "queryId2"

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
