package ch.admin.foitt.wallet.feature.credentialDetail.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.CredentialDetailScreen
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.CredentialDetailViewModel
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.CredentialDetailWrongDataScreen
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.CredentialDetailWrongDataViewModel
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
        entry<Destination.CredentialDetailWrongDataScreen> {
            val viewModel = hiltViewModel<CredentialDetailWrongDataViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                CredentialDetailWrongDataScreen()
            }
        }

        entry<Destination.CredentialDetailScreen> { navKey ->
            val viewModel =
                hiltViewModel<CredentialDetailViewModel, CredentialDetailViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(credentialId = navKey.credentialId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                CredentialDetailScreen(viewModel = viewModel)
            }
        }
    }
}
