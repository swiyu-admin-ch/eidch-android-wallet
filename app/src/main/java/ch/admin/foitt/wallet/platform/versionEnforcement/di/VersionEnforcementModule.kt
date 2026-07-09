package ch.admin.foitt.wallet.platform.versionEnforcement.di

import ch.admin.foitt.wallet.platform.versionEnforcement.data.repository.VersionEnforcementRepositoryImpl
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.repository.VersionEnforcementRepository
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.FetchAppVersionInfo
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetAppVersion
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetDeviceModel
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetOSVersion
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation.FetchAppVersionInfoImpl
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation.GetAppVersionImpl
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation.GetDeviceModelImpl
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation.GetOSVersionImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal class VersionEnforcementModule

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
    @ActivityRetainedScoped
    fun bindVersionEnforcementRepository(
        repository: VersionEnforcementRepositoryImpl
    ): VersionEnforcementRepository
}
