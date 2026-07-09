package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.content.Context
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamError
import ch.admin.foitt.avwrapper.AVBeamPackageResult
import ch.admin.foitt.avwrapper.AvBeamNotification
import ch.admin.foitt.avwrapper.config.AVBeamRecordDocumentConfig
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.toTextRes
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetEIdRequestCase
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestFiles
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButtonState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentRecording.EIdDocumentRecordingStatus
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentRecording.EIdDocumentRecordingUiState
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.OnPermissionResult
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.PermissionState
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.infra.PermissionStateHandler
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFileCategory
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.DocumentScannerErrorType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdUiDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toEIdDocumentType
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.scanning.di.AvBeamSdkEntryPoint
import ch.admin.foitt.wallet.platform.utils.launchTimer
import ch.admin.foitt.wallet.platform.utils.openLink
import com.github.michaelbull.result.mapOrElse
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = EIdDocumentRecordingViewModel.Factory::class)
class EIdDocumentRecordingViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    private val saveEIdRequestFiles: SaveEIdRequestFiles,
    getEIdRequestCase: GetEIdRequestCase,
    private val permissionStateHandler: PermissionStateHandler,
    destinationScopedComponentManager: DestinationScopedComponentManager,
    @Assisted private val caseId: String,
    private val setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdDocumentRecordingViewModel
    }

    private val avBeamRepository = destinationScopedComponentManager.getEntryPoint(
        entryPointClass = AvBeamSdkEntryPoint::class.java,
        componentScope = ComponentScope.AvBeamSdkSession,
    ).avBeamRepository()

    private val avBeam: AVBeam get() = avBeamRepository.getBeam()

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
                    val titleId = if (state.status is EIdDocumentRecordingStatus.BackSideScanning) {
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

    val permissionState get() = permissionStateHandler.permissionState

    val onPermissionResult =
        OnPermissionResult { permissionGranted, shouldShowRationale, isActivePrompt ->
            viewModelScope.launch {
                permissionStateHandler.updateState(
                    hasPermission = permissionGranted,
                    shouldShowRationale = shouldShowRationale,
                    isActivePrompt = isActivePrompt,
                )
            }
        }

    private val errorState = MutableStateFlow<DocumentScannerErrorType>(DocumentScannerErrorType.None)

    private val eIdDocumentRecordingStatus =
        MutableStateFlow<EIdDocumentRecordingStatus>(EIdDocumentRecordingStatus.Initializing)

    private var viewWidth = 0
    private var viewHeight = 0
    private var recordingProgressJob: Job? = null
    private val isViewReady = MutableStateFlow(false)

    val uiState: StateFlow<EIdDocumentRecordingUiState> = combine(
        errorState,
        permissionState,
        avBeam.initializedFlow,
        eIdDocumentRecordingStatus,
        avBeam.statusFlow,
    ) { errorState, permissionState, initialized, eIdDocumentRecordingStatus, avBeamStatus ->
        when {
            errorState != DocumentScannerErrorType.None -> EIdDocumentRecordingUiState.Error(
                type = errorState,
                onRetry = ::onRetry,
                onHelp = ::onHelp,
            )
            permissionState !is PermissionState.Granted -> EIdDocumentRecordingUiState.Initializing
            !initialized -> EIdDocumentRecordingUiState.Initializing
            else -> {
                EIdDocumentRecordingUiState.Recording(
                    infoText = avBeamStatus.toTextRes(),
                    status = eIdDocumentRecordingStatus,
                )
            }
        }
    }.toStateFlow(EIdDocumentRecordingUiState.Initializing)

    val documentType: StateFlow<EIdUiDocumentType> = flow {
        val documentType = getEIdRequestCase(caseId).mapOrElse(
            default = { EIdUiDocumentType.IDENTITY_CARD },
            transform = { it.selectedDocumentType.toEIdDocumentType() }
        )
        emit(documentType)
    }.toStateFlow(EIdUiDocumentType.IDENTITY_CARD)

    val shouldLock: StateFlow<Boolean> = uiState.map { state ->
        state is EIdDocumentRecordingUiState.Initializing && permissionState.value is PermissionState.Granted ||
            (state is EIdDocumentRecordingUiState.Recording && state.status.isRecording)
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
        navManager.popBackStack()
    }

    fun onClose() {
        navManager.navigateBackToHomeScreen(Destination.EIdStartAvSessionScreen::class)
    }

    fun initScannerSdk(activity: AppCompatActivity) = viewModelScope.launch {
        launch {
            eIdDocumentRecordingStatus.update { EIdDocumentRecordingStatus.Initializing }
            avBeamRepository.init(activity)
            avBeam.initializedFlow.awaitValue(true)
            Timber.d("$logTitle initScannerSdk done")
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
        eIdDocumentRecordingStatus.update { EIdDocumentRecordingStatus.FrontSide }
    }

    private fun CoroutineScope.setupSdkFlowCollection() {
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
                if (errorNotification != AVBeamError.None) {
                    Timber.e(message = "$logTitle Error - avBeam errorFlow: ${errorNotification.name}")
                    errorState.update { DocumentScannerErrorType.Generic }
                }
            }
        }
    }

    fun onToggleScan() {
        viewModelScope.launch {
            when (eIdDocumentRecordingStatus.value) {
                EIdDocumentRecordingStatus.FrontSide -> startRecordingDocument()

                is EIdDocumentRecordingStatus.FrontSideScanning,
                is EIdDocumentRecordingStatus.BackSideScanning -> stopRecordingDocument()

                EIdDocumentRecordingStatus.Initializing,
                EIdDocumentRecordingStatus.Finished -> {}
            }
        }
    }

    private fun onRecordingCompleted(packageResult: AVBeamPackageResult) = viewModelScope.launch {
        // The AvBeam sdk stops the recording and camera tasks automatically.
        eIdDocumentRecordingStatus.update { EIdDocumentRecordingStatus.Finished }

        delay(NAVIGATION_DELAY)

        avBeam.stopRecordingDocument()
        recordingProgressJob?.cancel()
        recordingProgressJob = null
        avBeam.stopCamera()
        isViewReady.update { false }

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

        val configRecording = AVBeamRecordDocumentConfig(
            rectWidth = viewWidth,
            rectHeight = viewHeight,
            docRecVideoLength = VIDEO_LENGTH_SECONDS,
        )

        if (eIdDocumentRecordingStatus.value.toScannerButtonState() !is ScannerButtonState.Scanning) {
            avBeam.startRecordingDocument(configRecording)
            recordingProgressJob = startRecordingTimer()
        }

        eIdDocumentRecordingStatus.update { EIdDocumentRecordingStatus.FrontSideScanning(0f) }
    }

    private fun startRecordingTimer(): Job = viewModelScope.launchTimer(VIDEO_LENGTH_MILLIS) { progressRatio ->
        if (
            eIdDocumentRecordingStatus.value is EIdDocumentRecordingStatus.FrontSideScanning ||
            eIdDocumentRecordingStatus.value is EIdDocumentRecordingStatus.BackSideScanning
        ) {
            if (progressRatio > 0.5f) {
                eIdDocumentRecordingStatus.update { EIdDocumentRecordingStatus.BackSideScanning(progressRatio) }
            } else {
                eIdDocumentRecordingStatus.update { EIdDocumentRecordingStatus.FrontSideScanning(progressRatio) }
            }
        }
    }

    private fun stopRecordingDocument() {
        if (eIdDocumentRecordingStatus.value.isRecording) {
            avBeam.stopRecordingDocument()
            viewModelScope.launch {
                prepareScanner()
            }
        }
        recordingProgressJob?.cancel()
        recordingProgressJob = null

        eIdDocumentRecordingStatus.update { EIdDocumentRecordingStatus.FrontSide }
    }

    private fun resetRecordingState() {
        stopRecordingDocument()
        avBeam.stopCamera()

        isViewReady.update { false }
    }

    private fun onRetry() {
        viewModelScope.launch {
            Timber.w("$logTitle - retry")
            resetRecordingState()
            errorState.update { DocumentScannerErrorType.None }
            prepareScanner()
        }
    }

    private fun onHelp() = appContext.openLink(appContext.getString(R.string.tk_error_generic_helpLink_value))

    private suspend inline fun <T> StateFlow<T>.awaitValue(value: T) =
        this.first { it == value }

    companion object {
        const val VIDEO_LENGTH_SECONDS = 10
        private const val VIDEO_LENGTH_MILLIS = VIDEO_LENGTH_SECONDS * 1000L
        private const val NAVIGATION_DELAY = 1000L
    }
}
