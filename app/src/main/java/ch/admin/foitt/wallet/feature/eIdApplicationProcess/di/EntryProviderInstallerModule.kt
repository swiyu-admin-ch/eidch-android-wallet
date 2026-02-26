package ch.admin.foitt.wallet.feature.eIdApplicationProcess.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdAttestationScreen
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdAttestationViewModel
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdDocumentSelectionScreen
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdDocumentSelectionViewModel
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdGuardianConsentResultScreen
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdGuardianConsentResultViewModel
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdGuardianConsentScreen
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdGuardianConsentViewModel
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdGuardianSelectionScreen
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdGuardianSelectionViewModel
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdGuardianVerificationScreen
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdGuardianVerificationViewModel
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdGuardianshipScreen
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdGuardianshipViewModel
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdIntroScreen
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdIntroViewModel
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdPrivacyPolicyScreen
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdPrivacyPolicyViewModel
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdProcessDataConfirmationScreen
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdProcessDataConfirmationViewModel
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdProcessDataScreen
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdProcessDataViewModel
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdQueueScreen
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdQueueViewModel
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdReadyForAvScreen
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdReadyForAvViewModel
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdStartAvSessionScreen
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdStartAvSessionViewModel
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdStartSelfieVideoScreen
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.EIdStartSelfieVideoViewModel
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
        entry<Destination.EIdIntroScreen> {
            val viewModel = hiltViewModel<EIdIntroViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdIntroScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdAttestationScreen> {
            val viewModel = hiltViewModel<EIdAttestationViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdAttestationScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdDocumentSelectionScreen> {
            val viewModel = hiltViewModel<EIdDocumentSelectionViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdDocumentSelectionScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdGuardianConsentResultScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdGuardianConsentResultViewModel, EIdGuardianConsentResultViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            rawDeadline = navKey.rawDeadline,
                            screenState = navKey.screenState
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdGuardianConsentResultScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdGuardianConsentScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdGuardianConsentViewModel, EIdGuardianConsentViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            caseId = navKey.caseId
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdGuardianConsentScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdGuardianSelectionScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdGuardianSelectionViewModel, EIdGuardianSelectionViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            caseId = navKey.caseId
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdGuardianSelectionScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdGuardianshipScreen> {
            val viewModel = hiltViewModel<EIdGuardianshipViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdGuardianshipScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdGuardianVerificationScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdGuardianVerificationViewModel, EIdGuardianVerificationViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            caseId = navKey.caseId
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdGuardianVerificationScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdPrivacyPolicyScreen> {
            val viewModel = hiltViewModel<EIdPrivacyPolicyViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdPrivacyPolicyScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdProcessDataScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdProcessDataViewModel, EIdProcessDataViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            caseId = navKey.caseId
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdProcessDataScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdQueueScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdQueueViewModel, EIdQueueViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            rawDeadline = navKey.rawDeadline
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdQueueScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdReadyForAvScreen> {
            val viewModel = hiltViewModel<EIdReadyForAvViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdReadyForAvScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdStartAvSessionScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdStartAvSessionViewModel, EIdStartAvSessionViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(caseId = navKey.caseId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdStartAvSessionScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdStartSelfieVideoScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdStartSelfieVideoViewModel, EIdStartSelfieVideoViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(caseId = navKey.caseId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdStartSelfieVideoScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdProcessDataConfirmationScreen> {
            val viewModel = hiltViewModel<EIdProcessDataConfirmationViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdProcessDataConfirmationScreen(viewModel = viewModel)
            }
        }
    }
}
