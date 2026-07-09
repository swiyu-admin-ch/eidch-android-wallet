@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.composables.presentation

import android.os.SystemClock
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.LayoutDirection
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.platform.utils.isScreenReaderOn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Modifier.addTopScaffoldPadding(): Modifier {
    val topPadding = LocalScaffoldPaddings.current.calculateTopPadding()
    return this
        .padding(top = topPadding)
        .consumeWindowInsets(WindowInsets(top = topPadding))
}

/**
 * Acts as an anchor for talk back which is always focused when view is first composed. Counters the problem that buttons that do not
 * change for talk back (e.g. back button) don't give up focus when screen changes. Does only work with non-focusable composables.
 */
@Composable
fun Modifier.nonFocusableAccessibilityAnchor(): Modifier {
    val context = LocalContext.current
    if (!context.isScreenReaderOn()) return Modifier
    var isFocusable by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(50) // delays are needed so that focus changes are applied
        focusManager.clearFocus(true)
        // make the composable shortly focusable to request focus but re-disable it that it is not focusable by tabbing through
        isFocusable = true
        delay(50)
        focusRequester.requestFocus()
        delay(50)
        isFocusable = false
    }
    return this
        .focusRequester(focusRequester)
        .focusable(isFocusable)
}

@Composable
fun Modifier.requestFocus(focusRequester: FocusRequester): Modifier {
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    return this
        .focusRequester(focusRequester)
        .focusable()
}

@Composable
fun Modifier.spaceBarKeyClickable(onSpace: () -> Unit): Modifier = composed {
    var lastSpace = remember {
        0L
    }
    onKeyEvent { keyEvent ->
        if (keyEvent.key == Key.Spacebar && SystemClock.elapsedRealtime() - lastSpace > 500L) {
            onSpace()
            lastSpace = SystemClock.elapsedRealtime()
            true
        } else {
            false
        }
    }
}

@Composable
fun Modifier.horizontalSafeDrawing() = windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))

@Composable
fun Modifier.verticalSafeDrawing() = windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))

@Composable
fun Modifier.startSafeDrawing() = windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))

@Composable
fun Modifier.endSafeDrawing() = windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.End))

@Composable
fun Modifier.topSafeDrawing() = windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))

@Composable
fun Modifier.bottomSafeDrawing() = windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))

@Composable
fun Modifier.centerHorizontallyOnFullscreen() = this.offset(
    x = (
        WindowInsets.safeDrawing.only(WindowInsetsSides.End).asPaddingValues().calculateEndPadding(LayoutDirection.Ltr) -
            WindowInsets.safeDrawing.only(WindowInsetsSides.Start).asPaddingValues().calculateStartPadding(LayoutDirection.Ltr)
        ) / 2
)

/**
 * Modifier to execute an action when a given key on an external keyboard was pressed
 * while the Composable was in Focus.
 *
 * Usage:
 * ```
 * WalletTexts.TitleTopBar(
 *    modifier = Modifier
 *    .actOnKeyWhenFocused(key = Key.DirectionDown, action = onAction)
 * ```
 * or without function to pass, via trailing lambda
 * ```
 * WalletTexts.TitleTopBar(
 *    modifier = Modifier
 *    .actOnKeyWhenFocused(key = Key.DirectionDown) {
 *          //Act on key down
 *    }
 * ```
 * @param key the key to react to, reaction happens upon [KeyEventType.KeyDown]
 * @param action the action to execute when the key gets pressed
 */
@Composable
fun Modifier.actOnKeyWhenFocused(key: Key, action: (() -> Unit)?): Modifier {
    if (action == null) {
        return this
    }
    var isFocused by remember { mutableStateOf(false) }
    return this
        .onFocusChanged {
            isFocused = it.isFocused
        }
        .onPreviewKeyEvent { event ->
            if (event.key == key && event.type == KeyEventType.KeyDown && isFocused) {
                action.invoke()
                true
            } else {
                false
            }
        }
}

/**
 * Modifier that detects when a [LazyListState] scroll is stuck (e.g. because the focused item
 * is pinned on screen by the LazyColumn) and scrolls to the first item to allow focus
 * traversal to continue.
 *
 * On each UP key press the current scroll position is recorded. After a short delay the
 * position is checked again. If it hasn't changed, the scroll is considered stuck and the
 * list is scrolled to item 0 so that previously off-screen focusable items become reachable.
 *
 * @param lazyListState the [LazyListState] of the LazyColumn to monitor
 * @param coroutineScope a [CoroutineScope] used to launch the delayed position check
 */
@Composable
fun Modifier.scrollToTopOnStuckFocus(
    lazyListState: LazyListState,
    coroutineScope: CoroutineScope,
): Modifier = onPreviewKeyEvent { event ->
    if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionUp) {
        val posBefore = lazyListState.firstVisibleItemIndex to
            lazyListState.firstVisibleItemScrollOffset
        coroutineScope.launch {
            delay(50)
            val posAfter = lazyListState.firstVisibleItemIndex to
                lazyListState.firstVisibleItemScrollOffset
            if (posBefore == posAfter) {
                lazyListState.scrollToItem(0)
            }
        }
    }
    // Always propagate the key event so normal scroll/focus behavior is preserved
    false
}
