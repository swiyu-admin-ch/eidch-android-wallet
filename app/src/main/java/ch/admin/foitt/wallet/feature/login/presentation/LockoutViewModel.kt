package ch.admin.foitt.wallet.feature.login.presentation

import android.content.Context
import android.os.CountDownTimer
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricPromptType
import ch.admin.foitt.wallet.platform.biometricPrompt.presentation.AndroidBiometricPrompt
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.ResetBiometrics
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.HandleDeeplink
import ch.admin.foitt.wallet.platform.login.domain.model.CanUseBiometricsForLoginResult
import ch.admin.foitt.wallet.platform.login.domain.usecase.CanUseBiometricsForLogin
import ch.admin.foitt.wallet.platform.login.domain.usecase.GetLockoutDuration
import ch.admin.foitt.wallet.platform.login.domain.usecase.LoginWithBiometrics
import ch.admin.foitt.wallet.platform.login.domain.usecase.ResetLockout
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.AppVersionInfo
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.FetchAppVersionInfo
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class LockoutViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    private val getLockoutDuration: GetLockoutDuration,
    private val resetLockout: ResetLockout,
    private val canUseBiometricsForLogin: CanUseBiometricsForLogin,
    private val resetBiometrics: ResetBiometrics,
    private val loginWithBiometrics: LoginWithBiometrics,
    private val fetchAppVersionInfo: FetchAppVersionInfo,
    private val handleDeeplink: HandleDeeplink,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState, systemBarsFixedLightColor = true) {
    override val topBarState = TopBarState.None

    private var _countdown = MutableStateFlow(Pair(4L, TimeUnit.MINUTES))
    val countdown = _countdown.asStateFlow()

    private var _showBiometricLoginButton = MutableStateFlow(false)
    val showBiometricLoginButton = _showBiometricLoginButton.asStateFlow()

    private var appVersionInfo = flow {
        emit(fetchAppVersionInfo())
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        AppVersionInfo.Unknown,
    )

    private var _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun canUseBiometrics() = viewModelScope.launch {
        _showBiometricLoginButton.value = canUseBiometricsForLogin() == CanUseBiometricsForLoginResult.Usable
    }

    fun checkLockoutDuration() {
        val lockoutDuration = getLockoutDuration()
        if (lockoutDuration > Duration.ZERO) {
            startCountdown(timeout = lockoutDuration)
        } else {
            navigateToPassphraseLoginScreen()
        }
    }

    private fun startCountdown(timeout: Duration) {
        val timer = object : CountDownTimer(timeout.toMillis(), 100) {
            override fun onTick(millisUntilFinished: Long) {
                val duration = Duration.ofMillis(millisUntilFinished)

                _countdown.value = when {
                    duration.toMinutes() >= 1 -> Pair(duration.toMinutes(), TimeUnit.MINUTES)
                    else -> Pair(duration.seconds, TimeUnit.SECONDS)
                }
            }

            override fun onFinish() {
                resetLockout()
                _countdown.value = Pair(0, TimeUnit.MINUTES)
                navigateToPassphraseLoginScreen()
            }
        }
        timer.start()
    }

    fun tryLoginWithBiometrics(activity: FragmentActivity) = viewModelScope.launch {
        canUseBiometricsForLogin().let { result ->
            if (result != CanUseBiometricsForLoginResult.Usable) {
                Timber.w("Biometrics cannot be used for login: $result")
                resetBiometrics()
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
            }
    }.trackCompletion(_isLoading)

    private fun navigateToPassphraseLoginScreen() = navManager.replaceCurrentWith(
        Destination.PassphraseLoginScreen(biometricsLocked = false)
    )

    private fun navigateToAppVersionBlocked(title: String?, text: String?) = navManager.replaceCurrentWith(
        destination = Destination.AppVersionBlockedScreen(title = title, text = text)
    )

    fun onPassphraseForgotten() = appContext.openLink(R.string.tk_login_locked_secondarybutton_value)

    enum class TimeUnit {
        SECONDS, MINUTES
    }
}
