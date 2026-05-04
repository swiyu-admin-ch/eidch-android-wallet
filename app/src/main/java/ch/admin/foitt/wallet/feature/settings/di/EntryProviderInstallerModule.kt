package ch.admin.foitt.wallet.feature.settings.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.SettingsScreen
import ch.admin.foitt.wallet.feature.settings.presentation.SettingsViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.biometrics.AuthWithPassphraseScreen
import ch.admin.foitt.wallet.feature.settings.presentation.biometrics.AuthWithPassphraseViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.biometrics.EnableBiometricsErrorScreen
import ch.admin.foitt.wallet.feature.settings.presentation.biometrics.EnableBiometricsErrorViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.biometrics.EnableBiometricsLockoutScreen
import ch.admin.foitt.wallet.feature.settings.presentation.biometrics.EnableBiometricsLockoutViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.biometrics.EnableBiometricsScreen
import ch.admin.foitt.wallet.feature.settings.presentation.biometrics.EnableBiometricsViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.impressum.ImpressumScreen
import ch.admin.foitt.wallet.feature.settings.presentation.impressum.ImpressumViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.language.LanguageScreen
import ch.admin.foitt.wallet.feature.settings.presentation.language.LanguageViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.licences.LicencesScreen
import ch.admin.foitt.wallet.feature.settings.presentation.licences.LicencesViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.lottieViewer.LottieViewerScreen
import ch.admin.foitt.wallet.feature.settings.presentation.lottieViewer.LottieViewerViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.security.ActivityListSettingsScreen
import ch.admin.foitt.wallet.feature.settings.presentation.security.ActivityListSettingsViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.security.DataAnalysisScreen
import ch.admin.foitt.wallet.feature.settings.presentation.security.DataAnalysisViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.security.SecuritySettingsScreen
import ch.admin.foitt.wallet.feature.settings.presentation.security.SecuritySettingsViewModel
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
        entry<Destination.SettingsScreen> {
            val viewModel = hiltViewModel<SettingsViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                SettingsScreen(viewModel = viewModel)
            }
        }

        entry<Destination.AuthWithPassphraseScreen> { navKey ->
            val viewModel =
                hiltViewModel<AuthWithPassphraseViewModel, AuthWithPassphraseViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(enableBiometrics = navKey.enableBiometrics)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                AuthWithPassphraseScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EnableBiometricsErrorScreen> {
            val viewModel = hiltViewModel<EnableBiometricsErrorViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                EnableBiometricsErrorScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EnableBiometricsLockoutScreen> {
            val viewModel = hiltViewModel<EnableBiometricsLockoutViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                EnableBiometricsLockoutScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EnableBiometricsScreen> { navKey ->
            val viewModel =
                hiltViewModel<EnableBiometricsViewModel, EnableBiometricsViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(pin = navKey.pin)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EnableBiometricsScreen(viewModel = viewModel)
            }
        }

        entry<Destination.ImpressumScreen> {
            val viewModel = hiltViewModel<ImpressumViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                ImpressumScreen(viewModel = viewModel)
            }
        }

        entry<Destination.LottieViewerScreen> {
            val viewModel = hiltViewModel<LottieViewerViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                LottieViewerScreen(viewModel = viewModel)
            }
        }

        entry<Destination.LanguageScreen> {
            val viewModel = hiltViewModel<LanguageViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                LanguageScreen(viewModel = viewModel)
            }
        }

        entry<Destination.LicencesScreen> {
            val viewModel = hiltViewModel<LicencesViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                LicencesScreen(viewModel = viewModel)
            }
        }

        entry<Destination.DataAnalysisScreen> {
            val viewModel = hiltViewModel<DataAnalysisViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                DataAnalysisScreen()
            }
        }

        entry<Destination.ActivityListSettingsScreen> {
            val viewModel = hiltViewModel<ActivityListSettingsViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                ActivityListSettingsScreen(viewModel = viewModel)
            }
        }

        entry<Destination.SecuritySettingsScreen> {
            val viewModel = hiltViewModel<SecuritySettingsViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                SecuritySettingsScreen(viewModel = viewModel)
            }
        }
    }
}
