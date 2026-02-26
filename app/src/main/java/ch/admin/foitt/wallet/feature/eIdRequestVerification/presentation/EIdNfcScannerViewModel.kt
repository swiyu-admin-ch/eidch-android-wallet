package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamInitConfig
import ch.admin.foitt.avwrapper.AVBeamPackageResult
import ch.admin.foitt.avwrapper.AVBeamStatus
import ch.admin.foitt.avwrapper.AvBeamNotification
import ch.admin.foitt.avwrapper.config.AVBeamConfigLogLevel
import ch.admin.foitt.avwrapper.config.AVBeamScanNfcConfig
import ch.admin.foitt.wallet.R
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
import ch.admin.foitt.wallet.platform.utils.openLink
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

    override val topBarState: TopBarState = TopBarState.EmptyWithCloseButton(
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

    private val nfcAdapter: NfcAdapter by lazy {
        NfcAdapter.getDefaultAdapter(appContext)
    }

    init {
        computeState()
    }

    val uiState: StateFlow<EIdNfcScannerUiState> = combine(
        nfcScannerState,
        nfcScanIsFinished,
    ) { nfcScannerState, scanIsFinished ->
        when {
            nfcScannerState is NfcScannerState.ScanSuccess && !scanIsFinished -> EIdNfcScannerUiState.Success
            nfcScannerState is NfcScannerState.ScanSuccess -> {
                navigateToSummary(nfcScannerState.packageResult)
                EIdNfcScannerUiState.Success
            }

            nfcScannerState is NfcScannerState.Initializing -> {
                EIdNfcScannerUiState.Initializing
            }

            nfcScannerState is NfcScannerState.Ready -> EIdNfcScannerUiState.Info(
                onStart = ::onStartNfcScan,
                onTips = ::onTips,
            )

            nfcScannerState is NfcScannerState.Scanning -> EIdNfcScannerUiState.Scanning(
                onStop = ::resetNFCState,
            )

            nfcScannerState is NfcScannerState.ReadingChipData -> EIdNfcScannerUiState.ReadingChipData(
                onStop = ::resetNFCState,
            )

            nfcScannerState is NfcScannerState.NfcOff -> EIdNfcScannerUiState.NfcDisabled(
                onEnable = appContext::openNFCSettings,
            )

            nfcScannerState is NfcScannerState.ScanFailure -> {
                EIdNfcScannerUiState.Error(
                    onRetry = ::onStartNfcScan,
                )
            }

            else -> EIdNfcScannerUiState.Error(
                onRetry = ::onStartNfcScan,
            )
        }
    }.toStateFlow(EIdNfcScannerUiState.Initializing)

    private suspend fun initScannerSdk(activity: AppCompatActivity) = withContext(Dispatchers.IO) {
        val logLevel = if (environmentSetupRepository.avBeamLoggingEnabled) {
            AVBeamConfigLogLevel.DEBUG
        } else {
            AVBeamConfigLogLevel.NONE
        }
        avBeam.init(AVBeamInitConfig(logLevel), activity)
        avBeam.initializedFlow.awaitValue(true)
        Timber.d("NfcScan: initialization done")
    }

    private fun CoroutineScope.setupSdkFlowCollectionJob() {
        currentFlowCollectionJob?.cancel()
        val job = SupervisorJob()
        CoroutineScope(this.coroutineContext + job).apply {
            launch {
                avBeam.statusFlow.collect { status ->
                    Timber.d("NfcScan: status notification ${status.name}")
                    when (status) {
                        // Somehow it signals the start of the chip reading
                        AVBeamStatus.NFC_ChipClonedDetectionStart -> nfcScannerState.update { NfcScannerState.ReadingChipData }
                        // This event signal success. When it does not happen, we have to assume an error.
                        AVBeamStatus.NFC_DataReadingEndSuccess -> nfcScanIsSuccessful.update { true }
                        else -> {}
                    }
                }
            }
            launch {
                avBeam.scanNfcFlow.collect { notification ->
                    when (notification) {
                        is AvBeamNotification.Completed -> onNfcScanCompleted(notification.packageData)
                        is AvBeamNotification.Error -> onNfcScanFailed(notification.packageData)
                        AvBeamNotification.Empty, AvBeamNotification.Initial -> {}
                        AvBeamNotification.Loading -> {}
                    }
                }
            }
            launch {
                avBeam.errorFlow.collect { errorNotification ->
                    Timber.d("NfcScan error: ${errorNotification.name}")
                }
            }
        }
        currentFlowCollectionJob = job
    }

    private suspend fun onNfcScanCompleted(packageResult: AVBeamPackageResult) {
        Timber.d("NfcScan: onNfcScanCompleted,\nerrorCode${packageResult.errorCode}")
        if (!nfcScanIsSuccessful.value) {
            nfcScannerState.update { NfcScannerState.ScanFailure }
            return
        }

        saveEIdRequestFiles(
            sIdCaseId = caseId,
            filesDataList = packageResult.files,
            filesCategory = EIdRequestFileCategory.NFC_SCAN,
        ).onFailure { error ->
            Timber.d("NfcScan: error saving files $error")
            nfcScannerState.update { NfcScannerState.ScanFailure }
        }.onSuccess {
            Timber.d("NfcScan: success saving files")
            nfcScannerState.update { NfcScannerState.ScanSuccess(packageResult) }
            triggerShowSummaryDelay()
        }
    }

    private suspend fun onNfcScanFailed(packageResult: AVBeamPackageResult?) {
        // It seems it never happen in practice
        Timber.d("NfcScan: onNfcScanFailed,\nerrorCode${packageResult?.errorCode}")
        nfcScannerState.update { NfcScannerState.ScanFailure }
    }

    private fun onStartNfcScan() = viewModelScope.launch {
        runSuspendCatching {
            Timber.d("NfcScan: onStartNfcScan")
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
            Timber.e(t = it)
            nfcScannerState.update { NfcScannerState.UnexpectedError }
        }.onSuccess {
            nfcScannerState.update { NfcScannerState.Scanning }
        }
    }

    private fun onTips() = appContext.openLink(appContext.getString(R.string.tk_eidRequest_nfcScan_helpLink))

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
        resetNFCState()
        currentFlowCollectionJob?.cancel()
        currentFlowCollectionJob = null
        super.onCleared()
    }

    private fun resetNFCState() {
        runSuspendCatching {
            avBeam.onPauseNfc()
            avBeam.stopScanNfc()
            nfcScanIsFinished.update { false }
            nfcScanIsSuccessful.update { false }
        }.onFailure {
            Timber.e(t = it)
            nfcScannerState.update { NfcScannerState.UnexpectedError }
        }.onSuccess {
            nfcScannerState.update { NfcScannerState.Ready }
        }
    }

    fun onNewIntent(intent: Intent) {
        viewModelScope.launch {
            runSuspendCatching {
                Timber.d("NfcScan: onNewIntent delivered $intent")
                if (intent.action != NfcAdapter.ACTION_TECH_DISCOVERED) {
                    Timber.d("NfcScan: onNewIntent ignored")
                    return@launch
                }
                avBeam.onNewIntentNfc(intent)
            }.onFailure {
                Timber.e(t = it)
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
                    "NfcScan: combine" +
                        "\n  NfcState: ${nfcScannerState.value::class.simpleName}," +
                        "\n  ActivityState: $activityState"
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
                            Timber.e(t = it)
                            nfcScannerState.update { NfcScannerState.UnexpectedError }
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
                            Timber.e(t = it)
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
                            Timber.e(t = it)
                            nfcScannerState.update { NfcScannerState.UnexpectedError }
                        }
                    }
                    // At that point the scanner is neither scanning nor reading.
                    activityState is ActivityState.Resumed &&
                        (scannerState is NfcScannerState.ScanFailure || scannerState is NfcScannerState.UnexpectedError) -> {
                        runSuspendCatching {
                            avBeam.onPauseNfc()
                            avBeam.stopScanNfc()
                        }.onFailure {
                            Timber.e(t = it)
                            nfcScannerState.update { NfcScannerState.UnexpectedError }
                        }
                    }

                    activityState is ActivityState.Paused -> {
                        runSuspendCatching {
                            avBeam.onPauseNfc()
                        }.onFailure {
                            Timber.e(t = it)
                            nfcScannerState.update { NfcScannerState.UnexpectedError }
                        }
                    }
                }
            }.collect()
        }
    }

    private suspend fun triggerShowSummaryDelay() = runSuspendCatching {
        avBeam.onPauseNfc()
        avBeam.stopScanNfc()
        delay(SUCCESS_DELAY_MS)
        nfcScanIsFinished.update { true }
    }.onFailure {
        Timber.e(t = it)
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
    }
}
