package ch.admin.foitt.wallet.platform.nonCompliance.presentation

import android.content.Context
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceIssuerReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportingData
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceVerifierReportReason
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

@HiltViewModel(assistedFactory = NonComplianceListViewModel.Factory::class)
class NonComplianceListViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    @param:ApplicationContext private val appContext: Context,
    setTopBarState: SetTopBarState,
    @Assisted private val reportingData: NonComplianceReportingData,
    @Assisted private val goBackToRoot: Boolean,
) : ScreenViewModel(setTopBarState) {
    @AssistedFactory
    interface Factory {
        fun create(
            reportingData: NonComplianceReportingData,
            goBackToRoot: Boolean,
        ): NonComplianceListViewModel
    }

    override val topBarState = TopBarState.Details(
        titleId = R.string.tk_nonCompliance_list_title,
        topBarBackground = TopBarBackground.CLUSTER,
        onUp = this::onBack
    )

    val reasons: List<NonComplianceReportReason> = when (reportingData.activityType) {
        ActivityType.ISSUANCE -> NonComplianceIssuerReportReason.entries
        ActivityType.PRESENTATION_ACCEPTED,
        ActivityType.PRESENTATION_DECLINED -> NonComplianceVerifierReportReason.entries
    }

    fun onBack() {
        navManager.popBackStack()
    }

    fun onReason(reportReason: NonComplianceReportReason) {
        val titleId = when (reportReason) {
            NonComplianceVerifierReportReason.EXCESSIVE_DATA_REQUEST -> R.string.tk_nonCompliance_list_excessiveData_title
            NonComplianceIssuerReportReason.DUBIOUS_CREDENTIAL_ISSUANCE -> R.string.nonCompliance_reportType_dubiousCredentialIssuance_title
            NonComplianceIssuerReportReason.SENSITIVE_DATA_NOT_DECLARED -> R.string.nonCompliance_reportType_sensitiveDataNotDeclared_title
            NonComplianceIssuerReportReason.FALSE_IDENTITY -> R.string.nonCompliance_reportType_falseIdentity_title
            NonComplianceIssuerReportReason.ILLEGAL_CONTENT_OR_PURPOSE -> R.string.nonCompliance_reportType_illegalContentOrPurpose_title
            NonComplianceVerifierReportReason.VERIFICATION_NOT_REGISTERED ->
                R.string.nonCompliance_reportType_verificationNotRegistered_title
            NonComplianceVerifierReportReason.FALSE_IDENTITY_VERIFIER -> R.string.nonCompliance_reportType_falseIdentity_title
            NonComplianceVerifierReportReason.OTHER,
            NonComplianceIssuerReportReason.OTHER -> R.string.nonCompliance_reportType_other_title
        }

        navManager.navigateTo(
            Destination.NonComplianceFormScreen(
                titleId = titleId,
                reportingData = reportingData,
                reportReason = reportReason,
                goBackToRoot = goBackToRoot,
            )
        )
    }

    fun onMoreInformation() {
        val linkValue = R.string.tk_nonCompliance_report_info_moreInformation_link_value
        appContext.openLink(linkValue)
    }

    fun onPrivacy() {
        val linkValue = R.string.nonCompliance_reportIntro_privacyStatement_linkValue
        appContext.openLink(linkValue)
    }
}
