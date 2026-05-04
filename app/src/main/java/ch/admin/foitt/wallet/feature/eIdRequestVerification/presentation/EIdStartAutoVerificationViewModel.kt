package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.StartAutoVerificationUiState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AutoVerificationResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StartAutoVerificationError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetStartAutoVerificationResult
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.StartAutoVerification
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.unwrapError
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EIdStartAutoVerificationViewModel.Factory::class)
internal class EIdStartAutoVerificationViewModel @AssistedInject constructor(
    private val startAutoVerification: StartAutoVerification,
    private val navManager: NavigationManager,
    private val setStartAutoVerificationResult: SetStartAutoVerificationResult,
    @Assisted private val caseId: String,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdStartAutoVerificationViewModel
    }

    override val topBarState = TopBarState.Empty

    private val isLoading = MutableStateFlow(false)
    private val startAutoVerificationResult =
        MutableStateFlow<Result<AutoVerificationResponse, StartAutoVerificationError>?>(null)

    @OptIn(UnsafeResultValueAccess::class)
    val state: StateFlow<StartAutoVerificationUiState> = combine(
        isLoading,
        startAutoVerificationResult,
    ) { isLoading, startAutoVerificationResult ->
        when {
            isLoading -> StartAutoVerificationUiState.Loading
            startAutoVerificationResult == null -> StartAutoVerificationUiState.Info(
                onStart = ::onStart
            )

            startAutoVerificationResult.isOk -> StartAutoVerificationUiState.Started(
                onContinue = { onContinue(startAutoVerificationResult.value) }
            )

            startAutoVerificationResult.unwrapError() is EIdRequestError.NetworkError ->
                StartAutoVerificationUiState.NetworkError(
                    onClose = ::onClose,
                    onRetry = ::onRetry
                )

            else -> StartAutoVerificationUiState.Unexpected(
                onClose = ::onClose,
                onRetry = ::onRetry,
            )
        }
    }.toStateFlow(StartAutoVerificationUiState.Loading)

    private suspend fun onStartAv() {
        startAutoVerification(caseId = caseId)
            .also { result ->
                startAutoVerificationResult.value = result
            }
            .onSuccess {
                setStartAutoVerificationResult(startAutoVerificationResult = it)
                onContinue(autoVerificationResponse = it)
            }
    }

    private fun onStart() {
        if (isLoading.value) {
            return
        }
        viewModelScope.launch {
            onStartAv()
        }.trackCompletion(isLoading)
    }

    private fun onClose() = navManager.popBackStackTo(Destination.HomeScreen::class, false)

    private fun onRetry() = onStart()

    private fun onContinue(
        autoVerificationResponse: AutoVerificationResponse,
    ) = handleAutoVerificationResponse(
        useNfc = autoVerificationResponse.useNfc,
        recordDocumentVideo = autoVerificationResponse.recordDocumentVideo,
        scanDocument = autoVerificationResponse.scanDocument,
    ).let { navManager.navigateTo(it) }

    private fun handleAutoVerificationResponse(
        useNfc: Boolean,
        recordDocumentVideo: Boolean,
        scanDocument: Boolean,
    ): Destination = when {
        useNfc -> Destination.EIdNfcScannerScreen(caseId = caseId)
        scanDocument -> Destination.EIdDocumentScannerInfoScreen(caseId = caseId)
        recordDocumentVideo -> Destination.EIdDocumentRecordingInfoScreen(caseId = caseId)
        // At this point, default is to do a face scan
        else -> Destination.EIdStartSelfieVideoScreen(caseId = caseId)
    }
}
