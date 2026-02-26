package ch.admin.foitt.wallet.platform.genericScreens.presentation

import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetLocalizedDateTime
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class GenericErrorViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    private val getLocalizedDateTime: GetLocalizedDateTime,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    override val topBarState = TopBarState.None

    private val eventTime = ZonedDateTime.now()
    val dateTime: String get() = getLocalizedDateTime(eventTime)

    fun onBack() {
        navigationManager.popBackStackOrToRoot()
    }
}
