package ch.admin.foitt.wallet.feature.onboarding.presentation

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.onboarding.domain.usecase.SaveOnboardingState
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricManagerResult
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricPromptType
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.BiometricsStatus
import ch.admin.foitt.wallet.platform.biometricPrompt.presentation.AndroidBiometricPrompt
import ch.admin.foitt.wallet.platform.biometrics.domain.model.BiometricsError
import ch.admin.foitt.wallet.platform.biometrics.domain.model.EnableBiometricsError
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.InitializeCipherWithBiometrics
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.SaveUseBiometricLogin
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.DestinationGroup
import ch.admin.foitt.wallet.platform.passphrase.domain.model.InitializePassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.InitializePassphrase
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openSecuritySettings
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.crypto.Cipher

@HiltViewModel(assistedFactory = OnboardingRegisterBiometricsViewModel.Factory::class)
class OnboardingRegisterBiometricsViewModel @AssistedInject constructor(
    val initializeCipherWithBiometrics: InitializeCipherWithBiometrics,
    val initializePassphrase: InitializePassphrase,
    val saveUseBiometricLogin: SaveUseBiometricLogin,
    val biometricsStatus: BiometricsStatus,
    val saveOnboardingState: SaveOnboardingState,
    private val navManager: NavigationManager,
    @param:ApplicationContext private val appContext: Context,
    private val setTopBarState: SetTopBarState,
    @Assisted private val passphrase: String,
) : ScreenViewModel(setTopBarState) {
    @AssistedFactory
    interface Factory {
        fun create(passphrase: String): OnboardingRegisterBiometricsViewModel
    }

    override val topBarState = TopBarState.Details(navManager::popBackStack, null)

    private val _initializationInProgress = MutableStateFlow(false)
    val initializationInProgress = _initializationInProgress.asStateFlow()

    private val _screenState =
        MutableStateFlow<OnboardingRegisterBiometricsScreenState>(OnboardingRegisterBiometricsScreenState.Initial)
    val screenState = _screenState.asStateFlow()

    fun refreshScreenState() {
        _screenState.value = getScreenState()
    }

    fun enableBiometrics(activity: FragmentActivity) {
        viewModelScope.launch {
            initializeBiometrics(activity, passphrase)
        }
    }

    fun openSettings() = appContext.openSecuritySettings()

    fun declineBiometrics() {
        viewModelScope.launch {
            initializeWithLoading(passphrase, null)
        }
    }

    private suspend fun initializeBiometrics(activity: FragmentActivity, pin: String) {
        Timber.d("Passphrase: Showing biometric dialog")
        val biometricPromptWrapper = AndroidBiometricPrompt(
            activity = activity,
            promptType = BiometricPromptType.Setup,
        )

        initializeCipherWithBiometrics(
            promptWrapper = biometricPromptWrapper,
        ).onFailure { biometricsError: EnableBiometricsError ->
            when (biometricsError) {
                BiometricsError.Locked -> {
                    Timber.w("Biometrics registration: biometrics Locked")
                    _screenState.value = OnboardingRegisterBiometricsScreenState.Lockout
                }

                is BiometricsError.Unexpected -> {
                    Timber.e("Biometrics registration: Unexpected")
                    _screenState.value = OnboardingRegisterBiometricsScreenState.Error
                }

                BiometricsError.Cancelled -> {}
            }
        }.onSuccess { initializedCipher: Cipher ->
            initializeWithLoading(pin, initializedCipher)
        }
    }

    private suspend fun initializeWithLoading(pin: String, initializedCipher: Cipher?) {
        // start the setup animation
        _initializationInProgress.value = true
        setTopBarState(TopBarState.Empty)

        initializePassphrase(
            pin = pin,
            encryptionCipher = initializedCipher,
        ).andThen {
            initializedCipher?.let {
                saveUseBiometricLogin(true)
            }
            Ok(Unit)
        }.onSuccess {
            completeOnboarding()
        }.onFailure { error: InitializePassphraseError ->
            Timber.e(t = error.throwable, message = "Biometrics registration: Initialization error")
            navManager.popUpToAndNavigate(
                popToInclusive = Destination.OnboardingIntroScreen::class,
                destination = Destination.OnboardingErrorScreen
            )
        }
        _initializationInProgress.value = false
    }

    private suspend fun completeOnboarding() {
        saveOnboardingCompletedState()
        navigateToSuccess()
    }

    private fun navigateToSuccess() {
        navManager.navigateOutAndTo(DestinationGroup.Onboarding::class, Destination.OnboardingSuccessScreen)
    }

    private fun getScreenState(): OnboardingRegisterBiometricsScreenState = when (biometricsStatus()) {
        BiometricManagerResult.Available -> {
            if (
                screenState.value is OnboardingRegisterBiometricsScreenState.Lockout ||
                screenState.value is OnboardingRegisterBiometricsScreenState.Error
            ) {
                screenState.value
            } else {
                OnboardingRegisterBiometricsScreenState.Available
            }
        }

        BiometricManagerResult.CanEnroll, BiometricManagerResult.Unsupported -> OnboardingRegisterBiometricsScreenState.DisabledOnDevice
        BiometricManagerResult.Disabled -> OnboardingRegisterBiometricsScreenState.DisabledForApp
    }

    private suspend fun saveOnboardingCompletedState() {
        saveOnboardingState(isCompleted = true)
    }
}
