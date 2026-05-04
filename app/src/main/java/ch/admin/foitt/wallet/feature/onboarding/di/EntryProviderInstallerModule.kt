package ch.admin.foitt.wallet.feature.onboarding.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingActivityScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingActivityViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingConfirmPassphraseFailureScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingConfirmPassphraseScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingConfirmPassphraseViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingErrorScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingErrorViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingIntroScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingIntroViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingLocalDataScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingLocalDataViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingPassphraseConfirmationFailureViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingPassphraseExplanationScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingPassphraseExplanationViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingPresentScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingPresentViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingPrivacyPolicyScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingPrivacyPolicyViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingRegisterBiometricsScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingRegisterBiometricsViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingSetupPassphraseScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingSetupPassphraseViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingSuccessScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingSuccessViewModel
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
        entry<Destination.OnboardingIntroScreen> {
            val viewModel = hiltViewModel<OnboardingIntroViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                OnboardingIntroScreen(viewModel = viewModel)
            }
        }

        entry<Destination.OnboardingLocalDataScreen> {
            val viewModel = hiltViewModel<OnboardingLocalDataViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                OnboardingLocalDataScreen(viewModel = it)
            }
        }

        entry<Destination.OnboardingActivityScreen> {
            val viewModel = hiltViewModel<OnboardingActivityViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                OnboardingActivityScreen(viewModel = it)
            }
        }

        entry<Destination.OnboardingPresentScreen> {
            val viewModel = hiltViewModel<OnboardingPresentViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                OnboardingPresentScreen(viewModel = viewModel)
            }
        }

        entry<Destination.OnboardingPrivacyPolicyScreen> {
            val viewModel = hiltViewModel<OnboardingPrivacyPolicyViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                OnboardingPrivacyPolicyScreen(viewModel = it)
            }
        }

        entry<Destination.OnboardingPassphraseExplanationScreen> {
            val viewModel = hiltViewModel<OnboardingPassphraseExplanationViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                OnboardingPassphraseExplanationScreen(viewModel = viewModel)
            }
        }

        entry<Destination.OnboardingSetupPassphraseScreen> {
            val viewModel = hiltViewModel<OnboardingSetupPassphraseViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                OnboardingSetupPassphraseScreen(viewModel = viewModel)
            }
        }

        entry<Destination.OnboardingConfirmPassphraseScreen> { navKey ->
            val viewModel =
                hiltViewModel<OnboardingConfirmPassphraseViewModel, OnboardingConfirmPassphraseViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(originalPassphrase = navKey.passphrase)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                OnboardingConfirmPassphraseScreen(viewModel = viewModel)
            }
        }

        entry<Destination.OnboardingConfirmPassphraseFailureScreen> {
            val viewModel = hiltViewModel<OnboardingPassphraseConfirmationFailureViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                OnboardingConfirmPassphraseFailureScreen(viewModel = viewModel)
            }
        }

        entry<Destination.OnboardingRegisterBiometricsScreen> { navKey ->
            val viewModel =
                hiltViewModel<OnboardingRegisterBiometricsViewModel, OnboardingRegisterBiometricsViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(passphrase = navKey.passphrase)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                OnboardingRegisterBiometricsScreen(viewModel = viewModel)
            }
        }

        entry<Destination.OnboardingErrorScreen> {
            val viewModel = hiltViewModel<OnboardingErrorViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                OnboardingErrorScreen(viewModel = viewModel)
            }
        }

        entry<Destination.OnboardingSuccessScreen> {
            val viewModel = hiltViewModel<OnboardingSuccessViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                OnboardingSuccessScreen(viewModel = viewModel)
            }
        }
    }
}
