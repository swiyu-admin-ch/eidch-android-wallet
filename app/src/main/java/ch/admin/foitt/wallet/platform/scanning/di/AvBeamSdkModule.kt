package ch.admin.foitt.wallet.platform.scanning.di

import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponent
import ch.admin.foitt.wallet.platform.navigation.DestinationsScoped
import ch.admin.foitt.wallet.platform.scanning.data.AvBeamRepositoryImpl
import ch.admin.foitt.wallet.platform.scanning.domain.repository.AvBeamRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn

@Module
@InstallIn(DestinationScopedComponent::class)
internal interface AvBeamSdkModule {

    @Binds
    @DestinationsScoped
    fun bindAvBeamRepository(repo: AvBeamRepositoryImpl): AvBeamRepository
}

@EntryPoint
@InstallIn(DestinationScopedComponent::class)
interface AvBeamSdkEntryPoint {
    fun avBeamRepository(): AvBeamRepository
}
