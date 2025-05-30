package ch.admin.foitt.wallet.feature.onboarding.presentation

import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingErrorViewModel @Inject constructor(
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState: TopBarState = TopBarState.Empty
}
