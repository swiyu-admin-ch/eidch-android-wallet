package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityListError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityActorDisplayWithImageRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivityActorDisplaysFlow
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.ACTIVITY_ID
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.CREDENTIAL_ID
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.actorDisplay1
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.actorDisplay2
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

class GetActivityActorDisplaysFlowImplTest {

    @MockK
    private lateinit var mockActivityActorDisplayWithImageRepository: ActivityActorDisplayWithImageRepository

    @MockK
    private lateinit var mockMapToActivityActorDisplayData: MapToActivityActorDisplayData

    private lateinit var useCase: GetActivityActorDisplaysFlow

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetActivityActorDisplaysFlowImpl(
            activityActorDisplayWithImageRepository = mockActivityActorDisplayWithImageRepository,
            mapToActivityActorDisplayData = mockMapToActivityActorDisplayData,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting activities with displays correctly returns the correct data`() = runTest {
        val result = useCase(ACTIVITY_ID).firstOrNull()

        assertNotNull(result)
        val displayData = result.assertOk()
        assertEquals(activityActorDisplayData, displayData)
    }

    @Test
    fun `Getting activity actor displays maps errors from repository`() = runTest {
        coEvery {
            mockActivityActorDisplayWithImageRepository.getActorDisplaysWithImageByActivityIdFlow(ACTIVITY_ID)
        } returns flowOf(Err(ActivityListError.Unexpected(IllegalStateException())))

        val result = useCase(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result.assertErrorType(ActivityListError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockActivityActorDisplayWithImageRepository.getActorDisplaysWithImageByActivityIdFlow(ACTIVITY_ID)
        } returns flowOf(Ok(listOf(actorDisplay1, actorDisplay2)))

        coEvery {
            mockMapToActivityActorDisplayData(
                activityId = ACTIVITY_ID,
                actorDisplaysWithImages = listOf(actorDisplay1, actorDisplay2)
            )
        } returns activityActorDisplayData
    }
}
