package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.avwrapper.filesWithExtractDataList
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.SaveEIdRequestCaseError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.SaveEIdRequestStateError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.FetchSIdCase
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestCase
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestFiles
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.MrzSubmissionUiState
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFileCategory
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.ApplyRequest
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.ApplyRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.CaseResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.LegalRepresentant
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toLegalRepresentativeConsent
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetDocumentScanResult
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetHasLegalGuardian
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import com.github.michaelbull.result.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant

@HiltViewModel(assistedFactory = MrzSubmissionViewModel.Factory::class)
internal class MrzSubmissionViewModel @AssistedInject constructor(
    private val fetchSIdCase: FetchSIdCase,
    private val fetchSIdStatus: FetchSIdStatus,
    private val saveEIdRequestCase: SaveEIdRequestCase,
    private val saveEIdRequestState: SaveEIdRequestState,
    private val saveEIdRequestFiles: SaveEIdRequestFiles,
    private val getHasLegalGuardian: GetHasLegalGuardian,
    private val getDocumentScanResult: GetDocumentScanResult,
    private val navManager: NavigationManager,
    @Assisted private val mrzLines: List<String>,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(mrzLines: List<String>): MrzSubmissionViewModel
    }

    override val topBarState = TopBarState.Empty

    private val isLoading = MutableStateFlow(false)
    private val applyResult = MutableStateFlow<Result<Unit, ApplyRequestError>?>(null)
    private val fetchStatusResult = MutableStateFlow<Result<StateResponse, StateRequestError>?>(null)
    private val saveCaseResult = MutableStateFlow<Result<Unit, SaveEIdRequestCaseError>?>(null)
    private val saveStateResult = MutableStateFlow<Result<Long, SaveEIdRequestStateError>?>(null)

    val mrzState: StateFlow<MrzSubmissionUiState> = combine(
        isLoading,
        applyResult,
        fetchStatusResult,
        saveCaseResult,
    ) { isLoading, applyResult, fetchStatusResult, saveCaseResult ->
        when {
            isLoading -> MrzSubmissionUiState.Loading
            saveCaseResult?.isOk == true -> MrzSubmissionUiState.Valid
            applyResult?.getError() is EIdRequestError.NetworkError -> MrzSubmissionUiState.NetworkError(
                onClose = ::onClose,
                onRetry = ::onRetry
            )

            fetchStatusResult?.getError() is EIdRequestError.NetworkError -> MrzSubmissionUiState.NetworkError(
                onClose = ::onClose,
                onRetry = ::onRetry
            )

            else -> MrzSubmissionUiState.Unexpected(
                onClose = ::onClose,
                onRetry = ::onRetry,
            )
        }
    }.toStateFlow(MrzSubmissionUiState.Loading)

    private fun onRefreshState() {
        if (isLoading.value) {
            return
        }
        viewModelScope.launch {
            fetchSIdCase().onSuccess { sIdCase ->
                checkStatus(caseResponse = sIdCase, mrzLines = mrzLines)
            }
        }.trackCompletion(isLoading)
    }

    private suspend fun fetchSIdCase() = ApplyRequest(
        mrz = mrzLines,
        legalRepresentant = getHasLegalGuardian().value,
    ).let {
        fetchSIdCase(applyRequest = it)
    }

    private suspend fun checkStatus(caseResponse: CaseResponse, mrzLines: List<String>) {
        fetchStatusResult.value = fetchSIdStatus(caseId = caseResponse.caseId)
            .onSuccess { stateResponse ->
                val rawMrz = mrzLines.joinToString(";")
                saveData(
                    rawMrz,
                    caseResponse,
                    stateResponse
                )
                getDocumentScanResult().value?.let {
                    saveEIdRequestFiles(
                        sIdCaseId = caseResponse.caseId,
                        filesDataList = it.filesWithExtractDataList,
                        filesCategory = EIdRequestFileCategory.DOCUMENT_SCAN,
                    ).onSuccess {
                        Timber.d("Files saved successfully")
                    }
                } ?: Timber.d("No document scan result found")

                if (isLegalCaseNeeded(stateResponse.legalRepresentant)) {
                    navigateToNextScreen(Destination.EIdGuardianSelectionScreen(caseId = caseResponse.caseId))
                } else if (stateResponse.state == EIdRequestQueueState.READY_FOR_ONLINE_SESSION) {
                    navigateToNextScreen(Destination.EIdWalletPairingScreen(caseId = caseResponse.caseId))
                } else {
                    navigateToNextScreen(
                        Destination.EIdQueueScreen(
                            rawDeadline = stateResponse.queueInformation?.expectedOnlineSessionStart,
                        )
                    )
                }
            }
    }

    private suspend fun saveData(rawMrz: String, applyResponseBody: CaseResponse, stateResponseBody: StateResponse) {
        val eIdRequestCase = EIdRequestCase(
            id = applyResponseBody.caseId,
            rawMrz = rawMrz,
            documentNumber = applyResponseBody.identityNumber,
            selectedDocumentType = applyResponseBody.identityType,
            firstName = applyResponseBody.givenNames,
            lastName = applyResponseBody.surname,
        )

        val onlineSessionStartOpenAt: Long? = runSuspendCatching {
            Instant.parse(stateResponseBody.queueInformation?.expectedOnlineSessionStart).epochSecond
        }.get()

        val onlineSessionStartTimeoutAt: Long? = runSuspendCatching {
            Instant.parse(stateResponseBody.onlineSessionStartTimeout).epochSecond
        }.get()

        val eIdRequestState = EIdRequestState(
            eIdRequestCaseId = applyResponseBody.caseId,
            state = stateResponseBody.state,
            lastPolled = Instant.now().epochSecond,
            onlineSessionStartOpenAt = onlineSessionStartOpenAt,
            onlineSessionStartTimeoutAt = onlineSessionStartTimeoutAt,
            legalRepresentativeConsent = stateResponseBody.toLegalRepresentativeConsent(),
        )

        saveCaseResult.value = saveEIdRequestCase(eIdRequestCase)
        saveStateResult.value = saveEIdRequestState(eIdRequestState)
    }

    private fun isLegalCaseNeeded(legalRepresentant: LegalRepresentant?): Boolean = when {
        legalRepresentant != null && legalRepresentant.verified.not() -> true
        else -> false
    }

    private fun navigateToNextScreen(direction: Destination) = navManager.popUpToAndNavigate(
        popToInclusive = Destination.EIdGuardianshipScreen::class,
        destination = direction
    )

    init {
        onRefreshState()
    }

    fun onClose() = navManager.navigateBackToHomeScreen(Destination.EIdGuardianshipScreen::class)

    fun onRetry() = onRefreshState()
}
