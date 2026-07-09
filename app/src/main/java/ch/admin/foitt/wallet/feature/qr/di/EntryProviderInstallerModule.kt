package ch.admin.foitt.wallet.feature.qr.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.feature.qr.presentation.ShowOrScanQrCodeScreen
import ch.admin.foitt.wallet.feature.qr.presentation.qrgenerator.QrGeneratorViewModel
import ch.admin.foitt.wallet.feature.qr.presentation.qrscan.QrScannerScreen
import ch.admin.foitt.wallet.feature.qr.presentation.qrscan.QrScannerViewModel
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.EntryProviderInstaller
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
        entry<Destination.ShowOrScanQrCodeScreen> { navKey ->
            val qrScannerViewModel = hiltViewModel<QrScannerViewModel>()
            val qrGeneratorViewModel = hiltViewModel<QrGeneratorViewModel>()
            ShowOrScanQrCodeScreen(
                qrScannerViewModel = qrScannerViewModel,
                qrGeneratorViewModel = qrGeneratorViewModel,
                verificationMode = navKey.verificationMode
            )
        }
        entry<Destination.QrScannerScreen> {
            val viewModel = hiltViewModel<QrScannerViewModel>()
            QrScannerScreen(
                viewModel = viewModel,
                updateContentShown = {}
            )
        }
    }
}
