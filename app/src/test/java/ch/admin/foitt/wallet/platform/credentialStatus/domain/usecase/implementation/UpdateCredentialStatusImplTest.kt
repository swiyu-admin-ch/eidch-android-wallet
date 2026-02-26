package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

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
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateCredentialStatusImplTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var mockVerifiableCredentialRepo: VerifiableCredentialRepository

    @MockK
    private lateinit var mockBundleItemRepo: BundleItemRepository

    @MockK
    private lateinit var mockGetAllAnyCredentialsByCredentialId: GetAllAnyCredentialsByCredentialId

    @MockK
    private lateinit var mockFetchCredentialStatus: FetchCredentialStatus

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
    fun `Updating credential status when credential is expired does not update anything`() = runTest(testDispatcher) {
        coEvery { mockGetAllAnyCredentialsByCredentialId(CREDENTIAL_ID) } returns Ok(listOf(expiredVcSdJwtCredential))

        useCase(CREDENTIAL_ID).assertOk()

        coVerify(exactly = 0) {
            mockFetchCredentialStatus.invoke(any(), any())
            mockBundleItemRepo.updateStatusByCredentialId(any(), any())
        }
    }

    @Test
    fun `Updating credential status when credential is not yet valid does not update anything`() = runTest(testDispatcher) {
        coEvery { mockGetAllAnyCredentialsByCredentialId(CREDENTIAL_ID) } returns Ok(listOf(notYetValidVcSdJwtCredential))

        useCase(CREDENTIAL_ID).assertOk()

        coVerify(exactly = 0) {
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
    fun `Updating credential status where properties parsing fails does not update the status`() = runTest(testDispatcher) {
        coEvery {
            mockGetAllAnyCredentialsByCredentialId(CREDENTIAL_ID)
        } returns Ok(listOf(vcSdJwtCredentialWithInvalidStatusClaim))

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
        coEvery { mockGetAllAnyCredentialsByCredentialId(CREDENTIAL_ID) } returns Ok(listOf(validVcSdJwtCredential))
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

        val validVcSdJwtCredential = VcSdJwtCredential(
            id = CREDENTIAL_ID,
            payload = "ewogICJhbGciOiJFUzUxMiIsCiAgInR5cCI6IkpXVCIsCiAgImtpZCI6ImtleUlkIgp9.ewogICJfc2QiOlsKICAgICJZUkxmNjA2Y2x3dDQtaGp5R3plNDl5U0ZpNlZDbXdiOW41aHdiNFZVSlNZIiwiUWh1dklNUWQ1THlYOGdPUjN3ZVZ6U1kweUdaR0dIZFZYWTBFLU5oaFVmdyIKICBdLAogICJfc2RfYWxnIjoic2hhLTI1NiIsCiAgImlhdCI6MCwKICAibmJmIjoxLAogICJleHAiOjE5MjQ5ODgzOTksCiAgImlzcyI6Imlzc3VlciIsCiAgInZjdCI6InZjdCIsCiAgInN0YXR1cyI6ewogICJzdGF0dXNfbGlzdCI6ewogICJpZHgiOjAsCiAgInVyaSI6InVyaSIKICB9CiAgfQp9.ZXdvZ0lDSmhiR2NpT2lKRlV6VXhNaUlzQ2lBZ0luUjVjQ0k2SWtwWFZDSXNDaUFnSW10cFpDSTZJbXRsZVVsa0lncDkuLkFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUd3V2pRQUFaWDhnZmJkT1k2ZldJYWFicVBaT2RvUGpMYUp1SGZGcElwR3RBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFLM2xoLXlaeGxrZVRqWUVYT2xLMFptQUpiLUFKX0RhSzNBTlgxbDlWa3dF"
        )

        val expiredVcSdJwtCredential = VcSdJwtCredential(
            id = CREDENTIAL_ID,
            payload = "ewogICJhbGciOiJFUzUxMiIsCiAgInR5cCI6IkpXVCIsCiAgImtpZCI6ImtleUlkIgp9.ewogICJfc2QiOlsKICAgICJZUkxmNjA2Y2x3dDQtaGp5R3plNDl5U0ZpNlZDbXdiOW41aHdiNFZVSlNZIiwKICAgICJRaHV2SU1RZDVMeVg4Z09SM3dlVnpTWTB5R1pHR0hkVlhZMEUtTmhoVWZ3IgogIF0sCiAgIl9zZF9hbGciOiJzaGEtMjU2IiwKICAiaWF0IjowLAogICJuYmYiOjEsCiAgImV4cCI6MCwKICAiaXNzIjoiaXNzdWVyIiwKICAidmN0IjoidmN0IiwKICAic3RhdHVzIjp7CiAgICAic3RhdHVzX2xpc3QiOnsKICAgICAgImlkeCI6MCwKICAgICAgInVyaSI6InVyaSIKICAgIH0KICB9Cn0.ZXdvZ0lDSmhiR2NpT2lKRlV6VXhNaUlzQ2lBZ0luUjVjQ0k2SWtwWFZDSXNDaUFnSW10cFpDSTZJbXRsZVVsa0lncDkuLkFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQVBLWkdqd3lWajZFNTNET2VnWXVGdjJPLVlES2YzM280WDhON3lwYVd1aVlBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFMN1RNX0p6Qy1IdE1RU21sM2luTEZ1aU0xWEZHRVN3ZGc2TWY5cGlLX3JZ"
        )

        val notYetValidVcSdJwtCredential = VcSdJwtCredential(
            id = CREDENTIAL_ID,
            payload = "ewogICJhbGciOiJFUzUxMiIsCiAgInR5cCI6IkpXVCIsCiAgImtpZCI6ImtleUlkIgp9.ewogICJfc2QiOlsKICAgICJZUkxmNjA2Y2x3dDQtaGp5R3plNDl5U0ZpNlZDbXdiOW41aHdiNFZVSlNZIiwKICAgICJRaHV2SU1RZDVMeVg4Z09SM3dlVnpTWTB5R1pHR0hkVlhZMEUtTmhoVWZ3IgogIF0sCiAgIl9zZF9hbGciOiJzaGEtMjU2IiwKICAiaWF0IjowLAogICJuYmYiOjE5MjQ5ODgzOTksCiAgImV4cCI6MTkyNDk4ODM5OSwKICAiaXNzIjoiaXNzdWVyIiwKICAidmN0IjoidmN0IiwKICAic3RhdHVzIjp7CiAgICAic3RhdHVzX2xpc3QiOnsKICAgICAgImlkeCI6MCwKICAgICAgInVyaSI6InVyaSIKICAgIH0KICB9Cn0.ZXdvZ0lDSmhiR2NpT2lKRlV6VXhNaUlzQ2lBZ0luUjVjQ0k2SWtwWFZDSXNDaUFnSW10cFpDSTZJbXRsZVVsa0lncDkuLkFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFLLUw5a3lVRGxRTy1pNVY0MHZyLUwzdHdUc2h4SHItSUhxbDdlenFIQm9BQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFDUXdrS1luUk9FOGRTREZkdTBzbG1PME5CVWFZTGpOd2xPbXFGSnlRLVJw"
        )

        /*
        ...
        "status":"something"
        ...
         */
        val vcSdJwtCredentialWithInvalidStatusClaim = VcSdJwtCredential(
            id = CREDENTIAL_ID,
            payload = "ewogICJhbGciOiJFUzUxMiIsCiAgInR5cCI6IkpXVCIsCiAgImtpZCI6ImtleUlkIgp9.ewogICJfc2QiOlsKICAgICJZUkxmNjA2Y2x3dDQtaGp5R3plNDl5U0ZpNlZDbXdiOW41aHdiNFZVSlNZIiwKICAgICJRaHV2SU1RZDVMeVg4Z09SM3dlVnpTWTB5R1pHR0hkVlhZMEUtTmhoVWZ3IgogIF0sCiAgIl9zZF9hbGciOiJzaGEtMjU2IiwKICAiaWF0IjowLAogICJuYmYiOjEsCiAgImV4cCI6MTkyNDk4ODM5OSwKICAiaXNzIjoiaXNzdWVyIiwKICAidmN0IjoidmN0IiwKICAic3RhdHVzIjoic29tZXRoaW5nIgp9.ZXdvZ0lDSmhiR2NpT2lKRlV6VXhNaUlzQ2lBZ0luUjVjQ0k2SWtwWFZDSXNDaUFnSW10cFpDSTZJbXRsZVVsa0lncDkuLkFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUxFOVpEWTF5QmVHNm9HS2gtdU56dFlfR0RYX3ItbWdmbWc3VlcxbzgtYmZBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFJVmwyNFZZRjRJeVNNZmhtSEhUTFc4bTRJU1pOWXhlaXZJenBSd0pxZld0"
        )

        val credentialStatusProperties = TokenStatusListProperties(
            TokenStatusListProperties.StatusList(
                index = 0,
                uri = "uri"
            )
        )
    }
}
