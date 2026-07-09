package ch.admin.foitt.wallet.feature.onboarding.presentation

import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

abstract class OnboardingViewModel(
    setTopBarState: SetTopBarState,
    areBaseSystemBarsInverted: Boolean = false,
    systemBarsFixedLightColor: Boolean = false
) : ScreenViewModel(setTopBarState, areBaseSystemBarsInverted, systemBarsFixedLightColor) {
    private val _focusEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val focusEvents = _focusEvents.asSharedFlow()

    protected fun tryEmitFocusEvents() {
        _focusEvents.tryEmit(Unit)
    }
}
