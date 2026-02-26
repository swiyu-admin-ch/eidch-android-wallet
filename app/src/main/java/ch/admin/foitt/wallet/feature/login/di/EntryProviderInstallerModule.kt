package ch.admin.foitt.wallet.feature.login.di

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.ui.NavDisplay
import ch.admin.foitt.wallet.feature.login.presentation.BiometricLoginScreen
import ch.admin.foitt.wallet.feature.login.presentation.BiometricLoginViewModel
import ch.admin.foitt.wallet.feature.login.presentation.LockScreen
import ch.admin.foitt.wallet.feature.login.presentation.LockViewModel
import ch.admin.foitt.wallet.feature.login.presentation.LockoutScreen
import ch.admin.foitt.wallet.feature.login.presentation.LockoutViewModel
import ch.admin.foitt.wallet.feature.login.presentation.PassphraseLoginScreen
import ch.admin.foitt.wallet.feature.login.presentation.PassphraseLoginViewModel
import ch.admin.foitt.wallet.feature.login.presentation.UnsecuredDeviceScreen
import ch.admin.foitt.wallet.feature.login.presentation.UnsecuredDeviceViewModel
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.EntryProviderInstaller
import ch.admin.foitt.wallet.platform.scaffold.presentation.SyncedScaffoldScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object EntryProviderInstallerModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(): EntryProviderInstaller = {
        entry<Destination.BiometricLoginScreen>(
            metadata = loginAnimation()
        ) {
            val viewModel = hiltViewModel<BiometricLoginViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                BiometricLoginScreen(viewModel = viewModel)
            }
        }

        entry<Destination.LockoutScreen> {
            val viewModel = hiltViewModel<LockoutViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                LockoutScreen(viewModel = viewModel)
            }
        }

        entry<Destination.LockScreen>(
            metadata = loginAnimation()
        ) {
            val viewModel = hiltViewModel<LockViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                LockScreen(viewModel = viewModel)
            }
        }

        entry<Destination.PassphraseLoginScreen>(
            metadata = loginAnimation()
        ) { navKey ->
            val viewModel =
                hiltViewModel<PassphraseLoginViewModel, PassphraseLoginViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(biometricsLocked = navKey.biometricsLocked)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                PassphraseLoginScreen(viewModel = viewModel)
            }
        }

        entry<Destination.UnsecuredDeviceScreen> {
            val viewModel = hiltViewModel<UnsecuredDeviceViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                UnsecuredDeviceScreen(viewModel = viewModel)
            }
        }
    }

    private fun loginAnimation(): Map<String, Any> = NavDisplay.transitionSpec {
        // Fade new content in, keeping the old content in place underneath
        fadeIn(
            animationSpec = tween(durationMillis = 200, easing = LinearEasing)
        ) togetherWith ExitTransition.KeepUntilTransitionsFinished
    } + NavDisplay.popTransitionSpec {
        // Fade old content out, revealing the new content in place underneath
        EnterTransition.None togetherWith
            fadeOut(animationSpec = tween(durationMillis = 200, easing = LinearEasing))
    } + NavDisplay.predictivePopTransitionSpec {
        // Fade old content out, revealing the new content in place underneath
        EnterTransition.None togetherWith
            fadeOut(animationSpec = tween(durationMillis = 200, easing = LinearEasing))
    }
}
