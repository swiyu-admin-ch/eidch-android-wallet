package ch.admin.foitt.wallet.platform.activityList.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.platform.activityList.presentation.ActivityDetailScreen
import ch.admin.foitt.wallet.platform.activityList.presentation.ActivityDetailViewModel
import ch.admin.foitt.wallet.platform.activityList.presentation.ActivityListScreen
import ch.admin.foitt.wallet.platform.activityList.presentation.ActivityListViewModel
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
        entry<Destination.ActivityListScreen> { navKey ->
            val viewModel =
                hiltViewModel<ActivityListViewModel, ActivityListViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(credentialId = navKey.credentialId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                ActivityListScreen(viewModel = viewModel)
            }
        }

        entry<Destination.ActivityDetailScreen> { navKey ->
            val viewModel =
                hiltViewModel<ActivityDetailViewModel, ActivityDetailViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(credentialId = navKey.credentialId, activityId = navKey.activityId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                ActivityDetailScreen(viewModel = viewModel)
            }
        }
    }
}
