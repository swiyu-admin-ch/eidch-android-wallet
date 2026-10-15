package ch.admin.foitt.wallet.feature.changePassphrase.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.feature.changePassphrase.presentation.ConfirmNewPassphraseScreen
import ch.admin.foitt.wallet.feature.changePassphrase.presentation.ConfirmNewPassphraseViewModel
import ch.admin.foitt.wallet.feature.changePassphrase.presentation.EnterCurrentPassphraseScreen
import ch.admin.foitt.wallet.feature.changePassphrase.presentation.EnterCurrentPassphraseViewModel
import ch.admin.foitt.wallet.feature.changePassphrase.presentation.EnterNewPassphraseScreen
import ch.admin.foitt.wallet.feature.changePassphrase.presentation.EnterNewPassphraseViewModel
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
        entry<Destination.ConfirmNewPassphraseScreen> { navKey ->
            val viewModel =
                hiltViewModel<ConfirmNewPassphraseViewModel, ConfirmNewPassphraseViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(originalPassphrase = navKey.originalPassphrase)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                ConfirmNewPassphraseScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EnterNewPassphraseScreen> {
            val viewModel = hiltViewModel<EnterNewPassphraseViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                EnterNewPassphraseScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EnterCurrentPassphraseScreen> {
            val viewModel = hiltViewModel<EnterCurrentPassphraseViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                EnterCurrentPassphraseScreen(viewModel = viewModel)
            }
        }
    }
}
