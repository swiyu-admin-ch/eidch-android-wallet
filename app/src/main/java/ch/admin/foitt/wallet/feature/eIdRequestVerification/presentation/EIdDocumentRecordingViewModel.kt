package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamInitConfig
import ch.admin.foitt.avwrapper.AVBeamPackageResult
import ch.admin.foitt.avwrapper.AVBeamStatus
import ch.admin.foitt.avwrapper.AvBeamNotification
import ch.admin.foitt.avwrapper.config.AVBeamConfigLogLevel
import ch.admin.foitt.avwrapper.config.AVBeamRecordDocumentConfig
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.toTextRes
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetEIdRequestCase
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestFiles
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.SDKInfoState
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFileCategory
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.IdentityType
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = EIdDocumentRecordingViewModel.Factory::class)
class EIdDocumentRecordingViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    private val avBeam: AVBeam,
    private val saveEIdRequestFiles: SaveEIdRequestFiles,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val getEIdRequestCase: GetEIdRequestCase,
    @Assisted private val caseId: String,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdDocumentRecordingViewModel
    }

    override val topBarState = TopBarState.None

    private val _changeToBackCard = MutableStateFlow(false)
    val changeToBackCard = _changeToBackCard.asStateFlow()

    private val _infoState = MutableStateFlow<SDKInfoState>(SDKInfoState.Empty)
    val infoState = _infoState.asStateFlow()

    private val _infoText = MutableStateFlow<Int?>(null)
    val infoText = _infoText.asStateFlow()

    private var viewWidth = 0
    private var viewHeight = 0
    private var recordingTimerJob: Job? = null

    private val isScannerLoading = MutableStateFlow(true)
    private val isRecordLoading = MutableStateFlow(false)

    private val isCameraRunning = MutableStateFlow(false)
    private val isRecording = MutableStateFlow(false)
    private val isViewReady = MutableStateFlow(false)

    private val _documentType = MutableStateFlow(IdentityType.SWISS_IDK)
    val documentType: StateFlow<IdentityType?> = _documentType.asStateFlow()

    private suspend fun fetchDocumentType() {
        getEIdRequestCase(caseId).onSuccess { eIdCase ->
            _documentType.value = eIdCase.selectedDocumentType
        }
    }

    val isLoading = combine(
        isScannerLoading,
        isRecordLoading,
    ) { isScannerLoading, isRecordLoading ->
        isScannerLoading || isRecordLoading
    }.toStateFlow(true)

    private val isReadyToRecord = combine(
        isScannerLoading,
        isViewReady,
        isCameraRunning,
    ) { isScannerLoading, isViewReady, isCameraRunning ->
        !isScannerLoading && isViewReady && isCameraRunning
    }.toStateFlow(false)

    fun onResume() {
        viewModelScope.launch {
            Timber.d("Recording: view resumed")
            fetchDocumentType()
            startRecordingDocument()
        }
    }

    fun onPause() {
        viewModelScope.launch {
            isScannerLoading.awaitValue(false)
            resetRecordingState()
            Timber.d("Recording: view paused")
        }
    }

    override fun onCleared() {
        resetRecordingState()
        super.onCleared()
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
            Timber.d("Recording: initScannerSdk done")
        }.trackCompletion(isScannerLoading)

        setupSdkFlowCollection()
    }

    suspend fun getScannerView(width: Int, height: Int): SurfaceView {
        isViewReady.update { false }
        isScannerLoading.awaitValue(false)
        Timber.d("Recording: getScannerView: $width, $height")
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
                    else -> {}
                }
            }
        }
        launch {
            avBeam.recordDocumentFlow.collect { notification ->
                when (notification) {
                    is AvBeamNotification.Completed -> onRecordingCompleted(notification.packageData)
                    AvBeamNotification.Empty, AvBeamNotification.Initial -> {}
                }
            }
        }
        launch {
            avBeam.errorFlow.collect { errorNotification ->
                Timber.d("Received error: ${errorNotification.name}")
            }
        }
    }

    private fun onRecordingCompleted(packageResult: AVBeamPackageResult) = viewModelScope.launch {
        // The AvBeam sdk stops the recording and camera tasks automatically.
        isRecording.update { false }
        isCameraRunning.update { false }
        resetRecordingState()
        Timber.d(message = "Recording: Completed: ${packageResult.data?.size()}")

        saveEIdRequestFiles(
            sIdCaseId = caseId,
            filesDataList = packageResult.files,
            filesCategory = EIdRequestFileCategory.DOCUMENT_RECORDING,
        )

        navManager.popUpToAndNavigate(
            popToInclusive = Destination.EIdStartAutoVerificationScreen::class,
            destination = Destination.EIdStartSelfieVideoScreen(caseId = caseId)
        )
    }.trackCompletion(isRecordLoading)

    private suspend fun startRecordingDocument() {
        isScannerLoading.awaitValue(false)
        isViewReady.awaitValue(true)
        avBeam.stopCamera()
        avBeam.startCamera()
        isReadyToRecord.awaitValue(true)
        Timber.d(message = "Recording: startRecordingDocument: $viewWidth, $viewHeight")

        val configRecording = AVBeamRecordDocumentConfig(
            docRecVideoLength = VIDEO_LENGTH_SECONDS,
            rectWidth = viewWidth,
            rectHeight = viewHeight,
        )
        isRecording.update { isRecording ->
            if (!isRecording) {
                avBeam.startRecordingDocument(
                    config = configRecording,
                )
                recordingTimerJob = startRecordingTimer()
            }
            true
        }
    }

    private fun startRecordingTimer(): Job = viewModelScope.launch {
        _changeToBackCard.value = false
        delay(VIDEO_LENGTH_SECONDS * 1000 / 2L)
        _changeToBackCard.value = true
    }

    private fun resetRecordingState() {
        isRecording.update { isRecording ->
            if (isRecording) {
                avBeam.stopRecordingDocument()
            }
            recordingTimerJob?.cancel()
            recordingTimerJob = null
            false
        }

        isCameraRunning.update { isCameraRunning ->
            if (isCameraRunning) {
                avBeam.stopCamera()
            }
            false
        }
        isViewReady.update { false }
    }

    fun onBack() {
        resetRecordingState()
        navManager.popBackStack()
    }

    private suspend inline fun <T> StateFlow<T>.awaitValue(value: T) =
        this.first { it == value }

    companion object {
        private const val VIDEO_LENGTH_SECONDS = 10
    }
}
