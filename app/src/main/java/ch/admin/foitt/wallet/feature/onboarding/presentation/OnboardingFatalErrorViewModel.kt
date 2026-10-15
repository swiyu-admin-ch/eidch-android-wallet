package ch.admin.foitt.wallet.feature.onboarding.presentation

import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = OnboardingFatalErrorViewModel.Factory::class)
class OnboardingFatalErrorViewModel @AssistedInject constructor(
    setTopBarState: SetTopBarState,
    @Assisted("primaryTextRes") val primaryTextRes: Int,
    @Assisted("secondaryTextRes") val secondaryTextRes: Int,
    @Assisted("errorCode") val errorCode: String,
) : ScreenViewModel(setTopBarState) {
    override val topBarState: TopBarState = TopBarState.Empty

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("primaryTextRes") primaryTextRes: Int,
            @Assisted("secondaryTextRes") secondaryTextRes: Int,
            @Assisted("errorCode") errorCode: String,
        ): OnboardingFatalErrorViewModel
    }
}
