package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.onboarding.domain.Constants.MAX_CONFIRMATION_ATTEMPTS
import ch.admin.foitt.wallet.feature.onboarding.domain.usecase.SaveOnboardingState
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricManagerResult
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.BiometricsStatus
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.DestinationGroup
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.InitializePassphrase
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseValidationState
import ch.admin.foitt.wallet.platform.passphraseInput.domain.usecase.ValidatePassphrase
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.mapBoth
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = OnboardingConfirmPassphraseViewModel.Factory::class)
class OnboardingConfirmPassphraseViewModel @AssistedInject constructor(
    private val biometricsStatus: BiometricsStatus,
    private val initializePassphrase: InitializePassphrase,
    private val saveOnboardingState: SaveOnboardingState,
    private val validatePassphrase: ValidatePassphrase,
    private val navManager: NavigationManager,
    private val setTopBarState: SetTopBarState,
    @Assisted private val originalPassphrase: String,
) : ScreenViewModel(setTopBarState, systemBarsFixedLightColor = true) {
    @AssistedFactory
    interface Factory {
        fun create(originalPassphrase: String): OnboardingConfirmPassphraseViewModel
    }

    override val topBarState = TopBarState.OnGradient(navManager::popBackStack, R.string.tk_onboarding_passwordConfirmation_title)

    private val isBiometricAuthenticationAvailable: Boolean by lazy {
        biometricsStatus() != BiometricManagerResult.Unsupported
    }

    private val _isInitializing = MutableStateFlow(false)
    val isInitializing = _isInitializing.asStateFlow()

    private val _textFieldValue = MutableStateFlow(TextFieldValue(""))
    val textFieldValue = _textFieldValue.asStateFlow()

    private var _passphraseInputFieldState: MutableStateFlow<PassphraseInputFieldState> =
        MutableStateFlow(PassphraseInputFieldState.Typing)
    val passphraseInputFieldState = _passphraseInputFieldState.asStateFlow()

    val isPassphraseValid: StateFlow<Boolean> = textFieldValue.map { textField ->
        validatePassphrase(textField.text) == PassphraseValidationState.VALID
    }.toStateFlow(false, 0)

    private val _showPassphraseErrorToast = MutableStateFlow(false)
    val showPassphraseErrorToast = _showPassphraseErrorToast.asStateFlow()

    private val _remainingConfirmationAttempts = MutableStateFlow(MAX_CONFIRMATION_ATTEMPTS)
    val remainingConfirmationAttempts = _remainingConfirmationAttempts.asStateFlow()

    private val _showSupportText = MutableStateFlow(remainingConfirmationAttempts.value < MAX_CONFIRMATION_ATTEMPTS)
    val showSupportText = _showSupportText.asStateFlow()

    fun onTextFieldValueChange(textFieldValue: TextFieldValue) {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        _textFieldValue.value = textFieldValue
    }

    fun onCheckPassphrase() {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        when {
            textFieldValue.value.text == originalPassphrase -> onValidPassphrase(textFieldValue.value.text)
            else -> onInvalidPassphrase()
        }
    }

    private fun onValidPassphrase(passphrase: String) {
        resetConfirmationAttempts()
        _passphraseInputFieldState.value = PassphraseInputFieldState.Success
        if (isBiometricAuthenticationAvailable) {
            navigateToBiometrics(passphrase)
        } else {
            initializePassphrase(passphrase)
        }
        _textFieldValue.value = TextFieldValue("")
    }

    private fun initializePassphrase(passphrase: String) = viewModelScope.launch {
        setTopBarState(TopBarState.Empty)
        initializePassphrase(passphrase, null).mapBoth(
            success = {
                saveOnboardingState.invoke(isCompleted = true)
                _passphraseInputFieldState.value = PassphraseInputFieldState.Success
                handlePassphraseSuccess(passphrase)
            },
            failure = { error ->
                Timber.e(t = error.throwable, message = "Passphrase registration: Initialization error")
                _passphraseInputFieldState.value = PassphraseInputFieldState.Error
                navManager.popUpToAndNavigate(
                    popToInclusive = Destination.OnboardingIntroScreen::class,
                    destination = Destination.OnboardingErrorScreen
                )
            }
        )
    }.trackCompletion(_isInitializing)

    private fun handlePassphraseSuccess(passphrase: String) {
        if (isBiometricAuthenticationAvailable) {
            navigateToBiometrics(passphrase)
        } else {
            navManager.navigateOutAndTo(DestinationGroup.Onboarding::class, Destination.OnboardingSuccessScreen)
        }
    }

    private fun navigateToBiometrics(passphrase: String) {
        navManager.replaceCurrentWith(
            Destination.OnboardingRegisterBiometricsScreen(passphrase = passphrase)
        )
    }

    private fun onInvalidPassphrase() {
        decreaseRemainingAttempts()
        _passphraseInputFieldState.value = PassphraseInputFieldState.Error
        _showPassphraseErrorToast.value = true
        checkRemainingConfirmationAttempts()
    }

    private fun resetConfirmationAttempts() {
        _remainingConfirmationAttempts.value = MAX_CONFIRMATION_ATTEMPTS
    }

    private fun decreaseRemainingAttempts() {
        _remainingConfirmationAttempts.value -= 1
    }

    private fun checkRemainingConfirmationAttempts() {
        _showSupportText.value = remainingConfirmationAttempts.value < MAX_CONFIRMATION_ATTEMPTS
        if (remainingConfirmationAttempts.value <= 0) {
            resetConfirmationAttempts()
            navigateToConfirmationFailedScreen()
        }
    }

    private fun navigateToConfirmationFailedScreen() = navManager.replaceCurrentWith(
        destination = Destination.OnboardingConfirmPassphraseFailureScreen
    )

    fun onClosePassphraseError() {
        _showPassphraseErrorToast.value = false
    }
}
