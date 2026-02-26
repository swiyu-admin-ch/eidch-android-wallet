package ch.admin.foitt.wallet.feature.settings.presentation

import android.content.Context
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val navManager: NavigationManager,
    @param:ApplicationContext private val appContext: Context,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.Details(navManager::popBackStack, R.string.tk_settings_title)

    fun onSecurityAndPrivacy() = navManager.navigateTo(Destination.SecuritySettingsScreen)

    fun onLanguage() = navManager.navigateTo(Destination.LanguageScreen)

    fun onHelp() = appContext.openLink(R.string.tk_settings_general_help_link_value)

    fun onFeedback() = appContext.openLink(R.string.tk_settings_general_feedback_link_value)

    fun onLicenses() = navManager.navigateTo(Destination.LicencesScreen)

    fun onImprint() = navManager.navigateTo(Destination.ImpressumScreen)
}
