package ch.admin.foitt.wallet.feature.changePassphrase.di

import ch.admin.foitt.wallet.feature.changePassphrase.domain.usecase.ChangePassphrase
import ch.admin.foitt.wallet.feature.changePassphrase.domain.usecase.implementation.ChangePassphraseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
interface ChangePassphraseModule {
    @Binds
    fun bindChangePassphrase(useCase: ChangePassphraseImpl): ChangePassphrase
}
