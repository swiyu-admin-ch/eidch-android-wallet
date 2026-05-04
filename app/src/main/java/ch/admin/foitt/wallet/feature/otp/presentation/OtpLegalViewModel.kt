package ch.admin.foitt.wallet.feature.otp.presentation

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
internal class OtpLegalViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    override val topBarState = TopBarState.DetailsWithCloseButton(
        titleId = null,
        onUp = ::onBack,
        onClose = ::onClose,
    )

    fun onContinue() = navManager.navigateTo(Destination.OtpEmailInputScreen)

    fun onTerms() = appContext.openLink(appContext.getString(R.string.tk_eidRequest_otp_legal_linkValue))

    private fun onBack() = navManager.popBackStack()

    private fun onClose() = navManager.popBackStackTo(Destination.HomeScreen::class, false)
}
