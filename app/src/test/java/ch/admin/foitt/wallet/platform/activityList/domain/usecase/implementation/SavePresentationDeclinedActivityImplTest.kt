package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityStateRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SavePresentationDeclinedActivity
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SavePresentationDeclinedActivityImplTest {

    @MockK
    private lateinit var mockActivityStateRepository: ActivityStateRepository

    @MockK
    private lateinit var mockActivityRepository: ActivityRepository

    private lateinit var useCase: SavePresentationDeclinedActivity

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = SavePresentationDeclinedActivityImpl(
            activityStateRepository = mockActivityStateRepository,
            activityRepository = mockActivityRepository,
        )

        coEvery {
            mockActivityStateRepository.areActivitiesEnabled()
        } returns true

        coEvery {
            mockActivityRepository.saveActivity(any(), any(), any(), any(), any(), any())
        } returns Ok(1)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Saving a presentation activity where activity list is enabled calls the repo with the correct parameters`() = runTest {
        val credentialId = 1L
        val actorDisplayData = mockk<ActorDisplayData>()
        val actorFallbackName = "fallback"
        val claimIds = listOf(1L, 2L)
        val nonComplianceData = "nonComplianceData"

        useCase(
            credentialId = credentialId,
            actorDisplayData = actorDisplayData,
            verifierFallbackName = actorFallbackName,
            claimIds = claimIds,
            nonComplianceData = nonComplianceData,
        )

        coVerify {
            mockActivityRepository.saveActivity(
                activityType = ActivityType.PRESENTATION_DECLINED,
                credentialId = credentialId,
                actorDisplayData = actorDisplayData,
                actorFallbackName = actorFallbackName,
                claimIds = claimIds,
                nonComplianceData = nonComplianceData,
            )
        }
    }

    @Test
    fun `Saving a presentation activity where activity list is disabled does not call the repo`() = runTest {
        coEvery {
            mockActivityStateRepository.areActivitiesEnabled()
        } returns false

        val credentialId = 1L
        val actorDisplayData = mockk<ActorDisplayData>()
        val actorFallbackName = "fallback"
        val claimIds = listOf(1L, 2L)
        val nonComplianceData = "nonComplianceData"

        useCase(
            credentialId = credentialId,
            actorDisplayData = actorDisplayData,
            verifierFallbackName = actorFallbackName,
            claimIds = claimIds,
            nonComplianceData = nonComplianceData,
        )

        coVerify(exactly = 0) {
            mockActivityRepository.saveActivity(
                activityType = ActivityType.PRESENTATION_DECLINED,
                credentialId = credentialId,
                actorDisplayData = actorDisplayData,
                actorFallbackName = actorFallbackName,
                claimIds = claimIds,
                nonComplianceData = nonComplianceData,
            )
        }
    }
}
