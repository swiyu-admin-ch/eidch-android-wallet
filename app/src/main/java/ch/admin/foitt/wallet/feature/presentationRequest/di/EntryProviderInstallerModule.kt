package ch.admin.foitt.wallet.feature.presentationRequest.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationCredentialListScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationCredentialListViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationDeclinedScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationDeclinedViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationFailureScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationFailureViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationInvalidCredentialErrorScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationInvalidCredentialErrorViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationRequestScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationRequestViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationSuccessScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationSuccessViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationValidationErrorScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationValidationErrorViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationVerificationErrorScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationVerificationErrorViewModel
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
        entry<Destination.PresentationCredentialListScreen> { navKey ->
            val viewModel =
                hiltViewModel<PresentationCredentialListViewModel, PresentationCredentialListViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            compatibleCredentials = navKey.compatibleCredentials,
                            presentationRequestWithRaw = navKey.presentationRequestWithRaw,
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                PresentationCredentialListScreen(viewModel = viewModel)
            }
        }

        entry<Destination.PresentationDeclinedScreen> {
            val viewModel = hiltViewModel<PresentationDeclinedViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                PresentationDeclinedScreen(viewModel = viewModel)
            }
        }

        entry<Destination.PresentationFailureScreen> { navKey ->
            val viewModel =
                hiltViewModel<PresentationFailureViewModel, PresentationFailureViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            compatibleCredential = navKey.compatibleCredential,
                            presentationRequestWithRaw = navKey.presentationRequestWithRaw,
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                PresentationFailureScreen(viewModel = viewModel)
            }
        }

        entry<Destination.PresentationInvalidCredentialErrorScreen> { navKey ->
            val viewModel =
                hiltViewModel<PresentationInvalidCredentialErrorViewModel, PresentationInvalidCredentialErrorViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(sentFields = navKey.sentFields)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                PresentationInvalidCredentialErrorScreen(viewModel = viewModel)
            }
        }

        entry<Destination.PresentationRequestScreen> { navKey ->
            val viewModel =
                hiltViewModel<PresentationRequestViewModel, PresentationRequestViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            compatibleCredential = navKey.compatibleCredential,
                            presentationRequestWithRaw = navKey.presentationRequestWithRaw,
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                PresentationRequestScreen(viewModel = viewModel)
            }
        }

        entry<Destination.PresentationSuccessScreen> { navKey ->
            val viewModel =
                hiltViewModel<PresentationSuccessViewModel, PresentationSuccessViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            sentFields = navKey.sentFields
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                PresentationSuccessScreen(viewModel = viewModel)
            }
        }

        entry<Destination.PresentationValidationErrorScreen> {
            val viewModel = hiltViewModel<PresentationValidationErrorViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                PresentationValidationErrorScreen(viewModel = viewModel)
            }
        }

        entry<Destination.PresentationVerificationErrorScreen> {
            val viewModel = hiltViewModel<PresentationVerificationErrorViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                PresentationVerificationErrorScreen(viewModel = viewModel)
            }
        }
    }
}
