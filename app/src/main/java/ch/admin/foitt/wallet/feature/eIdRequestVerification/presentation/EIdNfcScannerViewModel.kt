package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamError
import ch.admin.foitt.avwrapper.AVBeamInitConfig
import ch.admin.foitt.avwrapper.AVBeamPackageResult
import ch.admin.foitt.avwrapper.AVBeamStatus
import ch.admin.foitt.avwrapper.AvBeamNotification
import ch.admin.foitt.avwrapper.config.AVBeamConfigLogLevel
import ch.admin.foitt.avwrapper.config.AVBeamScanNfcConfig
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.EIdRequestVerificationError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetDocumentScanData
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SaveEIdRequestFiles
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.nfcScanner.EIdNfcScannerUiState
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFileCategory
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetStartAutoVerificationResult
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openNFCSettings
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.ref.WeakReference

@HiltViewModel(assistedFactory = EIdNfcScannerViewModel.Factory::class)
class EIdNfcScannerViewModel @AssistedInject constructor(
    private val avBeam: AVBeam,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val getStartAutoVerificationResult: GetStartAutoVerificationResult,
    private val saveEIdRequestFiles: SaveEIdRequestFiles,
    private val getDocumentScanData: GetDocumentScanData,
    private val navManager: NavigationManager,
    @param:ApplicationContext private val appContext: Context,
    @Assisted private val caseId: String,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdNfcScannerViewModel
    }

    override val topBarState: TopBarState = TopBarState.WithCloseButton(
        onClose = { navManager.navigateBackToHomeScreen(Destination.EIdNfcScannerScreen::class) },
    )

    private var currentActivity: WeakReference<AppCompatActivity> = WeakReference(null)
    private var currentFlowCollectionJob: Job? = null

    private val nfcScannerState = MutableStateFlow<NfcScannerState>(NfcScannerState.Initializing)
    private val _activityState = MutableSharedFlow<ActivityState>(
        extraBufferCapacity = 8,
    )
    private val activityState = _activityState.toStateFlow(ActivityState.Initial)

    private val nfcScanIsSuccessful = MutableStateFlow(false)
    private val nfcScanIsFinished = MutableStateFlow(false)

    private val nfcFailureCounter = MutableStateFlow(0)

    private val nfcAdapter: NfcAdapter by lazy {
        NfcAdapter.getDefaultAdapter(appContext)
    }
    private val logTag = "NfcScan:"

    init {
        computeState()
    }

    val uiState: StateFlow<EIdNfcScannerUiState> = combine(
        nfcScannerState,
        nfcScanIsFinished,
        nfcFailureCounter,
    ) { nfcScannerState, scanIsFinished, nfcFailureCounter ->
        when (nfcScannerState) {
            is NfcScannerState.ScanSuccess if !scanIsFinished -> EIdNfcScannerUiState.Success
            is NfcScannerState.ScanSuccess -> {
                navigateToSummary(nfcScannerState.packageResult)
                EIdNfcScannerUiState.Success
            }
            is NfcScannerState.Initializing -> EIdNfcScannerUiState.Initializing
            is NfcScannerState.Ready -> EIdNfcScannerUiState.Info
            is NfcScannerState.Scanning -> EIdNfcScannerUiState.Scanning
            is NfcScannerState.ReadingChipData -> EIdNfcScannerUiState.ReadingChipData
            is NfcScannerState.NfcOff -> EIdNfcScannerUiState.NfcDisabled
            is NfcScannerState.ScanFailure,
            is NfcScannerState.UnexpectedError -> handleError(nfcFailureCounter)
        }
    }.toStateFlow(EIdNfcScannerUiState.Initializing)

    private fun handleError(failureCounter: Int) = when {
        failureCounter >= NFC_MIN_FAILURE -> EIdNfcScannerUiState.Failure
        else -> EIdNfcScannerUiState.Error
    }

    private suspend fun initScannerSdk(activity: AppCompatActivity) = withContext(Dispatchers.IO) {
        val logLevel = if (environmentSetupRepository.avBeamLoggingEnabled) {
            AVBeamConfigLogLevel.DEBUG
        } else {
            AVBeamConfigLogLevel.NONE
        }
        avBeam.init(AVBeamInitConfig(logLevel), activity)
        avBeam.initializedFlow.awaitValue(true)
        Timber.d("$logTag initialization done")
    }

    private fun CoroutineScope.setupSdkFlowCollectionJob() {
        currentFlowCollectionJob?.cancel()
        val job = SupervisorJob()
        CoroutineScope(this.coroutineContext + job).apply {
            launch {
                avBeam.statusFlow.collect { status ->
                    Timber.d("$logTag status notification ${status.name}")
                    when (status) {
                        // Somehow it signals the start of the chip reading
                        AVBeamStatus.NfcChipClonedDetectionStart -> nfcScannerState.update { NfcScannerState.ReadingChipData }
                        // This event signal success. When it does not happen, we have to assume an error.
                        AVBeamStatus.NfcDataReadingEndSuccess -> nfcScanIsSuccessful.update { true }
                        else -> {}
                    }
                }
            }
            launch {
                avBeam.scanNfcFlow.collect { notification ->
                    when (notification) {
                        is AvBeamNotification.Completed -> onNfcScanCompleted(notification.packageData)
                        AvBeamNotification.Empty, AvBeamNotification.Initial, AvBeamNotification.Loading -> {}
                    }
                }
            }
            launch {
                avBeam.errorFlow.collect { errorNotification ->
                    if (errorNotification != AVBeamError.None) {
                        Timber.e("$logTag Error - avBeam errorFlow ${errorNotification.name}")
                    }
                }
            }
        }
        currentFlowCollectionJob = job
    }

    private suspend fun onNfcScanCompleted(packageResult: AVBeamPackageResult) {
        Timber.d("$logTag onNfcScanCompleted,\nnfcErrorCode ${packageResult.nfcErrorCode}")
        if (!nfcScanIsSuccessful.value) {
            countAndReturnScanFailure("scan completed", null)
            return
        }

        saveEIdRequestFiles(
            sIdCaseId = caseId,
            filesDataList = packageResult.files,
            filesCategory = EIdRequestFileCategory.NFC_SCAN,
        ).onFailure { error ->
            when (error) {
                is EIdRequestVerificationError.Unexpected -> countAndReturnUnexpectedError("saving files", error.cause)
            }
        }.onSuccess {
            Timber.d("$logTag success saving files")
            nfcScannerState.update { NfcScannerState.ScanSuccess(packageResult) }
            triggerShowSummaryDelay()
        }
    }

    fun onContinue() {
        Timber.w("$logTag scan skipped after ${nfcFailureCounter.value} failures")
        val startAVResult = getStartAutoVerificationResult().value
        when {
            startAVResult == null -> navManager.replaceCurrentWith(Destination.EIdDocumentRecordingScreen(caseId = caseId))
            startAVResult.scanDocument -> navManager.replaceCurrentWith(Destination.EIdDocumentScannerScreen(caseId = caseId))
            startAVResult.recordDocumentVideo -> navManager.replaceCurrentWith(Destination.EIdDocumentRecordingScreen(caseId = caseId))
            else -> navManager.replaceCurrentWith(Destination.EIdStartSelfieVideoScreen(caseId = caseId))
        }
    }

    fun onStartNfcScan() = viewModelScope.launch {
        runSuspendCatching {
            Timber.d("$logTag onStartNfcScan")
            val token = getStartAutoVerificationResult().value?.jwt
                ?: error("Token is null")

            val config = AVBeamScanNfcConfig(
                saveNfcOnMobile = true,
                timeout = 60,
                saveActiveAuth = true,
                saveAdditionalFiles = true,
                authToken = token,
                processId = caseId,
            )
            val packageResult = getDocumentScanData(caseId).get()
                ?: error("Package result is null")

            nfcScanIsSuccessful.update { false }
            nfcScanIsFinished.update { false }
            avBeam.startScanNfc(
                documentScanPackageResult = packageResult,
                config = config,
            )
        }.onFailure {
            countAndReturnScanFailure("starting scan", it)
        }.onSuccess {
            nfcScannerState.update { NfcScannerState.Scanning }
        }
    }

    fun onEnableNfc() = appContext::openNFCSettings

    fun onLifecycleEvent(event: Lifecycle.Event, activity: AppCompatActivity) {
        currentActivity = WeakReference(activity)
        when (event) {
            Lifecycle.Event.ON_CREATE -> _activityState.tryEmit(ActivityState.Created)
            Lifecycle.Event.ON_RESUME -> _activityState.tryEmit(ActivityState.Resumed)
            Lifecycle.Event.ON_PAUSE -> _activityState.tryEmit(ActivityState.Paused)
            Lifecycle.Event.ON_DESTROY -> _activityState.tryEmit(ActivityState.Destroyed)
            Lifecycle.Event.ON_ANY -> {}
            Lifecycle.Event.ON_START -> _activityState.tryEmit(ActivityState.Started)
            Lifecycle.Event.ON_STOP -> _activityState.tryEmit(ActivityState.Stopped)
        }
    }

    fun onBack() {
        navManager.popBackStackOrToRoot()
    }

    override fun onCleared() {
        resetNfcState()
        currentFlowCollectionJob?.cancel()
        currentFlowCollectionJob = null
        super.onCleared()
    }

    fun resetNfcState() {
        runSuspendCatching {
            avBeam.onPauseNfc()
            avBeam.stopScanNfc()
            nfcScanIsFinished.update { false }
            nfcScanIsSuccessful.update { false }
        }.onFailure {
            countAndReturnUnexpectedError("resetting NFC", it)
        }.onSuccess {
            nfcScannerState.update { NfcScannerState.Ready }
        }
    }

    fun onNewIntent(intent: Intent) {
        viewModelScope.launch {
            runSuspendCatching {
                Timber.d("$logTag onNewIntent delivered $intent")
                if (intent.action != NfcAdapter.ACTION_TECH_DISCOVERED) {
                    Timber.d("$logTag onNewIntent ignored")
                    return@launch
                }
                avBeam.onNewIntentNfc(intent)
            }.onFailure {
                countAndReturnUnexpectedError("passing intent", it)
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun computeState() {
        viewModelScope.launch {
            combine(
                nfcScannerState,
                activityState,
            ) { scannerState, activityState ->
                Timber.d(
                    "$logTag combine" +
                        "\n NfcState: ${nfcScannerState.value::class.simpleName}," +
                        "\n ActivityState: $activityState"
                )
                val currentActivity = currentActivity.get()
                when {
                    activityState is ActivityState.Initial || currentActivity == null -> {
                        nfcScannerState.update { NfcScannerState.Initializing }
                    }

                    activityState is ActivityState.Started -> {
                        setupSdkFlowCollectionJob()
                        runSuspendCatching {
                            initScannerSdk(currentActivity)
                            avBeam.initNfcCardReader(currentActivity, environmentSetupRepository.eIdNfcWebSocketUrl)
                            if (nfcAdapter.isEnabled) {
                                nfcScannerState.update { NfcScannerState.Ready }
                            }
                        }.onFailure {
                            countAndReturnUnexpectedError("initializing NFC", it)
                        }
                    }

                    activityState is ActivityState.Resumed &&
                        !nfcAdapter.isEnabled &&
                        scannerState !is NfcScannerState.NfcOff -> {
                        runSuspendCatching {
                            if (avBeam.initializedFlow.value) {
                                avBeam.onPauseNfc()
                                if (scannerState is NfcScannerState.Scanning || scannerState is NfcScannerState.ReadingChipData) {
                                    avBeam.stopScanNfc()
                                }
                            }
                        }.onFailure {
                            countAndReturnUnexpectedError("stopping scan", it)
                        }
                        nfcScannerState.update { NfcScannerState.NfcOff }
                    }

                    activityState is ActivityState.Resumed &&
                        nfcAdapter.isEnabled &&
                        scannerState is NfcScannerState.NfcOff -> {
                        nfcScannerState.update { NfcScannerState.Ready }
                    }

                    activityState is ActivityState.Resumed &&
                        (scannerState is NfcScannerState.Scanning || scannerState is NfcScannerState.ReadingChipData) -> {
                        runSuspendCatching {
                            avBeam.onResumeNfc()
                        }.onFailure {
                            countAndReturnUnexpectedError("resuming scan", it)
                        }
                    }
                    // At that point the scanner is neither scanning nor reading.
                    activityState is ActivityState.Resumed &&
                        (scannerState is NfcScannerState.ScanFailure || scannerState is NfcScannerState.UnexpectedError) -> {
                        runSuspendCatching {
                            avBeam.onPauseNfc()
                            avBeam.stopScanNfc()
                        }.onFailure {
                            countAndReturnUnexpectedError("stopping scan", it)
                        }
                    }

                    activityState is ActivityState.Paused -> {
                        runSuspendCatching {
                            avBeam.onPauseNfc()
                        }.onFailure {
                            countAndReturnUnexpectedError("pausing scan", it)
                        }
                    }
                }
            }.collect()
        }
    }

    private fun countAndReturnUnexpectedError(contextMessage: String, throwable: Throwable?) = nfcScannerState.update {
        nfcFailureCounter.update { it + 1 }
        Timber.e(t = throwable, message = "$logTag Unexpected error when $contextMessage, failureCounter: ${nfcFailureCounter.value}")
        NfcScannerState.UnexpectedError
    }

    private fun countAndReturnScanFailure(contextMessage: String, throwable: Throwable?) = nfcScannerState.update {
        nfcFailureCounter.update { it + 1 }
        Timber.w(t = throwable, message = "$logTag scan failure when $contextMessage, failureCounter: ${nfcFailureCounter.value}")
        NfcScannerState.ScanFailure
    }

    private suspend fun triggerShowSummaryDelay() = runSuspendCatching {
        avBeam.onPauseNfc()
        avBeam.stopScanNfc()
        delay(SUCCESS_DELAY_MS)
        nfcScanIsFinished.update { true }
    }.onFailure {
        Timber.e(t = it, message = "$logTag unexpected error when stopping the NFC scan")
    }

    private suspend fun navigateToSummary(packageResult: AVBeamPackageResult) {
        val lastName = packageResult.getField(113)
        val firstName = packageResult.getField(114)
        val documentId = packageResult.getField(115)
        val expiryDate = packageResult.getField(119)
        val nfcImage = packageResult.nfcAvatar

        navManager.replaceCurrentWith(
            Destination.EIdNfcSummaryScreen(
                caseId = caseId,
                picture = nfcImage,
                givenName = firstName,
                surname = lastName,
                documentId = documentId,
                expiryDate = expiryDate,
            )
        )
    }

    private suspend inline fun <T> StateFlow<T>.awaitValue(value: T) =
        this.first { it == value }

    sealed interface NfcScannerState {
        data object NfcOff : NfcScannerState
        data object Initializing : NfcScannerState
        data object Ready : NfcScannerState
        data object Scanning : NfcScannerState
        data object ReadingChipData : NfcScannerState
        data object ScanFailure : NfcScannerState
        data class ScanSuccess(
            val packageResult: AVBeamPackageResult,
        ) : NfcScannerState

        data object UnexpectedError : NfcScannerState
    }

    sealed interface ActivityState {
        data object Initial : ActivityState
        data object Created : ActivityState
        data object Resumed : ActivityState
        data object Paused : ActivityState
        data object Destroyed : ActivityState
        data object Started : ActivityState
        data object Stopped : ActivityState
    }
    private fun AVBeamPackageResult.getField(index: Int): String = this.data?.getValue(index) ?: "-"
    private val AVBeamPackageResult.nfcAvatar
        get() = this.files?.value?.firstOrNull { file ->
            file.fileDescription == "images/id_document_nfc/NFCAvatar.jpg"
        }?.fileData ?: byteArrayOf()

    companion object {
        private const val SUCCESS_DELAY_MS = 1500L
        private const val NFC_MIN_FAILURE = 3
    }
}
