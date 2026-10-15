package ch.admin.foitt.wallet.platform.scaffold.di

import ch.admin.foitt.wallet.platform.scaffold.data.repository.TopBarStateRepositoryImpl
import ch.admin.foitt.wallet.platform.scaffold.domain.repository.TopBarStateRepository
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.implementation.SetTopBarStateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface ScaffoldModule {
    @Binds
    @ActivityRetainedScoped
    fun bindTopBarRepository(
        repository: TopBarStateRepositoryImpl
    ): TopBarStateRepository

    @Binds
    fun bindSetTopBar(
        useCase: SetTopBarStateImpl
    ): SetTopBarState
}
