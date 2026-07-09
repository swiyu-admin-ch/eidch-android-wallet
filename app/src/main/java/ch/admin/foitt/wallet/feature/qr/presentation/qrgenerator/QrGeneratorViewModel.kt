package ch.admin.foitt.wallet.feature.qr.presentation.qrgenerator

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProximityEngagementError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.toProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.toErrorDestination
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.HandleInvitationProcessingSuccess
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.permission.presentation.bluetooth.BluetoothPermissionViewModel
import ch.admin.foitt.wallet.platform.proximity.domain.model.ProximityEngagementEvent
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.GetProximityRepositoryForScope
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.ProximityEngagement
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrGeneratorViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val proximityEngagement: ProximityEngagement,
    private val handleInvitationProcessingSuccess: HandleInvitationProcessingSuccess,
    getProximityRepositoryForScope: GetProximityRepositoryForScope,
    setTopBarState: SetTopBarState,
) : BluetoothPermissionViewModel(setTopBarState) {

    private val proximityRepository = getProximityRepositoryForScope()

    override val topBarState
        get() = when (bluetoothState.value.isReady()) {
            false -> TopBarState.Details(onUp = ::onUp, titleId = null)
            true -> TopBarState.Details(onUp = ::onUp, titleId = R.string.qr_generator_title)
        }

    fun onUp() = navManager.popBackStack()

    var proximityEngagementJob: Job? = null
    val qrCodePayloadMutable = MutableStateFlow<String?>(null)
    val qrCodePayload = qrCodePayloadMutable.asStateFlow()

    init {
        viewModelScope.launch {
            bluetoothState.collect {
                setTopBarState(topBarState)
            }
        }
    }

    fun reset() {
        proximityEngagementJob?.cancel()
        proximityRepository.reset()
        qrCodePayloadMutable.value = null
    }

    fun startEngagementListener() {
        reset()
        proximityEngagementJob = viewModelScope.launch {
            proximityEngagement().collect {
                it.onSuccess { event ->
                    when (event) {
                        is ProximityEngagementEvent.QrCode -> {
                            qrCodePayloadMutable.value = event.qrCode
                        }

                        is ProximityEngagementEvent.Request -> {
                            proximityEngagementJob?.ensureActive()
                            handleInvitationProcessingSuccess(event.processPresentationRequestResult.toProcessInvitationResult())
                                .navigate()
                        }
                    }
                }.onFailure { failureResult ->
                    when (failureResult) {
                        is ProximityEngagementError.NoCompatibleCredential -> {
                            val destination = failureResult.toProcessInvitationError().toErrorDestination(null)
                            navManager.replaceCurrentWith(destination)
                        }
                        else -> proximityRepository.decline()
                    }
                }
            }
        }
    }
}
