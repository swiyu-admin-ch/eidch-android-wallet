package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.scanning.di.AvBeamSdkEntryPoint
import ch.admin.foitt.wallet.platform.utils.openLink
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EIdPrivacyPolicyViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val navManager: NavigationManager,
    private val destinationScopedComponentManager: DestinationScopedComponentManager,
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
            val avBeamRepository = destinationScopedComponentManager.getEntryPoint(
                entryPointClass = AvBeamSdkEntryPoint::class.java,
                componentScope = ComponentScope.AvBeamSdkSession,
            ).avBeamRepository()

            avBeamRepository.init(activity)
        }

        navManager.navigateTo(
            destination = Destination.EIdAttestationScreen
        )
    }
}
