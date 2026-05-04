package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.avwrapper.AVBeam
import ch.admin.foitt.avwrapper.AVBeamInitConfig
import ch.admin.foitt.avwrapper.config.AVBeamConfigLogLevel
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EIdPrivacyPolicyViewModel @Inject constructor(
    private val avBeam: AVBeam,
    @param:ApplicationContext private val context: Context,
    private val navManager: NavigationManager,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.DetailsWithCloseButton(
        titleId = null,
        onUp = navManager::popBackStack,
        onClose = { navManager.navigateBackToHomeScreen(Destination.EIdIntroScreen::class) }
    )

    fun onEIdPrivacyPolicy() = context.openLink(R.string.tk_getEid_dataPrivacy_link_value)

    fun onNext(activity: AppCompatActivity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val logLevel = if (environmentSetupRepository.avBeamLoggingEnabled) {
                    AVBeamConfigLogLevel.DEBUG
                } else {
                    AVBeamConfigLogLevel.NONE
                }
                avBeam.init(AVBeamInitConfig(logLevel), activity)
            }
        }

        navManager.navigateTo(
            destination = Destination.EIdAttestationScreen
        )
    }
}
