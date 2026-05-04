package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.content.Context
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamError
import ch.admin.foitt.avwrapper.AVBeamInitConfig
import ch.admin.foitt.avwrapper.AVBeamPackageResult
import ch.admin.foitt.avwrapper.AVBeamStatus
import ch.admin.foitt.avwrapper.AvBeamNotification
import ch.admin.foitt.avwrapper.config.AVBeamConfigLogLevel
import ch.admin.foitt.avwrapper.config.AVBeamRecordDocumentConfig
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.toTextRes
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetEIdRequestCase
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestFiles
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButtonState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentRecording.EIdDocumentRecordingUiState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.SDKInfoState
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFileCategory
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.DocumentScannerErrorType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.IdentityType
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import com.github.michaelbull.result.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = EIdDocumentRecordingViewModel.Factory::class)
class EIdDocumentRecordingViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    private val avBeam: AVBeam,
    private val saveEIdRequestFiles: SaveEIdRequestFiles,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val getEIdRequestCase: GetEIdRequestCase,
    @Assisted private val caseId: String,
    private val setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdDocumentRecordingViewModel
    }

    override val topBarState: TopBarState
        get() {
            return when (val state = uiState.value) {
                EIdDocumentRecordingUiState.Initializing -> TopBarState.Empty
                is EIdDocumentRecordingUiState.Error ->
                    TopBarState.DetailsWithCloseButton(
                        titleId = R.string.tk_eidRequest_recordDocument_recto,
                        onUp = ::onUp,
                        onClose = ::onClose,
                    )
                is EIdDocumentRecordingUiState.Recording -> {
                    val titleId = if (state.showSecondSide) {
                        R.string.tk_eidRequest_recordDocument_verso
                    } else {
                        R.string.tk_eidRequest_recordDocument_recto
                    }
                    TopBarState.DetailsWithCloseRoundButtons(
                        titleId = titleId,
                        onUp = ::onUp,
                        onClose = ::onClose,
                    )
                }
            }
        }

    private val logTitle = "Document Recording:"

    private val _uiState = MutableStateFlow<EIdDocumentRecordingUiState>(EIdDocumentRecordingUiState.Initializing)
    val uiState: StateFlow<EIdDocumentRecordingUiState> = _uiState.asStateFlow()

    private var viewWidth = 0
    private var viewHeight = 0
    private var recordingTimerJob: Job? = null
    private val isCameraRunning = MutableStateFlow(false)
    private val isViewReady = MutableStateFlow(false)

    private val _documentType = MutableStateFlow(IdentityType.SWISS_IDK)
    val documentType: StateFlow<IdentityType?> = _documentType.asStateFlow()

    private suspend fun fetchDocumentType() {
        getEIdRequestCase(caseId).onSuccess { eIdCase ->
            _documentType.value = eIdCase.selectedDocumentType
        }
    }

    val isLoading = combine(
        uiState,
        isViewReady,
    ) { state, isViewReady ->
        when (state) {
            EIdDocumentRecordingUiState.Initializing -> true
            is EIdDocumentRecordingUiState.Recording -> !isViewReady
            is EIdDocumentRecordingUiState.Error -> false
        }
    }.toStateFlow(true)

    val shouldLock: StateFlow<Boolean> = uiState.map { state ->
        state is EIdDocumentRecordingUiState.Initializing || (
            state is EIdDocumentRecordingUiState.Recording && state.scannerButtonState == ScannerButtonState.Scanning
            )
    }.toStateFlow(false)

    init {
        viewModelScope.launch {
            uiState.collectLatest {
                setTopBarState(topBarState)
            }
        }
    }

    fun onResume() {
        viewModelScope.launch {
            Timber.d("$logTitle view resumed")
            fetchDocumentType()
            prepareScanner()
        }
    }

    fun onPause() {
        viewModelScope.launch {
            resetRecordingState()
            Timber.d("$logTitle view paused")
        }
    }

    override fun onCleared() {
        resetRecordingState()
        super.onCleared()
    }

    fun onUp() {
        resetRecordingState()
        // Shutdown because up navigation might not guarantee shutdown
        avBeam.shutDown()
        navManager.popBackStack()
    }

    fun onClose() {
        avBeam.shutDown()
        navManager.navigateBackToHomeScreen(Destination.EIdStartAvSessionScreen::class)
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
            _uiState.update {
                EIdDocumentRecordingUiState.Recording(
                    infoState = SDKInfoState.Loading,
                    infoText = null,
                    showSecondSide = false,
                    scannerButtonState = ScannerButtonState.Ready,
                )
            }
        }

        setupSdkFlowCollection()
    }

    suspend fun getScannerView(width: Int, height: Int): SurfaceView {
        isViewReady.update { false }
        Timber.d("$logTitle getScannerView: $width, $height")
        return avBeam.getGLView(width, height)
    }

    fun onAfterViewLayout(witdh: Int, height: Int) {
        viewWidth = witdh
        viewHeight = height
        isViewReady.update { true }
        _uiState.update {
            EIdDocumentRecordingUiState.Recording(
                infoState = SDKInfoState.Ready,
                infoText = null,
                showSecondSide = false,
                scannerButtonState = ScannerButtonState.Ready,
            )
        }
    }

    fun onToggleScan() {
        viewModelScope.launch {
            val currentState = _uiState.value as? EIdDocumentRecordingUiState.Recording ?: return@launch
            when (currentState.scannerButtonState) {
                ScannerButtonState.Ready -> startRecordingDocument()
                ScannerButtonState.Scanning -> stopRecordingDocument()
                ScannerButtonState.Done -> {}
            }
        }
    }

    private fun CoroutineScope.setupSdkFlowCollection() {
        launch {
            avBeam.statusFlow.collect { status ->
                _uiState.updateRecordingState {
                    copy(
                        infoState = SDKInfoState.InfoData,
                        infoText = status.toTextRes(),
                    )
                }
                if (status == AVBeamStatus.StreamingStarted) {
                    isCameraRunning.update { true }
                }
            }
        }
        launch {
            avBeam.recordDocumentFlow.collect { notification ->
                when (notification) {
                    is AvBeamNotification.Completed -> onRecordingCompleted(notification.packageData)
                    AvBeamNotification.Empty, AvBeamNotification.Initial -> {
                        _uiState.updateRecordingState { copy(infoState = SDKInfoState.Empty) }
                    }
                }
            }
        }
        launch {
            avBeam.errorFlow.collect { errorNotification ->
                if (errorNotification != AVBeamError.None) {
                    Timber.e(message = "$logTitle Error - avBeam errorFlow: ${errorNotification.name}")
                    _uiState.update {
                        EIdDocumentRecordingUiState.Error(
                            type = DocumentScannerErrorType.GENERIC,
                            onRetry = ::onRetry,
                            onHelp = ::onHelp,
                        )
                    }
                }
            }
        }
    }

    private fun onRecordingCompleted(packageResult: AVBeamPackageResult) = viewModelScope.launch {
        // The AvBeam sdk stops the recording and camera tasks automatically.
        _uiState.updateRecordingState {
            copy(scannerButtonState = ScannerButtonState.Done)
        }
        delay(NAVIGATION_DELAY)
        resetRecordingState()
        Timber.d(message = "$logTitle Completed: ${packageResult.data?.size()}")

        saveEIdRequestFiles(
            sIdCaseId = caseId,
            filesDataList = packageResult.files,
            filesCategory = EIdRequestFileCategory.DOCUMENT_RECORDING,
        )

        navManager.popUpToAndNavigate(
            popToInclusive = Destination.EIdStartAutoVerificationScreen::class,
            destination = Destination.EIdStartSelfieVideoScreen(caseId = caseId)
        )
    }

    private suspend fun prepareScanner() {
        isViewReady.awaitValue(true)
        avBeam.stopCamera()
        avBeam.startCamera()
    }

    private suspend fun startRecordingDocument() {
        Timber.d(message = "$logTitle startRecordingDocument: $viewWidth, $viewHeight")

        if (!isCameraRunning.value) {
            prepareScanner()
        }
        isCameraRunning.awaitValue(true)

        val configRecording = AVBeamRecordDocumentConfig(
            rectWidth = viewWidth,
            rectHeight = viewHeight,
            docRecVideoLength = VIDEO_LENGTH_SECONDS,
        )

        val currentState = _uiState.value as? EIdDocumentRecordingUiState.Recording
        if (currentState?.scannerButtonState != ScannerButtonState.Scanning) {
            avBeam.startRecordingDocument(configRecording)
            recordingTimerJob = startRecordingTimer()
        }

        _uiState.updateRecordingState {
            copy(
                scannerButtonState = ScannerButtonState.Scanning,
                showSecondSide = false,
            )
        }
    }

    private fun startRecordingTimer(): Job = viewModelScope.launch {
        delay(VIDEO_LENGTH_SECONDS * 1000 / 2L)
        _uiState.updateRecordingState {
            copy(showSecondSide = true)
        }
    }

    private fun stopRecordingDocument() {
        val currentState = _uiState.value as? EIdDocumentRecordingUiState.Recording
        if (currentState?.scannerButtonState == ScannerButtonState.Scanning) {
            avBeam.stopRecordingDocument()
            viewModelScope.launch {
                prepareScanner()
            }
        }
        recordingTimerJob?.cancel()
        recordingTimerJob = null

        _uiState.updateRecordingState {
            copy(
                scannerButtonState = ScannerButtonState.Ready,
                showSecondSide = false,
            )
        }
    }

    private fun resetRecordingState() {
        stopRecordingDocument()
        avBeam.stopCamera()

        isCameraRunning.update { false }
        isViewReady.update { false }
    }

    private fun onRetry() {
        viewModelScope.launch {
            Timber.w("$logTitle - retry")
            resetRecordingState()
            _uiState.update { EIdDocumentRecordingUiState.Initializing }
            prepareScanner()
        }
    }

    private fun onHelp() = appContext.openLink(appContext.getString(R.string.tk_error_generic_helpLink_value))

    private fun MutableStateFlow<EIdDocumentRecordingUiState>.updateRecordingState(
        update: EIdDocumentRecordingUiState.Recording.() -> EIdDocumentRecordingUiState.Recording
    ) {
        update { currentState ->
            if (currentState is EIdDocumentRecordingUiState.Recording) {
                currentState.update()
            } else {
                currentState
            }
        }
    }

    private suspend inline fun <T> StateFlow<T>.awaitValue(value: T) =
        this.first { it == value }

    companion object {
        private const val VIDEO_LENGTH_SECONDS = 10
        private const val NAVIGATION_DELAY = 1000L
    }
}
