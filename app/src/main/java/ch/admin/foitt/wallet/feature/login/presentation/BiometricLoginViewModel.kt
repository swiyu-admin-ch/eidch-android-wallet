package ch.admin.foitt.wallet.feature.login.presentation

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricPromptType
import ch.admin.foitt.wallet.platform.biometricPrompt.presentation.AndroidBiometricPrompt
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.ResetBiometrics
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.HandleDeeplink
import ch.admin.foitt.wallet.platform.login.domain.model.CanUseBiometricsForLoginResult
import ch.admin.foitt.wallet.platform.login.domain.model.LoginError
import ch.admin.foitt.wallet.platform.login.domain.usecase.CanUseBiometricsForLogin
import ch.admin.foitt.wallet.platform.login.domain.usecase.LoginWithBiometrics
import ch.admin.foitt.wallet.platform.login.domain.usecase.ResetLockout
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.AppVersionInfo
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.FetchAppVersionInfo
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BiometricLoginViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val canUseBiometricsForLogin: CanUseBiometricsForLogin,
    private val loginWithBiometrics: LoginWithBiometrics,
    private val resetBiometrics: ResetBiometrics,
    private val resetLockout: ResetLockout,
    private val fetchAppVersionInfo: FetchAppVersionInfo,
    private val handleDeeplink: HandleDeeplink,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState, systemBarsFixedLightColor = true) {
    override val topBarState = TopBarState.None

    private var _showBiometricLoginButton = MutableStateFlow(false)
    val showBiometricLoginButton = _showBiometricLoginButton.asStateFlow()

    private var appVersionInfo = flow {
        emit(fetchAppVersionInfo())
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        AppVersionInfo.Unknown,
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            _showBiometricLoginButton.value = canUseBiometricsForLogin().let { result ->
                if (result != CanUseBiometricsForLoginResult.Usable) {
                    Timber.w("Biometrics cannot be used for login: $result")
                    resetBiometrics()
                    navigateToLoginWithPassphrase()
                    false
                } else {
                    true
                }
            }
        }
    }

    fun tryLoginWithBiometric(activity: FragmentActivity) {
        viewModelScope.launch {
            canUseBiometricsForLogin().let { result ->
                if (result != CanUseBiometricsForLoginResult.Usable) {
                    Timber.w("Biometrics cannot be used for login: $result")
                    resetBiometrics()
                    navigateToLoginWithPassphrase()
                    return@launch
                }
            }

            val biometricPromptWrapper = AndroidBiometricPrompt(
                activity = activity,
                promptType = BiometricPromptType.Login,
            )

            loginWithBiometrics(biometricPromptWrapper)
                .onSuccess {
                    resetLockout()
                    val info = appVersionInfo.value // if it is available now, perfect, otherwise it acts like a time-out
                    if (info is AppVersionInfo.Blocked) {
                        navigateToAppVersionBlocked(info.title, info.text)
                    } else {
                        handleDeeplink(fromOnboarding = false).navigate()
                    }
                }.onFailure { loginError ->
                    when (loginError) {
                        LoginError.Cancelled -> {}
                        LoginError.BiometricsLocked -> navigateToLoginWithPassphrase(biometricsLocked = true)
                        else -> navigateToLoginWithPassphrase()
                    }
                }
        }.trackCompletion(_isLoading)
    }

    fun navigateToLoginWithPassphrase(
        biometricsLocked: Boolean = false
    ) = navManager.replaceCurrentWith(destination = Destination.PassphraseLoginScreen(biometricsLocked = biometricsLocked))

    private fun navigateToAppVersionBlocked(title: String?, text: String?) {
        navManager.replaceCurrentWith(Destination.AppVersionBlockedScreen(title = title, text = text))
    }
}
