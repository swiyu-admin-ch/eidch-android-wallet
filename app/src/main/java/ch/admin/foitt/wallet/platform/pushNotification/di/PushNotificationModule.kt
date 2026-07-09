package ch.admin.foitt.wallet.platform.pushNotification.di

import ch.admin.foitt.wallet.platform.pushNotification.data.PushDeviceTokenRepositoryImpl
import ch.admin.foitt.wallet.platform.pushNotification.data.PushNotificationRepositoryImpl
import ch.admin.foitt.wallet.platform.pushNotification.domain.repository.PushDeviceTokenRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.repository.PushNotificationRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.DeletePushId
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.GeneratePushClientAttestation
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.UpdatePushToken
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.implementation.DeletePushIdImpl
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.implementation.GeneratePushClientAttestationImpl
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.implementation.UpdatePushTokenImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface PushNotificationModule {

    @Binds
    fun bindPushDeviceTokenRepository(
        repo: PushDeviceTokenRepositoryImpl
    ): PushDeviceTokenRepository

    @Binds
    fun bindPushNotificationRepository(
        repo: PushNotificationRepositoryImpl
    ): PushNotificationRepository

    @Binds
    fun bindGeneratePushClientAttestation(
        useCase: GeneratePushClientAttestationImpl
    ): GeneratePushClientAttestation

    @Binds
    fun bindUpdatePushToken(
        useCase: UpdatePushTokenImpl
    ): UpdatePushToken

    @Binds
    fun bindDeletePushId(
        useCase: DeletePushIdImpl
    ): DeletePushId
}
