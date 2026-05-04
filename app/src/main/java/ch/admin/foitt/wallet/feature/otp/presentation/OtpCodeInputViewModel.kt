package ch.admin.foitt.wallet.feature.otp.presentation

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.otp.di.OtpEntryPoint
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpCodeLengthValidationState
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpError
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpValidationState
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpVerify
import ch.admin.foitt.wallet.feature.otp.domain.repository.OtpStateCompletionRepository
import ch.admin.foitt.wallet.feature.otp.domain.usecase.ValidateCodeLength
import ch.admin.foitt.wallet.feature.otp.domain.usecase.VerifyOtp
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarBackground
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpCodeInputViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    destinationScopedComponentManager: DestinationScopedComponentManager,
    private val otpStateCompletionRepository: OtpStateCompletionRepository,
    private val validateCodeLength: ValidateCodeLength,
    private val verifyOtp: VerifyOtp,
    private val navManager: NavigationManager,
    private val setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    private val otpEmailRepository = destinationScopedComponentManager.getEntryPoint(
        entryPointClass = OtpEntryPoint::class.java,
        componentScope = ComponentScope.Otp,
    ).otpEmailRepository()

    private val otpToastRepository = destinationScopedComponentManager.getEntryPoint(
        entryPointClass = OtpEntryPoint::class.java,
        componentScope = ComponentScope.Otp,
    ).otpToastRepository()

    override val topBarState: TopBarState get() = when (uiState.value) {
        OtpCodeInputUiState.Initial,
        OtpCodeInputUiState.Loading,
        OtpCodeInputUiState.WrongCode -> topBarDetails(R.string.tk_eidRequest_otp_code_title)
        else -> topBarDetails(null)
    }

    private fun topBarDetails(titleId: Int?) =
        TopBarState.DetailsWithCloseButton(
            titleId = titleId,
            topBarBackground = TopBarBackground.DEFAULT,
            onUp = this::onBack,
            onClose = this::onBackHome
        )

    private val _validationState: MutableStateFlow<OtpCodeLengthValidationState> =
        MutableStateFlow(OtpValidationState.TooShort)
    val validationState = _validationState.asStateFlow()

    private val _textFieldValue = MutableStateFlow(TextFieldValue(""))
    val textFieldValue = _textFieldValue.asStateFlow()

    private val _uiState = MutableStateFlow<OtpCodeInputUiState>(OtpCodeInputUiState.Initial)
    val uiState: StateFlow<OtpCodeInputUiState> = _uiState.asStateFlow()

    val email = otpEmailRepository.email.value ?: ""

    init {
        validateInput()
        viewModelScope.launch {
            uiState.collectLatest {
                setTopBarState(topBarState)
            }
        }
    }

    fun onTextFieldValueChange(textFieldValue: TextFieldValue) {
        _textFieldValue.value = textFieldValue
        validateInput()
    }

    private fun validateInput() {
        val input = textFieldValue.value.text
        _validationState.value = validateCodeLength(input)
        if (_validationState.value is OtpValidationState.Valid) {
            onContinue()
        }
    }

    fun onContinue() {
        viewModelScope.launch {
            _uiState.value = OtpCodeInputUiState.Loading
            verifyOtp()
        }
    }

    private suspend fun verifyOtp() {
        verifyOtp(
            otpVerify = OtpVerify(
                email = email,
                code = textFieldValue.value.text
            )
        ).onFailure { error ->
            resetCodeFieldValue()
            when (error) {
                OtpError.OtpExpired -> onExpired()
                OtpError.NetworkError -> _uiState.value = OtpCodeInputUiState.NetworkError
                OtpError.ServiceDeactivated -> _uiState.value = OtpCodeInputUiState.Unavailable
                OtpError.InvalidClientAttestation -> _uiState.value = OtpCodeInputUiState.NotSupported
                OtpError.TooManyRequests -> _uiState.value = OtpCodeInputUiState.TooManyAttempts
                OtpError.InvalidField -> _uiState.value = OtpCodeInputUiState.WrongCode
                else -> _uiState.value = OtpCodeInputUiState.Unexpected
            }
        }.onSuccess {
            otpStateCompletionRepository.setOtpFlowWasDone(isCompleted = true)
            continueToEId()
        }
    }

    private fun continueToEId() {
        navManager.popUpToAndNavigate(
            popToInclusive = Destination.OtpIntroScreen::class,
            destination = Destination.EIdIntroScreen
        )
    }

    fun onBackHome() {
        navManager.navigateBackToHomeScreen(Destination.OtpIntroScreen::class)
    }

    fun onStatusPage() = appContext.openLink(appContext.getString(R.string.tk_eidRequest_otp_unavailable_link_value))

    fun onHelp() = appContext.openLink(appContext.getString(R.string.tk_eidRequest_attestation_helpLink_url))

    fun onPlaystore() = appContext.openLink(
        appContext.getString(R.string.tk_eidRequest_attestation_clientNotSupported_button_playstore_url)
    )

    private fun onExpired() {
        otpToastRepository.setShowToast(true)
        navManager.popBackStackTo(
            destination = Destination.OtpEmailInputScreen::class,
            inclusive = false
        )
    }

    private fun resetCodeFieldValue() {
        _textFieldValue.value = TextFieldValue("")
    }

    fun onBack() {
        when (uiState.value) {
            OtpCodeInputUiState.Initial,
            OtpCodeInputUiState.WrongCode,
            OtpCodeInputUiState.NotSupported,
            OtpCodeInputUiState.TooManyAttempts -> navManager.popBackStack()

            else -> {
                resetCodeFieldValue()
                _uiState.value = OtpCodeInputUiState.Initial
            }
        }
    }
}
