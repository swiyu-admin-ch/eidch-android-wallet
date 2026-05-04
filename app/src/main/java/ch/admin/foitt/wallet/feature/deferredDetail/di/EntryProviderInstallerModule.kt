package ch.admin.foitt.wallet.feature.deferredDetail.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.feature.deferredDetail.presentation.DeferredDetailScreen
import ch.admin.foitt.wallet.feature.deferredDetail.presentation.DeferredDetailViewModel
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
        entry<Destination.DeferredDetailScreen> { navKey ->
            val viewModel =
                hiltViewModel<DeferredDetailViewModel, DeferredDetailViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(credentialId = navKey.credentialId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                DeferredDetailScreen(viewModel = viewModel)
            }
        }
    }
}
