package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestStateRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateAllSIdStatuses
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.UpdateAllSIdStatusesImpl
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateAllSIdStatusesImplTest {

    @MockK
    private lateinit var mockEIdRequestStateRepository: EIdRequestStateRepository

    @MockK
    private lateinit var fetchSIdStatus: FetchSIdStatus

    lateinit var updateAllSIdStatuses: UpdateAllSIdStatuses

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        updateAllSIdStatuses = UpdateAllSIdStatusesImpl(
            eIdRequestStateRepository = mockEIdRequestStateRepository,
            fetchSIdStatus = fetchSIdStatus
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully update an eID status`() = runTest {
        val stateResponse = StateResponse(
            state = EIdRequestQueueState.READY_FOR_ONLINE_SESSION,
            queueInformation = null,
            legalRepresentant = null,
            onlineSessionStartTimeout = "2100-02-19T21:14:52Z",
            targetWallets = null
        )

        coEvery { mockEIdRequestStateRepository.getAllCaseIds() } returns Ok(listOf("caseID"))
        coEvery { fetchSIdStatus("caseID") } returns Ok(stateResponse)
        coEvery {
            mockEIdRequestStateRepository.updateStatusByCaseId(
                caseId = "caseID",
                stateResponse = stateResponse
            )
        } returns Ok(1)

        updateAllSIdStatuses()

        coVerify(exactly = 1) {
            mockEIdRequestStateRepository.updateStatusByCaseId(
                caseId = "caseID",
                stateResponse = stateResponse
            )
        }
    }

    @Test
    fun `If getAllCaseIds returns an error verify that update and fetchIdStatus are never called`() =
        runTest {
            val exception = IllegalStateException("error in db")
            coEvery { mockEIdRequestStateRepository.getAllCaseIds() } returns Err(
                EIdRequestError.Unexpected(
                    exception
                )
            )

            coVerify(exactly = 0) {
                fetchSIdStatus(any())
                mockEIdRequestStateRepository.updateStatusByCaseId(any(), any())
            }
        }

    @Test
    fun `If fetchSIdStatus returns an error verify that update is never called`() = runTest {
        val exception = IllegalStateException("error in db")
        coEvery { mockEIdRequestStateRepository.getAllCaseIds() } returns Ok(listOf("caseID"))
        coEvery { fetchSIdStatus(any()) } returns Err(EIdRequestError.Unexpected(exception))

        coVerify(exactly = 0) {
            mockEIdRequestStateRepository.updateStatusByCaseId(any(), any())
        }
    }
}
