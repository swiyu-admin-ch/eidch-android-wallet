package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.content.Context
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamError
import ch.admin.foitt.avwrapper.AVBeamErrorType
import ch.admin.foitt.avwrapper.AVBeamPackageResult
import ch.admin.foitt.avwrapper.AVBeamStatus
import ch.admin.foitt.avwrapper.AvBeamNotification
import ch.admin.foitt.avwrapper.config.AVBeamCaptureFaceConfig
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.TextKeyType
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.toTextRes
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.CreateSDKErrorTextKeys
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestFiles
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.faceScanner.EIdFaceScanStatus
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.faceScanner.EIdFaceScannerUiState
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.OnPermissionResult
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.PermissionState
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.infra.PermissionStateHandler
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFileCategory
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.DocumentScannerErrorType
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = EIdFaceScannerViewModel.Factory::class)
class EIdFaceScannerViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    private val saveEIdRequestFiles: SaveEIdRequestFiles,
    private val permissionStateHandler: PermissionStateHandler,
    private val createSDKErrorTextKeys: CreateSDKErrorTextKeys,
    destinationScopedComponentManager: DestinationScopedComponentManager,
    @Assisted private val caseId: String,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdFaceScannerViewModel
    }

    private val avBeamRepository = destinationScopedComponentManager.getEntryPoint(
        entryPointClass = AvBeamSdkEntryPoint::class.java,
        componentScope = ComponentScope.AvBeamSdkSession,
    ).avBeamRepository()

    private val avBeam: AVBeam get() = avBeamRepository.getBeam()

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
                is EIdFaceScannerUiState.Scanning ->
                    TopBarState.DetailsWithCloseRoundButtons(
                        titleId = null,
                        onUp = ::onUp,
                        onClose = ::onClose,
                    )
            }
        }

    private val logTitle = "Face Scan:"

    private val errorState = MutableStateFlow<DocumentScannerErrorType>(DocumentScannerErrorType.None)

    private val eIdFaceScanStatus = MutableStateFlow<EIdFaceScanStatus>(EIdFaceScanStatus.Initializing)

    private val isCameraRunning = MutableStateFlow(false)
    private val isViewReady = MutableStateFlow(false)

    private var viewWidth = 0
    private var viewHeight = 0
    private var lastActivityHash: Int? = null
    private var isRotation: Boolean = false
    private var faceScanProgressJob: Job? = null

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

    val uiState: StateFlow<EIdFaceScannerUiState> = combine(
        errorState,
        permissionState,
        avBeam.initializedFlow,
        eIdFaceScanStatus,
        avBeam.statusFlow,
    ) { errorState, permission, initialized, status, avBeamStatus ->
        when {
            errorState is DocumentScannerErrorType.SdkError ->
                EIdFaceScannerUiState.Error(
                    type = errorState,
                    onButton = {
                        when (errorState.errorType) {
                            AVBeamErrorType.None,
                            AVBeamErrorType.Unknown,
                            AVBeamErrorType.Unsupported -> onClose()

                            AVBeamErrorType.Blocking -> onUp()
                            AVBeamErrorType.Contextual -> onRetry()
                        }
                    },
                    onHelp = ::onHelp,
                    title = createSDKErrorTextKeys(errorState.errorCode, TextKeyType.TITLE),
                    content = createSDKErrorTextKeys(errorState.errorCode, TextKeyType.CONTENT),
                    buttonText = when (errorState.errorType) {
                        AVBeamErrorType.None,
                        AVBeamErrorType.Unknown,
                        AVBeamErrorType.Unsupported -> R.string.tk_global_close

                        AVBeamErrorType.Blocking,
                        AVBeamErrorType.Contextual -> R.string.tk_error_generic_button_primary
                    }
                )

            errorState == DocumentScannerErrorType.Generic -> EIdFaceScannerUiState.Error(
                type = errorState,
                onButton = ::onRetry,
                onHelp = ::onHelp,
                title = R.string.tk_error_generic_primary,
                content = R.string.tk_error_generic_secondary,
                buttonText = R.string.tk_error_generic_button_primary
            )

            permission !is PermissionState.Granted -> EIdFaceScannerUiState.Initializing
            !initialized -> EIdFaceScannerUiState.Initializing
            else -> EIdFaceScannerUiState.Scanning(
                infoText = avBeamStatus.toTextRes(),
                status = status,
            )
        }
    }.toStateFlow(EIdFaceScannerUiState.Initializing)

    val shouldLock: StateFlow<Boolean> = uiState.map { state ->
        state is EIdFaceScannerUiState.Initializing && permissionState.value is PermissionState.Granted ||
            (state is EIdFaceScannerUiState.Scanning && state.status.isScanning)
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
            Timber.d("$logTitle view paused")
        }
    }

    fun initScannerSdk(activity: AppCompatActivity) = viewModelScope.launch {
        isRotation = lastActivityHash != null && lastActivityHash != activity.hashCode()
        lastActivityHash = activity.hashCode()

        launch {
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
        eIdFaceScanStatus.update { EIdFaceScanStatus.Ready }
    }

    private fun CoroutineScope.setupSdkFlowCollection() {
        launch {
            avBeam.statusFlow.collect { status ->
                if (status == AVBeamStatus.StreamingStarted) {
                    isCameraRunning.update { true }
                }
            }
        }
        launch {
            avBeam.captureFaceFlow.collect { notification ->
                when (notification) {
                    is AvBeamNotification.Completed -> onFaceScanCompleted(notification.packageData)
                    AvBeamNotification.Empty, AvBeamNotification.Initial -> {}
                    is AvBeamNotification.Error -> onFaceScanError(notification)
                    AvBeamNotification.Loading -> {}
                }
            }
        }
        launch {
            avBeam.errorFlow.collect { errorNotification ->
                if (errorNotification != AVBeamError.None) {
                    Timber.e("$logTitle Error - avBeam errorFlow ${errorNotification.name}")
                    errorState.update { DocumentScannerErrorType.Generic }
                }
            }
        }
    }

    private fun onFaceScanCompleted(packageResult: AVBeamPackageResult) = viewModelScope.launch {
        eIdFaceScanStatus.update { EIdFaceScanStatus.Finished }

        delay(NAVIGATION_DELAY)

        avBeam.stopCaptureFace()
        faceScanProgressJob?.cancel()
        faceScanProgressJob = null
        avBeam.stopCamera()
        isViewReady.update { false }

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

            val errorCode = avBeamError.packageData?.errorCode ?: AVBeamError.Unknown
            val errorType = avBeamError.packageData?.errorType ?: AVBeamErrorType.Unknown
            errorState.update { DocumentScannerErrorType.SdkError(errorCode, errorType) }
        } else {
            isRotation = false
        }
    }

    fun onToggleScan() {
        viewModelScope.launch {
            when (eIdFaceScanStatus.value) {
                EIdFaceScanStatus.Ready -> startScanning()
                is EIdFaceScanStatus.Scanning -> stopScanning()
                EIdFaceScanStatus.Finished,
                EIdFaceScanStatus.Initializing -> {}
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

        isCameraRunning.awaitValue(true)

        val configScanning = AVBeamCaptureFaceConfig(
            rectWidth = viewWidth,
            rectHeight = viewHeight,
            videoLength = VIDEO_LENGTH_SECONDS,
        )

        if (eIdFaceScanStatus.value !is EIdFaceScanStatus.Scanning) {
            avBeam.startCaptureFace(config = configScanning)
            eIdFaceScanStatus.update { EIdFaceScanStatus.Scanning(0f) }
            faceScanProgressJob = viewModelScope.launchTimer(VIDEO_LENGTH_MILLIS) { progressRatio ->
                if (eIdFaceScanStatus.value is EIdFaceScanStatus.Scanning) {
                    eIdFaceScanStatus.update { EIdFaceScanStatus.Scanning(progressRatio) }
                }
            }
        }
    }

    private fun stopScanning() {
        if (eIdFaceScanStatus.value is EIdFaceScanStatus.Scanning) {
            avBeam.stopCaptureFace()
            faceScanProgressJob?.cancel()
            faceScanProgressJob = null

            viewModelScope.launch {
                prepareScanner()
            }
        }
        eIdFaceScanStatus.update { EIdFaceScanStatus.Ready }
    }

    private fun resetScanningState() {
        stopScanning()
        avBeam.stopCamera()
        isViewReady.update { false }
    }

    private fun clearFlows() {
        errorState.update { DocumentScannerErrorType.None }
        eIdFaceScanStatus.update { EIdFaceScanStatus.Initializing }
        avBeam.clearNotificationsFlow()
    }

    private fun onRetry() {
        viewModelScope.launch {
            resetScanningState()
            clearFlows()
            prepareScanner()
        }
    }

    fun onUp() {
        resetScanningState()
        clearFlows()
        navManager.popBackStack()
    }

    fun onClose() {
        navManager.navigateBackToHomeScreen(Destination.EIdStartAvSessionScreen::class)
    }

    override fun onCleared() {
        resetScanningState()
        clearFlows()
        super.onCleared()
    }

    fun onHelp() = appContext.openLink(appContext.getString(R.string.tk_error_generic_helpLink_value))

    private suspend inline fun <T> StateFlow<T>.awaitValue(value: T) =
        this.first { it == value }

    companion object {
        const val VIDEO_LENGTH_SECONDS = 10
        private const val VIDEO_LENGTH_MILLIS = VIDEO_LENGTH_SECONDS * 1000L
        private const val NAVIGATION_DELAY = 1000L
    }
}
