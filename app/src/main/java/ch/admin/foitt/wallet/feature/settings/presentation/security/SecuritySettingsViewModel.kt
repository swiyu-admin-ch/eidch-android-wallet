package ch.admin.foitt.wallet.feature.settings.presentation.security

import android.content.Context
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.ApplyUserPrivacyPolicy
import ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.IsUserPrivacyPolicyAcceptedFlow
import ch.admin.foitt.wallet.platform.login.domain.model.CanUseBiometricsForLoginResult
import ch.admin.foitt.wallet.platform.login.domain.usecase.CanUseBiometricsForLogin
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.PassphraseChangeEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.PassphraseChangeEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.GetPassphraseWasDeleted
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.SavePassphraseWasDeleted
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    private val navManager: NavigationManager,
    @param:ApplicationContext private val appContext: Context,
    private val canUseBiometricsForLogin: CanUseBiometricsForLogin,
    private val applyUserPrivacyPolicy: ApplyUserPrivacyPolicy,
    isUserPrivacyPolicyAcceptedFlow: IsUserPrivacyPolicyAcceptedFlow,
    private val getPassphraseWasDeleted: GetPassphraseWasDeleted,
    private val savePassphraseWasDeleted: SavePassphraseWasDeleted,
    private val passphraseChangeEventRepository: PassphraseChangeEventRepository,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    override val topBarState = TopBarState.Details(navManager::popBackStack, R.string.tk_settings_securityPrivacy_title)

    val biometricsHardwareIsAvailable: Flow<Boolean> = flow {
        emit(
            canUseBiometricsForLogin() != CanUseBiometricsForLoginResult.NoHardwareAvailable
        )
    }

    val isBiometricsToggleEnabled: Flow<Boolean> = flow {
        emit(
            canUseBiometricsForLogin() == CanUseBiometricsForLoginResult.Usable
        )
    }

    val showPassphraseDeletionMessage: Flow<Boolean> = flow {
        emit(getPassphraseWasDeleted())
        savePassphraseWasDeleted(false)
    }

    val shareAnalysisEnabled = isUserPrivacyPolicyAcceptedFlow()

    private val _isToastVisible = MutableStateFlow(false)
    val isToastVisible = _isToastVisible.asStateFlow()

    init {
        viewModelScope.launch {
            passphraseChangeEventRepository.event.collect { event ->
                when (event) {
                    PassphraseChangeEvent.NONE -> _isToastVisible.value = false
                    PassphraseChangeEvent.CHANGED -> {
                        _isToastVisible.value = true
                        delay(4000L)
                        passphraseChangeEventRepository.resetEvent()
                    }
                }
            }
        }
    }

    fun hidePassphraseChangeSuccessToast() = passphraseChangeEventRepository.resetEvent()

    fun onChangeBiometrics() {
        viewModelScope.launch {
            isBiometricsToggleEnabled.collect { isToggleCurrentStateOn ->
                if (isToggleCurrentStateOn) {
                    toggleBiometricsOff()
                } else {
                    toggleBiometricsOn()
                }
            }
        }
    }

    private fun toggleBiometricsOn() {
        navManager.navigateTo(Destination.AuthWithPassphraseScreen(enableBiometrics = true))
    }

    private fun toggleBiometricsOff() {
        navManager.navigateTo(Destination.AuthWithPassphraseScreen(enableBiometrics = false))
    }

    fun onDataProtection() = appContext.openLink(R.string.tk_settings_securityPrivacy_dataProtection_privacyPolicy_link_value)

    fun onChangePassphrase() = navManager.navigateTo(Destination.EnterCurrentPassphraseScreen)

    fun onShareAnalysisChange(isEnabled: Boolean) {
        applyUserPrivacyPolicy(isEnabled)
    }

    fun onDataAnalysis() = navManager.navigateTo(Destination.DataAnalysisScreen)
}
