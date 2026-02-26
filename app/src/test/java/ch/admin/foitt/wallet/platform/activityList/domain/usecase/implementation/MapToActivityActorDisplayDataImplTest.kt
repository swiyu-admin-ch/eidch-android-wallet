package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityActorDisplay1
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityActorDisplay2
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.actorDisplay1
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.actorDisplay2
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.imageData1
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MapToActivityActorDisplayDataImplTest {

    @MockK
    private lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    private lateinit var useCase: MapToActivityActorDisplayData

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = MapToActivityActorDisplayDataImpl(
            getLocalizedDisplay = mockGetLocalizedDisplay,
        )

        coEvery {
            mockGetLocalizedDisplay(listOf(activityActorDisplay1, activityActorDisplay2))
        } returns activityActorDisplay1
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Mapping an activityWithDisplays to an ActivityActorData works correctly`() = runTest {
        val result = useCase(activityId = 1, listOf(actorDisplay1, actorDisplay2))

        val expected = ActivityActorDisplayData(
            id = activityActorDisplay1.activityId,
            localizedActorName = activityActorDisplay1.name,
            actorImageData = imageData1
        )

        assertEquals(expected, result)
    }
}
