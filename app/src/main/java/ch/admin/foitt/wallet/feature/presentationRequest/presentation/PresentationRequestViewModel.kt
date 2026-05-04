package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseErrorBody
import ch.admin.foitt.openid4vc.domain.usecase.DeclinePresentation
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestDisplayData
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestFlow
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.SubmitPresentation
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.model.PresentationRequestUiState
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SavePresentationAcceptedActivity
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SavePresentationDeclinedActivity
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchAndCacheVerifierDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.GetActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.badges.domain.model.BadgeType
import ch.admin.foitt.wallet.platform.badges.presentation.model.BadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.badges.presentation.model.ClaimBadgeUiState
import ch.admin.foitt.wallet.platform.badges.presentation.model.toBadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.di.IoDispatcherScope
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.refreshableStateFlow
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimCluster
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimImage
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimItem
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText
import ch.admin.foitt.wallet.platform.ssi.domain.model.getClaimIds
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.utils.launchWithDelayedLoading
import ch.admin.foitt.wallet.platform.utils.openLink
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.onFailure
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = PresentationRequestViewModel.Factory::class)
class PresentationRequestViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    getPresentationRequestFlow: GetPresentationRequestFlow,
    private val fetchAndCacheVerifierDisplayData: FetchAndCacheVerifierDisplayData,
    private val submitPresentation: SubmitPresentation,
    private val declinePresentation: DeclinePresentation,
    @param:IoDispatcherScope private val ioDispatcherScope: CoroutineScope,
    private val getCredentialCardState: GetCredentialCardState,
    private val getActorUiState: GetActorUiState,
    getActorForScope: GetActorForScope,
    private val savePresentationAcceptedActivity: SavePresentationAcceptedActivity,
    private val savePresentationDeclinedActivity: SavePresentationDeclinedActivity,
    setTopBarState: SetTopBarState,
    @Assisted private val compatibleCredential: CompatibleCredential,
    @Assisted private val presentationRequestWithRaw: PresentationRequestWithRaw,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(
            compatibleCredential: CompatibleCredential,
            presentationRequestWithRaw: PresentationRequestWithRaw,
        ): PresentationRequestViewModel
    }

    override val topBarState: TopBarState = TopBarState.None

    private val verifierDisplayData = getActorForScope(ComponentScope.Verifier)
    val verifierUiState = verifierDisplayData.map { verifierDisplayData ->
        getActorUiState(
            actorDisplayData = verifierDisplayData,
        )
    }.toStateFlow(ActorUiState.EMPTY, 0)

    private val _badgeBottomSheetUiState: MutableStateFlow<BadgeBottomSheetUiState?> = MutableStateFlow(null)
    val badgeBottomSheet = _badgeBottomSheetUiState.asStateFlow()

    private val _showConfirmationBottomSheet: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showConfirmationBottomSheet = _showConfirmationBottomSheet.asStateFlow()

    val presentationRequestUiState = refreshableStateFlow(PresentationRequestUiState.EMPTY) {
        getPresentationRequestFlow(
            id = compatibleCredential.credentialId,
            requestedFields = compatibleCredential.requestedFields,
        ).map { result ->
            result.mapBoth(
                success = { presentationRequestUi ->
                    _isLoading.value = false
                    presentationRequestUi.toUiState()
                },
                failure = {
                    navigateToErrorScreen()
                    null
                },
            )
        }.filterNotNull()
    }

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _showDelayReason = MutableStateFlow(false)
    val showDelayReason = _showDelayReason.asStateFlow()

    init {
        viewModelScope.launch {
            fetchAndCacheVerifierDisplayData(
                presentationRequestWithRaw.authorizationRequest,
            )
        }
    }

    fun onAccept() = if (verifierUiState.value.trustStatus != TrustStatus.EXTERNAL) {
        submit()
    } else {
        _showConfirmationBottomSheet.value = true
    }

    fun submit() {
        viewModelScope.launchWithDelayedLoading(
            isLoadingFlow = _showDelayReason,
            delay = DELAY_REASON_DURATION
        ) {
            viewModelScope.launch {
                savePresentationAcceptedActivity(
                    credentialId = compatibleCredential.credentialId,
                    actorDisplayData = verifierDisplayData.value,
                    verifierFallbackName = appContext.getString(R.string.presentation_verifier_name_unknown),
                    claimIds = presentationRequestUiState.stateFlow.value.requestedClaims.getClaimIds(),
                    nonComplianceData = presentationRequestWithRaw.rawPresentationRequest,
                )

                submitPresentation(
                    authorizationRequest = presentationRequestWithRaw.authorizationRequest,
                    compatibleCredential = compatibleCredential,
                ).mapBoth(
                    success = { navigateToSuccess() },
                    failure = { error ->
                        when (error) {
                            PresentationRequestError.InvalidUrl,
                            PresentationRequestError.RawSdJwtParsingError,
                            PresentationRequestError.NetworkError,
                            is PresentationRequestError.Unexpected -> navigateToFailure()

                            PresentationRequestError.ValidationError -> navigateToValidationError()
                            PresentationRequestError.VerificationError -> navigateToVerificationError()
                            PresentationRequestError.InvalidCredentialError -> navigateToInvalidCredentialError()
                        }
                    },
                )
            }.trackCompletion(_isSubmitting)
        }
    }

    fun onDecline() {
        ioDispatcherScope.launch {
            savePresentationDeclinedActivity(
                credentialId = compatibleCredential.credentialId,
                actorDisplayData = verifierDisplayData.value,
                verifierFallbackName = appContext.getString(R.string.presentation_verifier_name_unknown),
                claimIds = presentationRequestUiState.stateFlow.value.requestedClaims.getClaimIds(),
                nonComplianceData = presentationRequestWithRaw.rawPresentationRequest,
            )

            declinePresentation(
                url = presentationRequestWithRaw.authorizationRequest.responseUri,
                reason = AuthorizationResponseErrorBody.ErrorType.CLIENT_REJECTED,
            ).onFailure { error ->
                Timber.w("Decline presentation error: $error")
            }
        }
        navManager.replaceCurrentWith(
            destination = Destination.PresentationDeclinedScreen
        )
    }

    private fun navigateToSuccess() {
        navManager.replaceCurrentWith(
            destination = Destination.PresentationSuccessScreen(
                sentFields = getSentFields(),
            )
        )
    }

    private fun navigateToValidationError() {
        navManager.replaceCurrentWith(
            destination = Destination.PresentationValidationErrorScreen
        )
    }

    private fun navigateToInvalidCredentialError() {
        navManager.replaceCurrentWith(
            destination = Destination.PresentationInvalidCredentialErrorScreen(
                sentFields = getSentFields(),
            )
        )
    }

    private fun navigateToVerificationError() {
        navManager.replaceCurrentWith(
            destination = Destination.PresentationVerificationErrorScreen
        )
    }

    private fun getSentFields() = presentationRequestUiState.stateFlow.value.requestedClaims.flatMap { item ->
        getClusterFields(item.items)
    }

    private fun getClusterFields(items: MutableList<CredentialClaimItem>): List<String> {
        val fields = mutableListOf<String>()
        items.forEach { item ->
            when (item) {
                is CredentialClaimText, is CredentialClaimImage -> fields.add(item.localizedLabel)
                is CredentialClaimCluster -> fields.addAll(getClusterFields(item.items))
            }
        }
        return fields
    }

    private fun navigateToFailure() = navManager.replaceCurrentWith(
        Destination.PresentationFailureScreen(
            compatibleCredential = compatibleCredential,
            presentationRequestWithRaw = presentationRequestWithRaw,
        )
    )

    private fun navigateToErrorScreen() {
        navManager.replaceCurrentWith(Destination.GenericErrorScreen(GenericErrorScreenState.GENERIC))
    }

    private suspend fun PresentationRequestDisplayData.toUiState(): PresentationRequestUiState {
        return PresentationRequestUiState(
            credentialCardState = getCredentialCardState(credential),
            requestedClaims = requestedClaims,
            claimBadgesUiStates = requestedClaims.toClaimBadgesUiStates(),
            numberOfClaims = getAmountOfClaims(requestedClaims)
        )
    }

    private fun List<CredentialClaimItem>.toClaimBadgesUiStates(): List<ClaimBadgeUiState> {
        return flatMap { claim ->
            when (claim) {
                is CredentialClaimCluster -> claim.items.toClaimBadgesUiStates()
                is CredentialClaimImage -> listOf(ClaimBadgeUiState(localizedLabel = claim.localizedLabel, isSensitive = claim.isSensitive))
                is CredentialClaimText -> listOf(ClaimBadgeUiState(localizedLabel = claim.localizedLabel, isSensitive = claim.isSensitive))
            }
        }.sortedByDescending { it.isSensitive }
    }

    private fun getAmountOfClaims(claims: List<CredentialClaimCluster>): Int {
        var amount = 0
        claims.forEach {
            amount += it.numberOfNonClusterChildren
        }
        return amount
    }

    fun onReportWrongData() {
        navManager.navigateTo(Destination.ReportWrongDataScreen)
    }

    fun onBadge(badgeType: BadgeType) {
        _badgeBottomSheetUiState.value = when (badgeType) {
            is BadgeType.ActorInfoBadge -> badgeType.toBadgeBottomSheetUiState(
                actorName = verifierUiState.value.name ?: "",
                reason = verifierUiState.value.nonComplianceReason,
                onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
            )

            is BadgeType.ClaimInfoBadge -> badgeType.toBadgeBottomSheetUiState(
                onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
            )
        }
    }

    fun onDismissBadgeBottomSheet() {
        _badgeBottomSheetUiState.value = null
    }

    fun onDismissConfirmationBottomSheet() {
        _showConfirmationBottomSheet.value = false
    }

    private fun onMoreInformation(@StringRes uriResource: Int) = appContext.openLink(uriResource)

    companion object {
        private const val DELAY_REASON_DURATION = 5000L
    }
}
