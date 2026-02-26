package ch.admin.foitt.wallet.platform.trustRegistry.di

import ch.admin.foitt.wallet.platform.trustRegistry.data.TrustStatementRepositoryImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.repository.TrustStatementRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchVcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustUrlFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.FetchVcSchemaTrustStatusImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.GetTrustDomainFromDidImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.GetTrustUrlFromDidImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ProcessIdentityV1TrustStatementImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateTrustStatementImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface TrustRegistryModule {
    @Binds
    fun bindProcessIdentityV1TrustStatement(
        useCase: ProcessIdentityV1TrustStatementImpl
    ): ProcessIdentityV1TrustStatement

    @Binds
    fun bindFetchVcSchemaTrustStatus(
        useCase: FetchVcSchemaTrustStatusImpl
    ): FetchVcSchemaTrustStatus

    @Binds
    fun bindGetTrustDomainFromDid(
        useCase: GetTrustDomainFromDidImpl
    ): GetTrustDomainFromDid

    @Binds
    fun bindGetTrustUrlFromDid(
        useCase: GetTrustUrlFromDidImpl
    ): GetTrustUrlFromDid

    @Binds
    @ActivityRetainedScoped
    fun bindTrustStatementRepository(
        repo: TrustStatementRepositoryImpl
    ): TrustStatementRepository

    @Binds
    fun bindValidateTrustStatement(
        useCase: ValidateTrustStatementImpl
    ): ValidateTrustStatement
}
