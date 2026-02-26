package ch.admin.foitt.wallet.platform.nonCompliance.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Avatar
import ch.admin.foitt.wallet.platform.composables.AvatarSize
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.clusterLazyListItem
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.verticalSafeDrawing
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceInputFieldState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceInputFieldType
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceValidationState
import ch.admin.foitt.wallet.platform.nonCompliance.presentation.model.NonComplianceActorUiState
import ch.admin.foitt.wallet.platform.nonCompliance.presentation.model.NonComplianceFormUiState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun NonComplianceFormScreen(viewModel: NonComplianceFormViewModel) {
    OnResumeEventHandler {
        viewModel.validateForm()
    }

    NonComplianceFormScreenContent(
        nonComplianceActorUiState = viewModel.nonComplianceActorUiState.collectAsStateWithLifecycle().value,
        nonComplianceFormUiState = viewModel.nonComplianceFormUiState.collectAsStateWithLifecycle().value,
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        onTextInputField = viewModel::onTextInputField,
        onClear = viewModel::onClearInput,
        onSend = viewModel::onSend,
    )
}

@Composable
private fun NonComplianceFormScreenContent(
    nonComplianceActorUiState: NonComplianceActorUiState,
    nonComplianceFormUiState: NonComplianceFormUiState,
    isLoading: Boolean,
    onTextInputField: (NonComplianceInputFieldType) -> Unit,
    onClear: (NonComplianceInputFieldType) -> Unit,
    onSend: () -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxSize()
        .background(color = WalletTheme.colorScheme.surfaceContainerLow)
) {
    val buttonHeight = remember { mutableStateOf(0.dp) }

    WalletLayouts.LazyColumn(
        modifier = Modifier
            .widthIn(max = Sizes.contentMaxWidth)
            .align(Alignment.TopCenter)
            .horizontalSafeDrawing(),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(start = Sizes.s04, top = Sizes.s02, end = Sizes.s04, bottom = Sizes.s04),
        useTopInsets = false,
    ) {
        item {
            WalletLayouts.TopInsetSpacer(
                shouldScrollUnderTopBar = true,
                scaffoldPaddings = LocalScaffoldPaddings.current,
            )
        }

        item { Spacer(modifier = Modifier.height(Sizes.s02)) }

        nonComplianceActor(nonComplianceActorUiState)

        item { Spacer(modifier = Modifier.height(Sizes.s06)) }

        reportSection(
            nonComplianceFormUiState = nonComplianceFormUiState,
            onTextInputField = onTextInputField,
            onClear = onClear,
        )

        item { Spacer(modifier = Modifier.height(buttonHeight.value)) }
    }

    HeightReportingLayout(
        modifier = Modifier
            .widthIn(max = Sizes.contentMaxWidth)
            .align(Alignment.BottomCenter)
            .horizontalSafeDrawing()
            .verticalSafeDrawing(),
        onContentHeightMeasured = { measuredHeight ->
            buttonHeight.value = measuredHeight
        }
    ) {
        Buttons.FilledPrimary(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Sizes.s04)
                .padding(bottom = Sizes.s04),
            text = stringResource(R.string.tk_nonCompliance_report_form_send_button),
            enabled = nonComplianceFormUiState.isFormValid(),
            onClick = onSend,
        )
    }

    LoadingOverlay(isLoading)
}

private fun LazyListScope.nonComplianceActor(
    nonComplianceActorUiState: NonComplianceActorUiState,
) {
    clusterLazyListItem(
        isFirstItem = true,
        isLastItem = true,
    ) {
        Actor(nonComplianceActorUiState)
    }

    item { Spacer(modifier = Modifier.height(Sizes.s02)) }

    item {
        WalletTexts.LabelMedium(
            modifier = Modifier.padding(horizontal = Sizes.s04),
            text = stringResource(R.string.tk_nonCompliance_report_form_actor_footer),
        )
    }
}

@Composable
private fun Actor(nonComplianceActorUiState: NonComplianceActorUiState) {
    val issuerIcon = nonComplianceActorUiState.logo ?: painterResource(id = R.drawable.wallet_ic_actor_default)
    ListItem(
        colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
        headlineContent = { Text(text = nonComplianceActorUiState.name) },
        leadingContent = {
            Avatar(
                imagePainter = issuerIcon,
                size = AvatarSize.SMALL,
                imageTint = WalletTheme.colorScheme.onSurface,
            )
        },
    )
}

