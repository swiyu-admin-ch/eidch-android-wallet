package ch.admin.foitt.wallet.app.presentation

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ch.admin.foitt.wallet.platform.navigation.domain.model.EntryProviderInstaller
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenContainer
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun MainScreen(
    activity: AppCompatActivity,
    entryProviderBuilders: Set<@JvmSuppressWildcards EntryProviderInstaller>,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val backstack = viewModel.getBackStack()
    WalletTheme {
        CompositionLocalProvider(LocalActivity provides activity) {
            ScreenContainer {
                NavDisplay(
                    backStack = backstack,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    onBack = {
                        backstack.removeLastOrNull()
                    },
                    entryProvider = entryProvider<NavKey> {
                        entryProviderBuilders.forEach { builder ->
                            this.builder()
                        }
                    },
                    transitionSpec = {
                        // Slide in from right when navigating forward
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300)
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(300)
                        )
                    },
                    popTransitionSpec = {
                        // Slide in from left when navigating back
                        slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(300)
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(300)
                        )
                    },
                    predictivePopTransitionSpec = {
                        // Slide in from left when navigating back
                        slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(300)
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(300)
                        )
                    }
                )
            }
        }

        LaunchedEffect(activity.intent) {
            viewModel.handleIntent(activity)
        }
    }
}
