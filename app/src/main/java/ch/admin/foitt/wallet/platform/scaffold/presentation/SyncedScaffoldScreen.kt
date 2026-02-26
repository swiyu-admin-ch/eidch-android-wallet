package ch.admin.foitt.wallet.platform.scaffold.presentation

import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.theme.LocalIsInDarkTheme

/**
 * A composable that synchronizes scaffold state before navigating to a screen.
 *
 * This function prepares the status bar and navigation bar styling based on
 * the current theme as defined in the viewModel. It automatically synchronizes
 * the scaffold state with the activity's window insets, handling dark theme
 * considerations.
 *
 * @param viewModel The view model associated with the screen
 * @param screen The composable content to display after scaffold setup is complete
 *
 * @sample
 * ```kotlin
 * val viewModel = hiltViewModel<ExampleScreenViewModel>()
 * SyncedScaffoldScreen(viewModel = viewModel) {
 *     ExampleScreen(viewModel = viewModel)
 * }
 * ```
 *
 * @see [ScreenViewModel.syncScaffoldState] for the implementation of the scaffold state synchronization
 */
@Composable
inline fun <reified T : ScreenViewModel> SyncedScaffoldScreen(viewModel: T, crossinline screen: @Composable (viewModel: T) -> Unit) {
    val currentActivity = LocalActivity.current
    val isInDarkTheme = LocalIsInDarkTheme.current
    // Setup the top and bottom bar areas, including the navigation and status bar styling
    LaunchedEffect(currentActivity, viewModel, isInDarkTheme) {
        viewModel.syncScaffoldState(
            currentActivity::enableEdgeToEdge,
            isInDarkTheme
        )
    }
    screen(viewModel)
}
