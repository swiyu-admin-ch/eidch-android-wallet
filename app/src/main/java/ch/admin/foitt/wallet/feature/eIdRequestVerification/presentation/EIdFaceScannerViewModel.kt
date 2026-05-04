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
import ch.admin.foitt.avwrapper.config.AVBeamCaptureFaceConfig
import ch.admin.foitt.avwrapper.config.AVBeamConfigLogLevel
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.toTextRes
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestFiles
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.composables.ScannerButtonState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.faceScanner.EIdFaceScannerUiState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.SDKInfoState
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFileCategory
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

@HiltViewModel(assistedFactory = EIdFaceScannerViewModel.Factory::class)
class EIdFaceScannerViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    private val avBeam: AVBeam,
    private val saveEIdRequestFiles: SaveEIdRequestFiles,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    @Assisted private val caseId: String,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdFaceScannerViewModel
    }

    override val topBarState: TopBarState
        get() {
            return when (uiState.value) {
                EIdFaceScannerUiState.Initializing -> TopBarState.Empty
                is EIdFaceScannerUiState.Error ->
                    TopBarState.DetailsWithCloseButton(
                        titleId = null,
                        onUp = ::onUp,
                        onClose = ::onClose,
                    )
                EIdFaceScannerUiState.Finish,
                is EIdFaceScannerUiState.Scan ->
                    TopBarState.DetailsWithCloseRoundButtons(
                        titleId = null,
                        onUp = ::onUp,
                        onClose = ::onClose,
                    )
            }
        }

    private val logTitle = "Face Scan:"

    private val _uiState = MutableStateFlow<EIdFaceScannerUiState>(EIdFaceScannerUiState.Initializing)
    val uiState: StateFlow<EIdFaceScannerUiState> = _uiState.asStateFlow()

    private val isCameraRunning = MutableStateFlow(false)
    private val isViewReady = MutableStateFlow(false)

    private var viewWidth = 0
    private var viewHeight = 0
    private var lastActivityHash: Int? = null
    private var isRotation: Boolean = false

    val isLoading: StateFlow<Boolean> = combine(
        uiState,
        isViewReady,
    ) { state, viewReady ->
        when (state) {
            EIdFaceScannerUiState.Initializing -> true
            is EIdFaceScannerUiState.Scan -> !viewReady
            is EIdFaceScannerUiState.Error -> false
            EIdFaceScannerUiState.Finish -> false
        }
    }.toStateFlow(true)

    val shouldLock: StateFlow<Boolean> = uiState.map { state ->
        state is EIdFaceScannerUiState.Initializing || (
            state is EIdFaceScannerUiState.Scan && state.scannerButtonState == ScannerButtonState.Scanning
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
            prepareScanner()
        }
    }

    fun onPause() {
        viewModelScope.launch {
            resetScanningState()
            _uiState.update { EIdFaceScannerUiState.Initializing }
            Timber.d("$logTitle view paused")
        }
    }

    fun initScannerSdk(activity: AppCompatActivity) = viewModelScope.launch {
        isRotation = lastActivityHash != null && lastActivityHash != activity.hashCode()
        lastActivityHash = activity.hashCode()

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
                EIdFaceScannerUiState.Scan(
                    infoState = SDKInfoState.Loading,
                    infoText = null,
                    scannerButtonState = ScannerButtonState.Ready
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
            EIdFaceScannerUiState.Scan(
                infoState = SDKInfoState.Ready,
                infoText = null,
                scannerButtonState = ScannerButtonState.Ready
            )
        }
    }

    private fun CoroutineScope.setupSdkFlowCollection() {
        launch {
            avBeam.statusFlow.collect { status ->
                _uiState.updateScanState {
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
            avBeam.captureFaceFlow.collect { notification ->
                when (notification) {
                    is AvBeamNotification.Completed -> onFaceScanCompleted(notification.packageData)
                    AvBeamNotification.Empty, AvBeamNotification.Initial -> {
                        _uiState.updateScanState { copy(infoState = SDKInfoState.Empty) }
                    }

                    is AvBeamNotification.Error -> onFaceScanError(notification)

                    AvBeamNotification.Loading -> {
                        _uiState.updateScanState { copy(infoState = SDKInfoState.Loading) }
                    }
                }
            }
        }
        launch {
            avBeam.errorFlow.collect { errorNotification ->
                if (errorNotification != AVBeamError.None) {
                    Timber.e("$logTitle Error - avBeam errorFlow ${errorNotification.name}")
                    _uiState.update { EIdFaceScannerUiState.Error(onRetry = ::onRetry) }
                }
            }
        }
    }

    private fun MutableStateFlow<EIdFaceScannerUiState>.updateScanState(
        update: EIdFaceScannerUiState.Scan.() -> EIdFaceScannerUiState.Scan
    ) {
        update { currentState ->
            if (currentState is EIdFaceScannerUiState.Scan) {
                currentState.update()
            } else {
                currentState
            }
        }
    }

    private fun onFaceScanCompleted(packageResult: AVBeamPackageResult) = viewModelScope.launch {
        _uiState.updateScanState {
            copy(
                scannerButtonState = ScannerButtonState.Done,
                isProcessing = true
            )
        }
        delay(NAVIGATION_DELAY)
        resetScanningState()
        _uiState.update { EIdFaceScannerUiState.Finish }
        avBeam.shutDown()
        Timber.d(message = "$logTitle Completed: ${packageResult.data?.size()}")

        saveEIdRequestFiles(
            sIdCaseId = caseId,
            filesDataList = packageResult.files,
            filesCategory = EIdRequestFileCategory.FACE_RECORDING,
        )

        navManager.popUpToAndNavigate(
            popToInclusive = Destination.EIdStartSelfieVideoScreen::class,
            destination = Destination.EIdProcessDataScreen(caseId = caseId)
        )
    }

    private fun onFaceScanError(avBeamError: AvBeamNotification.Error) {
        if (!isRotation) {
            val baseError = StringBuilder("$logTitle Error - faceScan notification")
            avBeamError.packageData?.let { packageData ->
                baseError.append("\nerror: ${packageData.errorType}, ${packageData.errorCode}")
                baseError.append("\nerrorList: ${packageData.errorCodeList.joinToString()}")
            }
            Timber.e(message = baseError.toString())

            _uiState.update { EIdFaceScannerUiState.Error(onRetry = ::onRetry) }
        } else {
            isRotation = false
        }
    }

    fun onToggleScan() {
        viewModelScope.launch {
            val currentState = _uiState.value as? EIdFaceScannerUiState.Scan ?: return@launch
            when (currentState.scannerButtonState) {
                ScannerButtonState.Ready -> startScanning()
                ScannerButtonState.Scanning -> stopScanning()
                ScannerButtonState.Done -> {}
            }
        }
    }

    private suspend fun prepareScanner() {
        isViewReady.awaitValue(true)
        avBeam.stopCamera()
        avBeam.startFrontCamera()
    }

    private suspend fun startScanning() {
        Timber.d(message = "$logTitle startFaceScanning: $viewWidth, $viewHeight")

        if (!isCameraRunning.value) {
            prepareScanner()
        }

        isCameraRunning.first { it }

        val configScanning = AVBeamCaptureFaceConfig(
            rectWidth = viewWidth,
            rectHeight = viewHeight,
            videoLength = VIDEO_LENGTH_SECONDS,
        )

        val currentState = _uiState.value as? EIdFaceScannerUiState.Scan
        if (currentState?.scannerButtonState != ScannerButtonState.Scanning) {
            avBeam.startCaptureFace(config = configScanning)
        }

        _uiState.updateScanState {
            copy(scannerButtonState = ScannerButtonState.Scanning)
        }
    }

    private fun stopScanning() {
        val currentState = _uiState.value as? EIdFaceScannerUiState.Scan
        if (currentState?.scannerButtonState == ScannerButtonState.Scanning) {
            avBeam.stopCaptureFace()
            viewModelScope.launch {
                prepareScanner()
            }
        }
        _uiState.updateScanState {
            copy(
                scannerButtonState = ScannerButtonState.Ready
            )
        }
    }

    private fun resetScanningState() {
        stopScanning()
        avBeam.stopCamera()
        isViewReady.update { false }
    }

    private fun onRetry() {
        viewModelScope.launch {
            resetScanningState()
            _uiState.update { EIdFaceScannerUiState.Initializing }
            prepareScanner()
        }
    }

    fun onUp() {
        resetScanningState()
        avBeam.shutDown()
        _uiState.update { EIdFaceScannerUiState.Finish }
        navManager.popBackStack()
    }

    fun onClose() {
        avBeam.shutDown()
        navManager.navigateBackToHomeScreen(Destination.EIdStartAvSessionScreen::class)
    }

    override fun onCleared() {
        resetScanningState()
        _uiState.update { EIdFaceScannerUiState.Finish }
        super.onCleared()
    }

    fun onHelp() = appContext.openLink(appContext.getString(R.string.tk_error_generic_helpLink_value))

    private suspend inline fun <T> StateFlow<T>.awaitValue(value: T) =
        this.first { it == value }

    companion object {
        private const val VIDEO_LENGTH_SECONDS = 10
        private const val NAVIGATION_DELAY = 1000L
    }
}
