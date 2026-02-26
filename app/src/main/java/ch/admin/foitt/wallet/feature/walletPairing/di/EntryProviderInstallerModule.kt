package ch.admin.foitt.wallet.feature.walletPairing.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.feature.walletPairing.presentation.EIdPairingOverviewScreen
import ch.admin.foitt.wallet.feature.walletPairing.presentation.EIdPairingOverviewViewModel
import ch.admin.foitt.wallet.feature.walletPairing.presentation.EIdWalletPairingQrCodeScreen
import ch.admin.foitt.wallet.feature.walletPairing.presentation.EIdWalletPairingQrCodeViewModel
import ch.admin.foitt.wallet.feature.walletPairing.presentation.EIdWalletPairingScreen
import ch.admin.foitt.wallet.feature.walletPairing.presentation.EIdWalletPairingViewModel
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
        entry<Destination.EIdPairingOverviewScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdPairingOverviewViewModel, EIdPairingOverviewViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(caseId = navKey.caseId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdPairingOverviewScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdWalletPairingQrCodeScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdWalletPairingQrCodeViewModel, EIdWalletPairingQrCodeViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(caseId = navKey.caseId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdWalletPairingQrCodeScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdWalletPairingScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdWalletPairingViewModel, EIdWalletPairingViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(caseId = navKey.caseId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdWalletPairingScreen(viewModel = viewModel)
            }
        }
    }
}