private fun LazyListScope.reportSection(
    nonComplianceFormUiState: NonComplianceFormUiState,
    onTextInputField: (NonComplianceInputFieldType) -> Unit,
    onClear: (NonComplianceInputFieldType) -> Unit,
) {
    item {
        WalletTexts.HeadlineSmallEmphasized(
            modifier = Modifier
                .semantics {
                    heading()
                }
                .padding(horizontal = Sizes.s04),
            text = stringResource(R.string.tk_nonCompliance_report_form_reportSection_title)
        )
        Spacer(modifier = Modifier.height(Sizes.s02))
    }
    textInput(
        nonComplianceInputFieldState = nonComplianceFormUiState.descriptionInputFieldState,
        nonComplianceInputFieldType = NonComplianceInputFieldType.DESCRIPTION,
        isFirstItem = true,
        isLastItem = false,
        title = R.string.tk_nonCompliance_report_form_description_title,
        titleAltText = R.string.tk_nonCompliance_report_form_description_title_alt,
        placeholder = R.string.tk_nonCompliance_report_form_description_placeholder,
        supportingText = when (nonComplianceFormUiState.isDescriptionValid) {
            NonComplianceValidationState.Valid -> R.string.tk_nonCompliance_report_form_description_footer
            NonComplianceValidationState.TooShort -> R.string.tk_nonCompliance_report_form_description_footer
            NonComplianceValidationState.TooLong -> R.string.tk_nonCompliance_report_form_description_maxCharacter_footer
        },
        isInputValid = nonComplianceFormUiState.isDescriptionValid is NonComplianceValidationState.Valid,
        textInputValue = nonComplianceFormUiState.description,
        maxInputLength = nonComplianceFormUiState.descriptionMaxInputLength,
        onTextInputField = onTextInputField,
        onClear = onClear,
    )
    textInput(
        nonComplianceInputFieldState = nonComplianceFormUiState.emailInputFieldState,
        nonComplianceInputFieldType = NonComplianceInputFieldType.EMAIL,
        isFirstItem = false,
        isLastItem = true,
        title = R.string.tk_nonCompliance_report_form_contact_title,
        placeholder = R.string.tk_nonCompliance_report_form_contact_placeholder,
        supportingText = if (nonComplianceFormUiState.isEmailValid is NonComplianceValidationState.Valid) {
            R.string.tk_nonCompliance_report_form_contact_footer
        } else {
            R.string.tk_nonCompliance_report_form_email_error
        },
        isInputValid = nonComplianceFormUiState.isEmailValid is NonComplianceValidationState.Valid,
        textInputValue = nonComplianceFormUiState.email,
        maxInputLength = nonComplianceFormUiState.descriptionMaxInputLength,
        onTextInputField = onTextInputField,
        onClear = onClear,
    )
}

@Suppress("LongParameterList")
private fun LazyListScope.textInput(
    nonComplianceInputFieldState: NonComplianceInputFieldState,
    nonComplianceInputFieldType: NonComplianceInputFieldType,
    isFirstItem: Boolean,
    isLastItem: Boolean,
    @StringRes title: Int,
    @StringRes titleAltText: Int? = null,
    @StringRes placeholder: Int,
    @StringRes supportingText: Int,
    isInputValid: Boolean,
    textInputValue: TextFieldValue,
    maxInputLength: Int,
    onTextInputField: (NonComplianceInputFieldType) -> Unit,
    onClear: (NonComplianceInputFieldType) -> Unit,
) = clusterLazyListItem(
    isFirstItem = isFirstItem,
    isLastItem = isLastItem,
    showDivider = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Sizes.s04, vertical = Sizes.s03)
    ) {
        WalletTexts.TitleMedium(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (titleAltText != null) {
                        val altText = stringResource(titleAltText)
                        Modifier.semantics {
                            contentDescription = altText
                        }
                    } else {
                        Modifier
                    }
                ),
            text = stringResource(title),
        )
        Spacer(modifier = Modifier.height(Sizes.s01))
        NonComplianceTextInputComponentReadOnly(
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = placeholder,
            textFieldValue = textInputValue,
            supportingText = supportingText,
            isError = !isInputValid,
            nonComplianceInputFieldState = nonComplianceInputFieldState,
            nonComplianceInputFieldType = nonComplianceInputFieldType,
            maxInputLength = maxInputLength,
            onClick = onTextInputField,
            onTrailingIcon = { onClear(nonComplianceInputFieldType) }
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun NonComplianceFormScreenPreview() {
    WalletTheme {
        NonComplianceFormScreenContent(
            nonComplianceActorUiState = NonComplianceActorUiState(
                name = "Preview actor",
                logo = null,
            ),
            isLoading = false,
            nonComplianceFormUiState = NonComplianceFormUiState(
                description = TextFieldValue(""),
                email = TextFieldValue(""),
                isDescriptionValid = NonComplianceValidationState.Valid,
                isEmailValid = NonComplianceValidationState.Valid,
                descriptionMaxInputLength = 500,
                descriptionInputFieldState = NonComplianceInputFieldState.Edited,
                emailInputFieldState = NonComplianceInputFieldState.Edited,
            ),
            onTextInputField = {},
            onClear = {},
            onSend = {},
        )
    }
}
