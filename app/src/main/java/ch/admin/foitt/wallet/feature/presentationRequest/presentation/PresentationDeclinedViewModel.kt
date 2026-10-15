package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.model.PresentationDeclinedUiState
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.GetActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.toBadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.badges.presentation.model.BadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidateRedirectUri
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.GetNonComplianceReportingData
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.utils.openLink
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = PresentationDeclinedViewModel.Factory::class)
class PresentationDeclinedViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    private val getActorUiState: GetActorUiState,
    getActorForScope: GetActorForScope,
    private val validateRedirectUri: ValidateRedirectUri,
    private val getNonComplianceReportingData: GetNonComplianceReportingData,
    environmentSetupRepository: EnvironmentSetupRepository,
    setTopBarState: SetTopBarState,
    @Assisted val redirectUri: String?,
    @Assisted("credentialId") val credentialId: Long,
    @Assisted("activityId") val activityId: Long?,
) : ScreenViewModel(setTopBarState) {
    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted redirectUri: String?,
            @Assisted("credentialId") credentialId: Long,
            @Assisted("activityId") activityId: Long?,
        ): PresentationDeclinedViewModel
    }

    override val topBarState = TopBarState.None

    private val verifierDisplayData = getActorForScope(ComponentScope.Verifier)

    val uiState = verifierDisplayData.map { displayData ->
        PresentationDeclinedUiState(
            verifierUiState = getActorUiState(
                actorDisplayData = displayData
            ),
            canReport = environmentSetupRepository.nonComplianceEnabled &&
                displayData.trustStatus != TrustStatus.TRUSTED_PROXIMITY_VERIFIER
        )
    }.toStateFlow(PresentationDeclinedUiState.EMPTY, 0)

    private val _badgeBottomSheetUiState: MutableStateFlow<BadgeBottomSheetUiState?> = MutableStateFlow(null)
    val badgeBottomSheet = _badgeBottomSheetUiState.asStateFlow()

    fun onBack() = if (redirectUri != null) {
        validateRedirectUri(redirectUri)
            .onOk {
                appContext.openLink(redirectUri)
                navManager.popBackStackOrToRoot()
            }.onErr {
                navManager.replaceCurrentWith(Destination.GenericErrorScreen(GenericErrorScreenState.General.invalidRedirectUri()))
            }
    } else {
        navManager.popBackStackOrToRoot()
    }

    fun onActorNameTap() {
        _badgeBottomSheetUiState.value = uiState.value.verifierUiState.toBadgeBottomSheetUiState {
            onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value)
        }
    }

    fun onReportedActorInfo() {
        val verifier = uiState.value.verifierUiState
        _badgeBottomSheetUiState.value = BadgeBottomSheetUiState.NonCompliantActor(
            actorName = verifier.name ?: "",
            actorPainter = verifier.painter,
            reason = verifier.nonComplianceReason,
            onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
        )
    }

    fun onReport() {
        viewModelScope.launch {
            getNonComplianceReportingData(
                credentialId = credentialId,
                actorDisplayData = verifierDisplayData.value,
                activityType = ActivityType.PRESENTATION_DECLINED,
                activityId = activityId,
            ).onOk { reportingData ->
                navManager.navigateTo(
                    Destination.NonComplianceListScreen(
                        reportingData = reportingData,
                        goBackToRoot = true,
                    )
                )
            }.onErr { error ->
                Timber.e("Failed to gather reporting data: $error")
            }
        }
    }

    fun onDismissBottomSheet() {
        _badgeBottomSheetUiState.value = null
    }

    private fun onMoreInformation(@StringRes uriResource: Int) = appContext.openLink(uriResource)
}
