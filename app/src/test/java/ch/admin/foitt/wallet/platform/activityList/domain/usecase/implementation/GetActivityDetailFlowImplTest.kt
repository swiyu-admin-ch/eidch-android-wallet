package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityDetail
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityListError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityWithDetailsRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivityDetailFlow
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityDetailDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.ACTIVITY_ID
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.CREDENTIAL_ID
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityDetailDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityWithDetails
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.claimsWithDisplays
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.credentialClaimCluster
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.filteredClusters
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.mockCredentialDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.mockCredentialDisplays
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.mockVerifiableCredential
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.verifiableCredentialWithDisplaysAndClusters
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.credentialCluster.domain.usercase.MapToCredentialClaimCluster
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithDisplaysAndClustersRepository
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

class GetActivityDetailFlowImplTest {

    @MockK
    private lateinit var mockVerifiableCredentialWithDisplaysAndClustersRepository:
        VerifiableCredentialWithDisplaysAndClustersRepository

    @MockK
    private lateinit var mockActivityWithDetailsRepository: ActivityWithDetailsRepository

    @MockK
    private lateinit var mockMapToActivityDetailDisplayData: MapToActivityDetailDisplayData

    @MockK
    private lateinit var mockMapToCredentialDisplayData: MapToCredentialDisplayData

    @MockK
    private lateinit var mockMapToCredentialClaimCluster: MapToCredentialClaimCluster

    private lateinit var useCase: GetActivityDetailFlow

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetActivityDetailFlowImpl(
            verifiableCredentialWithDisplaysAndClustersRepository = mockVerifiableCredentialWithDisplaysAndClustersRepository,
            activityWithDetailsRepository = mockActivityWithDetailsRepository,
            mapToActivityDetailDisplayData = mockMapToActivityDetailDisplayData,
            mapToCredentialDisplayData = mockMapToCredentialDisplayData,
            mapToCredentialClaimCluster = mockMapToCredentialClaimCluster,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting activity details without claims returns the correct data`() = runTest {
        val result = useCase(CREDENTIAL_ID, ACTIVITY_ID).firstOrNull()

        assertNotNull(result)
        val activityDetail = result.assertOk()

        val expected = ActivityDetail(
            activity = activityDetailDisplayData,
            credential = mockCredentialDisplayData,
            claims = emptyList(),
        )

        assertEquals(expected, activityDetail)
    }

    @Test
    fun `Getting activity details with claims returns the correct data`() = runTest {
        coEvery { mockMapToCredentialClaimCluster(filteredClusters) } returns listOf(credentialClaimCluster)

        val result = useCase(CREDENTIAL_ID, ACTIVITY_ID).firstOrNull()

        assertNotNull(result)
        val activityDetail = result.assertOk()

        val expected = ActivityDetail(
            activity = activityDetailDisplayData,
            credential = mockCredentialDisplayData,
            claims = listOf(credentialClaimCluster),
        )

        assertEquals(expected, activityDetail)
    }

    @Test
    fun `Getting activity details where getting the credential fails returns an error`() = runTest {
        coEvery {
            mockVerifiableCredentialWithDisplaysAndClustersRepository.getVerifiableCredentialWithDisplaysAndClustersFlowById(CREDENTIAL_ID)
        } returns flowOf(Err(SsiError.Unexpected(IllegalStateException())))

        val result = useCase(CREDENTIAL_ID, ACTIVITY_ID).firstOrNull()

        assertNotNull(result)
        result.assertErrorType(ActivityListError.Unexpected::class)
    }

    @Test
    fun `Getting activity details where getting the activity fails returns an error`() = runTest {
        coEvery {
            mockActivityWithDetailsRepository.getNullableByIdFlow(ACTIVITY_ID)
        } returns flowOf(Err(ActivityListError.Unexpected(IllegalStateException())))

        val result = useCase(CREDENTIAL_ID, ACTIVITY_ID).firstOrNull()

        assertNotNull(result)
        result.assertErrorType(ActivityListError.Unexpected::class)
    }

    @Test
    fun `Getting activity details maps errors from mapping to credentialDisplayData`() = runTest {
        coEvery {
            mockMapToCredentialDisplayData(
                verifiableCredential = mockVerifiableCredential,
                credentialDisplays = mockCredentialDisplays,
                claims = claimsWithDisplays,
            )
        } returns Err(CredentialError.Unexpected(IllegalStateException()))

        val result = useCase(CREDENTIAL_ID, ACTIVITY_ID).firstOrNull()

        assertNotNull(result)
        result.assertErrorType(ActivityListError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockVerifiableCredentialWithDisplaysAndClustersRepository.getVerifiableCredentialWithDisplaysAndClustersFlowById(CREDENTIAL_ID)
        } returns flowOf(Ok(verifiableCredentialWithDisplaysAndClusters))

        coEvery {
            mockActivityWithDetailsRepository.getNullableByIdFlow(ACTIVITY_ID)
        } returns flowOf(Ok(activityWithDetails))

        coEvery {
            mockMapToActivityDetailDisplayData(activityWithDetails)
        } returns activityDetailDisplayData

        coEvery {
            mockMapToCredentialDisplayData(
                verifiableCredential = mockVerifiableCredential,
                credentialDisplays = mockCredentialDisplays,
                claims = claimsWithDisplays,
            )
        } returns Ok(mockCredentialDisplayData)

        coEvery { mockMapToCredentialClaimCluster(filteredClusters) } returns emptyList()
    }
}
