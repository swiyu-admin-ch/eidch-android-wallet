package ch.admin.foitt.wallet.feature.presentationRequest.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationCredentialListScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationCredentialListViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationDeclinedScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationDeclinedViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationFailureScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationFailureViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationRequestBlockedScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationRequestBlockedViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationRequestReviewScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationRequestReviewViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationRequestScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationRequestViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationSuccessScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationSuccessViewModel
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

        entry<Destination.PresentationDeclinedScreen> { navKey ->
            val viewModel = hiltViewModel<PresentationDeclinedViewModel, PresentationDeclinedViewModel.Factory>(
                creationCallback = { factory ->
                    factory.create(
                        redirectUri = navKey.redirectUri,
                        credentialId = navKey.credentialId,
                        activityId = navKey.activityId
                    )
                }
            )
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

        entry<Destination.PresentationRequestReviewScreen> { navKey ->
            val viewModel =
                hiltViewModel<PresentationRequestReviewViewModel, PresentationRequestReviewViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            compatibleCredentials = navKey.compatibleCredentials,
                            presentationRequestWithRaw = navKey.presentationRequestWithRaw,
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                PresentationRequestReviewScreen(viewModel = viewModel)
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

        entry<Destination.PresentationRequestBlockedScreen> {
            val viewModel = hiltViewModel<PresentationRequestBlockedViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                PresentationRequestBlockedScreen(viewModel = viewModel)
            }
        }

        entry<Destination.PresentationSuccessScreen> { navKey ->
            val viewModel =
                hiltViewModel<PresentationSuccessViewModel, PresentationSuccessViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            redirectUri = navKey.redirectUri
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                PresentationSuccessScreen(viewModel = viewModel)
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
