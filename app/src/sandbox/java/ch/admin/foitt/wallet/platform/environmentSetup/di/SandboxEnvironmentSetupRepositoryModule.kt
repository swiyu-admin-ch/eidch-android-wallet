package ch.admin.foitt.wallet.platform.environmentSetup.di

import ch.admin.foitt.wallet.platform.environmentSetup.data.SandboxEnvironmentSetupRepositoryImpl
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap

@Module
@InstallIn(ActivityRetainedComponent::class)
object SandboxEnvironmentSetupRepositoryModule {
    @Provides
    @IntoMap
    @IntKey(1)
    fun provideSandboxEnvironmentSetupRepository(): EnvironmentSetupRepository = SandboxEnvironmentSetupRepositoryImpl()
}
