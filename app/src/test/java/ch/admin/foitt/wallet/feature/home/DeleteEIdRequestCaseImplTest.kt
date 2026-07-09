package ch.admin.foitt.wallet.feature.home

import ch.admin.foitt.wallet.feature.home.domain.model.HomeError
import ch.admin.foitt.wallet.feature.home.domain.usecase.DeleteEIdRequestCase
import ch.admin.foitt.wallet.feature.home.domain.usecase.implementation.DeleteEIdRequestCaseImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.mocks.EIdRequestMocks
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.PushNotificationError
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.DeletePushId
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteEIdRequestCaseImplTest {

    @MockK
    private lateinit var mockEIdRequestCaseRepository: EIdRequestCaseRepository

    @MockK
    private lateinit var mockDeletePushId: DeletePushId

    private val caseWithoutPushId = EIdRequestMocks.eIdRequestCase
    private val caseWithPushId = EIdRequestMocks.eIdRequestCase.copy(pushId = "pushId")

    lateinit var deleteEIdRequestCase: DeleteEIdRequestCase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        deleteEIdRequestCase = DeleteEIdRequestCaseImpl(mockEIdRequestCaseRepository, mockDeletePushId)
    }

    @Test
    fun `Successful delete without push id returns ok`() = runTest {
        coEvery { mockEIdRequestCaseRepository.getEIdRequestCase("caseId") } returns Ok(caseWithoutPushId)
        coEvery { mockEIdRequestCaseRepository.deleteEIdRequestCase("caseId") } returns Ok(Unit)

        deleteEIdRequestCase("caseId").assertOk()
    }

    @Test
    fun `Successful delete with push id returns ok`() = runTest {
        coEvery { mockEIdRequestCaseRepository.getEIdRequestCase("caseId") } returns Ok(caseWithPushId)
        coEvery { mockEIdRequestCaseRepository.deleteEIdRequestCase("caseId") } returns Ok(Unit)
        coEvery { mockDeletePushId("pushId") } returns Ok(Unit)

        deleteEIdRequestCase("caseId").assertOk()
    }

    @Test
    fun `Failed get e-id returns error`() = runTest {
        val exception = IllegalStateException("error getting case")
        coEvery {
            mockEIdRequestCaseRepository.getEIdRequestCase("caseId")
        } returns Err(EIdRequestError.Unexpected(exception))

        deleteEIdRequestCase("caseId").assertErrorType(HomeError.Unexpected::class)
    }

    @Test
    fun `Failed delete push id returns error`() = runTest {
        coEvery { mockEIdRequestCaseRepository.getEIdRequestCase("caseId") } returns Ok(caseWithPushId)
        coEvery { mockEIdRequestCaseRepository.deleteEIdRequestCase("caseId") } returns Ok(Unit)
        coEvery { mockDeletePushId("pushId") } returns Err(PushNotificationError.NetworkError)

        deleteEIdRequestCase("caseId").assertErrorType(HomeError.Unexpected::class)
    }
}
