package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.content.Context
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamError
import ch.admin.foitt.avwrapper.AVBeamErrorType
import ch.admin.foitt.avwrapper.AVBeamStatus
import ch.admin.foitt.avwrapper.AvBeamNotification
import ch.admin.foitt.avwrapper.DocumentScanPackageResult
import ch.admin.foitt.avwrapper.config.AVBeamScanDocumentConfig
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.TextKeyType
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.toTextRes
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.AreEIdDocumentsEqual
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.CreateSDKErrorTextKeys
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetEIdRequestCase
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner.EIdDocumentScanStatus
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner.EIdDocumentScannerUiState
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.OnPermissionResult
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.PermissionState
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.infra.PermissionStateHandler
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.DocumentScannerErrorType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdUiDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toEIdDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetDocumentScanResult
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.scanning.di.AvBeamSdkEntryPoint
import ch.admin.foitt.wallet.platform.utils.openLink
import com.github.michaelbull.result.mapOrElse
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
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

@Suppress("TooManyFunctions")
@HiltViewModel(assistedFactory = EIdDocumentScannerViewModel.Factory::class)
class EIdDocumentScannerViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    private val setDocumentScanResult: SetDocumentScanResult,
    private val areEIdDocumentsEqual: AreEIdDocumentsEqual,
    private val permissionStateHandler: PermissionStateHandler,
    destinationScopedComponentManager: DestinationScopedComponentManager,
    getDocumentType: GetDocumentType,
    getEIdRequestCase: GetEIdRequestCase,
    private val createSDKErrorTextKeys: CreateSDKErrorTextKeys,
    @Assisted private val caseId: String,
    private val setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdDocumentScannerViewModel
    }

    private val avBeamRepository = destinationScopedComponentManager.getEntryPoint(
        entryPointClass = AvBeamSdkEntryPoint::class.java,
        componentScope = ComponentScope.AvBeamSdkSession,
    ).avBeamRepository()

    private val avBeam: AVBeam get() = avBeamRepository.getBeam()

    override val topBarState: TopBarState
        get() {
            val state = uiState.value
            val permissionState = permissionState.value
            return when {
                permissionState !is PermissionState.Granted -> TopBarState.DetailsWithCloseButton(
                    titleId = null,
                    onUp = ::onUp,
                    onClose = ::onClose,
                )

                state is EIdDocumentScannerUiState.Error -> TopBarState.DetailsWithCloseButton(
                    titleId = null,
                    onUp = ::onUp,
                    onClose = ::onClose,
                )

                state is EIdDocumentScannerUiState.Scan -> if (state.status == EIdDocumentScanStatus.BACKSIDE_INFO) {
                    TopBarState.Empty
                } else {
                    TopBarState.DetailsWithCloseRoundButtons(
                        titleId = null,
                        onUp = ::onUp,
                        onClose = ::onClose,
                    )
                }

                else -> TopBarState.Empty
            }
        }

    private val logTitle = "Document Scan:"

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
    private val eIdDocumentScanStatus = MutableStateFlow(EIdDocumentScanStatus.INITIALIZING)

    private val isViewReady = MutableStateFlow(false)
    private var viewWidth = 0
    private var viewHeight = 0

    val documentType: StateFlow<EIdUiDocumentType> = if (isFirstDocScan) {
        getDocumentType()
    } else {
        flow {
            val documentType = getEIdRequestCase(caseId).mapOrElse(
                default = { EIdUiDocumentType.IDENTITY_CARD },
                transform = { it.selectedDocumentType.toEIdDocumentType() }
            )
            emit(documentType)
        }.toStateFlow(EIdUiDocumentType.IDENTITY_CARD)
    }

    private val isFirstDocScan: Boolean get() = caseId.isBlank()

    val uiState: StateFlow<EIdDocumentScannerUiState> = combine(
        errorState,
        permissionState,
        avBeam.initializedFlow,
        eIdDocumentScanStatus,
        avBeam.statusFlow,

    ) {
            errorState,
            permissionState,
            initialized,
            eIdDocumentScanStatus,
            avBeamStatus,
        ->
        when {
            errorState is DocumentScannerErrorType.SdkError ->
                EIdDocumentScannerUiState.Error(
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

            errorState == DocumentScannerErrorType.Generic -> EIdDocumentScannerUiState.Error(
                type = errorState,
                onButton = ::onRetry,
                onHelp = ::onHelp,
                title = R.string.tk_error_generic_primary,
                content = R.string.tk_error_generic_secondary,
                buttonText = R.string.tk_error_generic_button_primary
            )

            errorState == DocumentScannerErrorType.UnequalDocuments -> EIdDocumentScannerUiState.Error(
                type = errorState,
                onButton = ::onRetry,
                title = R.string.tk_eidRequest_scanDocument_wrongDocument_primary,
                content = R.string.tk_eidRequest_scanDocument_wrongDocument_secondary,
                buttonText = R.string.tk_eidRequest_scanDocument_wrongDocument_button
            )

            permissionState !is PermissionState.Granted -> EIdDocumentScannerUiState.Initializing
            !initialized -> EIdDocumentScannerUiState.Initializing
            else -> {
                EIdDocumentScannerUiState.Scan(
                    infoText = avBeamStatus.toTextRes(),
                    status = eIdDocumentScanStatus,
                )
            }
        }
    }.toStateFlow(EIdDocumentScannerUiState.Initializing)

    val shouldLock: StateFlow<Boolean> = uiState.map { state ->
        state is EIdDocumentScannerUiState.Initializing && permissionState.value is PermissionState.Granted ||
            (state is EIdDocumentScannerUiState.Scan && state.status.isScanning)
    }.toStateFlow(false)

    init {
        viewModelScope.launch {
            uiState.collectLatest {
                setTopBarState(topBarState)
            }
            permissionState.collectLatest {
                setTopBarState(topBarState)
            }
        }
    }

    fun onResumeScan() {
        viewModelScope.launch {
            Timber.d("$logTitle view resumed")
            prepareScanner()
        }
    }

    fun onPauseScan() {
        viewModelScope.launch {
            resetScanningState()
            Timber.d("$logTitle  view paused")
        }
    }

    fun initScannerSdk(activity: AppCompatActivity) = viewModelScope.launch {
        launch {
            eIdDocumentScanStatus.update { EIdDocumentScanStatus.INITIALIZING }
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

    fun onAfterViewLayout(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        isViewReady.update { true }
        eIdDocumentScanStatus.update { EIdDocumentScanStatus.FRONTSIDE }
    }

    private fun CoroutineScope.setupSdkFlowCollection() {
        launch {
            avBeam.statusFlow.collect { status ->
                if (status == AVBeamStatus.IdNeedSecondPageForMatching) {
                    eIdDocumentScanStatus.update { EIdDocumentScanStatus.BACKSIDE_INFO }
                }
            }
        }
        launch {
            avBeam.scanDocumentFlow.collect { notification ->
                when (notification) {
                    is AvBeamNotification.DocumentScanCompleted -> onScanningCompleted(notification.packageData)
                    AvBeamNotification.Empty, AvBeamNotification.Initial -> {
                    }

                    is AvBeamNotification.Error -> onScanningError(notification)

                    AvBeamNotification.Loading -> {
                    }
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

    fun onUp() {
        resetScanningState()
        clearFlows()
        navManager.popBackStack()
    }

    fun onClose() {
        navManager.navigateBackToHomeScreen(Destination.EIdStartAvSessionScreen::class)
    }

    fun onToggleScan() {
        viewModelScope.launch {
            when (eIdDocumentScanStatus.value) {
                EIdDocumentScanStatus.FRONTSIDE -> startScanning()
                EIdDocumentScanStatus.BACKSIDE -> continueScanningBackside()

                EIdDocumentScanStatus.FRONTSIDE_SCANNING,
                EIdDocumentScanStatus.BACKSIDE_SCANNING -> stopScanning()

                EIdDocumentScanStatus.INITIALIZING,
                EIdDocumentScanStatus.BACKSIDE_INFO,
                EIdDocumentScanStatus.FINISHED -> {}
            }
        }
    }

    fun onContinueToBackside() {
        eIdDocumentScanStatus.update { EIdDocumentScanStatus.BACKSIDE }
    }

    private suspend fun prepareScanner() {
        isViewReady.awaitValue(true)
        avBeam.stopCamera()
        avBeam.startCamera()
    }

    private suspend fun startScanning() {
        Timber.d(message = "$logTitle startScanningDocument: $viewWidth, $viewHeight")

        val configScanning = AVBeamScanDocumentConfig(
            rectWidth = viewWidth,
            rectHeight = viewHeight,
            expectSecondScanNotification = true,
        )

        if (!eIdDocumentScanStatus.value.isScanning) {
            avBeam.startScanDocument(config = configScanning)
        }

        eIdDocumentScanStatus.update { EIdDocumentScanStatus.FRONTSIDE_SCANNING }
    }

    private fun continueScanningBackside() {
        avBeam.notifySecondScan()
        eIdDocumentScanStatus.update { EIdDocumentScanStatus.BACKSIDE_SCANNING }
    }

    private fun onScanningCompleted(packageResult: DocumentScanPackageResult) = viewModelScope.launch {
        eIdDocumentScanStatus.update { EIdDocumentScanStatus.FINISHED }
        Timber.d(message = "$logTitle Completed")

        areEIdDocumentsEqual(caseId, packageResult.mrzValues.toTypedArray())
            .onFailure { error ->
                Timber.e("$logTitle Error - areEIdDocumentsEqual: $error")
                errorState.update { DocumentScannerErrorType.Generic }
                return@launch
            }
            .onSuccess { areDocumentsEqual ->
                if (!areDocumentsEqual) {
                    Timber.w("$logTitle Error - document are not equal")
                    errorState.update { DocumentScannerErrorType.UnequalDocuments }
                    return@launch
                }
            }

        setDocumentScanResult(documentScanResult = packageResult)
        delay(NAVIGATION_DELAY)
        navManager.replaceCurrentWith(
            Destination.EIdDocumentScanSummaryScreen(caseId = caseId)
        )

        resetScanningState()
    }

    private fun onScanningError(avBeamError: AvBeamNotification.Error) {
        val baseError = StringBuilder("$logTitle Error - documentScan notification")
        avBeamError.packageData?.let { packageData ->
            baseError.append("\nerror: ${packageData.errorType}, ${packageData.errorCode}")
            baseError.append("\nerrorList: ${packageData.errorCodeList.joinToString()}")
        }
        Timber.e(message = baseError.toString())

        val errorCode = avBeamError.packageData?.errorCode ?: AVBeamError.Unknown
        val errorType = avBeamError.packageData?.errorType ?: AVBeamErrorType.Unknown
        errorState.update { DocumentScannerErrorType.SdkError(errorCode, errorType) }
    }

    private fun resetScanningState() {
        stopScanning()
        avBeam.stopCamera()
        isViewReady.update { false }
    }

    private fun clearFlows() {
        errorState.update { DocumentScannerErrorType.None }
        eIdDocumentScanStatus.update { EIdDocumentScanStatus.INITIALIZING }
        avBeam.clearNotificationsFlow()
    }

    private fun stopScanning() {
        if (eIdDocumentScanStatus.value.isScanning) {
            avBeam.stopScanDocument()
        }
        eIdDocumentScanStatus.update { EIdDocumentScanStatus.FRONTSIDE }
    }

    private fun onRetry() {
        viewModelScope.launch {
            Timber.w("$logTitle - retry")
            resetScanningState()
            clearFlows()
            prepareScanner()
        }
    }

    private suspend inline fun <T> StateFlow<T>.awaitValue(value: T) =
        this.first { it == value }

    override fun onCleared() {
        resetScanningState()
        clearFlows()
        super.onCleared()
    }

    private fun onHelp() = appContext.openLink(appContext.getString(R.string.tk_error_generic_helpLink_value))

    companion object {
        private const val NAVIGATION_DELAY = 1000L
    }
}
