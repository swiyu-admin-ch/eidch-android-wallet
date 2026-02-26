package ch.admin.foitt.wallet.platform.actorMetadata.di

import ch.admin.foitt.wallet.platform.actorMetadata.data.repository.ActorRepositoryImpl
import ch.admin.foitt.wallet.platform.actorMetadata.domain.repository.ActorRepository
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.CacheIssuerDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchAndCacheIssuerDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchAndCacheVerifierDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.GetActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.InitializeActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.CacheIssuerDisplayDataImpl
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.FetchAndCacheIssuerDisplayDataImpl
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.FetchAndCacheVerifierDisplayDataImpl
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.GetActorForScopeImpl
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.InitializeActorForScopeImpl
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.implementation.GetActorUiStateImpl
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponent
import ch.admin.foitt.wallet.platform.navigation.DestinationsScoped
import dagger.Binds
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface ActorMetadataModule {
    @Binds
    fun bindCacheIssuerDisplayData(
        useCase: CacheIssuerDisplayDataImpl
    ): CacheIssuerDisplayData

    @Binds
    fun bindFetchAndCacheVerifierDisplayData(
        useCase: FetchAndCacheVerifierDisplayDataImpl
    ): FetchAndCacheVerifierDisplayData

    @Binds
    fun bindFetchAndCacheIssuerDisplayData(
        useCase: FetchAndCacheIssuerDisplayDataImpl
    ): FetchAndCacheIssuerDisplayData

    @Binds
    fun bindGetActorUiState(
        adapter: GetActorUiStateImpl
    ): GetActorUiState

    @Binds
    fun bindInitializeActorForScope(
        useCase: InitializeActorForScopeImpl
    ): InitializeActorForScope

    @Binds
    fun bindGetActorForScope(
        useCase: GetActorForScopeImpl
    ): GetActorForScope
}

@Module
@InstallIn(DestinationScopedComponent::class)
internal interface ActorRepositoryModule {
    @Binds
    @DestinationsScoped
    fun bindGetActorRepository(
        repo: ActorRepositoryImpl
    ): ActorRepository
}

@EntryPoint
@InstallIn(DestinationScopedComponent::class)
interface ActorRepositoryEntryPoint {
    fun actorRepository(): ActorRepository
}
