package ch.admin.foitt.wallet.feature.home.di

import ch.admin.foitt.wallet.feature.home.domain.usecase.DeleteEIdRequestCase
import ch.admin.foitt.wallet.feature.home.domain.usecase.EIdRequestsPriorityOrdering
import ch.admin.foitt.wallet.feature.home.domain.usecase.GetEIdRequestsFlow
import ch.admin.foitt.wallet.feature.home.domain.usecase.implementation.DeleteEIdRequestCaseImpl
import ch.admin.foitt.wallet.feature.home.domain.usecase.implementation.EIdRequestsPriorityOrderingImpl
import ch.admin.foitt.wallet.feature.home.domain.usecase.implementation.GetEIdRequestsFlowImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
interface HomeModule {
    @Binds
    fun bindGetEIdRequestsFlow(
        useCase: GetEIdRequestsFlowImpl
    ): GetEIdRequestsFlow

    @Binds
    fun bindDeleteEIdRequestCase(
        useCase: DeleteEIdRequestCaseImpl
    ): DeleteEIdRequestCase

    @Binds
    fun bindEIdRequestsPriorityOrdering(
        useCase: EIdRequestsPriorityOrderingImpl
    ): EIdRequestsPriorityOrdering
}
