package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.content.Context
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamError
import ch.admin.foitt.avwrapper.AVBeamInitConfig
import ch.admin.foitt.avwrapper.AVBeamStatus
import ch.admin.foitt.avwrapper.AvBeamNotification
import ch.admin.foitt.avwrapper.DocumentScanPackageResult
import ch.admin.foitt.avwrapper.config.AVBeamConfigLogLevel
import ch.admin.foitt.avwrapper.config.AVBeamScanDocumentConfig
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.toTextRes
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.AreEIdDocumentsEqual
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner.EIdDocumentScanStatus
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner.EIdDocumentScannerUiState
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.model.SDKInfoState
import ch.admin.foitt.wallet.platform.di.IoDispatcherScope
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.DocumentScannerErrorType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetDocumentType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetDocumentScanResult
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@Suppress("TooManyFunctions")
@HiltViewModel(assistedFactory = EIdDocumentScannerViewModel.Factory::class)
class EIdDocumentScannerViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    private val avBeam: AVBeam,
    private val setDocumentScanResult: SetDocumentScanResult,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val areEIdDocumentsEqual: AreEIdDocumentsEqual,
    @param:IoDispatcherScope private val ioDispatcherScope: CoroutineScope,
    getDocumentType: GetDocumentType,
    @Assisted private val caseId: String,
    private val setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdDocumentScannerViewModel
    }

    override val topBarState: TopBarState
        get() {
            return when (val state = uiState.value) {
                EIdDocumentScannerUiState.Initializing -> TopBarState.Empty

                is EIdDocumentScannerUiState.Error -> TopBarState.DetailsWithCloseButton(
                    titleId = null,
                    onUp = ::onUp,
                    onClose = ::onClose,
                )

                is EIdDocumentScannerUiState.Scan -> if (state.status == EIdDocumentScanStatus.BACKSIDE_INFO) {
                    TopBarState.Empty
                } else {
                    TopBarState.DetailsWithCloseRoundButtons(
                        titleId = null,
                        onUp = ::onUp,
                        onClose = ::onClose,
                    )
                }
            }
        }

    private val logTitle = "Document Scan:"

    private val _uiState = MutableStateFlow<EIdDocumentScannerUiState>(EIdDocumentScannerUiState.Initializing)
    val uiState: StateFlow<EIdDocumentScannerUiState> = _uiState.asStateFlow()

    private val isViewReady = MutableStateFlow(false)
    private var viewWidth = 0
    private var viewHeight = 0

    val documentType = getDocumentType().value

    val isLoading: StateFlow<Boolean> = combine(
        uiState,
        isViewReady
    ) { state, viewReady ->
        when (state) {
            EIdDocumentScannerUiState.Initializing -> true
            is EIdDocumentScannerUiState.Scan -> !viewReady
            is EIdDocumentScannerUiState.Error -> false
        }
    }.toStateFlow(true)

    val shouldLock: StateFlow<Boolean> = uiState.map { state ->
        state is EIdDocumentScannerUiState.Initializing ||
            (state is EIdDocumentScannerUiState.Scan && state.status.isScanning)
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
            Timber.d("$logTitle  view paused")
        }
    }

    fun initScannerSdk(activity: AppCompatActivity) = viewModelScope.launch {
        ioDispatcherScope.launch {
            val logLevel = if (environmentSetupRepository.avBeamLoggingEnabled) {
                AVBeamConfigLogLevel.DEBUG
            } else {
                AVBeamConfigLogLevel.NONE
            }
            avBeam.init(AVBeamInitConfig(logLevel), activity)
            avBeam.initializedFlow.awaitValue(true)
            Timber.d("$logTitle initScannerSdk done")
            _uiState.update {
                EIdDocumentScannerUiState.Scan(
                    infoState = SDKInfoState.Loading,
                    infoText = null,
                    status = EIdDocumentScanStatus.FRONTSIDE,
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

    fun onAfterViewLayout(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        isViewReady.update { true }
        _uiState.update {
            EIdDocumentScannerUiState.Scan(
                infoState = SDKInfoState.Ready,
                infoText = null,
                status = EIdDocumentScanStatus.FRONTSIDE,
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
                        status = if (status == AVBeamStatus.IdNeedSecondPageForMatching) {
                            EIdDocumentScanStatus.BACKSIDE_INFO
                        } else {
                            this.status
                        }
                    )
                }
            }
        }
        launch {
            avBeam.scanDocumentFlow.collect { notification ->
                when (notification) {
                    is AvBeamNotification.DocumentScanCompleted -> onScanningCompleted(notification.packageData)
                    AvBeamNotification.Empty, AvBeamNotification.Initial -> {
                        _uiState.updateScanState { copy(infoState = SDKInfoState.Empty) }
                    }

                    is AvBeamNotification.Error -> onScanningError(notification)

                    AvBeamNotification.Loading -> {
                        _uiState.updateScanState { copy(infoState = SDKInfoState.Loading) }
                    }
                }
            }
        }
        launch {
            avBeam.errorFlow.collect { errorNotification ->
                if (errorNotification != AVBeamError.None) {
                    Timber.e(message = "$logTitle Error - avBeam errorFlow: ${errorNotification.name}")
                    _uiState.update {
                        EIdDocumentScannerUiState.Error(type = DocumentScannerErrorType.GENERIC, onRetry = ::onRetry, onHelp = ::onHelp)
                    }
                }
            }
        }
    }

    private fun MutableStateFlow<EIdDocumentScannerUiState>.updateScanState(
        update: EIdDocumentScannerUiState.Scan.() -> EIdDocumentScannerUiState.Scan
    ) {
        update { currentState ->
            if (currentState is EIdDocumentScannerUiState.Scan) {
                currentState.update()
            } else {
                currentState
            }
        }
    }

    fun onUp() {
        resetScanningState()
        avBeam.shutDown()
        navManager.popBackStack()
    }

    fun onClose() {
        avBeam.shutDown()
        navManager.navigateBackToHomeScreen(Destination.EIdStartAvSessionScreen::class)
    }

    fun onToggleScan() {
        viewModelScope.launch {
            val currentState = _uiState.value as? EIdDocumentScannerUiState.Scan ?: return@launch
            when (currentState.status) {
                EIdDocumentScanStatus.FRONTSIDE -> startScanning()
                EIdDocumentScanStatus.BACKSIDE -> continueScanningBackside()

                EIdDocumentScanStatus.FRONTSIDE_SCANNING,
                EIdDocumentScanStatus.BACKSIDE_SCANNING -> stopScanning()

                EIdDocumentScanStatus.BACKSIDE_INFO,
                EIdDocumentScanStatus.FINISHED -> {}
            }
        }
    }

    fun onContinueToBackside() {
        _uiState.updateScanState {
            copy(status = EIdDocumentScanStatus.BACKSIDE)
        }
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

        val currentState = _uiState.value as? EIdDocumentScannerUiState.Scan
        if (currentState?.status?.isScanning == false) {
            avBeam.startScanDocument(config = configScanning)
        }

        _uiState.updateScanState {
            copy(status = EIdDocumentScanStatus.FRONTSIDE_SCANNING)
        }
    }

    private fun continueScanningBackside() {
        avBeam.notifySecondScan()
        _uiState.updateScanState {
            copy(status = EIdDocumentScanStatus.BACKSIDE_SCANNING)
        }
    }

    private fun onScanningCompleted(packageResult: DocumentScanPackageResult) = viewModelScope.launch {
        _uiState.updateScanState {
            copy(status = EIdDocumentScanStatus.FINISHED)
        }
        Timber.d(message = "$logTitle Completed")

        areEIdDocumentsEqual(caseId, packageResult.mrzValues.toTypedArray())
            .onFailure { error ->
                Timber.e("$logTitle Error - areEIdDocumentsEqual: $error")
                _uiState.update {
                    EIdDocumentScannerUiState.Error(type = DocumentScannerErrorType.GENERIC, onRetry = ::onRetry, onHelp = ::onHelp)
                }
                return@launch
            }
            .onSuccess { areDocumentsEqual ->
                if (!areDocumentsEqual) {
                    Timber.w("$logTitle Error - document are not equal")
                    _uiState.update {
                        EIdDocumentScannerUiState.Error(type = DocumentScannerErrorType.UNEQUAL_DOCUMENTS, onRetry = ::onRetry)
                    }
                    return@launch
                }
            }

        setDocumentScanResult(documentScanResult = packageResult)
        delay(NAVIGATION_DELAY)
        avBeam.shutDown()
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

        _uiState.update {
            EIdDocumentScannerUiState.Error(type = DocumentScannerErrorType.GENERIC, onRetry = ::onRetry, onHelp = ::onHelp)
        }
    }

    private fun resetScanningState() {
        stopScanning()
        avBeam.stopCamera()
        isViewReady.update { false }
    }

    private fun stopScanning() {
        val currentState = _uiState.value as? EIdDocumentScannerUiState.Scan
        if (currentState?.status?.isScanning == true) {
            avBeam.stopScanDocument()
        }
        _uiState.updateScanState {
            copy(status = EIdDocumentScanStatus.FRONTSIDE)
        }
    }

    private fun onRetry() {
        viewModelScope.launch {
            Timber.w("$logTitle - retry")
            resetScanningState()
            prepareScanner()
        }
    }

    private suspend inline fun <T> StateFlow<T>.awaitValue(value: T) =
        this.first { it == value }

    override fun onCleared() {
        resetScanningState()
        super.onCleared()
    }

    fun onHelp() = appContext.openLink(appContext.getString(R.string.tk_error_generic_helpLink_value))

    companion object {
        private const val NAVIGATION_DELAY = 1000L
    }
}
