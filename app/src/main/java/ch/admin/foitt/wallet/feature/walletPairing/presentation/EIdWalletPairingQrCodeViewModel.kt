package ch.admin.foitt.wallet.feature.walletPairing.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.QrBoxUiState
import ch.admin.foitt.wallet.feature.walletPairing.presentation.model.WalletPairingQrCodeUiState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.PairWalletError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.PairWalletResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.WalletPairingState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.WalletPairingStateError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.WalletPairingStateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.PairWallet
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.WalletPairingStatus
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.WalletPairingEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.WalletPairingEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.generateQRBitmap
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = EIdWalletPairingQrCodeViewModel.Factory::class)
internal class EIdWalletPairingQrCodeViewModel @AssistedInject constructor(
    private val pairWallet: PairWallet,
    private val walletPairingStatus: WalletPairingStatus,
    private val navManager: NavigationManager,
    private val walletPairingEventRepository: WalletPairingEventRepository,
    setTopBarState: SetTopBarState,
    @Assisted private val caseId: String
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdWalletPairingQrCodeViewModel
    }

    override val topBarState = TopBarState.Details(
        titleId = null,
        onUp = ::onBack,
    )

    private val isRequestLoading = MutableStateFlow<Boolean>(false)
    private var pollingJob: Job? = null

    private val walletPairingStatusResponse =
        MutableStateFlow<Result<WalletPairingStateResponse, WalletPairingStateError>?>(null)
    private val pairWalletResponse =
        MutableStateFlow<Result<PairWalletResponse, PairWalletError>?>(null)

    val qrBoxState: StateFlow<QrBoxUiState> = pairWalletResponse.map { response ->
        val responseValue = response?.get()
        when {
            response == null -> QrBoxUiState.Loading
            responseValue is PairWalletResponse -> {
                QrBoxUiState.Success(
                    qrBitmap = responseValue.credentialOfferLink.generateQRBitmap()
                )
            }
            else -> QrBoxUiState.Failure
        }
    }.toStateFlow(QrBoxUiState.Loading)

    val screenUiState: StateFlow<WalletPairingQrCodeUiState> = combine(
        pairWalletResponse,
        walletPairingStatusResponse,
    ) { pairWalletResponse, walletPairingStatusResponse ->
        Timber.d(message = "new screenUiState: $pairWalletResponse, $walletPairingStatusResponse, $qrBoxState")
        val pairWalletResponseValue = pairWalletResponse?.get()
        val pairWalletResponseError = pairWalletResponse?.getError()
        val walletPairingStatusResponseValue = walletPairingStatusResponse?.get()
        val walletPairingStatusResponseError = walletPairingStatusResponse?.getError()

        when {
            pairWalletResponse == null -> WalletPairingQrCodeUiState.LoadingInvitation
            pairWalletResponseError != null -> WalletPairingQrCodeUiState.LoadingInvitationError
            pairWalletResponseValue != null && walletPairingStatusResponse == null -> startPolling(pairWalletResponseValue)
            walletPairingStatusResponseValue != null -> handleWalletPairingState(walletPairingStatusResponseValue.state)
            walletPairingStatusResponseError != null -> handleWalletPairingStatusError(walletPairingStatusResponseError)
            else -> WalletPairingQrCodeUiState.UnexpectedError
        }
    }.toStateFlow(WalletPairingQrCodeUiState.LoadingInvitation)

    fun onResume() {
        if (isRequestLoading.value) return

        viewModelScope.launch {
            if (
                pairWalletResponse.value?.get() !is PairWalletResponse ||
                walletPairingStatusResponse.value?.getError() != null
            ) {
                refreshRequest()
            }
        }.trackCompletion(isRequestLoading)
    }

    fun onPause() {
        stopPolling()
    }

    private fun startPolling(walletResponse: PairWalletResponse): WalletPairingQrCodeUiState {
        if (pollingJob?.isActive == true) return WalletPairingQrCodeUiState.Polling

        pollingJob = viewModelScope.launch {
            walletPairingStatusResponse.update { null }

            while (shouldPoll()) {
                walletPairingStatusResponse.update {
                    walletPairingStatus(caseId, walletResponse.walletPairingId)
                }
                delay(5000L)
            }
        }

        return WalletPairingQrCodeUiState.Polling
    }

    private fun shouldPoll(): Boolean {
        val responseValue = walletPairingStatusResponse.value?.get()
        val responseError = walletPairingStatusResponse.value?.getError()
        return (responseValue == null || responseValue.state == WalletPairingState.OPEN) &&
            responseError == null
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun handleWalletPairingState(state: WalletPairingState): WalletPairingQrCodeUiState = when (state) {
        WalletPairingState.OPEN -> WalletPairingQrCodeUiState.Polling
        WalletPairingState.ACCEPTED -> {
            walletPairingEventRepository.setEvent(WalletPairingEvent.ADDED)
            onBack()
            WalletPairingQrCodeUiState.Done
        }
        WalletPairingState.REJECTED -> {
            walletPairingEventRepository.setEvent(WalletPairingEvent.NONE)
            WalletPairingQrCodeUiState.PairingFailure
        }
    }

    private fun handleWalletPairingStatusError(error: WalletPairingStateError): WalletPairingQrCodeUiState = when (error) {
        is EIdRequestError.NetworkError -> WalletPairingQrCodeUiState.NetworkError
        is EIdRequestError.InvalidClientAttestation,
        is EIdRequestError.Unexpected -> WalletPairingQrCodeUiState.UnexpectedError
    }

    private suspend fun refreshRequest() {
        pairWalletResponse.update { null }
        walletPairingStatusResponse.update { null }
        pairWalletResponse.update {
            pairWallet(caseId)
        }
    }

    fun onBack() {
        stopPolling()
        navManager.popBackStackOrToRoot()
    }
}
