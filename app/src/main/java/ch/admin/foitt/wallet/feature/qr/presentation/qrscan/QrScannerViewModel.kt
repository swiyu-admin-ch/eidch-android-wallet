package ch.admin.foitt.wallet.feature.qr.presentation.qrscan

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseErrorBody
import ch.admin.foitt.openid4vc.domain.usecase.DeclinePresentation
import ch.admin.foitt.wallet.feature.qr.domain.model.qrscan.FlashLightState
import ch.admin.foitt.wallet.feature.qr.infra.qrscan.QrScanner
import ch.admin.foitt.wallet.platform.appSetupState.domain.usecase.GetFirstCredentialWasAdded
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.toErrorDestination
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.HandleInvitationProcessingSuccess
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ProcessInvitation
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.permission.domain.model.BluetoothState
import ch.admin.foitt.wallet.platform.permission.domain.model.CameraState
import ch.admin.foitt.wallet.platform.permission.presentation.bluetooth.BluetoothPermissionViewModel
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.GetProximityRepositoryForScope
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.hasCameraPermission
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class QrScannerViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val processInvitation: ProcessInvitation,
    private val handleInvitationProcessingSuccess: HandleInvitationProcessingSuccess,
    private val declinePresentation: DeclinePresentation,
    private val qrScanner: QrScanner,
    private val navManager: NavigationManager,
    private val getFirstCredentialWasAdded: GetFirstCredentialWasAdded,
    getProximityRepositoryForScope: GetProximityRepositoryForScope,
    environmentSetupRepository: EnvironmentSetupRepository,
    setTopBarState: SetTopBarState,
) : BluetoothPermissionViewModel(setTopBarState) {

    private val proximityRepository = getProximityRepositoryForScope()

    val isProximityEngagementEnabled = environmentSetupRepository.isProximityEngagementEnabled

    private val cameraStateMutable = MutableStateFlow(
        CameraState(isPermissionGranted = hasCameraPermission(appContext))
    )
    private val cameraState = cameraStateMutable.asStateFlow()

    val isCameraRunning = combine(bluetoothState, cameraState) { bluetooth: BluetoothState, camera: CameraState ->
        (isProximityEngagementEnabled.not() || bluetooth.isReady()) && camera.isPermissionGranted
    }.toStateFlow(false)

    override val topBarState
        get() = when (isCameraRunning.value) {
            true -> TopBarState.None
            false -> TopBarState.Details(onUp = ::onUp, titleId = null)
        }

    val flashLightState = qrScanner.flashLightState

    private val _infoState = MutableStateFlow<QrInfoState>(QrInfoState.Empty)
    val infoState = _infoState.asStateFlow()

    private var job: Job? = null

    val scanIsRunning = qrScanner.isRunning

    fun initProximity() {
        proximityRepository.reset()
    }

    fun reset() {
        job?.cancel()
        proximityRepository.reset()
        viewModelScope.launch {
            if (flashLightState.value == FlashLightState.ON) {
                qrScanner.toggleFlashLight()
            }
            qrScanner.unRegisterScanner()
        }
    }

    fun onInitScan(previewView: PreviewView) {
        qrScanner.registerScanner(previewView).onFailure { throwable ->
            _infoState.update { QrInfoState.UnexpectedError }
            Timber.e(t = throwable, message = "ValidationFailure while registering scanner")
        }
    }

    private fun onQrScanSuccess(barcodesContent: List<String>) {
        if (job?.isActive == true) {
            return
        }
        qrScanner.pauseScanner()
        _infoState.update { QrInfoState.Loading }
        job = viewModelScope.launch {
            val invitationUri = barcodesContent.firstOrNull() ?: ""
            processInvitation(invitationUri = invitationUri)
                .onSuccess { invitation ->
                    job?.ensureActive()
                    handleInvitationProcessingSuccess(invitation).navigate()
                }
                .onFailure { invitationError ->
                    job?.ensureActive()
                    handleProcessingFailure(invitationError)
                }
        }.apply {
            invokeOnCompletion {
                this@QrScannerViewModel.job = null
                if (infoState.value is QrInfoState.Loading) {
                    _infoState.update { QrInfoState.Empty }
                }
            }
        }
    }

    private suspend fun handleProcessingFailure(failureResult: ProcessInvitationError) {
        _infoState.update { failureResult.toQrInfoState() }

        if (proximityRepository.isPresentationStarted) {
            when (failureResult) {
                is InvitationError.NoCompatibleCredential,
                is InvitationError.EmptyWallet -> navigateToInvitationFailureError(failureResult, null)
                else -> proximityRepository.decline()
            }
        } else {
            when (failureResult) {
                is InvitationError.InvalidPresentation -> declinePresentation(
                    url = failureResult.responseUri,
                    reason = AuthorizationResponseErrorBody.ErrorType.INVALID_REQUEST
                )
                is InvitationError.InvalidTransactionData -> {
                    declinePresentation(
                        url = failureResult.responseUri,
                        reason = AuthorizationResponseErrorBody.ErrorType.INVALID_REQUEST
                    )
                    navigateToInvitationFailureError(failureResult, failureResult.responseUri)
                }
                is InvitationError.InvalidClientPresentation -> {
                    declinePresentation(
                        url = failureResult.responseUri,
                        reason = AuthorizationResponseErrorBody.ErrorType.INVALID_CLIENT
                    )
                    navigateToInvitationFailureError(failureResult, failureResult.responseUri)
                }
                is InvitationError.EmptyWallet -> navigateToInvitationFailureError(failureResult, failureResult.responseUri)
                is InvitationError.NoCompatibleCredential -> navigateToInvitationFailureError(failureResult, failureResult.responseUri)
                InvitationError.CredentialRequestDenied,
                InvitationError.InsufficientScope,
                InvitationError.InvalidClient,
                InvitationError.InvalidCredentialRequest,
                InvitationError.InvalidEncryptionParameters,
                InvitationError.InvalidNonce,
                InvitationError.InvalidProof,
                InvitationError.InvalidRequest,
                InvitationError.InvalidRequestBearerToken,
                InvitationError.InvalidToken,
                InvitationError.UnauthorizedClient,
                InvitationError.UnauthorizedGrantType,
                InvitationError.UnknownCredentialIdentifier,
                InvitationError.UnknownCredentialConfiguration -> navigateToInvitationFailureError(failureResult, null)

                else -> {}
            }
        }
        delay(DELAY_SCANNING_IN_MS)
        if (infoState.value !is QrInfoState.Loading) {
            qrScanner.resumeScanner()
        }
    }

    private fun navigateToInvitationFailureError(failureResult: ProcessInvitationError, uri: String?) {
        val destination = failureResult.toErrorDestination(uri)
        navManager.replaceCurrentWith(destination)
    }

    private fun initAnalyser() {
        qrScanner.initAnalyser(onBarcodesScanned = ::onQrScanSuccess)
    }

    fun onFlashLight() {
        viewModelScope.launch {
            qrScanner.toggleFlashLight()
        }
    }

    fun onUp() = navManager.popBackStack()

    fun onCloseToast() {
        _infoState.update { QrInfoState.Empty }
        job?.cancel()
        qrScanner.resumeScanner()
    }

    private suspend fun initQrInfoHint() {
        if (getFirstCredentialWasAdded().not()) {
            _infoState.value = QrInfoState.Hint
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun ProcessInvitationError.toQrInfoState() = when (this) {
        InvitationError.InvalidInput -> QrInfoState.InvalidQr
        is InvitationError.MetadataMisconfiguration -> QrInfoState.InvalidCredentialOffer
        InvitationError.InvalidCredentialOffer -> QrInfoState.InvalidCredentialOffer
        InvitationError.CredentialOfferExpired -> QrInfoState.ExpiredCredentialOffer
        InvitationError.NetworkError -> QrInfoState.NetworkError

        // unexpected error when fetching presentation request
        InvitationError.InvalidPresentationRequest -> QrInfoState.InvalidPresentation

        // real presentation request errors
        is InvitationError.NoCompatibleCredential,
        is InvitationError.EmptyWallet -> QrInfoState.Empty // -> go to grey error screen
        is InvitationError.InvalidClientPresentation,
        is InvitationError.InvalidTransactionData -> QrInfoState.Empty // -> go to spec error screen
        is InvitationError.InvalidPresentation -> QrInfoState.InvalidPresentation // -> Show invalid presentation toast

        InvitationError.Unexpected -> QrInfoState.UnexpectedError
        InvitationError.UnknownIssuer -> QrInfoState.UnknownIssuer
        InvitationError.UnknownVerifier -> QrInfoState.UnknownVerifier
        InvitationError.UnsupportedKeyStorageSecurityLevel -> QrInfoState.UnsupportedKeyStorageSecurityLevel
        InvitationError.IncompatibleDeviceKeyStorage -> QrInfoState.IncompatibleDeviceKeyStorage

        InvitationError.CredentialRequestDenied,
        InvitationError.InsufficientScope,
        InvitationError.InvalidClient,
        InvitationError.InvalidCredentialRequest,
        InvitationError.InvalidEncryptionParameters,
        InvitationError.InvalidNonce,
        InvitationError.InvalidProof,
        InvitationError.InvalidRequest,
        InvitationError.InvalidRequestBearerToken,
        InvitationError.InvalidToken,
        InvitationError.UnauthorizedClient,
        InvitationError.UnauthorizedGrantType,
        InvitationError.UnknownCredentialConfiguration,
        InvitationError.UnknownCredentialIdentifier -> QrInfoState.Empty
    }

    fun onCameraStateChange() {
        cameraStateMutable.value = CameraState(isPermissionGranted = hasCameraPermission(appContext))
    }

    init {
        viewModelScope.launch {
            isCameraRunning.collect { isReady ->
                if (isReady) {
                    initAnalyser()
                }
            }
        }
        viewModelScope.launch {
            initQrInfoHint()
        }
    }

    override fun onCleared() {
        qrScanner.unRegisterScanner()
        super.onCleared()
    }

    companion object {
        private const val DELAY_SCANNING_IN_MS = 1500L
    }
}
