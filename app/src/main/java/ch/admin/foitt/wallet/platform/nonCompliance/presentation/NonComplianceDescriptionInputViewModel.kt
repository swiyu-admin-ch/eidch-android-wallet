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
class NonComplianceDescriptionInputViewModel @Inject constructor(
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
        titleId = R.string.tk_nonCompliance_report_form_description_title,
        titleAltTextId = R.string.tk_nonCompliance_report_form_description_title_alt,
        topBarBackground = TopBarBackground.DEFAULT,
        onUp = this::onBack,
    )

    val maxInputLength = nonComplianceTextInputConstraints.maxLength

    private val _validationState: MutableStateFlow<NonComplianceTextLengthValidationState> =
        MutableStateFlow(NonComplianceValidationState.TooShort)
    val validationState = _validationState.asStateFlow()

    val textFieldValue = nonComplianceFormRepository.reportDescription

    val textFieldState = nonComplianceFormRepository.descriptionInputFieldState

    init {
        validateInput()
    }

    fun onTextFieldValueChange(textFieldValue: TextFieldValue) {
        nonComplianceFormRepository.setReportDescription(textFieldValue)
        validateInput()
    }

    private fun validateInput() {
        val input = textFieldValue.value.text
        _validationState.value = validateTextLength(input)
    }

    fun onClearInput() {
        nonComplianceFormRepository.clearReportDescription()
        validateInput()
    }

    fun onBack() {
        navManager.popBackStack()
    }
}
