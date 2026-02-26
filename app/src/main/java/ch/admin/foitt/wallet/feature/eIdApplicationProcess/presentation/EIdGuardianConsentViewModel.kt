package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.QrBoxUiState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GuardianConsentResultState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GuardianVerificationError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GuardianVerificationResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toSIdRequestDisplayStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchGuardianVerification
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateSIdStatusByCaseId
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.generateQRBitmap
import ch.admin.foitt.wallet.platform.utils.shareText
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EIdGuardianConsentViewModel.Factory::class)
internal class EIdGuardianConsentViewModel @AssistedInject constructor(
    private val fetchGuardianVerification: FetchGuardianVerification,
    private val fetchSIdStatus: FetchSIdStatus,
    private val updateSIdStatusByCaseId: UpdateSIdStatusByCaseId,
    private val navManager: NavigationManager,
    @param:ApplicationContext private val appContext: Context,
    @Assisted private val caseId: String,
    setTopBarState: SetTopBarState
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdGuardianConsentViewModel
    }

    override val topBarState: TopBarState = TopBarState.DetailsWithCloseButton(
        titleId = null,
        onUp = { navManager.popBackStackOrToRoot() },
        onClose = { navManager.navigateBackToHomeScreen(popUntil = Destination.EIdIntroScreen::class) },
    )

    private val _isRequestLoading = MutableStateFlow(false)
    val isRequestLoading = _isRequestLoading.asStateFlow()

    private val _isRequestStatusLoading = MutableStateFlow(false)
    val isRequestStatusLoading = _isRequestStatusLoading.asStateFlow()

    private val _guardianVerificationResponse =
        MutableStateFlow<Result<GuardianVerificationResponse, GuardianVerificationError>?>(null)

    val qrBoxState: StateFlow<QrBoxUiState> = combine(isRequestLoading, _guardianVerificationResponse) { isRequestLoading, response ->
        when {
            isRequestLoading || response == null -> QrBoxUiState.Loading
            response.isOk -> {
                val r = response.getOrElse { return@combine QrBoxUiState.Failure }
                QrBoxUiState.Success(
                    qrBitmap = r.legalRepresentantVerificationRequestUrl.generateQRBitmap()
                )
            }

            else -> QrBoxUiState.Failure
        }
    }.toStateFlow(QrBoxUiState.Loading)

    fun onRefreshRequest() {
        viewModelScope.launch {
            refreshRequest()
        }.trackCompletion(_isRequestLoading)
    }

    fun onShareRequest() {
        viewModelScope.launch {
            _guardianVerificationResponse.value?.get()?.let { response ->
                appContext.shareText(
                    textContent = response.legalRepresentantVerifierLink,
                    mimeType = "text/uri-list",
                )
            }
        }
    }

    private suspend fun refreshRequest() {
        _guardianVerificationResponse.update { fetchGuardianVerification(caseId) }
    }

    fun onContinue() = viewModelScope.launch {
        fetchSIdStatus(caseId)
            .onSuccess { stateResponse ->
                updateSIdStatusByCaseId(caseId, stateResponse)

                when (stateResponse.toSIdRequestDisplayStatus()) {
                    SIdRequestDisplayStatus.AV_READY,
                    SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_OK -> navManager.popUpToAndNavigate(
                        popToInclusive = Destination.EIdIntroScreen::class,
                        destination = Destination.EIdReadyForAvScreen,
                    )

                    SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_PENDING -> navigateToResultScreen(
                        state = GuardianConsentResultState.AV_READY_LEGAL_CONSENT_PENDING,
                        deadline = stateResponse.onlineSessionStartTimeout
                    )

                    SIdRequestDisplayStatus.QUEUEING,
                    SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_OK -> navigateToResultScreen(
                        state = GuardianConsentResultState.QUEUEING_LEGAL_CONSENT_OK,
                        deadline = stateResponse.queueInformation?.expectedOnlineSessionStart
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
                    SIdRequestDisplayStatus.OTHER -> navManager.navigateBackToHomeScreen(Destination.EIdIntroScreen::class)
                }
            }
            .onFailure {
                navManager.navigateBackToHomeScreen(Destination.EIdIntroScreen::class)
            }
    }.trackCompletion(_isRequestStatusLoading)

    private fun navigateToResultScreen(state: GuardianConsentResultState, deadline: String?) = navManager.popUpToAndNavigate(
        popToInclusive = Destination.EIdIntroScreen::class,
        destination = Destination.EIdGuardianConsentResultScreen(
            screenState = state,
            rawDeadline = deadline
        ),
    )
}
