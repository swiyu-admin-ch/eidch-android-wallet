package ch.admin.foitt.wallet.feature.qr.presentation.qrgenerator

import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QrGeneratorPermissionViewModel @Inject constructor(
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState, systemBarsFixedLightColor = true) {
    override val topBarState = TopBarState.None
}
