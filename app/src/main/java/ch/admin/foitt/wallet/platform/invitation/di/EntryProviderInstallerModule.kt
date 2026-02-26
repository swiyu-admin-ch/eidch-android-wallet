package ch.admin.foitt.wallet.platform.invitation.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.platform.invitation.presentation.InvitationFailureScreen
import ch.admin.foitt.wallet.platform.invitation.presentation.InvitationFailureViewModel
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
        entry<Destination.InvitationFailureScreen> { navKey ->
            val viewModel =
                hiltViewModel<InvitationFailureViewModel, InvitationFailureViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(
                            invitationErrorScreenState = navKey.invitationError,
                            uri = navKey.uri
                        )
                    }
                )
            SyncedScaffoldScreen(viewModel = viewModel) {
                InvitationFailureScreen(viewModel = viewModel)
            }
        }
    }
}
