package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SaveIssuanceActivity
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

class SaveIssuanceActivityImplTest {

    @MockK
    private lateinit var mockActivityRepository: ActivityRepository

    private lateinit var useCase: SaveIssuanceActivity

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = SaveIssuanceActivityImpl(
            activityRepository = mockActivityRepository,
        )

        coEvery {
            mockActivityRepository.saveActivity(any(), any(), any(), any(), any(), any())
        } returns Ok(1)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Saving an issuance activity calls the repo with the correct parameters`() = runTest {
        val credentialId = 1L
        val actorDisplayData = mockk<ActorDisplayData>()
        val actorFallbackName = "fallback"

        useCase(
            credentialId = credentialId,
            actorDisplayData = actorDisplayData,
            issuerFallbackName = actorFallbackName
        )

        coVerify {
            mockActivityRepository.saveActivity(
                activityType = ActivityType.ISSUANCE,
                credentialId = credentialId,
                actorDisplayData = actorDisplayData,
                actorFallbackName = actorFallbackName,
                nonComplianceData = null,
            )
        }
    }
}
