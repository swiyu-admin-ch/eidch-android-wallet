package ch.admin.foitt.wallet.platform.nonCompliance.domain.repository

import androidx.compose.ui.text.input.TextFieldValue
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceInputFieldState
import kotlinx.coroutines.flow.StateFlow

interface NonComplianceFormRepository {
    val reportDescription: StateFlow<TextFieldValue>
    val descriptionInputFieldState: StateFlow<NonComplianceInputFieldState>
    val email: StateFlow<TextFieldValue>
    val emailInputFieldState: StateFlow<NonComplianceInputFieldState>

    fun setReportDescription(description: TextFieldValue)
    fun setEmail(email: TextFieldValue)

    fun clearReportDescription()
    fun clearEmail()
    fun clearAll()
}
