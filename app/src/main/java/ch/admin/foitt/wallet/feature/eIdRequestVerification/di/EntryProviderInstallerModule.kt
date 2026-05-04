package ch.admin.foitt.wallet.feature.eIdRequestVerification.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdDocumentRecordingInfoScreen
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdDocumentRecordingInfoViewModel
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdDocumentRecordingScreen
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdDocumentRecordingViewModel
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdDocumentScanSummaryScreen
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdDocumentScanSummaryViewModel
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdDocumentScannerInfoScreen
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdDocumentScannerInfoViewModel
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdDocumentScannerScreen
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdDocumentScannerViewModel
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdFaceScannerScreen
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdFaceScannerViewModel
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdNfcScannerScreen
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdNfcScannerViewModel
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdNfcSummaryScreen
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdNfcSummaryViewModel
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdStartAutoVerificationScreen
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.EIdStartAutoVerificationViewModel
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.MrzChooserScreen
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.MrzChooserViewModel
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.MrzScanPermissionScreen
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.MrzScanPermissionViewModel
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.MrzSubmissionScreen
import ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.MrzSubmissionViewModel
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
        entry<Destination.EIdDocumentRecordingScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdDocumentRecordingViewModel, EIdDocumentRecordingViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(caseId = navKey.caseId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdDocumentRecordingScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdDocumentScannerScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdDocumentScannerViewModel, EIdDocumentScannerViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(caseId = navKey.caseId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdDocumentScannerScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdDocumentScanSummaryScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdDocumentScanSummaryViewModel, EIdDocumentScanSummaryViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(caseId = navKey.caseId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdDocumentScanSummaryScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdFaceScannerScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdFaceScannerViewModel, EIdFaceScannerViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(caseId = navKey.caseId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdFaceScannerScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdNfcScannerScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdNfcScannerViewModel, EIdNfcScannerViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(caseId = navKey.caseId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdNfcScannerScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdNfcSummaryScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdNfcSummaryViewModel, EIdNfcSummaryViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            caseId = navKey.caseId,
                            picture = navKey.picture,
                            givenName = navKey.givenName,
                            surname = navKey.surname,
                            documentId = navKey.documentId,
                            expiryDate = navKey.expiryDate
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdNfcSummaryScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdStartAutoVerificationScreen> { navKey ->
            val viewModel =
                hiltViewModel<EIdStartAutoVerificationViewModel, EIdStartAutoVerificationViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(caseId = navKey.caseId)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdStartAutoVerificationScreen(viewModel = viewModel)
            }
        }

        entry<Destination.MrzChooserScreen> {
            val viewModel = hiltViewModel<MrzChooserViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                MrzChooserScreen(viewModel = viewModel)
            }
        }

        entry<Destination.MrzScanPermissionScreen> { navKey ->
            val viewModel = hiltViewModel<MrzScanPermissionViewModel, MrzScanPermissionViewModel.Factory>(
                creationCallback = { factory ->
                    factory.create(caseId = navKey.caseId)
                }
            )
            SyncedScaffoldScreen(viewModel = viewModel) {
                MrzScanPermissionScreen(viewModel = viewModel)
            }
        }

        entry<Destination.MrzSubmissionScreen> { navKey ->
            val viewModel =
                hiltViewModel<MrzSubmissionViewModel, MrzSubmissionViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(mrzLines = navKey.mrzLines)
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                MrzSubmissionScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdDocumentScannerInfoScreen> { navKey ->
            val viewModel = hiltViewModel<EIdDocumentScannerInfoViewModel, EIdDocumentScannerInfoViewModel.Factory>(
                creationCallback = { factory ->
                    factory.create(caseId = navKey.caseId)
                }
            )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdDocumentScannerInfoScreen(viewModel = viewModel)
            }
        }

        entry<Destination.EIdDocumentRecordingInfoScreen> { navKey ->
            val viewModel = hiltViewModel<EIdDocumentRecordingInfoViewModel, EIdDocumentRecordingInfoViewModel.Factory>(
                creationCallback = { factory ->
                    factory.create(caseId = navKey.caseId)
                }
            )
            SyncedScaffoldScreen(viewModel = viewModel) {
                EIdDocumentRecordingInfoScreen(viewModel = viewModel)
            }
        }
    }
}
