package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityListError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityWithActorDisplaysRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivitiesWithDisplaysFlow
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityWithActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.CREDENTIAL_ID
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityWithActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.mock.ActivityListMocks.activityWithActorDisplays
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

class GetActivitiesWithDisplaysFlowImplTest {

    @MockK
    private lateinit var mockActivityWithActorDisplaysRepository: ActivityWithActorDisplaysRepository

    @MockK
    private lateinit var mockMapToActivityWithActorDisplayData: MapToActivityWithActorDisplayData

    private lateinit var useCase: GetActivitiesWithDisplaysFlow

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetActivitiesWithDisplaysFlowImpl(
            activityWithActorDisplaysRepository = mockActivityWithActorDisplaysRepository,
            mapToActivityWithActorDisplayData = mockMapToActivityWithActorDisplayData,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting activities with displays correctly returns the correct data`() = runTest {
        val result = useCase(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        val displayData = result.assertOk()
        assertEquals(listOf(activityWithActorDisplayData), displayData)
    }

    @Test
    fun `Getting activities with displays maps errors from repository`() = runTest {
        coEvery {
            mockActivityWithActorDisplaysRepository.getActivitiesByCredentialId(CREDENTIAL_ID)
        } returns flowOf(Err(ActivityListError.Unexpected(IllegalStateException())))

        val result = useCase(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result.assertErrorType(ActivityListError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockActivityWithActorDisplaysRepository.getActivitiesByCredentialId(CREDENTIAL_ID)
        } returns flowOf(Ok(listOf(activityWithActorDisplays)))

        coEvery {
            mockMapToActivityWithActorDisplayData(activityWithActorDisplays)
        } returns activityWithActorDisplayData
    }
}
