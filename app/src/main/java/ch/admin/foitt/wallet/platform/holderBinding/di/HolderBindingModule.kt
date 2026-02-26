package ch.admin.foitt.wallet.platform.holderBinding.di

import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateProofKeyPairs
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.implementation.GenerateProofKeyPairsImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
class HolderBindingModule

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface HolderBindingBindingsModule {
    @Binds
    fun bindGenerateKeyPair(
        useCase: GenerateProofKeyPairsImpl
    ): GenerateProofKeyPairs
}
