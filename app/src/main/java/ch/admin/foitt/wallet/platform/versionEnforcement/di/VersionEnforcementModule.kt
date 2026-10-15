package ch.admin.foitt.wallet.platform.versionEnforcement.di

import ch.admin.foitt.wallet.platform.di.IoDispatcherScope
import ch.admin.foitt.wallet.platform.utils.CacheManager
import ch.admin.foitt.wallet.platform.versionEnforcement.data.repository.VersionEnforcementRepositoryImpl
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcementRaw
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.repository.VersionEnforcementRepository
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.FetchAppVersionInfo
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetAppVersion
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetDeviceModel
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetOSVersion
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetSwissProfileStatus
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetVersionEnforcement
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.VerifyDetachedJws
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation.FetchAppVersionInfoImpl
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation.GetAppVersionImpl
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation.GetDeviceModelImpl
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation.GetOSVersionImpl
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation.GetSwissProfileStatusImpl
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation.GetVersionEnforcementImpl
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation.VerifyDetachedJwsImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object VersionEnforcementModule {
    @Provides
    @ActivityRetainedScoped
    fun provideVersionEnforcementCache(
        @IoDispatcherScope scope: CoroutineScope,
    ): CacheManager<VersionEnforcementRaw> = CacheManager(scope)
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface VersionEnforcementBindings {
    @Binds
    fun bindFetchAppVersionInfo(
        useCase: FetchAppVersionInfoImpl
    ): FetchAppVersionInfo

    @Binds
    fun bindGetAppVersion(
        useCase: GetAppVersionImpl
    ): GetAppVersion

    @Binds
    fun bindGetDeviceModel(
        useCase: GetDeviceModelImpl
    ): GetDeviceModel

    @Binds
    fun bindGetOSVersion(
        useCase: GetOSVersionImpl
    ): GetOSVersion

    @Binds
    fun bindGetSwissProfileStatus(
        useCase: GetSwissProfileStatusImpl
    ): GetSwissProfileStatus

    @Binds
    fun bindVerifyDetachedJws(
        useCase: VerifyDetachedJwsImpl
    ): VerifyDetachedJws

    @Binds
    fun bindGetVersionEnforcement(
        useCase: GetVersionEnforcementImpl
    ): GetVersionEnforcement

    @Binds
    @ActivityRetainedScoped
    fun bindVersionEnforcementRepository(
        repository: VersionEnforcementRepositoryImpl
    ): VersionEnforcementRepository
}
