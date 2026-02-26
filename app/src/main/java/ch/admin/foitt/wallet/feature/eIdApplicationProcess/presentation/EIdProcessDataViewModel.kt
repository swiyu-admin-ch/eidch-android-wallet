package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.ProcessDataUiState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveMetadataFile
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SubmitCaseId
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.UploadAllFiles
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AvSubmitCaseError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AvUploadFilesError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetStartAutoVerificationResult
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getError
import com.github.michaelbull.result.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EIdProcessDataViewModel.Factory::class)
internal class EIdProcessDataViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    @param:ApplicationContext private val context: Context,
    private val uploadAllFiles: UploadAllFiles,
    private val saveMetadataFile: SaveMetadataFile,
    private val submitCaseId: SubmitCaseId,
    private val getStartAutoVerificationResult: GetStartAutoVerificationResult,
    @Assisted private val caseId: String,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdProcessDataViewModel
    }

    override val topBarState = TopBarState.Empty

    private val isLoading = MutableStateFlow(false)

    private val uploadFilesResult = MutableStateFlow<Result<Unit, AvUploadFilesError>?>(null)
    private val submitCaseIdResult = MutableStateFlow<Result<Unit, AvSubmitCaseError>?>(null)

    val state: StateFlow<ProcessDataUiState> = combine(
        isLoading,
        uploadFilesResult,
        submitCaseIdResult,
    ) { isLoading, uploadFiles, submitCaseId ->
        val uploadError = uploadFiles?.getError()
        val submitError = submitCaseId?.getError()
        when {
            isLoading -> ProcessDataUiState.Loading
            uploadFiles?.isOk == true && submitCaseId?.isOk == true -> ProcessDataUiState.Valid
            submitError is EIdRequestError.NetworkError ||
                uploadError is EIdRequestError.NetworkError ->
                ProcessDataUiState.GenericError(
                    onClose = ::onClose,
                    onRetry = ::onRetry,
                    onHelp = ::onHelp
                )

            submitError is EIdRequestError.DeclinedProcessData ||
                uploadError is EIdRequestError.DeclinedProcessData ->
                ProcessDataUiState.Declined(
                    onClose = ::onClose,
                    onHelp = ::onHelp
                )

            else -> ProcessDataUiState.GenericError(
                onClose = ::onClose,
                onRetry = ::onRetry,
                onHelp = ::onHelp
            )
        }
    }.toStateFlow(ProcessDataUiState.Loading)

    init {
        onRefreshState()
    }

    private fun onRefreshState() {
        if (isLoading.value) {
            return
        }
        viewModelScope.launch {
            val autoVerification = getStartAutoVerificationResult().value ?: return@launch
            val jwt = autoVerification.jwt

            saveMetadataFile(caseId = caseId)

            uploadFilesResult.value = uploadAllFiles(
                caseId = caseId,
                accessToken = jwt
            ).onSuccess {
                submitCaseId(
                    caseId = caseId,
                    accessToken = jwt
                ).also { submitCaseIdResult.value = it }
                    .onSuccess {
                        navManager.navigateTo(Destination.EIdProcessDataConfirmationScreen)
                    }
            }
        }.trackCompletion(isLoading)
    }

    fun onClose() = navManager.navigateBackToHomeScreen(Destination.EIdIntroScreen::class)

    fun onRetry() = onRefreshState()

    fun onHelp() = context.openLink(context.getString(R.string.tk_eidRequest_dataProcess_link_text))
}
