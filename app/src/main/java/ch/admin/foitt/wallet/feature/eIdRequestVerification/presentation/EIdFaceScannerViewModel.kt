package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamInitConfig
import ch.admin.foitt.avwrapper.AVBeamPackageResult
import ch.admin.foitt.avwrapper.AVBeamStatus
import ch.admin.foitt.avwrapper.AvBeamNotification
import ch.admin.foitt.avwrapper.config.AVBeamCaptureFaceConfig
import ch.admin.foitt.avwrapper.config.AVBeamConfigLogLevel
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.toTextRes
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestFiles
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.SDKInfoState
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFileCategory
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = EIdFaceScannerViewModel.Factory::class)
class EIdFaceScannerViewModel @AssistedInject constructor(
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

    override val topBarState = TopBarState.None

    private val logTitle = "Face Scan:"

    private val _infoState = MutableStateFlow<SDKInfoState>(SDKInfoState.Loading)
    val infoState = _infoState.asStateFlow()

    private val _infoText = MutableStateFlow<Int?>(null)
    val infoText = _infoText.asStateFlow()

    private val _lockOrientation = MutableStateFlow(false)
    val lockOrientation: StateFlow<Boolean> = _lockOrientation

    private var viewWidth = 0
    private var viewHeight = 0

    private val isScannerLoading = MutableStateFlow(true)
    private val isFaceScanLoading = MutableStateFlow(false)
    private val isCameraRunning = MutableStateFlow(false)
    private val isFaceScanning = MutableStateFlow(false)
    private val isViewReady = MutableStateFlow(false)

    private val isReadyToScan = combine(
        isScannerLoading,
        isViewReady,
        isCameraRunning,
    ) { isScannerLoading, isViewReady, isCameraRunning ->
        !isScannerLoading && isViewReady && isCameraRunning
    }.toStateFlow(false)

    val isLoading = combine(
        isScannerLoading,
        isFaceScanLoading,
    ) { isScannerLoading, isFaceScanLoading ->
        isScannerLoading || isFaceScanLoading
    }.toStateFlow(true)

    fun onResume() {
        viewModelScope.launch {
            Timber.d("$logTitle view resumed")
            startScanFace()
        }
    }

    fun onPause() {
        viewModelScope.launch {
            isScannerLoading.awaitValue(false)
            resetScanningState()
            Timber.d("$logTitle view paused")
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
                    else -> {}
                }
            }
        }
        launch {
            avBeam.captureFaceFlow.collect { notification ->
                when (notification) {
                    is AvBeamNotification.Completed -> onFaceScanCompleted(notification.packageData)
                    AvBeamNotification.Empty, AvBeamNotification.Initial -> {
                        _infoState.update { SDKInfoState.Empty }
                    }
                }
            }
        }
        launch {
            avBeam.errorFlow.collect { errorNotification ->
                Timber.d("Received error: ${errorNotification.name}")
            }
        }
    }

    private fun onFaceScanCompleted(packageResult: AVBeamPackageResult) = viewModelScope.launch {
        // The AvBeam sdk stops the faceScan and camera tasks automatically.
        isFaceScanning.update { false }
        isCameraRunning.update { false }
        resetScanningState()
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
    }.trackCompletion(isFaceScanLoading)

    private suspend fun startScanFace() {
        _lockOrientation.update { true }
        isScannerLoading.awaitValue(false)
        isViewReady.awaitValue(true)
        avBeam.stopCamera()
        avBeam.startFrontCamera()
        isReadyToScan.awaitValue(true)
        Timber.d(message = "$logTitle startScanFace: $viewWidth, $viewHeight")

        val configScanning = AVBeamCaptureFaceConfig(
            rectWidth = viewWidth,
            rectHeight = viewHeight,
            videoLength = VIDEO_LENGTH_SECONDS,
        )

        isFaceScanning.update { isScanning ->
            if (!isScanning) {
                avBeam.startCaptureFace(
                    config = configScanning,
                )
            }
            true
        }
    }

    private fun resetScanningState() {
        isFaceScanning.update { isScanning ->
            if (isScanning) {
                avBeam.stopCaptureFace()
            }
            false
        }

        isCameraRunning.update { isCameraRunning ->
            if (isCameraRunning) {
                avBeam.stopCamera()
            }
            false
        }
        isViewReady.update { false }
        _lockOrientation.update { false }
        _infoState.update { SDKInfoState.Empty }
    }

    fun onUp() {
        resetScanningState()
        navManager.popBackStack()
    }

    override fun onCleared() {
        avBeam.stopCaptureFace()
        super.onCleared()
    }

    private suspend inline fun <T> StateFlow<T>.awaitValue(value: T) =
        this.first { it == value }

    companion object {
        private const val VIDEO_LENGTH_SECONDS = 10
    }
}
