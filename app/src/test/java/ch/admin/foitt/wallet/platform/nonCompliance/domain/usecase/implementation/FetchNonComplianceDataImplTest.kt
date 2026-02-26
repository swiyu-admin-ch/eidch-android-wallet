package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceData
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.repository.NonComplianceTrustRepository
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.FetchNonComplianceData
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation.mock.NonComplianceMocks.NON_REPORTED_ACTOR_DID
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation.mock.NonComplianceMocks.REPORTED_ACTOR_DID
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation.mock.NonComplianceMocks.nonComplianceReasonDisplays
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation.mock.NonComplianceMocks.nonComplianceResponseSuccess
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FetchNonComplianceDataImplTest {

    @MockK
    private lateinit var mockGetTrustDomainFromDid: GetTrustDomainFromDid

    @MockK
    private lateinit var mockNonComplianceTrustRepository: NonComplianceTrustRepository

    private lateinit var useCase: FetchNonComplianceData

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = FetchNonComplianceDataImpl(
            getTrustDomainFromDid = mockGetTrustDomainFromDid,
            nonComplianceTrustRepository = mockNonComplianceTrustRepository,
        )

        coEvery {
            mockGetTrustDomainFromDid(any())
        } returns Ok(trustDomain)

        coEvery {
            mockNonComplianceTrustRepository.fetchNonComplianceData(trustDomain)
        } returns Ok(nonComplianceResponseSuccess)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching non compliance data for reported actor returns reported with localized reason`() = runTest {
        val result = useCase(REPORTED_ACTOR_DID)

        val expected = NonComplianceData(
            state = NonComplianceState.REPORTED,
            reasonDisplays = nonComplianceReasonDisplays,
        )

        assertEquals(expected, result)
    }

    @Test
    fun `Fetching non compliance data for non-reported actor returns not reported`() = runTest {
        val result = useCase(NON_REPORTED_ACTOR_DID)

        val expected = NonComplianceData(
            state = NonComplianceState.NOT_REPORTED,
            reasonDisplays = null,
        )

        assertEquals(expected, result)
    }

    @Test
    fun `Fetching non compliance data where a trust domain error occurs returns unknown`() = runTest {
        coEvery {
            mockGetTrustDomainFromDid(any())
        } returns Err(TrustRegistryError.Unexpected(IllegalStateException("error when getting trust domain")))

        val result = useCase(REPORTED_ACTOR_DID)

        val expected = NonComplianceData(
            state = NonComplianceState.UNKNOWN,
            reasonDisplays = null,
        )

        assertEquals(expected, result)
    }

    @Test
    fun `Fetching non compliance data where an error occurs returns unknown`() = runTest {
        coEvery {
            mockNonComplianceTrustRepository.fetchNonComplianceData(trustDomain)
        } returns Err(NonComplianceError.Unexpected(IllegalStateException("error when fetching non compliance data")))

        val result = useCase(REPORTED_ACTOR_DID)

        val expected = NonComplianceData(
            state = NonComplianceState.UNKNOWN,
            reasonDisplays = null,
        )

        assertEquals(expected, result)
    }

    private val trustDomain = "trustDomain.example.org"
}
