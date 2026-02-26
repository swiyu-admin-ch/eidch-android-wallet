package ch.admin.foitt.wallet.feature.walletPairing.presentation

import android.os.Build
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetEIdRequestCase
import ch.admin.foitt.wallet.feature.walletPairing.presentation.model.PairingMainWalletUiState
import ch.admin.foitt.wallet.feature.walletPairing.presentation.model.PairingOtherWalletUiState
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetLocalizedDateTime
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.PairCurrentWalletError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.PairCurrentWallet
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.WalletPairingEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.WalletPairingEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.epochSecondsToZonedDateTime
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    override val topBarState = TopBarState.DetailsWithCloseButton(
        titleId = null,
        onUp = navManager::popBackStack,
        onClose = {
            navManager.navigateBackToHomeScreen(Destination.EIdIntroScreen::class)
        }
    )

    private val deviceModel = Build.MODEL
    private val deviceManufacturer = Build.MANUFACTURER
    val deviceName = "$deviceManufacturer $deviceModel"
    private val fetchSIdStatusResult = MutableStateFlow<Result<StateResponse, StateRequestError>?>(null)
    private val limitReached = MutableStateFlow(false)
    private val _numberOfDevices = MutableStateFlow(0)
    val numberOfDevices = _numberOfDevices.asStateFlow()
    private val _isToastVisible = MutableStateFlow(false)
    val isToastVisible = _isToastVisible.asStateFlow()
    private val isWaitingForDevicePairing = MutableStateFlow(false)
    private val pairCurrentWalletResult = MutableStateFlow<Result<Unit, PairCurrentWalletError>?>(null)
    private val _dateAdded = MutableStateFlow<String?>(null)
    val dateAdded = _dateAdded.asStateFlow()
    private val currentWalletAdded = MutableStateFlow<Int>(0)

    val otherWalletUiState: StateFlow<PairingOtherWalletUiState> = combine(
        limitReached,
        fetchSIdStatusResult,
    ) { isLimitReached, fetchSIdStatusResult ->
        when {
            isLimitReached -> PairingOtherWalletUiState.LimitReached
            else -> PairingOtherWalletUiState.Open
        }
    }.toStateFlow(PairingOtherWalletUiState.Open)

    val mainWalletUiState: StateFlow<PairingMainWalletUiState> = combine(
        pairCurrentWalletResult,
        isWaitingForDevicePairing,
    ) { pairCurrentWallet, isWaitingForDevicePairing ->
        when {
            isWaitingForDevicePairing -> PairingMainWalletUiState.SyncMainWallet
            pairCurrentWallet == null || pairCurrentWallet.isErr -> PairingMainWalletUiState.Initial
            else -> PairingMainWalletUiState.MainWalletAdded
        }
    }.toStateFlow(PairingMainWalletUiState.Initial)

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
            fetchSIdStatusResult.update { fetchSIdStatus(caseId) }
            verifyCurrentWalletAdded()
            fetchSIdStatusResult.value?.onSuccess {
                val limit = it.targetWallets?.limitReached ?: false
                limitReached.update { limit }
                _numberOfDevices.value = (it.targetWallets?.pairedWallets?.size?.minus(currentWalletAdded.value)) ?: 0
            }
        }
    }

    fun onThisDeviceClick() {
        viewModelScope.launch {
            val result = pairCurrentWallet(caseId = caseId)
            pairCurrentWalletResult.update { result }

            result.onSuccess {
                verifyCurrentWalletAdded()
            }.onFailure {
                Timber.d("Cannot pair current wallet")
            }
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

    suspend fun verifyCurrentWalletAdded() {
        getEIdRequestCase(caseId).onSuccess { eIdCase ->
            if (eIdCase.credentialId != null) {
                val dateTime = getLocalizedDateTime(eIdCase.createdAt.epochSecondsToZonedDateTime())
                _dateAdded.value = dateTime
                currentWalletAdded.update { 1 }
                pairCurrentWalletResult.update { Ok(Unit) }
            }
        }
    }
}
