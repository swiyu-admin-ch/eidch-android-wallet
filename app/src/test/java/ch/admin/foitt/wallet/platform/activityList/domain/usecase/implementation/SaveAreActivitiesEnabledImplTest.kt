package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityStateRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SaveAreActivitiesEnabled
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SaveAreActivitiesEnabledImplTest {

    @MockK
    private lateinit var mockActivityStateRepository: ActivityStateRepository

    private lateinit var useCase: SaveAreActivitiesEnabled

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = SaveAreActivitiesEnabledImpl(
            activityStateRepository = mockActivityStateRepository,
        )

        coEvery {
            mockActivityStateRepository.saveAreActivitiesEnabled(any())
        } just runs
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @ParameterizedTest
    @ValueSource(
        booleans = [true, false]
    )
    fun `Saving activity list state calls the repo with the correct parameter`(activityListEnabled: Boolean) = runTest {
        useCase(activityListEnabled)

        coVerify {
            mockActivityStateRepository.saveAreActivitiesEnabled(activityListEnabled)
        }
    }
}
