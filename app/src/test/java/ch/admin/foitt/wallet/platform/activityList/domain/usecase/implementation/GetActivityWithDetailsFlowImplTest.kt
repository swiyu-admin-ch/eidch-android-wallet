package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityListError
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityWithDetails
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityWithDetailsRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivityWithDetailsFlow
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.ACTIVITY_ID
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityWithDetails
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

class GetActivityWithDetailsFlowImplTest {
    @MockK
    private lateinit var mockActivityWithDetailsRepository: ActivityWithDetailsRepository

    @MockK
    private lateinit var mockMapToActivityDisplayData: MapToActivityDisplayData

    private lateinit var useCase: GetActivityWithDetailsFlow

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetActivityWithDetailsFlowImpl(
            activityWithDetailsRepository = mockActivityWithDetailsRepository,
            mapToActivityDisplayData = mockMapToActivityDisplayData,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting activity with details returns the correct data`() = runTest {
        val result = useCase(ACTIVITY_ID).firstOrNull()

        assertNotNull(result)
        val activityWithDetails = result.assertOk()

        val expected = ActivityWithDetails(
            activity = activityDisplayData,
        )

        assertEquals(expected, activityWithDetails)
    }

    @Test
    fun `Getting activity details where getting the activity fails returns an error`() = runTest {
        coEvery {
            mockActivityWithDetailsRepository.getByIdFlow(ACTIVITY_ID)
        } returns flowOf(Err(ActivityListError.Unexpected(IllegalStateException())))

        val result = useCase(ACTIVITY_ID).firstOrNull()

        assertNotNull(result)
        result.assertErrorType(ActivityListError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockActivityWithDetailsRepository.getByIdFlow(ACTIVITY_ID)
        } returns flowOf(Ok(activityWithDetails))

        coEvery {
            mockMapToActivityDisplayData(activityWithDetails)
        } returns activityDisplayData
    }
}
