package ch.admin.foitt.wallet.platform.nonCompliance.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.EntryProviderInstaller
import ch.admin.foitt.wallet.platform.nonCompliance.presentation.NonComplianceDescriptionInputScreen
import ch.admin.foitt.wallet.platform.nonCompliance.presentation.NonComplianceDescriptionInputViewModel
import ch.admin.foitt.wallet.platform.nonCompliance.presentation.NonComplianceEmailInputScreen
import ch.admin.foitt.wallet.platform.nonCompliance.presentation.NonComplianceEmailInputViewModel
import ch.admin.foitt.wallet.platform.nonCompliance.presentation.NonComplianceFormScreen
import ch.admin.foitt.wallet.platform.nonCompliance.presentation.NonComplianceFormViewModel
import ch.admin.foitt.wallet.platform.nonCompliance.presentation.NonComplianceInfoScreen
import ch.admin.foitt.wallet.platform.nonCompliance.presentation.NonComplianceInfoViewModel
import ch.admin.foitt.wallet.platform.nonCompliance.presentation.NonComplianceListScreen
import ch.admin.foitt.wallet.platform.nonCompliance.presentation.NonComplianceListViewModel
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
        entry<Destination.NonComplianceEmailInputScreen> {
            val viewModel = hiltViewModel<NonComplianceEmailInputViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                NonComplianceEmailInputScreen(viewModel = viewModel)
            }
        }

        entry<Destination.NonComplianceDescriptionInputScreen> {
            val viewModel = hiltViewModel<NonComplianceDescriptionInputViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                NonComplianceDescriptionInputScreen(viewModel = viewModel)
            }
        }

        entry<Destination.NonComplianceFormScreen> { navKey ->
            val viewModel =
                hiltViewModel<NonComplianceFormViewModel, NonComplianceFormViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            activityId = navKey.activityId,
                            titleId = navKey.titleId,
                            reportReason = navKey.reportReason
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                NonComplianceFormScreen(viewModel = viewModel)
            }
        }

        entry<Destination.NonComplianceInfoScreen> { navKey ->
            val viewModel =
                hiltViewModel<NonComplianceInfoViewModel, NonComplianceInfoViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            activityId = navKey.activityId,
                            reportReason = navKey.reportReason
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                NonComplianceInfoScreen(viewModel = viewModel)
            }
        }

        entry<Destination.NonComplianceListScreen> { navKey ->
            val viewModel =
                hiltViewModel<NonComplianceListViewModel, NonComplianceListViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            activityId = navKey.activityId,
                            activityType = navKey.activityType
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                NonComplianceListScreen(viewModel = viewModel)
            }
        }
    }
}
