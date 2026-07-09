package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import android.os.SystemClock
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.IdentityType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseWithStateRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestStateRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.PollSIdRequestAfterFileSubmitImpl
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PollSIdRequestAfterFileSubmitImplTest {

    @MockK
    private lateinit var mockFetchSidStatus: FetchSIdStatus

    @MockK
    private lateinit var mockEIdRequestStateRepository: EIdRequestStateRepository

    @MockK
    private lateinit var mockEIdRequestCaseWithStateRepository: EIdRequestCaseWithStateRepository

    private val testDispatcher = StandardTestDispatcher()

    private val fileSubmittedIndex = EIdRequestQueueState.entries.size + 1

    private val fileSubmittedRequest = EIdRequestCaseWithState(
        case = EIdRequestCase(
            id = "case$fileSubmittedIndex",
            rawMrz = "rawMrz",
            documentNumber = "doc$fileSubmittedIndex",
            selectedDocumentType = IdentityType.SWISS_IDK,
            firstName = "First$fileSubmittedIndex",
            lastName = "Last$fileSubmittedIndex",
            filesSubmitted = true,
        ),
        state = EIdRequestState(
            eIdRequestCaseId = "case$fileSubmittedIndex",
            state = EIdRequestQueueState.IN_AUTO_VERIFICATION,
            lastPolled = 0L,
        )
    )

    val allQueueStatesList = (0..<EIdRequestQueueState.entries.size).map { index ->
        EIdRequestCaseWithState(
            case = EIdRequestCase(
                id = "case$index",
                rawMrz = "rawMrz",
                documentNumber = "doc$index",
                selectedDocumentType = IdentityType.SWISS_IDK,
                firstName = "First$index",
                lastName = "Last$index",
                filesSubmitted = false,
            ),
            state = EIdRequestState(
                eIdRequestCaseId = "case$index",
                state = EIdRequestQueueState.entries[index],
                lastPolled = 0L,
            )
        )
    }

    private val mockStateResponse: StateResponse = mockk()
    private val timeout = 60000L
    private val delay = 5000L
    private val timeTicksSingleLoop = listOf(1L, 1L, timeout + 1L)
    private val timeTicks6 = listOf(1L, 1L, 5L, delay + 1L, (delay * 2) + 1L, (delay * 2) + 5L, timeout + 1L)

    private lateinit var useCase: PollSIdRequestAfterFileSubmitImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(SystemClock::class)

        useCase = PollSIdRequestAfterFileSubmitImpl(
            fetchSIdStatus = mockFetchSidStatus,
            eIdRequestStateRepository = mockEIdRequestStateRepository,
            eIdRequestCaseWithStateRepository = mockEIdRequestCaseWithStateRepository,
            ioDispatcher = testDispatcher,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `The polling is triggered when one of the requests is in the targeted state`() = runTest(testDispatcher) {
        coEvery { SystemClock.elapsedRealtime() } returnsMany timeTicksSingleLoop
        coEvery {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates()
        } returns Ok(allQueueStatesList.plus(fileSubmittedRequest))

        useCase()

        coVerify {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates()
            mockFetchSidStatus(any())
            mockEIdRequestStateRepository.updateStatusByCaseId(any(), any())
        }
    }

    @Test
    fun `The polling only updates the requests that are in the targeted state`() = runTest(testDispatcher) {
        val submittedRequest2 = EIdRequestCaseWithState(
            case = fileSubmittedRequest.case.copy(id = "request2"),
            state = fileSubmittedRequest.state?.copy(eIdRequestCaseId = "request2"),
        )

        val submittedRequest3 = EIdRequestCaseWithState(
            case = fileSubmittedRequest.case.copy(id = "request3"),
            state = fileSubmittedRequest.state?.copy(eIdRequestCaseId = "request3"),
        )

        val requestListWith3Submitted = allQueueStatesList
            .plus(listOf(fileSubmittedRequest, submittedRequest2, submittedRequest3))

        coEvery { SystemClock.elapsedRealtime() } returnsMany timeTicksSingleLoop
        coEvery {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates()
        } returns Ok(requestListWith3Submitted)

        useCase()

        coVerifyOrder {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates()

            mockFetchSidStatus(caseId = fileSubmittedRequest.case.id)
            mockEIdRequestStateRepository.updateStatusByCaseId(fileSubmittedRequest.case.id, any())

            mockFetchSidStatus(caseId = submittedRequest2.case.id)
            mockEIdRequestStateRepository.updateStatusByCaseId(submittedRequest2.case.id, any())

            mockFetchSidStatus(caseId = submittedRequest3.case.id)
            mockEIdRequestStateRepository.updateStatusByCaseId(submittedRequest3.case.id, any())
        }

        coVerify(exactly = 3) {
            mockFetchSidStatus(caseId = any())
        }
    }

    @Test
    fun `If the state does not change, the polling keep refreshing that request until the timeout`() = runTest(testDispatcher) {
        coEvery { SystemClock.elapsedRealtime() } returnsMany timeTicks6
        coEvery {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates()
        } returns Ok(listOf(fileSubmittedRequest))

        useCase()

        coVerify(exactly = 5) {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates()
            mockFetchSidStatus(caseId = fileSubmittedRequest.case.id)
            mockEIdRequestStateRepository.updateStatusByCaseId(fileSubmittedRequest.case.id, mockStateResponse)
        }
    }

    @Test
    fun `The polling stops early when no request has the targeted state`() = runTest(testDispatcher) {
        useCase()

        coVerify(exactly = 1) {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates()
        }

        coVerify(exactly = 0) {
            mockFetchSidStatus(caseId = any())
            mockEIdRequestStateRepository.updateStatusByCaseId(any(), any())
        }
    }

    @Test
    fun `The polling stops as soon as no request has the targeted state`() = runTest(testDispatcher) {
        val changedState = EIdRequestCaseWithState(
            case = fileSubmittedRequest.case.copy(),
            state = fileSubmittedRequest.state?.copy(state = EIdRequestQueueState.WAITING_FOR_VERIFICATION_APPROVAL),
        )

        coEvery { SystemClock.elapsedRealtime() } returnsMany timeTicks6

        coEvery {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates()
        } returnsMany listOf(
            Ok(allQueueStatesList.plus(fileSubmittedRequest)),
            Ok(allQueueStatesList.plus(fileSubmittedRequest)),
            Ok(allQueueStatesList.plus(changedState)),
            Ok(allQueueStatesList.plus(fileSubmittedRequest)),
        )

        useCase()

        coVerify(exactly = 3) {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates()
        }

        coVerify(exactly = 2) {
            mockFetchSidStatus(caseId = fileSubmittedRequest.case.id)
            mockEIdRequestStateRepository.updateStatusByCaseId(fileSubmittedRequest.case.id, mockStateResponse)
        }
    }

    @Test
    fun `An EIdRequestCaseWithStateRepository error stops the polling`() = runTest(testDispatcher) {
        val exception = Exception("some exception")
        coEvery { SystemClock.elapsedRealtime() } returnsMany timeTicksSingleLoop
        coEvery {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates()
        } returns Err(EIdRequestError.Unexpected(exception))

        useCase()

        coVerifyOrder {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates()
        }

        coVerify(exactly = 0) {
            mockFetchSidStatus(caseId = any())
            mockEIdRequestStateRepository.updateStatusByCaseId(caseId = any(), stateResponse = any())
        }
    }

    @Test
    fun `An EIdRequestStateRepository error is ignored`() = runTest(testDispatcher) {
        val exception = Exception("some exception")
        coEvery { SystemClock.elapsedRealtime() } returnsMany timeTicksSingleLoop

        coEvery {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates()
        } returns Ok(allQueueStatesList.plus(fileSubmittedRequest))

        coEvery {
            mockEIdRequestStateRepository.updateStatusByCaseId(any(), any())
        } returns Err(EIdRequestError.Unexpected(exception))

        useCase()

        coVerifyOrder {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates()
            mockFetchSidStatus(caseId = any())
            mockEIdRequestStateRepository.updateStatusByCaseId(caseId = any(), stateResponse = any())
        }
    }

    @Test
    fun `A status fetch error does not update the request state`() = runTest(testDispatcher) {
        val exception = Exception("some exception")
        coEvery { SystemClock.elapsedRealtime() } returnsMany timeTicksSingleLoop

        coEvery { mockFetchSidStatus.invoke(caseId = any()) } returns Err(EIdRequestError.Unexpected(exception))

        coEvery {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates()
        } returns Ok(allQueueStatesList.plus(fileSubmittedRequest))

        useCase()

        coVerifyOrder {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates()
            mockFetchSidStatus(caseId = any())
        }

        coVerify(exactly = 0) {
            mockEIdRequestStateRepository.updateStatusByCaseId(caseId = any(), stateResponse = any())
        }
    }

    private fun setupDefaultMocks() {
        coEvery { mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates() } returns Ok(allQueueStatesList)
        coEvery { mockFetchSidStatus.invoke(caseId = any()) } returns Ok(mockStateResponse)
        coEvery { mockEIdRequestStateRepository.updateStatusByCaseId(any(), any()) } returns Ok(1)
        coEvery { SystemClock.elapsedRealtime() } returns 1L
    }
}
