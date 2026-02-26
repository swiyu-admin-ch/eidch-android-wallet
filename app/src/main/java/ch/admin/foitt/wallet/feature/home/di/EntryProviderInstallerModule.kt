package ch.admin.foitt.wallet.feature.home.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.feature.home.presentation.BetaIdScreen
import ch.admin.foitt.wallet.feature.home.presentation.BetaIdViewModel
import ch.admin.foitt.wallet.feature.home.presentation.HomeScreen
import ch.admin.foitt.wallet.feature.home.presentation.HomeViewModel
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
        entry<Destination.HomeScreen> {
            val viewModel = hiltViewModel<HomeViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                HomeScreen(viewModel = viewModel)
            }
        }

        entry<Destination.BetaIdScreen> {
            val viewModel = hiltViewModel<BetaIdViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                BetaIdScreen(viewModel = viewModel)
            }
        }
    }
}
