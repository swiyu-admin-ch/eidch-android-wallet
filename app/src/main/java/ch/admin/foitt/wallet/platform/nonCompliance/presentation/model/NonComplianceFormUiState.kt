package ch.admin.foitt.wallet.platform.nonCompliance.presentation.model

import androidx.compose.ui.text.input.TextFieldValue
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceEmailValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceInputFieldState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceTextInputConstraints
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceTextLengthValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceValidationState

data class NonComplianceFormUiState(
    val description: TextFieldValue,
    val descriptionMaxInputLength: Int,
    val isDescriptionValid: NonComplianceTextLengthValidationState,
    val descriptionInputFieldState: NonComplianceInputFieldState,
    val context: TextFieldValue,
    val contextInputFieldState: NonComplianceInputFieldState,
    val isContextValid: NonComplianceTextLengthValidationState,
    val contextMaxInputLength: Int,
    val email: TextFieldValue,
    val isEmailValid: NonComplianceEmailValidationState,
    val emailInputFieldState: NonComplianceInputFieldState,
) {
    fun isFormValid() = isDescriptionValid is NonComplianceValidationState.Valid && isEmailValid is NonComplianceValidationState.Valid &&
        (isContextValid is NonComplianceValidationState.Valid || context.text.isEmpty())

    companion object {
        val EMPTY = NonComplianceFormUiState(
            description = TextFieldValue(""),
            isDescriptionValid = NonComplianceValidationState.TooShort,
            descriptionMaxInputLength = NonComplianceTextInputConstraints().maxLength,
            descriptionInputFieldState = NonComplianceInputFieldState.Initial,
            context = TextFieldValue(""),
            contextInputFieldState = NonComplianceInputFieldState.Initial,
            isContextValid = NonComplianceValidationState.TooShort,
            contextMaxInputLength = NonComplianceTextInputConstraints().maxLength,
            email = TextFieldValue(""),
            isEmailValid = NonComplianceValidationState.Valid,
            emailInputFieldState = NonComplianceInputFieldState.Initial,
        )
    }
}
