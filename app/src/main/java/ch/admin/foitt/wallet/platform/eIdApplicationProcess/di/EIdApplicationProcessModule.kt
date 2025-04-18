package ch.admin.foitt.wallet.platform.eIdApplicationProcess.di

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository.EIdRequestCaseRepositoryImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository.EIdRequestCaseWithStateRepositoryImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository.EIdRequestStateRepositoryImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository.SIdRepositoryImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseWithStateRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestStateRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.SIdRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateAllSIdStatuses
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.FetchSIdStatusImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.UpdateAllSIdStatusesImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
interface EIdApplicationProcessModule {

    @Binds
    @ActivityRetainedScoped
    fun bindEIdRequestCaseRepository(
        repo: EIdRequestCaseRepositoryImpl
    ): EIdRequestCaseRepository

    @Binds
    @ActivityRetainedScoped
    fun bindEIdRequestStateRepository(
        repo: EIdRequestStateRepositoryImpl
    ): EIdRequestStateRepository

    @Binds
    @ActivityRetainedScoped
    fun bindSIdRepository(
        repo: SIdRepositoryImpl
    ): SIdRepository

    @Binds
    @ActivityRetainedScoped
    fun bindEIdRequestCaseWithStateRepository(
        repo: EIdRequestCaseWithStateRepositoryImpl
    ): EIdRequestCaseWithStateRepository

    @Binds
    fun bindFetchSIdStatus(
        useCase: FetchSIdStatusImpl
    ): FetchSIdStatus

    @Binds
    fun bindUpdateAllSIdStatuses(
        useCase: UpdateAllSIdStatusesImpl
    ): UpdateAllSIdStatuses
}
