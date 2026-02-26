package ch.admin.foitt.wallet.feature.credentialOffer.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.CredentialOfferScreen
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.CredentialOfferViewModel
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.DeclineCredentialOfferScreen
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.DeclineCredentialOfferViewModel
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
        entry<Destination.CredentialOfferScreen> { navKey ->
            val viewModel =
                hiltViewModel<CredentialOfferViewModel, CredentialOfferViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(credentialId = navKey.credentialId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                CredentialOfferScreen(viewModel = viewModel)
            }
        }
        entry<Destination.DeclineCredentialOfferScreen> { navKey ->
            val viewModel =
                hiltViewModel<DeclineCredentialOfferViewModel, DeclineCredentialOfferViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(credentialId = navKey.credentialId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                DeclineCredentialOfferScreen(viewModel = viewModel)
            }
        }
    }
}
