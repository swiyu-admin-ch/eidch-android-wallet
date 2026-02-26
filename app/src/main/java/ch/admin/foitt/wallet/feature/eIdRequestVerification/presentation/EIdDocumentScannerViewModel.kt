package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamInitConfig
import ch.admin.foitt.avwrapper.AVBeamStatus
import ch.admin.foitt.avwrapper.AvBeamNotification
import ch.admin.foitt.avwrapper.DocumentScanPackageResult
import ch.admin.foitt.avwrapper.config.AVBeamConfigLogLevel
import ch.admin.foitt.avwrapper.config.AVBeamScanDocumentConfig
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.toTextRes
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.AreEIdDocumentsEqual
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestFiles
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButtonState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.SDKInfoState
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFileCategory
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AutoVerificationResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.DocumentScannerErrorType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetStartAutoVerificationResult
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetDocumentScanResult
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = EIdDocumentScannerViewModel.Factory::class)
class EIdDocumentScannerViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    private val avBeam: AVBeam,
    private val setDocumentScanResult: SetDocumentScanResult,
    private val getStartAutoVerificationResult: GetStartAutoVerificationResult,
    private val saveEIdRequestFiles: SaveEIdRequestFiles,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val areEIdDocumentsEqual: AreEIdDocumentsEqual,
    getDocumentType: GetDocumentType,
    @Assisted private val caseId: String,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdDocumentScannerViewModel
    }

    override val topBarState = TopBarState.None

    private val logTitle = "Document Scan:"

    private val _infoState = MutableStateFlow<SDKInfoState>(SDKInfoState.Loading)
    val infoState = _infoState.asStateFlow()

    private val _infoText = MutableStateFlow<Int?>(null)
    val infoText = _infoText.asStateFlow()

    private val _changeToBackCard = MutableStateFlow(false)
    val changeToBackCard = _changeToBackCard.asStateFlow()

    private var viewWidth = 0
    private var viewHeight = 0

    private val isScannerLoading = MutableStateFlow(true)
    private val isScanDocumentLoading = MutableStateFlow(false)
    private val isCameraRunning = MutableStateFlow(false)
    private val isScanning = MutableStateFlow(false)
    private val isViewReady = MutableStateFlow(false)
    private val isScanningCompleted = MutableStateFlow(false)
    private val isCameraLoading = MutableStateFlow(false)

    val scannerButtonState: StateFlow<ScannerButtonState> = combine(
        isScanning,
        isScanningCompleted,
    ) { isScanning, isScanningCompleted ->
        when {
            isScanningCompleted -> ScannerButtonState.Done
            isScanning -> ScannerButtonState.Scanning
            else -> ScannerButtonState.Ready
        }
    }.toStateFlow(ScannerButtonState.Ready)

    val documentType = getDocumentType().value

    val isLoading = combine(
        isScannerLoading,
        isScanDocumentLoading,
        infoState,
        isCameraLoading,
        isScanningCompleted,
    ) { isScannerLoading, isScanDocumentLoading, infoState, isCameraLoading, isScanningCompleted ->
        when {
            isScanningCompleted -> false
            else -> isScannerLoading || isScanDocumentLoading || infoState == SDKInfoState.Loading || isCameraLoading
        }
    }.toStateFlow(false)

    fun onResume() {
        viewModelScope.launch {
            Timber.d("$logTitle view resumed")
            isScanningCompleted.update { false }
            prepareScanner()
        }.trackCompletion(isCameraLoading)
    }

    fun onPause() {
        viewModelScope.launch {
            isScannerLoading.awaitValue(false)
            resetScanningState()
            Timber.d("$logTitle  view paused")
        }
    }

    fun initScannerSdk(activity: AppCompatActivity) = viewModelScope.launch {
        launch(Dispatchers.IO) {
            val logLevel = if (environmentSetupRepository.avBeamLoggingEnabled) {
                AVBeamConfigLogLevel.DEBUG
            } else {
                AVBeamConfigLogLevel.NONE
            }
            avBeam.init(AVBeamInitConfig(logLevel), activity)
            avBeam.initializedFlow.awaitValue(true)
            Timber.d("$logTitle initScannerSdk done")
        }.trackCompletion(isScannerLoading)

        setupSdkFlowCollection()
    }

    suspend fun getScannerView(width: Int, height: Int): SurfaceView {
        isViewReady.update { false }
        isScannerLoading.awaitValue(false)
        Timber.d("$logTitle getScannerView: $width, $height")
        return avBeam.getGLView(width, height)
    }

    fun onAfterViewLayout(witdh: Int, height: Int) {
        viewWidth = witdh
        viewHeight = height
        isViewReady.update { true }
        _infoState.update { SDKInfoState.Ready }
    }

    private suspend fun CoroutineScope.setupSdkFlowCollection() {
        isScannerLoading.awaitValue(false)
        launch {
            avBeam.statusFlow.collect { status ->
                _infoState.update { SDKInfoState.InfoData }
                _infoText.update { status.toTextRes() }
                when (status) {
                    AVBeamStatus.StreamingStarted -> isCameraRunning.update { true }
                    AVBeamStatus.IdNeedSecondPageForMatching -> _changeToBackCard.update { true }
                    else -> {}
                }
            }
        }
        launch {
            avBeam.scanDocumentFlow.collect { notification ->
                when (notification) {
                    is AvBeamNotification.DocumentScanCompleted -> onScanningCompleted(notification.packageData)
                    AvBeamNotification.Empty, AvBeamNotification.Initial -> {
                        _infoState.update { SDKInfoState.Empty }
                    }

                    is AvBeamNotification.Error -> {
                        val packageResult = notification.packageData
                        Timber.d("$logTitle Empty package - ${packageResult?.errorType} type")
                        _infoText.value = R.string.avbeam_error_empty_package
                    }

                    AvBeamNotification.Loading -> {
                        _infoState.update { SDKInfoState.Loading }
                    }
                }
            }
        }
        launch {
            avBeam.errorFlow.collect { errorNotification ->
                Timber.d("$logTitle Received error: ${errorNotification.name}")
            }
        }
    }

    fun onUp() {
        resetScanningState()
        avBeam.shutDown()
        navManager.popBackStack()
    }

    fun onToggleScan() {
        viewModelScope.launch {
            when (scannerButtonState.value) {
                ScannerButtonState.Ready -> startScanning()
                ScannerButtonState.Scanning -> stopScanning()
                ScannerButtonState.Done -> {}
            }
        }
    }

    private suspend fun prepareScanner() {
        isScannerLoading.awaitValue(false)
        isViewReady.awaitValue(true)
        avBeam.stopCamera()
        avBeam.startCamera()
    }

    private suspend fun startScanning() {
        isCameraLoading.awaitValue(false)
        Timber.d(message = "$logTitle startScanningDocument: $viewWidth, $viewHeight")

        val configScanning = AVBeamScanDocumentConfig(
            rectWidth = viewWidth,
            rectHeight = viewHeight,
        )
        isScanning.update { isScanning ->
            if (!isScanning) {
                avBeam.startScanDocument(
                    config = configScanning
                )
            }
            true
        }
    }

    private fun onScanningCompleted(packageResult: DocumentScanPackageResult) = viewModelScope.launch {
        // The AvBeam sdk stops the scanning tasks automatically.
        isScanningCompleted.update { true }
        isScanning.update { false }
        Timber.d(message = "$logTitle Completed")

        val startAutoVerificationResult = getStartAutoVerificationResult().value

        areEIdDocumentsEqual(caseId, packageResult.mrzValues.toTypedArray())
            .onFailure { error ->
                Timber.d("$logTitle Received error: $error")
                navManager.replaceCurrentWith(Destination.EIdDocumentScannerErrorScreen(type = DocumentScannerErrorType.GENERIC))
                return@launch
            }
            .onSuccess { areDocumentsEqual ->
                if (!areDocumentsEqual) {
                    navManager.replaceCurrentWith(
                        Destination.EIdDocumentScannerErrorScreen(
                            type = DocumentScannerErrorType.UNEQUAL_DOCUMENTS
                        )
                    )
                    return@launch
                }
            }
        when {
            isFirstDocScan() -> {
                setDocumentScanResult(documentScanResult = packageResult)
                delay(NAVIGATION_DELAY)
                avBeam.shutDown()
                navManager.replaceCurrentWith(
                    Destination.MrzSubmissionScreen(mrzLines = packageResult.mrzValues)
                )
            }

            startAutoVerificationResult != null -> handleAvSessionNextStep(packageResult, startAutoVerificationResult)
            else -> {
                Timber.d("$logTitle Error - no start auto verification result")
            }
        }
        resetScanningState()
    }.trackCompletion(isScanDocumentLoading)

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
                Destination.EIdDocumentRecordingScreen(caseId = caseId)
            )

            else -> navManager.replaceCurrentWith(
                Destination.EIdStartSelfieVideoScreen(caseId = caseId)
            )
        }
    }

    private fun resetScanningState() {
        stopScanning()
        stopCamera()
        isViewReady.update { false }
        _infoState.update { SDKInfoState.Empty }
    }

    private fun stopScanning() = isScanning.update { isScanning ->
        if (isScanning) {
            avBeam.stopScanDocument()
        }
        _changeToBackCard.update { false }
        false
    }

    private fun stopCamera() = isCameraRunning.update { isCameraRunning ->
        if (isCameraRunning) {
            avBeam.stopCamera()
        }
        false
    }

    private suspend inline fun <T> StateFlow<T>.awaitValue(value: T) =
        this.first { it == value }

    override fun onCleared() {
        resetScanningState()
        super.onCleared()
    }

    companion object {
        private const val NAVIGATION_DELAY = 1000L
    }
}
