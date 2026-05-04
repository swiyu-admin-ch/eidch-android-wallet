package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityStateRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.AreActivitiesEnabledFlow
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AreActivitiesEnabledFlowImplTest {

    @MockK
    private lateinit var mockActivityStateRepository: ActivityStateRepository

    val stateFlow = MutableStateFlow(true).asStateFlow()

    private lateinit var useCase: AreActivitiesEnabledFlow

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = AreActivitiesEnabledFlowImpl(
            activityStateRepository = mockActivityStateRepository,
        )

        coEvery {
            mockActivityStateRepository.areActivitiesEnabledFlow()
        } returns stateFlow
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting the activity list enabled state as flow calls the repo`() = runTest {
        val result = useCase()

        assertEquals(stateFlow, result)

        coVerify {
            mockActivityStateRepository.areActivitiesEnabledFlow()
        }
    }
}
