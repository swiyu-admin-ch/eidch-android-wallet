package ch.admin.foitt.wallet.feature.walletPairing.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.walletPairing.presentation.model.WalletPairingUiState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.PairCurrentWalletError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StartOnlineSessionError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.PairCurrentWallet
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.StartOnlineSession
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.DestinationGroup
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.annotation.UnsafeResultErrorAccess
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EIdWalletPairingViewModel.Factory::class)
internal class EIdWalletPairingViewModel @AssistedInject constructor(
    private val startOnlineSession: StartOnlineSession,
    private val pairCurrentWallet: PairCurrentWallet,
    private val navManager: NavigationManager,
    @Assisted private val caseId: String,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdWalletPairingViewModel
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    override val topBarState = TopBarState.DetailsWithCloseButton(
        onUp = navManager::popBackStackOrToRoot,
        onClose = { navManager.navigateBackToHomeScreen(popUntil = Destination.EIdStartAvSessionScreen::class) },
        titleId = null
    )

    private val startOnlineSessionResult = MutableStateFlow<Result<Unit, StartOnlineSessionError>?>(null)
    private val pairCurrentWalletResult = MutableStateFlow<Result<Unit, PairCurrentWalletError>?>(null)

    @OptIn(UnsafeResultErrorAccess::class)
    val uiState: StateFlow<WalletPairingUiState> = combine(
        isLoading,
        startOnlineSessionResult,
        pairCurrentWalletResult,
    ) { isLoading, startOnlineSessionResult, pairCurrentWalletResult ->
        when {
            isLoading -> WalletPairingUiState.Initial
            startOnlineSessionResult?.getError() != null -> handleOnlineSessionError(startOnlineSessionResult.error)
            pairCurrentWalletResult?.getError() != null -> handlePairWalletError(pairCurrentWalletResult.error)
            else -> WalletPairingUiState.Initial
        }
    }.toStateFlow(WalletPairingUiState.Initial)

    fun onSingleDeviceFlow() {
        viewModelScope.launch {
            // Start online session can only succeed once per caseId, with a backend session timeout.
            // so we only call it once.
            startOnlineSessionResult.update { result ->
                if (result == null || result.isErr) {
                    startOnlineSession(caseId = caseId)
                } else {
                    result
                }
            }
            startOnlineSessionResult.value?.get() ?: return@launch

            pairCurrentWalletResult.update { pairCurrentWallet(caseId = caseId) }
            pairCurrentWalletResult.value?.get() ?: return@launch

            navManager.replaceCurrentWith(
                Destination.EIdStartAutoVerificationScreen(
                    caseId = caseId
                )
            )
        }.trackCompletion(_isLoading)
    }

    fun onMultiDeviceFlow() {
        viewModelScope.launch {
            startOnlineSessionResult.update { result ->
                if (result == null || result.isErr) {
                    startOnlineSession(caseId = caseId)
                } else {
                    result
                }
            }
            startOnlineSessionResult.value?.get() ?: return@launch
            navManager.popUpToAndNavigate(
                popToInclusive = Destination.EIdStartAvSessionScreen::class,
                destination = Destination.EIdPairingOverviewScreen(caseId = caseId)
            )
        }.trackCompletion(_isLoading)
    }

    fun onCloseError() {
        _isLoading.update { false }
        startOnlineSessionResult.update { result ->
            if (result?.isOk != true) {
                null
            } else {
                result
            }
        }
        pairCurrentWalletResult.update { result ->
            if (result?.isOk != true) {
                null
            } else {
                result
            }
        }
    }

    private fun handleOnlineSessionError(error: StartOnlineSessionError): WalletPairingUiState = when (error) {
        EIdRequestError.NetworkError -> WalletPairingUiState.NetworkError
        EIdRequestError.InvalidClientAttestation,
        is EIdRequestError.Unexpected -> WalletPairingUiState.Unexpected
    }

    private fun handlePairWalletError(error: PairCurrentWalletError): WalletPairingUiState = when (error) {
        EIdRequestError.NetworkError -> WalletPairingUiState.NetworkError
        EIdRequestError.InvalidDeferredCredentialOffer,
        EIdRequestError.InvalidClientAttestation,
        is EIdRequestError.Unexpected -> WalletPairingUiState.Unexpected
        EIdRequestError.RequestInWrongState -> {
            navManager.navigateOutAndTo(DestinationGroup.EIdRequestVerification::class, Destination.EIdWalletPairingTimeoutScreen)
            WalletPairingUiState.Initial
        }
    }
}
