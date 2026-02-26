package ch.admin.foitt.wallet.platform.nonCompliance.presentation

import android.content.Context
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportReason
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarBackground
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel(assistedFactory = NonComplianceInfoViewModel.Factory::class)
class NonComplianceInfoViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
    @Assisted private val activityId: Long,
    @Assisted val reportReason: NonComplianceReportReason
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(
            activityId: Long,
            reportReason: NonComplianceReportReason
        ): NonComplianceInfoViewModel
    }

    private val titleId = when (reportReason) {
        NonComplianceReportReason.EXCESSIVE_DATA_REQUEST -> R.string.tk_nonCompliance_reportExcessiveData_title
    }

    override val topBarState = TopBarState.DetailsWithCloseButton(
        titleId = titleId,
        topBarBackground = TopBarBackground.CLUSTER,
        onClose = this::onClose,
        onUp = this::onBack
    )

    private fun onBack() {
        navManager.popBackStack()
    }

    private fun onClose() {
        navManager.popBackStackTo(
            destination = Destination.NonComplianceListScreen::class,
            inclusive = true
        )
    }

    fun onMoreInformation() {
        val linkValue = when (reportReason) {
            NonComplianceReportReason.EXCESSIVE_DATA_REQUEST -> R.string.tk_nonCompliance_report_info_moreInformation_link_value
        }

        appContext.openLink(linkValue)
    }

    fun onContinue() {
        navManager.navigateTo(
            Destination.NonComplianceFormScreen(
                titleId = titleId,
                activityId = activityId,
                reportReason = reportReason,
            )
        )
    }
}
