package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.GuardianVerificationUiState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GuardianConsentResultState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GuardianVerificationError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GuardianVerificationResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toSIdRequestDisplayStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchGuardianVerification
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateSIdStatusByCaseId
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ProcessInvitation
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.annotation.UnsafeResultErrorAccess
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import com.github.michaelbull.result.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EIdGuardianVerificationViewModel.Factory::class)
internal class EIdGuardianVerificationViewModel @AssistedInject constructor(
    private val fetchGuardianVerification: FetchGuardianVerification,
    private val fetchSIdStatus: FetchSIdStatus,
    private val updateSIdStatusByCaseId: UpdateSIdStatusByCaseId,
    private val processInvitation: ProcessInvitation,
    private val navManager: NavigationManager,
    @param:ApplicationContext private val appContext: Context,
    @Assisted private val caseId: String,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdGuardianVerificationViewModel
    }

    override val topBarState: TopBarState = TopBarState.Details(
        onUp = ::onBack,
        titleId = null,
    )

    private val isLoading = MutableStateFlow(false)
    private val wasPresentationTriggered = MutableStateFlow(false)

    private val fetchGuardianVerificationResult =
        MutableStateFlow<Result<GuardianVerificationResponse, GuardianVerificationError>?>(null)
    private val processInvitationResult =
        MutableStateFlow<Result<ProcessInvitationResult, ProcessInvitationError>?>(null)
    private val fetchSIdStatusResult = MutableStateFlow<Result<StateResponse, StateRequestError>?>(null)

    @OptIn(UnsafeResultValueAccess::class, UnsafeResultErrorAccess::class)
    val uiState: StateFlow<GuardianVerificationUiState> = combine(
        isLoading,
        wasPresentationTriggered,
        fetchGuardianVerificationResult,
        processInvitationResult,
        fetchSIdStatusResult,
    ) { isLoading, wasPresentationTriggered, guardianVerificationResult, processInvitationResult, fetchSIdStatusResult ->
        when {
            wasPresentationTriggered && isLoading -> uiStateLoading
            wasPresentationTriggered && fetchSIdStatusResult?.get() != null -> handleVerificationDone(
                sIdStatus = fetchSIdStatusResult.value,
            )

            !wasPresentationTriggered && processInvitationResult?.get() != null -> handleProcessInvitationSuccess(
                processInvitationResult.value
            )

            processInvitationResult?.getError() != null -> handleProcessInvitationError(
                processInvitationResult.error
            )

            guardianVerificationResult?.getError() != null -> handleFetchGuardianVerificationError(
                guardianVerificationResult.error,
            )

            fetchSIdStatusResult?.getError() != null -> handleFetchSIdStatusError(
                fetchSIdStatusResult.error,
            )

            !wasPresentationTriggered -> GuardianVerificationUiState.Info(isLoading, ::onStartVerification)
            else -> uiStateLoading
        }
    }.toStateFlow(GuardianVerificationUiState.Info(isLoading.value, ::onStartVerification))

    private fun onStartVerification() {
        viewModelScope.launch {
            fetchSIdStatusResult.update { null }
            fetchGuardianVerificationResult.update { null }
            processInvitationResult.update { null }
            wasPresentationTriggered.update { false }

            fetchGuardianVerificationResult.update { fetchGuardianVerification(caseId) }
            fetchGuardianVerificationResult.value?.onSuccess { guardianVerificationResponse ->
                processInvitationResult.update {
                    processInvitation(guardianVerificationResponse.legalRepresentantVerificationRequestUrl)
                }
            }
        }.trackCompletion(isLoading)
    }

    fun onResume() {
        if (wasPresentationTriggered.value) {
            viewModelScope.launch {
                fetchSIdStatusResult.update { fetchSIdStatus(caseId) }
                fetchSIdStatusResult.value?.onSuccess {
                    updateSIdStatusByCaseId(caseId, it)
                }
            }.trackCompletion(isLoading)
        }
    }

    private fun handleProcessInvitationSuccess(
        processInvitationResult: ProcessInvitationResult,
    ): GuardianVerificationUiState = when (processInvitationResult) {
        is ProcessInvitationResult.DeferredCredential,
        is ProcessInvitationResult.CredentialOffer -> uiStateUnexpectedError

        is ProcessInvitationResult.PresentationRequest -> {
            val destination = Destination.PresentationRequestScreen(
                compatibleCredential = processInvitationResult.credential,
                presentationRequestWithRaw = processInvitationResult.request,
                shouldFetchTrustStatement = processInvitationResult.shouldCheckTrustStatement
            )
            navManager.navigateTo(destination)
            wasPresentationTriggered.update { true }
            uiStateLoading
        }

        is ProcessInvitationResult.PresentationRequestCredentialList -> {
            val destination = Destination.PresentationCredentialListScreen(
                compatibleCredentials = processInvitationResult.credentials,
                presentationRequestWithRaw = processInvitationResult.request,
                shouldFetchTrustStatement = processInvitationResult.shouldCheckTrustStatement
            )
            navManager.navigateTo(destination)
            wasPresentationTriggered.update { true }
            uiStateLoading
        }
    }

    private fun handleProcessInvitationError(
        processInvitationError: ProcessInvitationError,
    ): GuardianVerificationUiState = when (processInvitationError) {
        is InvitationError.NoCompatibleCredential,
        is InvitationError.EmptyWallet -> uiStateNoValidCredential

        InvitationError.NetworkError -> uiStateNetworkError
        InvitationError.CredentialOfferExpired,
        InvitationError.IncompatibleDeviceKeyStorage,
        InvitationError.InvalidCredentialOffer,
        InvitationError.InvalidInput,
        is InvitationError.InvalidPresentation,
        InvitationError.InvalidPresentationRequest,
        InvitationError.Unexpected,
        InvitationError.UnknownIssuer,
        InvitationError.UnknownVerifier,
        is InvitationError.MetadataMisconfiguration,
        InvitationError.UnsupportedKeyStorageSecurityLevel -> uiStateUnexpectedError
    }

    private fun handleVerificationDone(sIdStatus: StateResponse): GuardianVerificationUiState {
        when (sIdStatus.toSIdRequestDisplayStatus()) {
            SIdRequestDisplayStatus.AV_READY,
            SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_OK -> navManager.popUpToAndNavigate(
                popToInclusive = Destination.EIdGuardianSelectionScreen::class,
                destination = Destination.EIdReadyForAvScreen,
            )

            SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_PENDING -> navigateToResultScreen(
                state = GuardianConsentResultState.AV_READY_LEGAL_CONSENT_PENDING,
                deadline = sIdStatus.onlineSessionStartTimeout
            )

            SIdRequestDisplayStatus.QUEUEING,
            SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_OK -> navigateToResultScreen(
                state = GuardianConsentResultState.QUEUEING_LEGAL_CONSENT_OK,
                deadline = sIdStatus.queueInformation?.expectedOnlineSessionStart
            )

            SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_PENDING -> navigateToResultScreen(
                state = GuardianConsentResultState.QUEUEING_LEGAL_CONSENT_PENDING,
                deadline = null
            )

            SIdRequestDisplayStatus.AV_EXPIRED,
            SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_OK,
            SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_PENDING -> navigateToResultScreen(
                state = GuardianConsentResultState.AV_EXPIRED_LEGAL_CONSENT_PENDING,
                deadline = null
            )

            SIdRequestDisplayStatus.IN_ISSUANCE,
            SIdRequestDisplayStatus.IN_AGENT_REVIEW,
            SIdRequestDisplayStatus.REFUSED,
            SIdRequestDisplayStatus.UNKNOWN,
            SIdRequestDisplayStatus.OTHER -> navManager.navigateBackToHomeScreen(Destination.EIdGuardianSelectionScreen::class)
        }
        return uiStateLoading
    }

    private fun navigateToResultScreen(state: GuardianConsentResultState, deadline: String?) = navManager.popUpToAndNavigate(
        popToInclusive = Destination.EIdGuardianSelectionScreen::class,
        destination = Destination.EIdGuardianConsentResultScreen(
            screenState = state,
            rawDeadline = deadline
        )
    )

    private fun handleFetchGuardianVerificationError(
        guardianVerificationError: GuardianVerificationError,
    ): GuardianVerificationUiState = when (guardianVerificationError) {
        EIdRequestError.InvalidClientAttestation,
        is EIdRequestError.Unexpected -> uiStateUnexpectedError

        EIdRequestError.NetworkError -> uiStateNetworkError
    }

    private fun handleFetchSIdStatusError(
        sIdStatusError: StateRequestError,
    ): GuardianVerificationUiState = when (sIdStatusError) {
        EIdRequestError.InvalidClientAttestation,
        is EIdRequestError.Unexpected -> uiStateUnexpectedError

        EIdRequestError.NetworkError -> uiStateNetworkError
    }

    private fun onBack() = navManager.popBackStack()

    private fun onRequestEId() = navManager.popUpToAndNavigate(
        popToInclusive = Destination.EIdGuardianSelectionScreen::class,
        destination = Destination.EIdIntroScreen
    )

    private fun onHelp() = appContext.openLink(R.string.tk_eidRequest_guardianVerification_noCredential_link_url)

    //region uiStates
    private val uiStateNetworkError
        get() = GuardianVerificationUiState.NetworkError(
            onRetry = ::onStartVerification,
            onClose = ::onBack,
        )

    private val uiStateUnexpectedError
        get() = GuardianVerificationUiState.UnexpectedError(
            onClose = ::onBack,
        )

    private val uiStateNoValidCredential
        get() = GuardianVerificationUiState.NoValidCredential(
            onRequestEId = ::onRequestEId,
            onCancel = ::onBack,
            onHelp = ::onHelp,
        )

    private val uiStateLoading
        get() = GuardianVerificationUiState.Loading(
            onCancel = ::onBack,
        )
    //endregion
}
