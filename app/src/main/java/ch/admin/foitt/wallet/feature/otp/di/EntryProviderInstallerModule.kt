package ch.admin.foitt.wallet.feature.otp.di

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.admin.foitt.wallet.feature.otp.presentation.OtpCodeInputScreen
import ch.admin.foitt.wallet.feature.otp.presentation.OtpCodeInputViewModel
import ch.admin.foitt.wallet.feature.otp.presentation.OtpEmailInputScreen
import ch.admin.foitt.wallet.feature.otp.presentation.OtpEmailInputViewModel
import ch.admin.foitt.wallet.feature.otp.presentation.OtpIntroScreen
import ch.admin.foitt.wallet.feature.otp.presentation.OtpIntroViewModel
import ch.admin.foitt.wallet.feature.otp.presentation.OtpLegalScreen
import ch.admin.foitt.wallet.feature.otp.presentation.OtpLegalViewModel
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
        entry<Destination.OtpIntroScreen> {
            val viewModel =
                hiltViewModel<OtpIntroViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                OtpIntroScreen(viewModel = viewModel)
            }
        }

        entry<Destination.OtpLegalScreen> {
            val viewModel =
                hiltViewModel<OtpLegalViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                OtpLegalScreen(viewModel = viewModel)
            }
        }

        entry<Destination.OtpEmailInputScreen> {
            val viewModel =
                hiltViewModel<OtpEmailInputViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                OtpEmailInputScreen(viewModel = viewModel)
            }
        }

        entry<Destination.OtpCodeInputScreen> {
            val viewModel =
                hiltViewModel<OtpCodeInputViewModel>()
            SyncedScaffoldScreen(viewModel = viewModel) {
                OtpCodeInputScreen(viewModel = viewModel)
            }
        }
    }
}
