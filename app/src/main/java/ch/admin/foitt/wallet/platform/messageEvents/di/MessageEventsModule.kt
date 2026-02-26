package ch.admin.foitt.wallet.platform.messageEvents.di

import ch.admin.foitt.wallet.platform.messageEvents.data.repository.ActivityEventRepositoryImpl
import ch.admin.foitt.wallet.platform.messageEvents.data.repository.CredentialOfferEventRepositoryImpl
import ch.admin.foitt.wallet.platform.messageEvents.data.repository.NonComplianceEventRepositoryImpl
import ch.admin.foitt.wallet.platform.messageEvents.data.repository.PassphraseChangeEventRepositoryImpl
import ch.admin.foitt.wallet.platform.messageEvents.data.repository.WalletPairingEventRepositoryImpl
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.ActivityEventRepository
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialOfferEventRepository
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.NonComplianceEventRepository
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.PassphraseChangeEventRepository
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.WalletPairingEventRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
class MessageEventsModule

@Module
@InstallIn(ActivityRetainedComponent::class)
interface MessageEventsBindingModule {
    @Binds
    @ActivityRetainedScoped
    fun bindPassphraseChangeEventRepo(
        repo: PassphraseChangeEventRepositoryImpl
    ): PassphraseChangeEventRepository

    @Binds
    @ActivityRetainedScoped
    fun bindCredentialReceivedEventRepo(
        repo: CredentialOfferEventRepositoryImpl
    ): CredentialOfferEventRepository

    @Binds
    @ActivityRetainedScoped
    fun bindActivityEventRepo(
        repo: ActivityEventRepositoryImpl
    ): ActivityEventRepository

    @Binds
    @ActivityRetainedScoped
    fun bindNonComplianceEventRepo(
        repo: NonComplianceEventRepositoryImpl
    ): NonComplianceEventRepository

    @Binds
    @ActivityRetainedScoped
    fun bindWalletPairingEventRepo(
        repo: WalletPairingEventRepositoryImpl
    ): WalletPairingEventRepository
}
