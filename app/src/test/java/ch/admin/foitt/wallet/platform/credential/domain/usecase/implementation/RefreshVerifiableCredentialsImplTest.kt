package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAllAnyCredentialsByCredentialId
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshCredential
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.CredentialRefreshDataError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import ch.admin.foitt.wallet.platform.database.domain.model.BatchRefreshDataEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithAuthentication
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.SwissProfile
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetSwissProfileStatus
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.time.Instant
import java.time.temporal.ChronoUnit

class RefreshVerifiableCredentialsImplTest {
    @MockK
    private lateinit var mockCredentialRefreshDataRepository: CredentialRefreshDataRepository

    @MockK
    private lateinit var mockBundleItemRepository: BundleItemRepository

    @MockK
    private lateinit var mockGetAllAnyCredentialsByCredentialId: GetAllAnyCredentialsByCredentialId

    @MockK
    private lateinit var mockGetSwissProfileStatus: GetSwissProfileStatus

    @MockK
    private lateinit var mockRefreshCredential: RefreshCredential

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockAnyCredential: AnyCredential

    private lateinit var useCase: RefreshVerifiableCredentialsImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockCredentialRefreshDataRepository.getAllCredentialRefreshData() } returns Ok(listOf(credentialData()))
        coEvery { mockGetSwissProfileStatus(any()) } returns SwissProfile.Status.CURRENT
        coEvery { mockGetAllAnyCredentialsByCredentialId(any()) } returns Ok(listOf(mockAnyCredential))
        every { mockAnyCredential.profileVersion } returns "swiss-profile-vc:1"
        every { mockAnyCredential.validUntilInstant } returns Instant.now().plus(365, ChronoUnit.DAYS)
        coEvery { mockBundleItemRepository.getNeverPresentedCount(any()) } returns Ok(1)
        every { mockEnvironmentSetupRepository.batchIssuanceEnabled } returns true
        coEvery { mockRefreshCredential(any()) } returns Ok(Unit)

        useCase = RefreshVerifiableCredentialsImpl(
            credentialRefreshDataRepository = mockCredentialRefreshDataRepository,
            bundleItemRepository = mockBundleItemRepository,
            getAllAnyCredentialsByCredentialId = mockGetAllAnyCredentialsByCredentialId,
            getSwissProfileStatus = mockGetSwissProfileStatus,
            refreshCredential = mockRefreshCredential,
            environmentSetupRepository = mockEnvironmentSetupRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `No refresh when there are no credentials`() = runTest {
        coEvery { mockCredentialRefreshDataRepository.getAllCredentialRefreshData() } returns Ok(emptyList())
        useCase()

        coVerify(exactly = 0) { mockRefreshCredential(any()) }
    }

    @Test
    fun `No refresh when loading the credentials fails`() = runTest {
        coEvery {
            mockCredentialRefreshDataRepository.getAllCredentialRefreshData()
        } returns Err(CredentialRefreshDataError.Unexpected(Exception("exception")))
        useCase()

        coVerify(exactly = 0) { mockRefreshCredential(any()) }
    }

    @Test
    fun `No refresh when profile is current, not near expiry and not a batch`() = runTest {
        useCase()

        coVerify(exactly = 0) { mockRefreshCredential(any()) }
    }

    @ParameterizedTest
    @EnumSource(value = SwissProfile.Status::class, names = ["CURRENT"], mode = EnumSource.Mode.EXCLUDE)
    fun `Refreshes accepted credential when swiss profile is not current`(status: SwissProfile.Status) = runTest {
        coEvery { mockGetSwissProfileStatus(any()) } returns status
        useCase()

        coVerify(exactly = 1) { mockRefreshCredential(CREDENTIAL_ID) }
    }

    @Test
    fun `Refreshes an accepted credential when it is nearly expired`() = runTest {
        every { mockAnyCredential.validUntilInstant } returns Instant.now().plus(1, ChronoUnit.DAYS)
        useCase()

        coVerify(exactly = 1) { mockRefreshCredential(CREDENTIAL_ID) }
    }

    @Test
    fun `No refresh when non-accepted credential`() = runTest {
        coEvery {
            mockCredentialRefreshDataRepository.getAllCredentialRefreshData()
        } returns Ok(listOf(credentialData(state = VerifiableProgressionState.UNACCEPTED)))
        coEvery { mockGetSwissProfileStatus(any()) } returns SwissProfile.Status.DEPRECATED
        useCase()

        coVerify(exactly = 0) { mockRefreshCredential(any()) }
    }

    @Test
    fun `No refresh without a refresh token`() = runTest {
        coEvery {
            mockCredentialRefreshDataRepository.getAllCredentialRefreshData()
        } returns Ok(listOf(credentialData(token = null)))
        coEvery { mockGetSwissProfileStatus(any()) } returns SwissProfile.Status.DEPRECATED
        useCase()

        coVerify(exactly = 0) { mockRefreshCredential(any()) }
    }

    @Test
    fun `Refreshes when the batch is below threshold and batch issuance enabled`() = runTest {
        coEvery {
            mockCredentialRefreshDataRepository.getAllCredentialRefreshData()
        } returns Ok(listOf(credentialData(batchData = BatchRefreshDataEntity(CREDENTIAL_ID, batchSize = 10))))
        coEvery { mockBundleItemRepository.getNeverPresentedCount(CREDENTIAL_ID) } returns Ok(1)
        useCase()

        coVerify(exactly = 1) { mockRefreshCredential(CREDENTIAL_ID) }
    }

    @Test
    fun `No refresh on low batch threshold when batch issuance is disabled`() = runTest {
        every { mockEnvironmentSetupRepository.batchIssuanceEnabled } returns false
        coEvery {
            mockCredentialRefreshDataRepository.getAllCredentialRefreshData()
        } returns Ok(listOf(credentialData(batchData = BatchRefreshDataEntity(CREDENTIAL_ID, batchSize = 10))))
        coEvery { mockBundleItemRepository.getNeverPresentedCount(CREDENTIAL_ID) } returns Ok(1)
        useCase()

        coVerify(exactly = 0) { mockRefreshCredential(any()) }
    }

    @Test
    fun `No refresh on batch threshold not reached`() = runTest {
        coEvery {
            mockCredentialRefreshDataRepository.getAllCredentialRefreshData()
        } returns Ok(listOf(credentialData(batchData = BatchRefreshDataEntity(CREDENTIAL_ID, batchSize = 10))))
        coEvery { mockBundleItemRepository.getNeverPresentedCount(CREDENTIAL_ID) } returns Ok(5)
        useCase()

        coVerify(exactly = 0) { mockRefreshCredential(any()) }
    }

    @Test
    fun `Skipping refresh on error when getting never presented count`() = runTest {
        coEvery {
            mockCredentialRefreshDataRepository.getAllCredentialRefreshData()
        } returns Ok(listOf(credentialData(batchData = BatchRefreshDataEntity(CREDENTIAL_ID, batchSize = 10))))
        coEvery {
            mockBundleItemRepository.getNeverPresentedCount(CREDENTIAL_ID)
        } returns Err(SsiError.Unexpected(IllegalStateException("db error")))
        useCase()

        coVerify(exactly = 0) { mockRefreshCredential(any()) }
    }

    @Test
    fun `Refreshes when the swiss profile version is unknown`() = runTest {
        coEvery { mockGetSwissProfileStatus(any()) } returns null
        useCase()

        coVerify(exactly = 1) { mockRefreshCredential(CREDENTIAL_ID) }
    }

    @Test
    fun `A failing refresh does not stop the refresh of the other credentials`() = runTest {
        coEvery {
            mockCredentialRefreshDataRepository.getAllCredentialRefreshData()
        } returns Ok(listOf(credentialData(credId = 1L), credentialData(credId = 2L)))
        coEvery { mockGetSwissProfileStatus(any()) } returns SwissProfile.Status.DEPRECATED
        coEvery { mockRefreshCredential(1L) } returns Err(CredentialError.Unexpected(Exception("exception")))
        coEvery { mockRefreshCredential(2L) } returns Ok(Unit)
        useCase()
        coVerify(exactly = 1) { mockRefreshCredential(1L) }
        coVerify(exactly = 1) { mockRefreshCredential(2L) }
    }

    @Test
    fun `Does not refresh when the renewal interval has not elapsed`() = runTest {
        coEvery {
            mockCredentialRefreshDataRepository.getAllCredentialRefreshData()
        } returns Ok(listOf(credentialData(lastAttempt = Instant.now().epochSecond)))
        coEvery { mockGetSwissProfileStatus(any()) } returns SwissProfile.Status.DEPRECATED

        useCase()

        coVerify(exactly = 0) { mockRefreshCredential(any()) }
    }

    @Test
    fun `Refreshes when the renewal interval has elapsed`() = runTest {
        val tenDaysAgo = Instant.now().minus(10, ChronoUnit.DAYS).epochSecond
        coEvery {
            mockCredentialRefreshDataRepository.getAllCredentialRefreshData()
        } returns Ok(listOf(credentialData(lastAttempt = tenDaysAgo)))
        coEvery { mockGetSwissProfileStatus(any()) } returns SwissProfile.Status.DEPRECATED

        useCase()

        coVerify(exactly = 1) { mockRefreshCredential(CREDENTIAL_ID) }
    }

    @ParameterizedTest
    @MethodSource("renewalIntervalsByStatus")
    fun `Each swiss profile status refreshes only after its renewal interval has elapsed`(
        interval: Pair<SwissProfile.Status, Long>,
    ) = runTest {
        val (status, intervalDays) = interval
        coEvery { mockGetSwissProfileStatus(any()) } returns status
        every { mockAnyCredential.validUntilInstant } returns Instant.now().plus(1, ChronoUnit.DAYS)
        val intervalSeconds = intervalDays * 86_400

        // Last attempt is just inside the interval: no refresh
        coEvery {
            mockCredentialRefreshDataRepository.getAllCredentialRefreshData()
        } returns Ok(listOf(credentialData(lastAttempt = Instant.now().epochSecond - (intervalSeconds - 3_600))))
        useCase()
        coVerify(exactly = 0) { mockRefreshCredential(any()) }

        // Last attempt is just past the interval: refresh done
        coEvery {
            mockCredentialRefreshDataRepository.getAllCredentialRefreshData()
        } returns Ok(listOf(credentialData(lastAttempt = Instant.now().epochSecond - (intervalSeconds + 3_600))))
        useCase()
        coVerify(exactly = 1) { mockRefreshCredential(CREDENTIAL_ID) }
    }

    @Test
    fun `Batch below threshold refreshes even when it is within the renewal interval`() = runTest {
        coEvery {
            mockCredentialRefreshDataRepository.getAllCredentialRefreshData()
        } returns Ok(
            listOf(
                credentialData(
                    batchData = BatchRefreshDataEntity(CREDENTIAL_ID, batchSize = 10),
                    lastAttempt = Instant.now().epochSecond,
                ),
            ),
        )
        coEvery { mockBundleItemRepository.getNeverPresentedCount(CREDENTIAL_ID) } returns Ok(1)

        useCase()

        coVerify(exactly = 1) { mockRefreshCredential(CREDENTIAL_ID) }
    }

    private fun credentialData(
        state: VerifiableProgressionState = VerifiableProgressionState.ACCEPTED,
        token: String? = "refreshToken",
        batchData: BatchRefreshDataEntity? = null,
        credId: Long = CREDENTIAL_ID,
        lastAttempt: Long? = null,
    ): VerifiableCredentialWithAuthentication = VerifiableCredentialWithAuthentication(
        verifiableCredential = mockk {
            every { progressionState } returns state
            every { lastRefreshAttemptAt } returns lastAttempt
        },
        credential = mockk { every { id } returns credId },
        batchData = batchData,
        authentication = token?.let { t -> mockk { every { refreshToken } returns t } },
    )

    companion object {
        const val CREDENTIAL_ID = 1L

        @JvmStatic
        fun renewalIntervalsByStatus() = listOf(
            SwissProfile.Status.CURRENT to 7L,
            SwissProfile.Status.DEPRECATED to 5L,
            SwissProfile.Status.SUNSET to 2L,
            SwissProfile.Status.END_OF_LIFE to 1L,
        )
    }
}
