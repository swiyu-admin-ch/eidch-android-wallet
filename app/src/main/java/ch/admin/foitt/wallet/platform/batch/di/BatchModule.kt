package ch.admin.foitt.wallet.platform.batch.di

import ch.admin.foitt.wallet.platform.batch.data.repository.BatchRefreshDataRepositoryImpl
import ch.admin.foitt.wallet.platform.batch.domain.repository.BatchRefreshDataRepository
import ch.admin.foitt.wallet.platform.batch.domain.usecase.DeleteBundleItemsByAmount
import ch.admin.foitt.wallet.platform.batch.domain.usecase.RefreshBatchCredentials
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.DeleteBundleItemsByAmountImpl
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.RefreshBatchCredentialsImpl
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
    fun bindBatchRefreshDataRepository(
        repo: BatchRefreshDataRepositoryImpl
    ): BatchRefreshDataRepository

    @Binds
    @ActivityRetainedScoped
    fun bindRefreshBatchCredentials(
        useCase: RefreshBatchCredentialsImpl
    ): RefreshBatchCredentials

    @Binds
    @ActivityRetainedScoped
    fun bindDeleteBundleItemsByAmount(
        useCase: DeleteBundleItemsByAmountImpl
    ): DeleteBundleItemsByAmount
}
