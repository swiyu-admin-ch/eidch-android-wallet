package ch.admin.foitt.wallet.feature.home.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.home.domain.model.GetEIdRequestsFlowError
import ch.admin.foitt.wallet.feature.home.domain.model.toGetEIdRequestsFlowError
import ch.admin.foitt.wallet.feature.home.domain.usecase.GetEIdRequestsFlow
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithStateRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayData
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toSIdRequestDisplayStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseWithStateRepository
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.utils.asDayFullMonthYear
import ch.admin.foitt.wallet.platform.utils.epochSecondsToZonedDateTime
import ch.admin.foitt.wallet.platform.utils.mapError
import ch.admin.foitt.wallet.platform.utils.mapOk
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onFailure
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

class GetEIdRequestsFlowImpl @Inject constructor(
    private val eIdRequestCaseWithStateRepository: EIdRequestCaseWithStateRepository,
    private val getCurrentAppLocale: GetCurrentAppLocale,
) : GetEIdRequestsFlow {
    override fun invoke(): Flow<Result<List<SIdRequestDisplayData>, GetEIdRequestsFlowError>> {
        val currentLocale = getCurrentAppLocale()
        val requestCaseList = eIdRequestCaseWithStateRepository.getEIdRequestCasesWithStatesFlow()
            .mapError(EIdRequestCaseWithStateRepositoryError::toGetEIdRequestsFlowError)
            .mapOk { caseList ->
                caseList.map { case ->
                    case.toSIdRequestDisplayData(currentLocale)
                }
            }
        return requestCaseList
    }

    private fun EIdRequestCaseWithState.toSIdRequestDisplayData(currentLocale: Locale) = SIdRequestDisplayData(
        caseId = case.id,
        status = toSIdRequestDisplayStatus(),
        firstName = case.firstName,
        lastName = case.lastName,
        onlineSessionStartOpenAt = state?.let {
            runSuspendCatching {
                it.onlineSessionStartOpenAt?.epochSecondsToZonedDateTime()?.asDayFullMonthYear(currentLocale)
            }.onFailure { throwable ->
                Timber.w(throwable, "Could not parse onlineSessionStartOpenAt date")
            }.get()
        },
        onlineSessionStartTimeoutAt = state?.let {
            runSuspendCatching {
                it.onlineSessionStartTimeoutAt?.epochSecondsToZonedDateTime()?.asDayFullMonthYear(currentLocale)
            }.onFailure { throwable ->
                Timber.w(throwable, "Could not parse onlineSessionStartTimeoutAt date")
            }.get()
        },
        createdAt = case.createdAt,
    )
}
