package ch.admin.foitt.wallet.feature.otp.presentation

import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class OtpIntroViewModel @Inject constructor(
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    override val topBarState = TopBarState.WithCloseButton(
        onClose = ::onClose,
    )

    fun onContinue() = navManager.navigateTo(Destination.OtpLegalScreen)

    fun onCancel() = navManager.popBackStack()

    private fun onClose() = navManager.popBackStackTo(Destination.HomeScreen::class, false)
}
