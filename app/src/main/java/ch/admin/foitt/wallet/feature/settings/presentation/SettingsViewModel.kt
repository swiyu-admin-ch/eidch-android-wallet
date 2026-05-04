package ch.admin.foitt.wallet.feature.settings.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.otp.domain.repository.OtpStateCompletionRepository
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val otpStateCompletionRepository: OtpStateCompletionRepository,
    @param:ApplicationContext private val appContext: Context,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.Details(navManager::popBackStack, R.string.tk_settings_title)

    private val _otpBypassValue = MutableStateFlow(false)
    val otpBypassValue = _otpBypassValue.asStateFlow()

    init {
        viewModelScope.launch {
            _otpBypassValue.value = otpStateCompletionRepository.getOtpFlowWasDone()
        }
    }

    fun onChangeOtpBypass() {
        viewModelScope.launch {
            val newValue = !_otpBypassValue.value
            otpStateCompletionRepository.setOtpFlowWasDone(isCompleted = newValue)
            _otpBypassValue.value = newValue
        }
    }

    fun onSecurityAndPrivacy() = navManager.navigateTo(Destination.SecuritySettingsScreen)

    fun onLanguage() = navManager.navigateTo(Destination.LanguageScreen)

    fun onHelp() = appContext.openLink(R.string.tk_settings_general_help_link_value)

    fun onFeedback() = appContext.openLink(R.string.tk_settings_general_feedback_link_value)

    fun onLicenses() = navManager.navigateTo(Destination.LicencesScreen)

    fun onImprint() = navManager.navigateTo(Destination.ImpressumScreen)

    val onDevsViewer = environmentSetupRepository.devsSettingsEnabled

    val onLottieViewer: (() -> Unit)? = if (environmentSetupRepository.isLottieViewerEnabled) {
        { navManager.navigateTo(Destination.LottieViewerScreen) }
    } else {
        null
    }
}
