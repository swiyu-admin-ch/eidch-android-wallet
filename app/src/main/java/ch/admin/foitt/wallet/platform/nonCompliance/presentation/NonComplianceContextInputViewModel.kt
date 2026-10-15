package ch.admin.foitt.wallet.platform.nonCompliance.presentation

import androidx.compose.ui.text.input.TextFieldValue
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.nonCompliance.di.NonComplianceEntryPoint
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceTextInputConstraints
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceTextLengthValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.ValidateTextLength
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarBackground
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NonComplianceContextInputViewModel @Inject constructor(
    destinationScopedComponentManager: DestinationScopedComponentManager,
    nonComplianceTextInputConstraints: NonComplianceTextInputConstraints,
    private val validateTextLength: ValidateTextLength,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    private val nonComplianceFormRepository = destinationScopedComponentManager.getEntryPoint(
        entryPointClass = NonComplianceEntryPoint::class.java,
        componentScope = ComponentScope.NonComplianceFormInput,
    ).nonComplianceFormRepository()

    override val topBarState = TopBarState.Details(
        titleId = R.string.nonCompliance_report_form_context_title,
        topBarBackground = TopBarBackground.DEFAULT,
        onUp = this::onBack,
    )

    val maxInputLength = nonComplianceTextInputConstraints.maxLength

    private val _validationState: MutableStateFlow<NonComplianceTextLengthValidationState> =
        MutableStateFlow(NonComplianceValidationState.TooShort)
    val validationState = _validationState.asStateFlow()

    val textFieldValue = nonComplianceFormRepository.reportContext

    val textFieldState = nonComplianceFormRepository.contextInputFieldState

    init {
        validateInput()
    }

    fun onTextFieldValueChange(textFieldValue: TextFieldValue) {
        nonComplianceFormRepository.setReportContext(textFieldValue)
        validateInput()
    }

    private fun validateInput() {
        val input = textFieldValue.value.text
        _validationState.value = validateTextLength(input)
    }

    fun onClearInput() {
        nonComplianceFormRepository.clearReportContext()
        validateInput()
    }

    fun onBack() {
        navManager.popBackStack()
    }
}
