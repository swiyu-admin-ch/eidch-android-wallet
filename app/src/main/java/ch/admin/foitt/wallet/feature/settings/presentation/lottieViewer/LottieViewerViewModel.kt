package ch.admin.foitt.wallet.feature.settings.presentation.lottieViewer

import androidx.compose.ui.layout.ContentScale
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LottieViewerViewModel @Inject constructor(
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.Details(
        onUp = navManager::popBackStack,
        titleId = null,
    )

    fun onNextAnimation() {
        if (currentAnimationScaling.value == ContentScale.Fit) {
            currentAnimationScaling.value = ContentScale.Crop
            return
        } else {
            currentAnimationScaling.value = ContentScale.Fit
        }

        currentAnimationIndex = (currentAnimationIndex + 1) % animations.size
        _animationRes.value = animations[currentAnimationIndex]
    }

    private var currentAnimationIndex = 0
    private val currentAnimationScaling = MutableStateFlow(ContentScale.Fit)
    private val animations = arrayOf(
        R.raw.face_record,
        R.raw.doc_record,
        R.raw.doc_scan,
    )

    private val _animationRes = MutableStateFlow(animations[currentAnimationIndex])
    val animationRes = _animationRes.asStateFlow()
    val animationScaling = currentAnimationScaling.asStateFlow()
}
