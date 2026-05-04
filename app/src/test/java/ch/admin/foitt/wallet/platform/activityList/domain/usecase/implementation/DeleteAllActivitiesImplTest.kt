package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityListError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.CredentialActivityRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.DeleteAllActivities
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteAllActivitiesImplTest {

    @MockK
    private lateinit var mockCredentialActivityRepository: CredentialActivityRepository

    private lateinit var useCase: DeleteAllActivities

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = DeleteAllActivitiesImpl(
            credentialActivityRepository = mockCredentialActivityRepository,
        )

        coEvery {
            mockCredentialActivityRepository.deleteAllActivities()
        } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully deleting all activities returns success`() = runTest {
        useCase().assertOk()
    }

    @Test
    fun `Deleting all activities maps errors from the repository`() = runTest {
        coEvery {
            mockCredentialActivityRepository.deleteAllActivities()
        } returns Err(ActivityListError.Unexpected(IllegalStateException("error in repo")))

        useCase().assertErrorType(ActivityListError.Unexpected::class)
    }
}
