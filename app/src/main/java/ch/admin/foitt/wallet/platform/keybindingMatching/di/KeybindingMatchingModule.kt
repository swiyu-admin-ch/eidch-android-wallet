package ch.admin.foitt.wallet.platform.keybindingMatching.di

import ch.admin.foitt.wallet.platform.keybindingMatching.domain.usecase.MatchKeyBindingToPayloadCnf
import ch.admin.foitt.wallet.platform.keybindingMatching.domain.usecase.implementation.MatchKeyBindingToPayloadCnfImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class KeybindingMatchingModule {
    @Binds
    abstract fun bindMatchKeyBindingToPayloadCnf(
        impl: MatchKeyBindingToPayloadCnfImpl,
    ): MatchKeyBindingToPayloadCnf
}
