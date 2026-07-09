package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAllAnyCredentialsByCredentialId
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListProperties
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchCredentialStatus
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateCredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class UpdateCredentialStatusImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private val json = SafeJsonTestInstance.json

    @MockK
    private lateinit var mockVerifiableCredentialRepo: VerifiableCredentialRepository

    @MockK
    private lateinit var mockBundleItemRepo: BundleItemRepository

    @MockK
    private lateinit var mockGetAllAnyCredentialsByCredentialId: GetAllAnyCredentialsByCredentialId

    @MockK
    private lateinit var mockFetchCredentialStatus: FetchCredentialStatus

    @MockK
    private lateinit var mockVcSdJwtCredential: VcSdJwtCredential

    private lateinit var useCase: UpdateCredentialStatus

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = UpdateCredentialStatusImpl(
            ioDispatcher = testDispatcher,
            verifiableCredentialRepository = mockVerifiableCredentialRepo,
            bundleItemRepository = mockBundleItemRepo,
            getAllAnyCredentialsByCredentialId = mockGetAllAnyCredentialsByCredentialId,
            fetchCredentialStatus = mockFetchCredentialStatus,
            safeJson = SafeJsonTestInstance.safeJson,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Updating the status of a valid credential updates the status to the result of the status list check`() = runTest(testDispatcher) {
        val newStatus = CredentialStatus.SUSPENDED
        coEvery { mockFetchCredentialStatus(any(), credentialStatusProperties) } returns Ok(newStatus)

        useCase(CREDENTIAL_ID).assertOk()

        coVerify {
            mockBundleItemRepo.updateStatusByCredentialId(CREDENTIAL_ID, newStatus)
        }
    }

    @Test
    fun `Updating credential status when credential is expired does update`() = runTest(testDispatcher) {
        every { mockVcSdJwtCredential.validity } returns Validity.Expired(Instant.now())

        useCase(CREDENTIAL_ID).assertOk()

        coVerify(exactly = 1) {
            mockFetchCredentialStatus.invoke(any(), any())
            mockBundleItemRepo.updateStatusByCredentialId(any(), any())
        }
    }

    @Test
    fun `Updating credential status when credential is not yet valid does update`() = runTest(testDispatcher) {
        every { mockVcSdJwtCredential.validity } returns Validity.NotYetValid(Instant.now())

        useCase(CREDENTIAL_ID).assertOk()

        coVerify(exactly = 1) {
            mockFetchCredentialStatus.invoke(any(), any())
            mockBundleItemRepo.updateStatusByCredentialId(any(), any())
        }
    }

    @Test
    fun `Updating credential status for an unknown credential status does not update the status`() = runTest(testDispatcher) {
        coEvery {
            mockFetchCredentialStatus(any(), credentialStatusProperties)
        } returns Ok(CredentialStatus.UNKNOWN)

        useCase(CREDENTIAL_ID).assertOk()

        coVerify(exactly = 0) {
            mockBundleItemRepo.updateStatusByCredentialId(CREDENTIAL_ID, any())
        }
    }

    @Test
    fun `Updating credential status maps errors from getting any credential`() = runTest(testDispatcher) {
        val exception = Exception("exception")
        coEvery {
            mockGetAllAnyCredentialsByCredentialId(any())
        } returns Err(CredentialError.Unexpected(exception))

        val result = useCase(CREDENTIAL_ID)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    @Test
    fun `Updating credential status where issuer is blank does not update the status`() = runTest(testDispatcher) {
        every { mockVcSdJwtCredential.issuer } returns ""

        useCase(CREDENTIAL_ID).assertOk()

        coVerify(exactly = 0) {
            mockBundleItemRepo.updateStatusByCredentialId(CREDENTIAL_ID, any())
        }
    }

    @Test
    fun `Updating credential status where properties parsing fails does not update the status`() = runTest(testDispatcher) {
        every { mockVcSdJwtCredential.status } returns json.parseToJsonElement(INVALID_STATUS)

        useCase(CREDENTIAL_ID).assertOk()

        coVerify(exactly = 0) {
            mockBundleItemRepo.updateStatusByCredentialId(CREDENTIAL_ID, any())
        }
    }

    @Test
    fun `Updating credential status maps errors from fetching status update`() = runTest(testDispatcher) {
        val exception = Exception("exception")
        coEvery { mockFetchCredentialStatus(any(), any()) } returns Err(CredentialStatusError.Unexpected(exception))

        val result = useCase(CREDENTIAL_ID)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    @Test
    fun `Updating credential status maps errors from credential update`() = runTest(testDispatcher) {
        val exception = Exception("exception")
        coEvery {
            mockBundleItemRepo.updateStatusByCredentialId(CREDENTIAL_ID, any())
        } returns Err(SsiError.Unexpected(exception))

        val result = useCase(CREDENTIAL_ID)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    private fun setupDefaultMocks() {
        every { mockVcSdJwtCredential.id } returns CREDENTIAL_ID
        every { mockVcSdJwtCredential.validity } returns Validity.Valid
        every { mockVcSdJwtCredential.issuer } returns "issuer"
        every { mockVcSdJwtCredential.format } returns CredentialFormat.VC_SD_JWT
        every { mockVcSdJwtCredential.status } returns json.parseToJsonElement(status)

        coEvery { mockGetAllAnyCredentialsByCredentialId(CREDENTIAL_ID) } returns Ok(listOf(mockVcSdJwtCredential))
        coEvery { mockFetchCredentialStatus(any(), credentialStatusProperties) } returns Ok(CredentialStatus.VALID)
        coEvery {
            mockVerifiableCredentialRepo.onBundleItemUpdate(CREDENTIAL_ID)
        } returns Ok(CREDENTIAL_ID.toInt())
        coEvery {
            mockBundleItemRepo.updateStatusByCredentialId(CREDENTIAL_ID, any())
        } returns Ok(CREDENTIAL_ID.toInt())
    }

    private companion object {
        const val CREDENTIAL_ID = 1L
        val status = """
            {
                "status_list": {
                    "idx": 0,
                    "uri": "uri"
                }
            }
        """.trimIndent()

        const val INVALID_STATUS = "invalid"

        val credentialStatusProperties = TokenStatusListProperties(
            TokenStatusListProperties.StatusList(
                index = 0,
                uri = "uri"
            )
        )
    }
}
