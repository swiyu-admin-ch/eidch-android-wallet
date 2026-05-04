package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.avwrapper.DocumentScanPackageResult
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestFiles
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner.EIdDocumentScanSummaryUiState
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFileCategory
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AutoVerificationResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetDocumentScanResult
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetStartAutoVerificationResult
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = EIdDocumentScanSummaryViewModel.Factory::class)
class EIdDocumentScanSummaryViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    private val getDocumentScanResult: GetDocumentScanResult,
    private val getStartAutoVerificationResult: GetStartAutoVerificationResult,
    private val saveEIdRequestFiles: SaveEIdRequestFiles,
    private val getDocumentType: GetDocumentType,
    setTopBarState: SetTopBarState,
    @Assisted private val caseId: String,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(
            caseId: String,
        ): EIdDocumentScanSummaryViewModel
    }

    override val topBarState = TopBarState.WithCloseButton(
        onClose = ::onClose,
        titleId = R.string.tk_eidRequest_scanDocumentSubmit_title
    )

    private val logTitle = "Document Scan Summary:"

    private val _uiState = MutableStateFlow<EIdDocumentScanSummaryUiState>(EIdDocumentScanSummaryUiState.Initial)
    val uiState = _uiState.asStateFlow()

    init {
        initScanResult()
    }

    fun onClose() {
        navManager.navigateBackToHomeScreen(Destination.EIdGuardianshipScreen::class)
    }

    fun onContinue() = viewModelScope.launch {
        val scanResult = getDocumentScanResult().value

        if (scanResult == null) {
            _uiState.update { EIdDocumentScanSummaryUiState.Error }
            return@launch
        }

        val startAutoVerificationResult = getStartAutoVerificationResult().value

        when {
            isFirstDocScan() -> {
                navManager.replaceCurrentWith(
                    Destination.MrzSubmissionScreen(mrzLines = scanResult.mrzValues)
                )
            }

            startAutoVerificationResult != null -> handleAvSessionNextStep(scanResult, startAutoVerificationResult)
            else -> {
                Timber.w("$logTitle Error - no start auto verification result")
            }
        }
    }

    fun onRetry() {
        navManager.replaceCurrentWith(Destination.EIdDocumentScannerScreen(caseId = caseId))
    }

    fun onHelp() = appContext.openLink(appContext.getString(R.string.tk_error_generic_helpLink_value))

    private fun initScanResult() {
        val scanResult = getDocumentScanResult().value

        if (scanResult == null || scanResult.files?.value?.isEmpty() == true) {
            _uiState.update { EIdDocumentScanSummaryUiState.Error }
            return
        }

        val frontsideImage = scanResult.files!!.value.firstOrNull {
            it.fileDescription == FIRST_PAGE
        }

        val backsideImage = scanResult.files!!.value.firstOrNull {
            it.fileDescription == SECOND_PAGE
        }

        if (frontsideImage == null || backsideImage == null) {
            _uiState.update { EIdDocumentScanSummaryUiState.Error }
            return
        }

        val documentType = getDocumentType().value

        _uiState.update {
            EIdDocumentScanSummaryUiState.Ready(
                documentType = documentType,
                frontsideImage = frontsideImage.fileData,
                backsideImage = backsideImage.fileData,
            )
        }
    }

    private fun isFirstDocScan(): Boolean = caseId.isBlank()

    private suspend fun handleAvSessionNextStep(
        packageResult: DocumentScanPackageResult,
        autoVerificationResponse: AutoVerificationResponse,
    ) {
        saveEIdRequestFiles(
            sIdCaseId = caseId,
            filesDataList = packageResult.files,
            filesCategory = EIdRequestFileCategory.DOCUMENT_SCAN,
        )
        when {
            autoVerificationResponse.recordDocumentVideo -> navManager.replaceCurrentWith(
                Destination.EIdDocumentRecordingInfoScreen(caseId = caseId)
            )

            else -> navManager.replaceCurrentWith(
                Destination.EIdStartSelfieVideoScreen(caseId = caseId)
            )
        }
    }

    companion object {
        private const val FIRST_PAGE = "fullFrameFirstPage.png"
        private const val SECOND_PAGE = "fullFrameSecondPage.png"
    }
}
