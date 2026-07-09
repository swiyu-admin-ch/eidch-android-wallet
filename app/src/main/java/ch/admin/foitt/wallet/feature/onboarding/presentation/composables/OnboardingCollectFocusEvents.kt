package ch.admin.foitt.wallet.feature.onboarding.presentation.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.flow.SharedFlow

/**
 * Composable to observe the provided [focusEvents] and move focus of [LocalFocusManager] to
 * the next focusable element whenever a new event is emitted.
 *
 * Usage:
 * ```
 * CollectFocusEvents(viewModel.focusEvents)
 * ```
 * or with trailing lambda for alternative action
 * ```
 * CollectFocusEvents(viewModel.focusEvents) {
 *         passwordFocusRequester.requestFocus()
 * }
 * ```
 * @param focusEvents the events to observe
 * @action action to run alternatively instead of moving focus of [LocalFocusManager]
 */
@Composable
fun CollectFocusEvents(focusEvents: SharedFlow<Unit>, action: (() -> Unit)? = null) {
    val focusManager = LocalFocusManager.current
    LaunchedEffect(focusEvents) {
        focusEvents.collect {
            if (action == null) {
                focusManager.moveFocus(FocusDirection.Next)
            } else {
                action.invoke()
            }
        }
    }
}
