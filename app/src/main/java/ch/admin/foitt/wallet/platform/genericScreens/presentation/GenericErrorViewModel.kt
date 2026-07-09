package ch.admin.foitt.wallet.platform.genericScreens.presentation

import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = GenericErrorViewModel.Factory::class)
class GenericErrorViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    @Assisted private val errorScreenState: GenericErrorScreenState,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.Empty

    @AssistedFactory
    interface Factory {
        fun create(error: GenericErrorScreenState): GenericErrorViewModel
    }

    val title = when (errorScreenState) {
        is GenericErrorScreenState.Error -> errorScreenState.title
        is GenericErrorScreenState.PresentationError -> errorScreenState.title
    }

    val subtitle = when (errorScreenState) {
        is GenericErrorScreenState.Error -> errorScreenState.subtitle
        is GenericErrorScreenState.PresentationError -> errorScreenState.subtitle
    }

    val errorText = when (errorScreenState) {
        is GenericErrorScreenState.Error -> errorScreenState.errorText
        is GenericErrorScreenState.PresentationError -> errorScreenState.errorText
    }

    val errorDescription = when (errorScreenState) {
        is GenericErrorScreenState.Error -> errorScreenState.errorDescription
        is GenericErrorScreenState.PresentationError -> errorScreenState.errorDescription
    }

    fun onBack() = navManager.navigateBackToHomeScreen(Destination.GenericErrorScreen::class)
}
