package ch.admin.foitt.wallet.feature.walletPairing.presentation

import android.os.Build
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.GetEIdRequestCaseError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetEIdRequestCase
import ch.admin.foitt.wallet.feature.walletPairing.presentation.model.PairingMainWalletUiState
import ch.admin.foitt.wallet.feature.walletPairing.presentation.model.PairingOtherWalletUiState
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetLocalizedDateTime
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.PairCurrentWalletError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.PairCurrentWallet
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.WalletPairingEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.WalletPairingEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.DestinationGroup
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.epochSecondsToZonedDateTime
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapOr
import com.github.michaelbull.result.onFailure
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = EIdPairingOverviewViewModel.Factory::class)
internal class EIdPairingOverviewViewModel @AssistedInject constructor(
    private val fetchSIdStatus: FetchSIdStatus,
    private val navManager: NavigationManager,
    private val walletPairingEventRepository: WalletPairingEventRepository,
    private val pairCurrentWallet: PairCurrentWallet,
    private val getLocalizedDateTime: GetLocalizedDateTime,
    private val getEIdRequestCase: GetEIdRequestCase,
    setTopBarState: SetTopBarState,
    @Assisted private val caseId: String
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdPairingOverviewViewModel
    }

    override val topBarState = TopBarState.Details(
        titleId = null,
        onUp = {
            navManager.navigateBackToHomeScreen(Destination.EIdIntroScreen::class)
        },
    )

    private val deviceModel = Build.MODEL
    private val deviceManufacturer = Build.MANUFACTURER
    val deviceName = "$deviceManufacturer $deviceModel"
    private val fetchSIdStatusResult = MutableStateFlow<Result<StateResponse, StateRequestError>?>(null)
    private val getEIdRequestCaseResult = MutableStateFlow<Result<EIdRequestCase, GetEIdRequestCaseError>?>(null)
    private val _isToastVisible = MutableStateFlow(false)
    val isToastVisible = _isToastVisible.asStateFlow()
    private val isWaitingForDevicePairing = MutableStateFlow(false)
    private val pairCurrentWalletResult = MutableStateFlow<Result<Unit, PairCurrentWalletError>?>(null)
    private val _dateAdded = MutableStateFlow<String?>(null)
    val dateAdded = _dateAdded.asStateFlow()

    val mainWalletUiState: StateFlow<PairingMainWalletUiState> = combine(
        isWaitingForDevicePairing,
        getEIdRequestCaseResult
    ) { isWaitingForDevicePairing, getEIdRequestCaseResult ->
        val mainDeviceRequestValue = getEIdRequestCaseResult?.get()
        when {
            isWaitingForDevicePairing -> PairingMainWalletUiState.SyncMainWallet
            mainDeviceRequestValue != null && mainDeviceRequestValue.credentialId != null -> {
                val dateTime = getLocalizedDateTime(mainDeviceRequestValue.createdAt.epochSecondsToZonedDateTime())
                _dateAdded.value = dateTime
                PairingMainWalletUiState.MainWalletAdded
            }
            else -> PairingMainWalletUiState.Initial
        }
    }.toStateFlow(PairingMainWalletUiState.Initial)

    val otherWalletUiState: StateFlow<PairingOtherWalletUiState> = fetchSIdStatusResult.map { fetchSIdStatusResult ->
        val fetchSIdStatusValue = fetchSIdStatusResult?.get()
        when {
            fetchSIdStatusValue == null -> PairingOtherWalletUiState.Open
            fetchSIdStatusValue.state != EIdRequestQueueState.IN_TARGET_WALLET_PAIRING -> {
                handlePairingWindowTimeout()
                PairingOtherWalletUiState.Open
            }
            fetchSIdStatusValue.targetWallets?.limitReached == true -> PairingOtherWalletUiState.LimitReached
            else -> PairingOtherWalletUiState.Open
        }
    }.toStateFlow(PairingOtherWalletUiState.Open)

    val numberOfDevices: StateFlow<Int> = combine(
        fetchSIdStatusResult,
        mainWalletUiState,
    ) { fetchSIdStatusResult, mainWalletUiState ->
        fetchSIdStatusResult?.mapOr(0) {
            it.targetWallets?.pairedWallets?.size?.let { deviceNumber ->
                if (mainWalletUiState == PairingMainWalletUiState.MainWalletAdded) {
                    deviceNumber - 1
                } else {
                    deviceNumber
                }
            } ?: 0
        } ?: 0
    }.toStateFlow(0)

    init {
        viewModelScope.launch {
            walletPairingEventRepository.event.collect { event ->
                when (event) {
                    WalletPairingEvent.NONE -> _isToastVisible.value = false
                    WalletPairingEvent.ADDED -> {
                        _isToastVisible.value = true
                        delay(4000L)
                        walletPairingEventRepository.resetEvent()
                    }
                }
            }
        }
    }

    fun onResume() {
        viewModelScope.launch {
            refreshRequestStatus()
        }
    }

    fun onThisDeviceClick() {
        viewModelScope.launch {
            val pairingResult = pairCurrentWallet(caseId = caseId)
                .onFailure { error ->
                    when (error) {
                        is EIdRequestError.InvalidClientAttestation,
                        is EIdRequestError.InvalidDeferredCredentialOffer,
                        is EIdRequestError.Unexpected,
                        is EIdRequestError.NetworkError -> {
                            Timber.e("Pairing Overview: Cannot pair current wallet, $error")
                        }
                        is EIdRequestError.RequestInWrongState -> handlePairingWindowTimeout()
                    }
                }
            pairCurrentWalletResult.update { pairingResult }
            refreshRequestStatus()
        }.trackCompletion(isWaitingForDevicePairing)
    }

    fun onAdditionalDevicesClick() {
        navManager.navigateTo(Destination.EIdWalletPairingQrCodeScreen(caseId = caseId))
    }

    fun onContinue() {
        navManager.navigateTo(
            Destination.EIdStartAutoVerificationScreen(
                caseId = caseId
            )
        )
    }

    private suspend fun refreshRequestStatus() {
        fetchSIdStatusResult.update { fetchSIdStatus(caseId) }
        getEIdRequestCaseResult.update { getEIdRequestCase(caseId) }
    }

    private fun handlePairingWindowTimeout() = navManager.navigateOutAndTo(
        destinationGroup = DestinationGroup.EIdRequestVerification::class,
        destination = Destination.EIdWalletPairingTimeoutScreen,
    )
}
