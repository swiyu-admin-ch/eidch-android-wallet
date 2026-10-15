package ch.admin.foitt.wallet.feature.changePassphrase.presentation

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.errorhandling.domain.model.ViewmodelPrefixes
import ch.admin.foitt.errorhandling.domain.model.util.log
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.model.AuthWithPassphraseError
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.usecase.AuthenticateWithPassphrase
import ch.admin.foitt.wallet.platform.login.domain.usecase.GetRemainingLoginAttempts
import ch.admin.foitt.wallet.platform.login.domain.usecase.IncreaseFailedLoginAttemptsCounter
import ch.admin.foitt.wallet.platform.login.domain.usecase.ResetLockout
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseValidationState
import ch.admin.foitt.wallet.platform.passphraseInput.domain.usecase.ValidatePassphrase
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import com.github.michaelbull.result.mapBoth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnterCurrentPassphraseViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val getRemainingLoginAttempts: GetRemainingLoginAttempts,
    private val validatePassphrase: ValidatePassphrase,
    private val authenticateWithPassphrase: AuthenticateWithPassphrase,
    private val resetLockout: ResetLockout,
    private val increaseFailedLoginAttemptsCounter: IncreaseFailedLoginAttemptsCounter,
    private val setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.Details(navManager::popBackStack, R.string.tk_global_changepassword)

    private val _textFieldValue = MutableStateFlow(TextFieldValue(text = ""))
    val textFieldValue = _textFieldValue.asStateFlow()

    private val _passphraseInputFieldState: MutableStateFlow<PassphraseInputFieldState> =
        MutableStateFlow(PassphraseInputFieldState.Typing)
    val passphraseInputFieldState = _passphraseInputFieldState.asStateFlow()

    private val _isPassphraseValid =
        MutableStateFlow(validatePassphrase(textFieldValue.value.text) == PassphraseValidationState.VALID)
    val isPassphraseValid = _isPassphraseValid.asStateFlow()

    private val _remainingAuthAttempts = MutableStateFlow(getRemainingLoginAttempts())
    val remainingAuthAttempts = _remainingAuthAttempts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val hideSupportText = combine(
        passphraseInputFieldState,
        isLoading
    ) { passphraseInputFieldState, isLoading ->
        passphraseInputFieldState == PassphraseInputFieldState.Typing ||
            isLoading
    }.toStateFlow(true)

    fun onTextFieldValueChange(textFieldValue: TextFieldValue) {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        _textFieldValue.value = textFieldValue
        _isPassphraseValid.value = validatePassphrase(textFieldValue.text) == PassphraseValidationState.VALID
    }

    fun onCheckPassphrase() {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        if (isPassphraseValid.value) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    // Hide the back button while authenticating so TalkBack focuses the loading
                    // indicator instead of the back button (which otherwise wins by traversal priority).
                    setTopBarState(TopBarState.Empty)
                    authenticateWithPassphrase(passphrase = textFieldValue.value.text).mapBoth(
                        success = {
                            resetLockout()
                            _passphraseInputFieldState.value = PassphraseInputFieldState.Success
                            _textFieldValue.value = TextFieldValue("")
                            navigateToEnterNewPassphraseScreen()
                        },
                        failure = { error ->
                            increaseFailedLoginAttemptsCounter()
                            if (error is AuthWithPassphraseError.General) {
                                error.log(ViewmodelPrefixes.ENTER_CURRENT_PASSPHRASE)
                            }
                            _passphraseInputFieldState.value = PassphraseInputFieldState.Error(error.errorOccurrence.errorCode.value)
                            // The loading overlay has to be gone before the back button comes back, otherwise the
                            // reappearing top bar takes the accessibility focus before the error can be announced.
                            _isLoading.value = false
                            checkRemainingAttempts()
                            if (remainingAuthAttempts.value > 0) {
                                setTopBarState(topBarState)
                            }
                        }
                    )
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun checkRemainingAttempts() {
        _remainingAuthAttempts.value = getRemainingLoginAttempts()
        if (remainingAuthAttempts.value <= 0) {
            navigateToLockoutScreen()
        }
    }

    private fun navigateToEnterNewPassphraseScreen() = navManager.navigateTo(Destination.EnterNewPassphraseScreen)

    private fun navigateToLockoutScreen() = viewModelScope.launch {
        navManager.replaceCurrentWith(Destination.LockoutScreen)
    }
}
