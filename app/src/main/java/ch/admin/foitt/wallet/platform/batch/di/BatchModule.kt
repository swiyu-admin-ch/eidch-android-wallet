package ch.admin.foitt.wallet.platform.batch.di

import ch.admin.foitt.wallet.platform.batch.domain.usecase.GetBatchSize
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.GetBatchSizeImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
interface BatchModule {

    @Binds
    @ActivityRetainedScoped
    fun bindGetBatchSize(
        useCase: GetBatchSizeImpl
    ): GetBatchSize
}
