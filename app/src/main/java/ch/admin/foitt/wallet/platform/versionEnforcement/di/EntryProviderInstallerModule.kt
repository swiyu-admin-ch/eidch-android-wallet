package ch.admin.foitt.wallet.platform.versionEnforcement.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.EntryProviderInstaller
import ch.admin.foitt.wallet.platform.scaffold.presentation.SyncedScaffoldScreen
import ch.admin.foitt.wallet.platform.versionEnforcement.presentation.AppVersionBlockedScreen
import ch.admin.foitt.wallet.platform.versionEnforcement.presentation.AppVersionBlockedViewModel
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
        entry<Destination.AppVersionBlockedScreen> { navKey ->
            val viewModel =
                hiltViewModel<AppVersionBlockedViewModel, AppVersionBlockedViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(title = navKey.title, text = navKey.text)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                AppVersionBlockedScreen(viewModel = viewModel)
            }
        }
    }
}
