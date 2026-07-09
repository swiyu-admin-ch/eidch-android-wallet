package ch.admin.foitt.wallet.feature.onboarding.presentation.composables

import androidx.compose.runtime.Composable
import ch.admin.foitt.wallet.platform.composables.presentation.RequestViewFocusOnResume
import ch.admin.foitt.wallet.platform.composables.presentation.SwipeableScreen
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun OnboardingSwipeableScreen(
    onSwipeForward: () -> Unit,
    onSwipeBackWard: () -> Unit,
    ratioToSwipe: Float = 0.20f,
    focusEvents: SharedFlow<Unit>? = null,
    content: @Composable () -> Unit
) {
    RequestViewFocusOnResume()
    focusEvents?.let {
        CollectFocusEvents(it)
    }
    SwipeableScreen(
        onSwipeForward = onSwipeForward,
        onSwipeBackWard = onSwipeBackWard,
        ratioToSwipe = ratioToSwipe,
        content = content
    )
}
