package ch.admin.foitt.wallet.platform.keyPairGenerator.di

import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateJWSKeyPairInHardware
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateJWSKeyPairInSoftware
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.CreateKeyGenSpec
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.implementation.CreateJWSKeyPairInHardwareImpl
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.implementation.CreateJWSKeyPairInSoftwareImpl
import ch.admin.foitt.wallet.platform.keyPairGenerator.domain.usecase.implementation.CreateKeyGenSpecImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
class KeyPairGeneratorModule

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface KeyPairGeneratorBindingsModule {
    @Binds
    fun bindCreateJWSKeyPairInHardware(
        useCase: CreateJWSKeyPairInHardwareImpl
    ): CreateJWSKeyPairInHardware

    @Binds
    fun bindCreateKeyGenSpec(
        factory: CreateKeyGenSpecImpl
    ): CreateKeyGenSpec

    @Binds
    fun bindCreateJWSKeyPairInSoftware(
        useCase: CreateJWSKeyPairInSoftwareImpl
    ): CreateJWSKeyPairInSoftware
}
