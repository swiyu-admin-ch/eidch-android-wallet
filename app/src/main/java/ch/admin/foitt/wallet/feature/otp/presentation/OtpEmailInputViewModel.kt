package ch.admin.foitt.wallet.feature.otp.presentation

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.otp.di.OtpEntryPoint
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpEmailValidationState
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpError
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpInputFieldState
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpRequest
import ch.admin.foitt.wallet.feature.otp.domain.model.OtpValidationState
import ch.admin.foitt.wallet.feature.otp.domain.usecase.RequestOtp
import ch.admin.foitt.wallet.feature.otp.domain.usecase.ValidateEmail
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpEmailInputViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    destinationScopedComponentManager: DestinationScopedComponentManager,
    private val requestOtp: RequestOtp,
    private val validateEmail: ValidateEmail,
    private val navManager: NavigationManager,
    environmentSetupRepository: EnvironmentSetupRepository,
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
        OtpEmailInputUiState.Initial,
        OtpEmailInputUiState.Loading,
        OtpEmailInputUiState.ForbiddenEmail -> topBarDetails(R.string.tk_eidRequest_otp_email_title)
        else -> topBarDetails(null)
    }

    private fun topBarDetails(titleId: Int?) =
        TopBarState.DetailsWithCloseButton(
            titleId = titleId,
            topBarBackground = TopBarBackground.DEFAULT,
            onUp = this::onBack,
            onClose = this::onBackHome
        )

    val allowBypassOtp = environmentSetupRepository.allowBypassOtp

    private val _validationState: MutableStateFlow<OtpEmailValidationState> =
        MutableStateFlow(OtpValidationState.Valid)
    val validationState = _validationState.asStateFlow()

    private val _textFieldValue = MutableStateFlow(TextFieldValue(""))
    val textFieldValue = _textFieldValue.asStateFlow()

    private val _emailInputFieldState: MutableStateFlow<OtpInputFieldState> =
        MutableStateFlow(OtpInputFieldState.Initial)
    val emailInputFieldState = _emailInputFieldState.asStateFlow()

    private val _uiState = MutableStateFlow<OtpEmailInputUiState>(OtpEmailInputUiState.Initial)
    val uiState: StateFlow<OtpEmailInputUiState> = _uiState.asStateFlow()

    private val _isToastVisible = MutableStateFlow(false)
    val isToastVisible = _isToastVisible.asStateFlow()

    init {
        validateInput()
        viewModelScope.launch {
            uiState.collectLatest {
                setTopBarState(topBarState)
            }
        }
    }

    fun onResume() {
        if (otpToastRepository.showToast.value) {
            viewModelScope.launch {
                _isToastVisible.value = true
                delay(TOAST_DISPLAY_TIME_MILLIS)
                _isToastVisible.value = false
            }
        }

        val savedEmail = otpEmailRepository.email.value
        if (savedEmail != null) {
            _textFieldValue.value = TextFieldValue(savedEmail)
        }
    }

    fun onTextFieldValueChange(textFieldValue: TextFieldValue) {
        _textFieldValue.value = textFieldValue
        _emailInputFieldState.value = OtpInputFieldState.Edited
        validateInput()
    }

    private fun validateInput() {
        val input = textFieldValue.value.text
        val validationResult = if (input.isEmpty()) {
            OtpValidationState.Valid
        } else {
            validateEmail(input)
        }
        _validationState.value = validationResult
    }

    fun onContinue() {
        viewModelScope.launch {
            _uiState.value = OtpEmailInputUiState.Loading
            requestOtp()
        }
    }

    private suspend fun requestOtp() {
        requestOtp(otpRequest = OtpRequest(textFieldValue.value.text))
            .onFailure { error ->
                when (error) {
                    OtpError.InvalidField -> _uiState.value = OtpEmailInputUiState.ForbiddenEmail
                    OtpError.NetworkError -> _uiState.value = OtpEmailInputUiState.NetworkError
                    OtpError.ServiceDeactivated -> _uiState.value = OtpEmailInputUiState.Unavailable
                    OtpError.InvalidClientAttestation -> _uiState.value = OtpEmailInputUiState.NotSupported
                    OtpError.InvalidFormat,
                    OtpError.OtpExpired,
                    OtpError.TooManyRequests,
                    is OtpError.Unexpected -> _uiState.value = OtpEmailInputUiState.Unexpected
                }
            }
            .onSuccess {
                _uiState.value = OtpEmailInputUiState.Initial
                otpEmailRepository.setEmail(textFieldValue.value.text)
                navManager.navigateTo(Destination.OtpCodeInputScreen)
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

    fun onBypassOtp() {
        continueToEId()
    }

    fun onBack() {
        if (uiState.value == OtpEmailInputUiState.Initial || uiState.value == OtpEmailInputUiState.ForbiddenEmail) {
            navManager.popBackStack()
        } else {
            _uiState.value = OtpEmailInputUiState.Initial
        }
    }

    companion object {
        private const val TOAST_DISPLAY_TIME_MILLIS = 4000L
    }
}
