package ch.admin.foitt.wallet.platform.ssi.di

import ch.admin.foitt.wallet.platform.ssi.data.repository.BundleItemRepositoryImpl
import ch.admin.foitt.wallet.platform.ssi.data.repository.BundleItemWithKeyBindingRepositoryImpl
import ch.admin.foitt.wallet.platform.ssi.data.repository.CredentialClaimDisplayRepoImpl
import ch.admin.foitt.wallet.platform.ssi.data.repository.CredentialIssuerDisplayRepoImpl
import ch.admin.foitt.wallet.platform.ssi.data.repository.CredentialOfferRepositoryImpl
import ch.admin.foitt.wallet.platform.ssi.data.repository.CredentialRepoImpl
import ch.admin.foitt.wallet.platform.ssi.data.repository.DeferredCredentialRepositoryImpl
import ch.admin.foitt.wallet.platform.ssi.data.repository.DeferredCredentialWithDisplaysRepositoryImpl
import ch.admin.foitt.wallet.platform.ssi.data.repository.RawCredentialDataRepositoryImpl
import ch.admin.foitt.wallet.platform.ssi.data.repository.VerifiableCredentialRepositoryImpl
import ch.admin.foitt.wallet.platform.ssi.data.repository.VerifiableCredentialWithBundleItemsWithKeyBindingRepositoryImpl
import ch.admin.foitt.wallet.platform.ssi.data.repository.VerifiableCredentialWithDisplaysAndClustersRepositoryImpl
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemWithKeyBindingRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialClaimDisplayRepo
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialIssuerDisplayRepo
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialWithDisplaysRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.RawCredentialDataRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithBundleItemsWithKeyBindingRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithDisplaysAndClustersRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteBundleItems
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteCredential
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteKeyStoreEntry
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialDetailFlow
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialsWithDetailsFlow
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetDeferredCredentialsWithDetailsFlow
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.DeleteBundleItemsImpl
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.DeleteCredentialImpl
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.DeleteKeyStoreEntryImpl
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.GetCredentialDetailFlowImpl
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.GetCredentialsWithDetailsFlowImpl
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.GetDeferredCredentialsWithDetailsFlowImpl
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.MapToCredentialClaimDataImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
interface SsiModule {

    @Binds
    fun bindCredentialClaimDisplayRepo(
        useCase: CredentialClaimDisplayRepoImpl
    ): CredentialClaimDisplayRepo

    @Binds
    fun bindCredentialRepo(
        useCase: CredentialRepoImpl
    ): CredentialRepo

    @Binds
    @ActivityRetainedScoped
    fun bindVerifiableCredentialRepository(
        repo: VerifiableCredentialRepositoryImpl
    ): VerifiableCredentialRepository

    @Binds
    fun bindDeleteCredential(
        useCase: DeleteCredentialImpl
    ): DeleteCredential

    @Binds
    fun bindDeleteBundleItems(
        useCase: DeleteBundleItemsImpl
    ): DeleteBundleItems

    @Binds
    fun bindDeleteKeyStoreEntry(
        useCase: DeleteKeyStoreEntryImpl
    ): DeleteKeyStoreEntry

    @Binds
    fun bindMapToCredentialClaimData(
        useCase: MapToCredentialClaimDataImpl
    ): MapToCredentialClaimData

    @Binds
    @ActivityRetainedScoped
    fun bindCredentialOfferRepository(
        repo: CredentialOfferRepositoryImpl
    ): CredentialOfferRepository

    @Binds
    @ActivityRetainedScoped
    fun bindCredentialIssuerDisplayRepository(
        repo: CredentialIssuerDisplayRepoImpl
    ): CredentialIssuerDisplayRepo

    @Binds
    @ActivityRetainedScoped
    fun bindVerifiableCredentialWithDisplaysAndClustersRepository(
        repo: VerifiableCredentialWithDisplaysAndClustersRepositoryImpl
    ): VerifiableCredentialWithDisplaysAndClustersRepository

    @Binds
    @ActivityRetainedScoped
    fun bindCredentialWithKeyBindingRepository(
        repo: VerifiableCredentialWithBundleItemsWithKeyBindingRepositoryImpl
    ): VerifiableCredentialWithBundleItemsWithKeyBindingRepository

    @Binds
    @ActivityRetainedScoped
    fun bindBundleItemRepository(
        repo: BundleItemRepositoryImpl
    ): BundleItemRepository

    @Binds
    @ActivityRetainedScoped
    fun bindBundleItemWithKeyBindingRepository(
        repo: BundleItemWithKeyBindingRepositoryImpl
    ): BundleItemWithKeyBindingRepository

    @Binds
    @ActivityRetainedScoped
    fun bindDeferredCredentialRepository(
        repo: DeferredCredentialRepositoryImpl
    ): DeferredCredentialRepository

    @Binds
    @ActivityRetainedScoped
    fun bindDeferredCredentialWithDisplaysRepository(
        repo: DeferredCredentialWithDisplaysRepositoryImpl
    ): DeferredCredentialWithDisplaysRepository

    @Binds
    @ActivityRetainedScoped
    fun bindRawCredentialDataRepository(
        repo: RawCredentialDataRepositoryImpl
    ): RawCredentialDataRepository

    @Binds
    fun bindGetCredentialDetailFlow(
        useCase: GetCredentialDetailFlowImpl
    ): GetCredentialDetailFlow

    @Binds
    fun bindGetCredentialWithDetailsFlow(
        useCase: GetCredentialsWithDetailsFlowImpl
    ): GetCredentialsWithDetailsFlow

    @Binds
    fun bindGetGetDeferredCredentialsWithDetailsFlow(
        useCase: GetDeferredCredentialsWithDetailsFlowImpl
    ): GetDeferredCredentialsWithDetailsFlow
}
