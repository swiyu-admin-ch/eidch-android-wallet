package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import android.os.SystemClock
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toSIdRequestDisplayStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseWithStateRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestStateRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.PollSIdRequestAfterFileSubmit
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

internal class PollSIdRequestAfterFileSubmitImpl @Inject constructor(
    private val fetchSIdStatus: FetchSIdStatus,
    private val eIdRequestStateRepository: EIdRequestStateRepository,
    private val eIdRequestCaseWithStateRepository: EIdRequestCaseWithStateRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PollSIdRequestAfterFileSubmit {
    @Suppress("LoopWithTooManyJumpStatements")
    override suspend fun invoke() = withContext(ioDispatcher) {
        val endTime = SystemClock.elapsedRealtime() + POLLING_MAX_DURATION
        Timber.d("$POLLING_MSG started")

        while (true) {
            Timber.d("$POLLING_MSG run")
            if (SystemClock.elapsedRealtime() >= endTime) break

            val eIdRequests = eIdRequestCaseWithStateRepository.getEIdRequestCasesWithStates()
            val polledRequests = eIdRequests.get()?.let { requestList ->
                requestList.filter { request ->
                    request.toSIdRequestDisplayStatus() == SIdRequestDisplayStatus.AV_FILES_SUBMITTED
                }
            }

            if (polledRequests.isNullOrEmpty()) break

            for (request in polledRequests) {
                val caseId = request.case.id
                fetchSIdStatus(caseId)
                    .onSuccess { stateResponse ->
                        Timber.d("$POLLING_MSG updating caseId $caseId state")
                        eIdRequestStateRepository.updateStatusByCaseId(caseId, stateResponse)
                    }.onFailure { error ->
                        Timber.d(message = "$POLLING_MSG Could not get case Ids for status update, caseId $caseId, error $error")
                    }
            }
            delay(POLLING_DELAY)
        }
        Timber.d("$POLLING_MSG stop")
    }

    private companion object {
        const val POLLING_DELAY = 5000L
        const val POLLING_MAX_DURATION = 60000L
        const val POLLING_MSG = "Polling e-ID file submission:"
    }
}
