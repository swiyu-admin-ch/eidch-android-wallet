package ch.admin.foitt.wallet.platform.composables.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * This Composable ensures that when onResume triggers,
 * the current LocalView requests focus.
 * This is necessary in Composables that have scrollable content
 * and want to ensure that scrolling with an external keyboard is working.
 * It also ensures that screens with the same back button are able to
 * focus the back button when returning to the screen.
 *
 * Why is this needed?
 * There are 2 focus systems, one on Android View level and one on compose level.
 * The two need to be in sync to correctly work with a keyboard. However, in combination
 * with navigation it happens that the android view focus remains on an older/previous view
 * while the compose focus switches to the new Composable. By requesting the focus on the
 * view we are re-registering the AndroidComposeView with the Android view system and
 * therefore re-bridging the two focus systems.
 *
 * Usage:
 * ```
 * //Just call in your screens top most Composable
 * RequestViewFocusOnResume()
 * ```
 *
 */
@Composable
fun RequestViewFocusOnResume() {
    val view = LocalView.current
    LifecycleResumeEffect(Unit) {
        view.requestFocus()
        onPauseOrDispose { }
    }
}
