package ch.admin.foitt.wallet.platform.proximity.di

import ch.admin.foitt.swiyu.shared.proximity.ProximityPresentationController
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponent
import ch.admin.foitt.wallet.platform.navigation.DestinationsScoped
import ch.admin.foitt.wallet.platform.proximity.data.repository.ProximityRepositoryImpl
import ch.admin.foitt.wallet.platform.proximity.domain.model.domain.repository.ProximityRepository
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.GetProximityRepositoryForScope
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.ProximityEngagement
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.implementation.GetProximityRepositoryForScopeImpl
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.implementation.ProximityEngagementImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface ProximityModule {

    @Binds
    fun bindStartProximityEngagement(
        useCase: ProximityEngagementImpl
    ): ProximityEngagement

    @Binds
    fun bindGetProximityRepositoryForScope(
        useCase: GetProximityRepositoryForScopeImpl
    ): GetProximityRepositoryForScope
}

@Module
@InstallIn(DestinationScopedComponent::class)
internal interface ProximityRepositoryModule {

    @Binds
    @DestinationsScoped
    fun bindProximityRepository(
        proximityRepositoryImpl: ProximityRepositoryImpl
    ): ProximityRepository

    companion object {
        @Provides
        @DestinationsScoped
        fun provideProximityPresentationController(): ProximityPresentationController =
            ProximityPresentationController()
    }
}

@EntryPoint
@InstallIn(DestinationScopedComponent::class)
interface ProximityRepositoryEntryPoint {
    fun proximityRepository(): ProximityRepository
}
