package ch.admin.foitt.wallet.platform.nonCompliance.presentation

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivityActorDisplaysFlow
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromImageData
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.NonComplianceEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.NonComplianceEventRepository
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.nonCompliance.di.NonComplianceEntryPoint
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceEmailValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceInputFieldType
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceTextInputConstraints
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceTextLengthValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendNonComplianceReport
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.ValidateEmail
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.ValidateTextLength
import ch.admin.foitt.wallet.platform.nonCompliance.presentation.model.NonComplianceActorUiState
import ch.admin.foitt.wallet.platform.nonCompliance.presentation.model.NonComplianceFormUiState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarBackground
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.toPainter
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.mapBoth
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = NonComplianceFormViewModel.Factory::class)
class NonComplianceFormViewModel @AssistedInject constructor(
    getActivityActorDisplaysFlow: GetActivityActorDisplaysFlow,
    private val getDrawableFromImageData: GetDrawableFromImageData,
    destinationScopedComponentManager: DestinationScopedComponentManager,
    nonComplianceTextInputConstraints: NonComplianceTextInputConstraints,
    private val validateTextLength: ValidateTextLength,
    private val validateEmail: ValidateEmail,
    private val sendNonComplianceReport: SendNonComplianceReport,
    private val nonComplianceEventRepository: NonComplianceEventRepository,
    private val navManager: NavigationManager,
    @Assisted private val activityId: Long,
    @Assisted private val titleId: Int?,
    @Assisted private val reportReason: NonComplianceReportReason,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    @AssistedFactory
    interface Factory {
        fun create(
            activityId: Long,
            titleId: Int?,
            reportReason: NonComplianceReportReason
        ): NonComplianceFormViewModel
    }

    private val nonComplianceFormRepository = destinationScopedComponentManager.getEntryPoint(
        entryPointClass = NonComplianceEntryPoint::class.java,
        componentScope = ComponentScope.NonComplianceFormInput,
    ).nonComplianceFormRepository()

    override val topBarState = TopBarState.DetailsWithCloseButton(
        titleId = titleId,
        topBarBackground = TopBarBackground.CLUSTER,
        onClose = this::onClose,
        onUp = this::onBack
    )

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val description = nonComplianceFormRepository.reportDescription.map { description ->
        validateDescriptionInput(description.text)
        description
    }.toStateFlow(TextFieldValue(""))

    private val _descriptionValidationState: MutableStateFlow<NonComplianceTextLengthValidationState> =
        MutableStateFlow(NonComplianceValidationState.TooShort)
    val descriptionValidationState = _descriptionValidationState.asStateFlow()

    val email = nonComplianceFormRepository.email.map { email ->
        validateEmailInput(email.text)
        email
    }.toStateFlow(TextFieldValue(""))

    private val _emailValidationState: MutableStateFlow<NonComplianceEmailValidationState> =
        MutableStateFlow(NonComplianceValidationState.Valid)
    val emailValidationState = _emailValidationState.asStateFlow()

    val nonComplianceFormUiState = combine(
        description,
        descriptionValidationState,
        nonComplianceFormRepository.descriptionInputFieldState,
        email,
        emailValidationState,
        nonComplianceFormRepository.emailInputFieldState,
    ) {
        NonComplianceFormUiState(
            descriptionInputFieldState = nonComplianceFormRepository.descriptionInputFieldState.value,
            description = description.value,
            isDescriptionValid = descriptionValidationState.value,
            descriptionMaxInputLength = nonComplianceTextInputConstraints.maxLength,
            emailInputFieldState = nonComplianceFormRepository.emailInputFieldState.value,
            email = email.value,
            isEmailValid = emailValidationState.value,
        )
    }.toStateFlow(NonComplianceFormUiState.EMPTY)

    val nonComplianceActorUiState = getActivityActorDisplaysFlow(activityId).map { result ->
        result.mapBoth(
            success = { activityActorDisplayData ->
                _isLoading.value = false
                mapToUiState(activityActorDisplayData = activityActorDisplayData)
            },
            failure = {
                navigateToErrorScreen()
                null
            }
        )
    }.filterNotNull()
        .toStateFlow(NonComplianceActorUiState.EMPTY)

    private suspend fun mapToUiState(activityActorDisplayData: ActivityActorDisplayData): NonComplianceActorUiState {
        val drawable = activityActorDisplayData.actorImageData?.let {
            getDrawableFromImageData(it)
        }

        return NonComplianceActorUiState(
            name = activityActorDisplayData.localizedActorName,
            logo = drawable?.toPainter(),
        )
    }

    private fun navigateToErrorScreen() {
        navManager.replaceCurrentWith(Destination.GenericErrorScreen(GenericErrorScreenState.GENERIC))
    }

    fun validateDescriptionInput(description: String) {
        _descriptionValidationState.value = validateTextLength(description)
    }

    fun validateEmailInput(email: String) {
        val emailValidationResult = if (email.isEmpty()) {
            NonComplianceValidationState.Valid
        } else {
            validateEmail(email)
        }
        _emailValidationState.value = emailValidationResult
    }

    fun validateForm() {
        validateDescriptionInput(description.value.text)
        validateEmailInput(email.value.text)
    }

    fun onClearInput(nonComplianceInputFieldType: NonComplianceInputFieldType) {
        when (nonComplianceInputFieldType) {
            NonComplianceInputFieldType.DESCRIPTION -> {
                nonComplianceFormRepository.clearReportDescription()
            }

            NonComplianceInputFieldType.EMAIL -> {
                nonComplianceFormRepository.clearEmail()
            }
        }
    }

    fun onTextInputField(
        inputFieldType: NonComplianceInputFieldType,
    ) {
        val destination = when (inputFieldType) {
            NonComplianceInputFieldType.DESCRIPTION -> Destination.NonComplianceDescriptionInputScreen
            NonComplianceInputFieldType.EMAIL -> Destination.NonComplianceEmailInputScreen
        }
        navManager.navigateTo(destination)
    }

    fun onSend() {
        viewModelScope.launch {
            sendNonComplianceReport(
                activityId = activityId,
                reportReason = reportReason,
                description = description.value.text,
                email = email.value.text.ifEmpty { null },
            ).mapBoth(
                success = {
                    nonComplianceEventRepository.setEvent(NonComplianceEvent.REPORT_SENT)
                    onClose()
                },
                failure = {
                    navigateToErrorScreen()
                }
            )
        }.trackCompletion(_isLoading)
    }

    private fun onBack() {
        navManager.popBackStack()
    }

    private fun onClose() {
        navManager.popBackStackTo(
            destination = Destination.NonComplianceListScreen::class,
            inclusive = true
        )
    }
}
