package ch.admin.foitt.wallet.feature.qrscan.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.feature.qrscan.presentation.QrScanPermissionScreen
import ch.admin.foitt.wallet.feature.qrscan.presentation.QrScanPermissionViewModel
import ch.admin.foitt.wallet.feature.qrscan.presentation.QrScannerScreen
import ch.admin.foitt.wallet.feature.qrscan.presentation.QrScannerViewModel
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
        entry<Destination.QrScannerScreen> { navKey ->
            val viewModel =
                hiltViewModel<QrScannerViewModel, QrScannerViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            firstCredentialWasAdded = navKey.firstCredentialWasAdded
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                QrScannerScreen(viewModel = viewModel)
            }
        }

        entry<Destination.QrScanPermissionScreen> {
            val viewModel = hiltViewModel<QrScanPermissionViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                QrScanPermissionScreen(viewModel = viewModel)
            }
        }
    }
}
