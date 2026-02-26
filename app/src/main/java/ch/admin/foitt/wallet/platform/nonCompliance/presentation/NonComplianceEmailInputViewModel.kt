package ch.admin.foitt.wallet.platform.nonCompliance.presentation

import androidx.compose.ui.text.input.TextFieldValue
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.nonCompliance.di.NonComplianceEntryPoint
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceEmailValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.ValidateEmail
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarBackground
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NonComplianceEmailInputViewModel @Inject constructor(
    destinationScopedComponentManager: DestinationScopedComponentManager,
    private val validateEmail: ValidateEmail,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    private val nonComplianceFormRepository = destinationScopedComponentManager.getEntryPoint(
        entryPointClass = NonComplianceEntryPoint::class.java,
        componentScope = ComponentScope.NonComplianceFormInput,
    ).nonComplianceFormRepository()

    override val topBarState = TopBarState.Details(
        titleId = R.string.tk_nonCompliance_report_form_contact_title,
        topBarBackground = TopBarBackground.DEFAULT,
        onUp = this::onBack,
    )

    private val _validationState: MutableStateFlow<NonComplianceEmailValidationState> =
        MutableStateFlow(NonComplianceValidationState.Valid)
    val validationState = _validationState.asStateFlow()

    val textFieldValue = nonComplianceFormRepository.email

    val textFieldState = nonComplianceFormRepository.emailInputFieldState

    init {
        validateInput()
    }

    fun onTextFieldValueChange(textFieldValue: TextFieldValue) {
        nonComplianceFormRepository.setEmail(textFieldValue)
        validateInput()
    }

    private fun validateInput() {
        val input = textFieldValue.value.text
        val validationResult = if (input.isEmpty()) {
            NonComplianceValidationState.Valid
        } else {
            validateEmail(input)
        }
        _validationState.value = validationResult
    }

    fun onClearInput() {
        nonComplianceFormRepository.clearEmail()
        validateInput()
    }

    fun onBack() {
        navManager.popBackStack()
    }
}
