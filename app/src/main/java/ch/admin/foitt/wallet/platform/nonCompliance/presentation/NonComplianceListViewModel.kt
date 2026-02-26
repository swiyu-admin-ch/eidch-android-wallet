package ch.admin.foitt.wallet.platform.nonCompliance.presentation

import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportReason
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarBackground
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = NonComplianceListViewModel.Factory::class)
class NonComplianceListViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
    @Assisted private val activityId: Long,
    @Assisted private val activityType: ActivityType
) : ScreenViewModel(setTopBarState) {
    @AssistedFactory
    interface Factory {
        fun create(activityId: Long, activityType: ActivityType): NonComplianceListViewModel
    }

    override val topBarState = TopBarState.Details(
        titleId = R.string.tk_nonCompliance_list_title,
        topBarBackground = TopBarBackground.CLUSTER,
        onUp = this::onBack
    )

    private val issuanceReportingReasons = listOf<NonComplianceReportReason>()
    private val verificationReportingReasons = listOf(NonComplianceReportReason.EXCESSIVE_DATA_REQUEST)

    val reasons = when (activityType) {
        ActivityType.ISSUANCE -> issuanceReportingReasons
        ActivityType.PRESENTATION_ACCEPTED,
        ActivityType.PRESENTATION_DECLINED -> verificationReportingReasons
    }

    fun onBack() {
        navManager.popBackStack()
    }

    fun onReason(reportReason: NonComplianceReportReason) {
        navManager.navigateTo(
            Destination.NonComplianceInfoScreen(
                activityId = activityId,
                reportReason = reportReason,
            )
        )
    }
}
