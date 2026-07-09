package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.EIdPushNotificationUiState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.RegisterEIdPushNotification
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.LegalRepresentant
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdStatus
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarAction
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EIdPushNotificationViewModel.Factory::class)
class EIdPushNotificationViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    private val registerEIdPushNotification: RegisterEIdPushNotification,
    private val fetchSIdStatus: FetchSIdStatus,
    @Assisted private val caseId: String,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdPushNotificationViewModel
    }

    override val topBarState: TopBarState
        get() {
            val state = uiState.value
            if (state == EIdPushNotificationUiState.Loading) {
                return TopBarState.Empty
            }

            return TopBarState.Custom(
                actions = listOf(
                    TopBarAction.Close(onClose = ::onClose)
                )
            )
        }

    private val _uiState = MutableStateFlow<EIdPushNotificationUiState>(EIdPushNotificationUiState.Initial)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            uiState.collectLatest {
                setTopBarState(topBarState)
            }
        }
    }

    fun onClose() {
        if (_uiState.value != EIdPushNotificationUiState.Loading) {
            navManager.navigateBackToHomeScreen(Destination.EIdGuardianshipScreen::class)
        }
    }

    fun handlePermissionResult(granted: Boolean) {
        if (granted) {
            setupPushNotification()
        } else {
            _uiState.update { EIdPushNotificationUiState.SettingsRationale }
        }
    }

    fun skipPushNotification() {
        _uiState.update { EIdPushNotificationUiState.Loading }
        finish()
    }

    private fun setupPushNotification() {
        if (_uiState.value == EIdPushNotificationUiState.Loading) return
        _uiState.update { EIdPushNotificationUiState.Loading }
        viewModelScope.launch {
            registerEIdPushNotification(caseId)
                .onFailure { error() }
                .onSuccess {
                    finish()
                }
        }
    }

    private fun finish() {
        viewModelScope.launch {
            fetchSIdStatus(caseId)
                .onFailure { error() }
                .onSuccess { stateResponse ->
                    if (isLegalCaseNeeded(stateResponse.legalRepresentant)) {
                        navigateToNextScreen(Destination.EIdGuardianSelectionScreen(caseId = caseId))
                    } else if (stateResponse.state == EIdRequestQueueState.READY_FOR_ONLINE_SESSION) {
                        navigateToNextScreen(Destination.EIdWalletPairingScreen(caseId = caseId))
                    } else {
                        navigateToNextScreen(
                            Destination.EIdQueueScreen(
                                rawDeadline = stateResponse.queueInformation?.expectedOnlineSessionStart,
                            )
                        )
                    }
                }
        }
    }

    private fun isLegalCaseNeeded(legalRepresentant: LegalRepresentant?): Boolean = when {
        legalRepresentant != null && legalRepresentant.verified.not() -> true
        else -> false
    }

    private fun navigateToNextScreen(direction: Destination) = navManager.replaceCurrentWith(destination = direction)

    private fun error() {
        _uiState.update {
            EIdPushNotificationUiState.Error(
                onRetry = ::setupPushNotification,
                onSkip = ::skipPushNotification
            )
        }
    }
}
