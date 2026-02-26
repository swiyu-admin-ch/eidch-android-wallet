package ch.admin.foitt.wallet.platform.genericScreens.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.platform.genericScreens.presentation.GenericErrorScreen
import ch.admin.foitt.wallet.platform.genericScreens.presentation.GenericErrorViewModel
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
        entry<Destination.GenericErrorScreen> {
            val viewModel = hiltViewModel<GenericErrorViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                GenericErrorScreen(viewModel = viewModel)
            }
        }
    }
}
