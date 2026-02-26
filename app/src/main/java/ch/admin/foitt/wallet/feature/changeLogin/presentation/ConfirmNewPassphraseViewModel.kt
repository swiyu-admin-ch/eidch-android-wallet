package ch.admin.foitt.wallet.feature.changeLogin.presentation

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.ChangePassphrase
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.PassphraseChangeEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.PassphraseChangeEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = ConfirmNewPassphraseViewModel.Factory::class)
class ConfirmNewPassphraseViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    private val validatePassphrase: ValidatePassphrase,
    private val changePassphrase: ChangePassphrase,
    private val passphraseChangeEventRepository: PassphraseChangeEventRepository,
    setTopBarState: SetTopBarState,
    @Assisted private val originalPassphrase: String
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(originalPassphrase: String): ConfirmNewPassphraseViewModel
    }

    override val topBarState = TopBarState.Details(navManager::popBackStack, R.string.pin_change_title)

    private val _textFieldValue = MutableStateFlow(TextFieldValue(text = ""))
    val textFieldValue = _textFieldValue.asStateFlow()

    private var _passphraseInputFieldState: MutableStateFlow<PassphraseInputFieldState> =
        MutableStateFlow(PassphraseInputFieldState.Typing)
    val passphraseInputFieldState = _passphraseInputFieldState.asStateFlow()

    val isPassphraseValid: StateFlow<Boolean> = textFieldValue.map { textField ->
        validatePassphrase(textField.text) == PassphraseValidationState.VALID
    }.toStateFlow(false, 0)

    private val _remainingConfirmationAttempts = MutableStateFlow(MAX_NEW_PASSPHRASE_CONFIRMATION_ATTEMPTS)
    val remainingConfirmationAttempts = _remainingConfirmationAttempts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val hideSupportText = combine(
        passphraseInputFieldState,
        remainingConfirmationAttempts,
        isLoading
    ) { passphraseInputFieldState, remainingConfirmationAttempts, isLoading ->
        passphraseInputFieldState == PassphraseInputFieldState.Typing ||
            remainingConfirmationAttempts >= MAX_NEW_PASSPHRASE_CONFIRMATION_ATTEMPTS ||
            isLoading
    }.toStateFlow(true)

    fun onTextFieldValueChange(textFieldValue: TextFieldValue) {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        _textFieldValue.value = textFieldValue
    }

    fun onCheckPassphrase() {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        if (isPassphraseValid.value) {
            viewModelScope.launch {
                when (textFieldValue.value.text == originalPassphrase) {
                    true -> onValidPassphrase()
                    false -> onInvalidPassphrase()
                }
            }.trackCompletion(_isLoading)
        }
    }

    private fun onInvalidPassphrase() {
        decreaseRemainingAttempts()
        _passphraseInputFieldState.value = PassphraseInputFieldState.Error
        checkRemainingConfirmationAttempts()
    }

    private suspend fun onValidPassphrase() = changePassphrase(textFieldValue.value.text).mapBoth(
        success = {
            _passphraseInputFieldState.value = PassphraseInputFieldState.Success
            passphraseChangeEventRepository.setEvent(PassphraseChangeEvent.CHANGED)
            navManager.popBackStackTo(Destination.SecuritySettingsScreen::class, false)
        },
        failure = { error ->
            Timber.e(error.throwable, "Could not change password")
            _passphraseInputFieldState.value = PassphraseInputFieldState.Error
        }
    )

    private fun resetConfirmationAttempts() {
        _remainingConfirmationAttempts.value = MAX_NEW_PASSPHRASE_CONFIRMATION_ATTEMPTS
    }

    private fun decreaseRemainingAttempts() {
        _remainingConfirmationAttempts.value -= 1
    }

    fun checkRemainingConfirmationAttempts() {
        if (remainingConfirmationAttempts.value <= 0) {
            resetConfirmationAttempts()
            navManager.popBackStack()
        }
    }

    private companion object {
        const val MAX_NEW_PASSPHRASE_CONFIRMATION_ATTEMPTS = 3
    }
}
