package ch.admin.foitt.wallet.feature.home.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.home.domain.usecase.DeleteEIdRequestCase
import ch.admin.foitt.wallet.feature.home.domain.usecase.GetEIdRequestsFlow
import ch.admin.foitt.wallet.feature.home.presentation.model.HomeContainerState
import ch.admin.foitt.wallet.feature.home.presentation.model.HomeScreenState
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.DeferredCredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshDeferredCredentials
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateAllCredentialStatuses
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayData
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateAllSIdStatuses
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.CredentialOfferEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialOfferEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.refreshableStateFlow
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialsWithDetailsFlow
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetDeferredCredentialsWithDetailsFlow
import ch.admin.foitt.wallet.platform.utils.openLink
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Suppress("TooManyFunctions")
class HomeViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    getCredentialsWithDetailsFlow: GetCredentialsWithDetailsFlow,
    getDeferredCredentialsWithDetailsFlow: GetDeferredCredentialsWithDetailsFlow,
    getEIdRequestsFlow: GetEIdRequestsFlow,
    private val getCredentialCardState: GetCredentialCardState,
    private val updateAllCredentialStatuses: UpdateAllCredentialStatuses,
    private val updateAllSIdStatuses: UpdateAllSIdStatuses,
    private val refreshDeferredCredentials: RefreshDeferredCredentials,
    private val deleteEIdRequestCase: DeleteEIdRequestCase,
    environmentSetupRepository: EnvironmentSetupRepository,
    private val navManager: NavigationManager,
    private val credentialOfferEventRepository: CredentialOfferEventRepository,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.None

    private val _eventMessage = MutableStateFlow<Int?>(null)
    val eventMessage = _eventMessage.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    @OptIn(UnsafeResultValueAccess::class)
    val screenContentState = refreshableStateFlow(initialData = HomeScreenState.Initial) {
        combine(
            getEIdRequestsFlow(),
            getCredentialsWithDetailsFlow(),
            getDeferredCredentialsWithDetailsFlow(),
        ) { eIdRequestsFlow, credentialsWithDetails, deferredCredential ->
            when {
                credentialsWithDetails.isOk && eIdRequestsFlow.isOk && deferredCredential.isOk -> mapToUiState(
                    credentials = credentialsWithDetails.value,
                    deferredCredentials = deferredCredential.value,
                    eIdRequestCases = eIdRequestsFlow.value,
                )

                else -> {
                    navigateTo(Destination.GenericErrorScreen)
                    null
                }
            }
        }.filterNotNull()
    }

    private val _homeContainerState = MutableStateFlow(
        HomeContainerState(
            showEIdRequestButton = environmentSetupRepository.eIdRequestEnabled,
            showBetaIdRequestButton = environmentSetupRepository.betaIdRequestEnabled,
            showMenu = false,
            onScan = { navigateTo(Destination.QrScanPermissionScreen) },
            onGetEId = { navigateTo(Destination.EIdIntroScreen) },
            onGetBetaId = { navigateTo(Destination.BetaIdScreen) },
            onSettings = { navigateTo(Destination.SettingsScreen) },
            onHelp = { onHelp() },
        )
    )
    val homeContainerState = _homeContainerState.asStateFlow()

    init {
        viewModelScope.launch {
            credentialOfferEventRepository.event.collect { event ->
                _eventMessage.value = when (event) {
                    CredentialOfferEvent.ACCEPTED -> R.string.tk_home_notification_credential_accepted
                    CredentialOfferEvent.DECLINED -> R.string.tk_home_notification_credential_declined
                    CredentialOfferEvent.NONE -> null
                }
                if (_eventMessage.value != null) {
                    delay(TOAST_DISPLAY_TIME_MILLIS)
                    credentialOfferEventRepository.resetEvent()
                }
            }
        }
    }

    private suspend fun mapToUiState(
        credentials: List<CredentialDisplayData>,
        deferredCredentials: List<DeferredCredentialDisplayData>,
        eIdRequestCases: List<SIdRequestDisplayData>
    ): HomeScreenState = when {
        credentials.isNotEmpty() || deferredCredentials.isNotEmpty() -> {
            HomeScreenState.CredentialList(
                eIdRequests = filterEIdRequests(eIdRequestCases),
                credentials = getCredentialStateList(
                    credentialsDisplayData = credentials,
                    deferredCredentialsDisplayData = deferredCredentials,
                ),
                onCredentialClick = ::handleCredentialClick,
            )
        }

        eIdRequestCases.isNotEmpty() -> HomeScreenState.NoCredential(
            eIdRequests = filterEIdRequests(eIdRequestCases),
        )

        else -> HomeScreenState.WalletEmpty
    }

    private fun filterEIdRequests(eIdRequestCases: List<SIdRequestDisplayData>): List<SIdRequestDisplayData> {
        return eIdRequestCases.filter { requestCase ->
            requestCase.status != SIdRequestDisplayStatus.OTHER
        }
    }

    private suspend fun getCredentialStateList(
        credentialsDisplayData: List<CredentialDisplayData>,
        deferredCredentialsDisplayData: List<DeferredCredentialDisplayData>,
    ): List<CredentialCardState> {
        val deferredCredentials = deferredCredentialsDisplayData
            .sortedWith { first, second ->
                when (first.status) {
                    second.status -> {
                        when {
                            first.credentialId > second.credentialId -> -1
                            first.credentialId < second.credentialId -> 1
                            else -> 0
                        }
                    }
                    DeferredProgressionState.IN_PROGRESS -> -1
                    DeferredProgressionState.INVALID -> 1
                }
            }
            .map { deferredCredentialDisplayData ->
                getCredentialCardState(deferredCredentialDisplayData)
            }

        val (unacceptedCredentials, acceptedCredential) = credentialsDisplayData
            .map { credentialDisplayData ->
                getCredentialCardState(credentialDisplayData)
            }.partition {
                it.isUnaccepted
            }

        return unacceptedCredentials + deferredCredentials + acceptedCredential
    }

    fun onRefresh() {
        viewModelScope.launch {
            refreshDeferredCredentials()
            updateAllSIdStatuses()
            updateAllCredentialStatuses()
        }.trackCompletion(_isRefreshing)
    }

    fun onRefreshSIdStatuses() {
        if (!isRefreshing.value) {
            viewModelScope.launch {
                refreshDeferredCredentials()
                updateAllSIdStatuses()
            }.trackCompletion(_isRefreshing)
        }
    }

    fun onCloseToast() {
        _eventMessage.value = null
        credentialOfferEventRepository.resetEvent()
    }

    fun onStartOnlineIdentification(caseId: String) {
        navigateTo(Destination.EIdStartAvSessionScreen(caseId = caseId))
    }

    fun onObtainConsent(caseId: String) {
        navigateTo(Destination.EIdGuardianSelectionScreen(caseId = caseId))
    }

    fun onCloseEId(caseId: String) {
        viewModelScope.launch {
            deleteEIdRequestCase(caseId)
        }
    }

    fun onLearnMore() = appContext.openLink(R.string.tk_getEid_notification_declined_faqLink)

    fun onMenu(showMenu: Boolean) {
        _homeContainerState.update { currentState ->
            currentState.copy(showMenu = showMenu)
        }
    }

    private fun handleCredentialClick(id: Long, progressState: VerifiableProgressionState) {
        when (progressState) {
            VerifiableProgressionState.ACCEPTED -> navigateTo(
                Destination.CredentialDetailScreen(credentialId = id)
            )
            VerifiableProgressionState.UNACCEPTED -> navigateTo(
                Destination.CredentialOfferScreen(credentialId = id)
            )
        }
    }

    private fun navigateTo(destination: Destination) {
        // hide menu on navigation, so when coming back it is closed
        onMenu(false)
        navManager.navigateTo(destination)
    }

    fun onHelp() {
        // hide menu on navigation, so when coming back it is closed
        onMenu(false)
        appContext.openLink(R.string.tk_settings_general_help_link_value)
    }

    companion object {
        private const val TOAST_DISPLAY_TIME_MILLIS = 4000L
    }
}
