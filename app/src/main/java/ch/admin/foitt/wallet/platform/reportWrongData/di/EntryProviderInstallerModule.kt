package ch.admin.foitt.wallet.platform.reportWrongData.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.EntryProviderInstaller
import ch.admin.foitt.wallet.platform.reportWrongData.presentation.ReportWrongDataScreen
import ch.admin.foitt.wallet.platform.reportWrongData.presentation.ReportWrongDataViewModel
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
        entry<Destination.ReportWrongDataScreen> {
            val viewModel = hiltViewModel<ReportWrongDataViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                ReportWrongDataScreen()
            }
        }
    }
}
