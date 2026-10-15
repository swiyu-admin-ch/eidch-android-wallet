package ch.admin.foitt.wallet.feature.home.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.HasExhaustedBatchCopies
import ch.admin.foitt.wallet.feature.home.domain.usecase.DeleteEIdRequestCase
import ch.admin.foitt.wallet.feature.home.domain.usecase.EIdRequestsPriorityOrdering
import ch.admin.foitt.wallet.feature.home.domain.usecase.GetEIdRequestsFlow
import ch.admin.foitt.wallet.feature.home.presentation.model.HomeContainerState
import ch.admin.foitt.wallet.feature.home.presentation.model.HomeScreenState
import ch.admin.foitt.wallet.feature.otp.domain.repository.OtpStateCompletionRepository
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.DeferredCredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.repository.CredentialRefreshRepository
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshDeferredCredentials
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshVerifiableCredentials
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialComparator
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateAllCredentialStatuses
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateCachedImages
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayData
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.PollSIdRequestAfterFileSubmit
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateAllSIdStatuses
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.CredentialEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.NonComplianceEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialEventRepository
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.NonComplianceEventRepository
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
import ch.admin.foitt.wallet.platform.verification.domain.model.VerificationMode
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
@Suppress("TooManyFunctions")
class HomeViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    getCredentialsWithDetailsFlow: GetCredentialsWithDetailsFlow,
    getDeferredCredentialsWithDetailsFlow: GetDeferredCredentialsWithDetailsFlow,
    getEIdRequestsFlow: GetEIdRequestsFlow,
    private val getCredentialCardState: GetCredentialCardState,
    private val hasExhaustedBatchCopies: HasExhaustedBatchCopies,
    private val updateAllCredentialStatuses: UpdateAllCredentialStatuses,
    private val updateCachedImages: UpdateCachedImages,
    private val updateAllSIdStatuses: UpdateAllSIdStatuses,
    private val refreshDeferredCredentials: RefreshDeferredCredentials,
    private val refreshVerifiableCredentials: RefreshVerifiableCredentials,
    private val credentialRefreshRepository: CredentialRefreshRepository,
    private val deleteEIdRequestCase: DeleteEIdRequestCase,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val navManager: NavigationManager,
    private val credentialEventRepository: CredentialEventRepository,
    private val nonComplianceEventRepository: NonComplianceEventRepository,
    private val otpStateCompletionRepository: OtpStateCompletionRepository,
    private val eIdRequestsPriorityOrdering: EIdRequestsPriorityOrdering,
    private val pollSIdRequestAfterFileSubmit: PollSIdRequestAfterFileSubmit,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.None

    private val _eventMessage = MutableStateFlow<Int?>(null)
    val eventMessage = _eventMessage.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var pollingJob: Job? = null

    private var statusRefreshJob: Job? = null

    private var cachedImagesRefreshJob: Job? = null

    private var lastCachedImagesRefresh: Long = 0L // epoch seconds

    @OptIn(UnsafeResultValueAccess::class)
    val screenContentState = refreshableStateFlow(initialData = HomeScreenState.Initial) {
        combine(
            getEIdRequestsFlow(),
            getCredentialsWithDetailsFlow(),
            getDeferredCredentialsWithDetailsFlow(),
        ) { eIdRequestsFlow, credentialsWithDetails, deferredCredential ->
            when {
                eIdRequestsFlow.isOk && credentialsWithDetails.isOk && deferredCredential.isOk -> mapToUiState(
                    credentials = credentialsWithDetails.value,
                    deferredCredentials = deferredCredential.value,
                    eIdRequestCases = eIdRequestsFlow.value,
                )

                else -> {
                    navigateTo(Destination.GenericErrorScreen(GenericErrorScreenState.Offer.generic()))
                    null
                }
            }
        }.filterNotNull()
    }

    private val _homeContainerState = MutableStateFlow(
        HomeContainerState(
            showEIdRequestButton = environmentSetupRepository.eIdRequestEnabled,
            showBetaIdRequestButton = environmentSetupRepository.betaIdRequestEnabled,
            isProximityEngagementEnabled = environmentSetupRepository.isProximityEngagementEnabled,
            showMenu = false,
            onScan = {
                if (environmentSetupRepository.isProximityEngagementEnabled) {
                    navigateTo(Destination.ShowOrScanQrCodeScreen(VerificationMode.SCANNER))
                } else {
                    navigateTo(Destination.QrScannerScreen)
                }
            },
            onQrCode = {
                navigateTo(Destination.ShowOrScanQrCodeScreen(VerificationMode.QR_CODE))
            },
            onGetEId = {
                viewModelScope.launch {
                    if (otpStateCompletionRepository.getOtpFlowWasDone()) {
                        navigateTo(Destination.EIdIntroScreen)
                    } else {
                        navigateTo(Destination.OtpIntroScreen)
                    }
                }
            },
            onGetBetaId = { navigateTo(Destination.BetaIdScreen) },
            onSettings = { navigateTo(Destination.SettingsScreen) },
            onHelp = { onHelp() },
        )
    )
    val homeContainerState = _homeContainerState.asStateFlow()

    init {
        viewModelScope.launch {
            credentialEventRepository.event.collect { event ->
                _eventMessage.value = when (event) {
                    CredentialEvent.ACCEPTED -> R.string.tk_home_notification_credential_accepted
                    CredentialEvent.DECLINED -> R.string.tk_home_notification_credential_declined
                    CredentialEvent.DELETED -> R.string.tk_home_notification_credential_deleted
                    CredentialEvent.NONE -> null
                }
                if (eventMessage.value != null) {
                    delay(TOAST_DISPLAY_TIME_MILLIS.milliseconds)
                    credentialEventRepository.resetEvent()
                }
            }
        }
        viewModelScope.launch {
            nonComplianceEventRepository.event.collect { event ->
                _eventMessage.value = when (event) {
                    NonComplianceEvent.ReportSentHome -> R.string.tk_activity_activityList_nonCompliance_reportSent_title
                    NonComplianceEvent.ReportSent,
                    NonComplianceEvent.None -> null
                }
                if (eventMessage.value != null) {
                    delay(TOAST_DISPLAY_TIME_MILLIS.milliseconds)
                    nonComplianceEventRepository.resetEvent()
                }
            }
        }
    }

    private suspend fun mapToUiState(
        eIdRequestCases: List<SIdRequestDisplayData>,
        credentials: List<CredentialDisplayData>,
        deferredCredentials: List<DeferredCredentialDisplayData>,
    ): HomeScreenState {
        val orderedEIdRequests = eIdRequestsPriorityOrdering(eIdRequestCases)

        return when {
            credentials.isNotEmpty() || deferredCredentials.isNotEmpty() -> {
                HomeScreenState.CredentialList(
                    eIdRequests = orderedEIdRequests,
                    credentials = getCredentialStateList(
                        credentialsDisplayData = credentials,
                        deferredCredentialsDisplayData = deferredCredentials,
                    ),
                    onCredentialClick = ::handleCredentialClick,
                )
            }

            eIdRequestCases.isNotEmpty() -> HomeScreenState.NoCredential(
                eIdRequests = orderedEIdRequests,
            )

            else -> HomeScreenState.WalletEmpty
        }
    }

    private suspend fun getCredentialStateList(
        credentialsDisplayData: List<CredentialDisplayData>,
        deferredCredentialsDisplayData: List<DeferredCredentialDisplayData>,
    ): List<CredentialCardState> {
        val groupOrder = mapOf(
            VerifiableProgressionState.UNACCEPTED to 0,
            VerifiableProgressionState.ACCEPTED to 1,
            DeferredProgressionState.IN_PROGRESS to 2,
            DeferredProgressionState.INVALID to 3,
            DeferredProgressionState.FAILED to 4,
        )

        val credentials = credentialsDisplayData.map { credentialDisplayData ->
            CredentialComparator(
                state = getCredentialCardState(credentialDisplayData).copy(
                    showBatchWarning = hasExhaustedBatchCopies(credentialDisplayData.credentialId),
                ),
                groupOrder = groupOrder.getValue(credentialDisplayData.progressionState),
                createdAt = credentialDisplayData.createdAt,
            )
        }

        val deferredCredentials = deferredCredentialsDisplayData.map { deferredCredentialDisplayData ->
            CredentialComparator(
                state = getCredentialCardState(deferredCredentialDisplayData),
                groupOrder = groupOrder.getValue(deferredCredentialDisplayData.status),
                createdAt = deferredCredentialDisplayData.createdAt,
            )
        }

        return (credentials + deferredCredentials)
            .sortedWith(
                compareBy<CredentialComparator> { it.groupOrder }
                    .thenByDescending { it.createdAt }
            ).map { it.state }
    }

    fun onResume() {
        if (environmentSetupRepository.eIdRequestEnabled) {
            tryLaunchEidPolling()
        }
        tryRefreshCredentialStatuses()
        tryRefreshCachedImages()
    }

    fun onPause() {
        pollingJob?.cancel()
        pollingJob = null
    }

    // Pull-to-refresh always refreshes (ignores the shared cooldown); only skipped if one is already running.
    fun onRefresh() {
        if (isRefreshing.value) return
        credentialRefreshRepository.markRefreshed()
        viewModelScope.launch {
            refreshDeferredCredentials()
            refreshVerifiableCredentials()
            updateAllSIdStatuses()
            updateAllCredentialStatuses()
        }.trackCompletion(_isRefreshing)
    }

    private fun tryRefreshCredentialStatuses() {
        if (statusRefreshJob != null) return
        if (!credentialRefreshRepository.isRefreshDue()) return
        credentialRefreshRepository.markRefreshed()
        statusRefreshJob = viewModelScope.launch {
            refreshDeferredCredentials()
            refreshVerifiableCredentials()
            updateAllCredentialStatuses()
        }.apply {
            invokeOnCompletion {
                if (statusRefreshJob == this) statusRefreshJob = null
            }
        }
    }

    /**
     * Backfills the cached display images.
     * The cooldown keeps credentials whose metadata has no fetchable
     * logo from re-parsing their raw metadata on every resume.
     */
    private fun tryRefreshCachedImages() {
        if (cachedImagesRefreshJob != null) return
        val now = Instant.now().epochSecond
        if (now - lastCachedImagesRefresh < CACHED_IMAGES_REFRESH_COOLDOWN_SECONDS) return
        lastCachedImagesRefresh = now
        cachedImagesRefreshJob = viewModelScope.launch {
            updateCachedImages()
        }.apply {
            invokeOnCompletion {
                if (cachedImagesRefreshJob == this) cachedImagesRefreshJob = null
            }
        }
    }

    private fun onRefreshSIdStatuses() {
        if (!isRefreshing.value) {
            viewModelScope.launch {
                refreshDeferredCredentials()
                updateAllSIdStatuses()
            }.trackCompletion(_isRefreshing)
        }
    }

    private fun tryLaunchEidPolling() {
        if (pollingJob == null) {
            pollingJob = viewModelScope.launch {
                updateAllSIdStatuses()
                pollSIdRequestAfterFileSubmit()
            }.apply {
                invokeOnCompletion {
                    if (pollingJob == this) pollingJob = null
                }
            }
        }
    }

    fun onCloseToast() {
        _eventMessage.value = null
        credentialEventRepository.resetEvent()
        nonComplianceEventRepository.resetEvent()
    }

    fun onEidNotificationAction(caseId: String, status: SIdRequestDisplayStatus) {
        when (status) {
            SIdRequestDisplayStatus.AV_READY,
            SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_OK -> navigateTo(Destination.EIdStartAvSessionScreen(caseId = caseId))

            SIdRequestDisplayStatus.AV_READY_LEGAL_CONSENT_PENDING,
            SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_PENDING -> navigateTo(Destination.EIdGuardianSelectionScreen(caseId = caseId))

            SIdRequestDisplayStatus.IN_AUTO_VERIFICATION -> navigateTo(Destination.EIdStartAutoVerificationScreen(caseId = caseId))
            SIdRequestDisplayStatus.IN_TARGET_WALLET_PAIRING -> navigateTo(Destination.EIdPairingOverviewScreen(caseId = caseId))
            SIdRequestDisplayStatus.UNKNOWN -> onRefreshSIdStatuses()
            SIdRequestDisplayStatus.REFUSED -> onLearnMore()
            SIdRequestDisplayStatus.AV_FILES_SUBMITTED,
            SIdRequestDisplayStatus.QUEUEING,
            SIdRequestDisplayStatus.QUEUEING_LEGAL_CONSENT_OK,
            SIdRequestDisplayStatus.IN_AGENT_REVIEW,
            SIdRequestDisplayStatus.READY_FOR_FINAL_ENTITLEMENT_CHECK,
            SIdRequestDisplayStatus.IN_ISSUANCE,
            SIdRequestDisplayStatus.AV_EXPIRED,
            SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_OK,
            SIdRequestDisplayStatus.AV_EXPIRED_LEGAL_CONSENT_PENDING,
            SIdRequestDisplayStatus.CANCELLED,
            SIdRequestDisplayStatus.CLOSED -> {
                Timber.w(message = "EIdRequest notification should not be clickable in this state ${status.name}")
            }
        }
    }

    fun onCloseEId(caseId: String) {
        viewModelScope.launch {
            deleteEIdRequestCase(caseId)
        }
    }

    private fun onLearnMore() = appContext.openLink(R.string.tk_getEid_notification_declined_faqLink)

    fun onMenu(showMenu: Boolean) {
        _homeContainerState.update { currentState ->
            currentState.copy(showMenu = showMenu)
        }
    }

    private fun handleCredentialClick(credentialCardState: CredentialCardState) {
        val id = credentialCardState.credentialId
        if (credentialCardState.isDeferred) {
            navigateTo(Destination.DeferredDetailScreen(credentialId = id))
        } else {
            when (credentialCardState.progressionState) {
                VerifiableProgressionState.ACCEPTED -> navigateTo(
                    Destination.CredentialDetailScreen(credentialId = id)
                )

                VerifiableProgressionState.UNACCEPTED -> navigateTo(
                    Destination.CredentialOfferScreen(credentialId = id)
                )
            }
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
        appContext.openLink(R.string.settings_help_contact_linkValue)
    }

    companion object {
        private const val TOAST_DISPLAY_TIME_MILLIS = 4000L
        private const val CACHED_IMAGES_REFRESH_COOLDOWN_SECONDS = 5 * 60L
    }
}
