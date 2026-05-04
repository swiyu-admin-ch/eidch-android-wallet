package ch.admin.foitt.wallet.platform.activityList.di

import ch.admin.foitt.wallet.platform.activityList.data.repository.ActivityActorDisplayRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.data.repository.ActivityActorDisplayWithImageRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.data.repository.ActivityClaimRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.data.repository.ActivityRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.data.repository.ActivityStateRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.data.repository.ActivityWithActorDisplaysRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.data.repository.ActivityWithDetailsRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.data.repository.CredentialActivityRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.data.repository.ImageRepositoryImpl
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityActorDisplayRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityActorDisplayWithImageRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityClaimRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityStateRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityWithActorDisplaysRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityWithDetailsRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.CredentialActivityRepository
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ImageRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.AreActivitiesEnabledFlow
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.DeleteActivity
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.DeleteAllActivities
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivitiesWithDisplaysFlow
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivityActorDisplaysFlow
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivityDetailFlow
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityDetailDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActivityWithActorDisplayData
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SaveAreActivitiesEnabled
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SaveIssuanceActivity
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SavePresentationAcceptedActivity
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.SavePresentationDeclinedActivity
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.AreActivitiesEnabledFlowImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.DeleteActivityImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.DeleteAllActivitiesImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.GetActivitiesWithDisplaysFlowImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.GetActivityActorDisplaysFlowImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.GetActivityDetailFlowImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.MapToActivityActorDisplayDataImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.MapToActivityDetailDisplayDataImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.MapToActivityWithActorDisplayDataImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.SaveAreActivitiesEnabledImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.SaveIssuanceActivityImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.SavePresentationAcceptedActivityImpl
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation.SavePresentationDeclinedActivityImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent

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
    fun bindMapToActivityDetailDisplayData(
        useCase: MapToActivityDetailDisplayDataImpl
    ): MapToActivityDetailDisplayData

    @Binds
    fun bindMapToActivityActorDisplayData(
        useCase: MapToActivityActorDisplayDataImpl
    ): MapToActivityActorDisplayData

    @Binds
    fun bindMapToActivityWithActorDisplayData(
        useCase: MapToActivityWithActorDisplayDataImpl
    ): MapToActivityWithActorDisplayData

    @Binds
    fun bindGetActivityWithDisplaysFlow(
        useCase: GetActivitiesWithDisplaysFlowImpl
    ): GetActivitiesWithDisplaysFlow

    @Binds
    fun bindGetActivityDetailFlow(
        useCase: GetActivityDetailFlowImpl
    ): GetActivityDetailFlow

    @Binds
    fun bindGetActivityActorDisplaysFlow(
        useCase: GetActivityActorDisplaysFlowImpl
    ): GetActivityActorDisplaysFlow

    @Binds
    fun bindDeleteActivity(
        useCase: DeleteActivityImpl
    ): DeleteActivity

    @Binds
    fun bindDeleteAllActivities(
        useCase: DeleteAllActivitiesImpl
    ): DeleteAllActivities

    @Binds
    fun bindSaveAreActivitiesEnabled(
        useCase: SaveAreActivitiesEnabledImpl
    ): SaveAreActivitiesEnabled

    @Binds
    fun bindAreActivitiesEnabled(
        useCase: AreActivitiesEnabledFlowImpl
    ): AreActivitiesEnabledFlow
}

@Module
@InstallIn(SingletonComponent::class)
internal interface ActivityListSingletonModule {
    @Binds
    fun bindActivityStateRepository(
        repo: ActivityStateRepositoryImpl
    ): ActivityStateRepository
}
