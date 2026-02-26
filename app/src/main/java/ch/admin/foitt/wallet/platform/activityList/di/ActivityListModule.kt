package ch.admin.foitt.wallet.platform.activityList.di

import ch.admin.foitt.wallet.platform.activityList.data.repository.ActivityActorDisplayRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.data.repository.ActivityActorDisplayWithImageRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.data.repository.ActivityClaimRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.data.repository.ActivityRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.data.repository.ActivityWithActorDisplaysRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.data.repository.ActivityWithDetailsRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.data.repository.CredentialActivityRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.data.repository.ImageRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityActorDisplayRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityActorDisplayWithImageRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityClaimRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityWithActorDisplaysRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityWithDetailsRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.CredentialActivityRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ImageRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.DeleteActivity
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivitiesWithDisplaysFlow
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivityDetailFlow
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivityWithDetailsFlow
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SaveIssuanceActivity
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SavePresentationAcceptedActivity
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SavePresentationDeclinedActivity
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.DeleteActivityImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.GetActivitiesWithDisplaysFlowImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.GetActivityDetailFlowImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.GetActivityWithDetailsFlowImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.MapToActivityActorDisplayDataImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.MapToActivityDisplayDataImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.SaveIssuanceActivityImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.SavePresentationAcceptedActivityImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.SavePresentationDeclinedActivityImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface ActivityListModule {
    @Binds
    @ActivityRetainedScoped
    fun bindCredentialActivityRepository(
        repo: CredentialActivityRepositoryImpl
    ): CredentialActivityRepository

    @Binds
    @ActivityRetainedScoped
    fun bindActivityClaimRepository(
        repo: ActivityClaimRepositoryImpl
    ): ActivityClaimRepository

    @Binds
    @ActivityRetainedScoped
    fun bindActivityActorDisplayRepository(
        repo: ActivityActorDisplayRepositoryImpl
    ): ActivityActorDisplayRepository

    @Binds
    @ActivityRetainedScoped
    fun bindActivityActorDisplayWithImageRepository(
        repo: ActivityActorDisplayWithImageRepositoryImpl
    ): ActivityActorDisplayWithImageRepository

    @Binds
    @ActivityRetainedScoped
    fun bindActivityWithDetailsRepository(
        repo: ActivityWithDetailsRepositoryImpl
    ): ActivityWithDetailsRepository

    @Binds
    @ActivityRetainedScoped
    fun bindActivityWithDisplaysRepository(
        repo: ActivityWithActorDisplaysRepositoryImpl
    ): ActivityWithActorDisplaysRepository

    @Binds
    @ActivityRetainedScoped
    fun bindImageRepository(
        repo: ImageRepositoryImpl
    ): ImageRepository

    @Binds
    @ActivityRetainedScoped
    fun bindActivityRepository(
        repo: ActivityRepositoryImpl
    ): ActivityRepository

    @Binds
    fun bindSaveIssuanceActivity(
        useCase: SaveIssuanceActivityImpl
    ): SaveIssuanceActivity

    @Binds
    fun bindSavePresentationAcceptedActivity(
        useCase: SavePresentationAcceptedActivityImpl
    ): SavePresentationAcceptedActivity

    @Binds
    fun bindSavePresentationDeclinedActivity(
        useCase: SavePresentationDeclinedActivityImpl
    ): SavePresentationDeclinedActivity

    @Binds
    fun bindMapToActivityDisplayData(
        useCase: MapToActivityDisplayDataImpl
    ): MapToActivityDisplayData

    @Binds
    fun bindMapToActivityActorDisplayData(
        useCase: MapToActivityActorDisplayDataImpl
    ): MapToActivityActorDisplayData

    @Binds
    fun bindGetActivityWithDisplaysFlow(
        useCase: GetActivitiesWithDisplaysFlowImpl
    ): GetActivitiesWithDisplaysFlow

    @Binds
    fun bindGetActivityDetailFlow(
        useCase: GetActivityDetailFlowImpl
    ): GetActivityDetailFlow

    @Binds
    fun bindGetActivityWithDetailsFlow(
        useCase: GetActivityWithDetailsFlowImpl
    ): GetActivityWithDetailsFlow

    @Binds
    fun bindDeleteActivity(
        useCase: DeleteActivityImpl
    ): DeleteActivity
}
