package ch.admin.foitt.wallet.feature.settings.presentation.accessibility

import android.content.Context
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class AccessibilityViewModel @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState
) : ScreenViewModel(setTopBarState) {

    override val topBarState: TopBarState
        get() = TopBarState.Details(navManager::popBackStack, R.string.tk_settings_accessibility_title)

    fun onDeclaration() = applicationContext.openLink(R.string.tk_settings_accessibility_declaration_link_value)
    fun onReportIssue() = applicationContext.openLink(R.string.tk_settings_accessibility_report_issue_link_value)
}
