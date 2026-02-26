package ch.admin.foitt.wallet.platform.nonCompliance.data.repository

import androidx.compose.ui.text.input.TextFieldValue
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceInputFieldState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.repository.NonComplianceFormRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class NonComplianceFormRepositoryImpl @Inject constructor() : NonComplianceFormRepository {
    private val _reportDescription = MutableStateFlow(TextFieldValue(""))
    override val reportDescription = _reportDescription.asStateFlow()

    private val _descriptionInputFieldState: MutableStateFlow<NonComplianceInputFieldState> =
        MutableStateFlow(NonComplianceInputFieldState.Initial)
    override val descriptionInputFieldState = _descriptionInputFieldState.asStateFlow()

    private val _email = MutableStateFlow(TextFieldValue(""))
    override val email = _email.asStateFlow()

    private val _emailInputFieldState: MutableStateFlow<NonComplianceInputFieldState> =
        MutableStateFlow(NonComplianceInputFieldState.Initial)
    override val emailInputFieldState = _emailInputFieldState.asStateFlow()

    override fun setReportDescription(description: TextFieldValue) {
        _reportDescription.value = description
        _descriptionInputFieldState.value = NonComplianceInputFieldState.Edited
    }

    override fun setEmail(email: TextFieldValue) {
        _email.value = email
        _emailInputFieldState.value = NonComplianceInputFieldState.Edited
    }

    override fun clearReportDescription() {
        _reportDescription.value = TextFieldValue("")
    }

    override fun clearEmail() {
        _email.value = TextFieldValue("")
    }

    override fun clearAll() {
        clearReportDescription()
        clearEmail()
    }
}
