package ch.admin.foitt.wallet.feature.login.di

import ch.admin.foitt.wallet.feature.login.domain.usecase.IsDeviceSecureLockScreenConfigured
import ch.admin.foitt.wallet.feature.login.domain.usecase.LockTrigger
import ch.admin.foitt.wallet.feature.login.domain.usecase.implementation.IsDeviceSecureLockScreenConfiguredImpl
import ch.admin.foitt.wallet.feature.login.domain.usecase.implementation.LockTriggerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
class LoginModule

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface LoginBindingsModule {
    @Binds
    fun bindLockTriggerUseCase(
        useCase: LockTriggerImpl
    ): LockTrigger

    @Binds
    fun bindIsDeviceSecureLockScreenConfigured(
        useCase: IsDeviceSecureLockScreenConfiguredImpl
    ): IsDeviceSecureLockScreenConfigured
}
