package ch.admin.foitt.wallet.platform.credential.di

import ch.admin.foitt.wallet.platform.credential.data.CredentialRefreshRepositoryImpl
import ch.admin.foitt.wallet.platform.credential.domain.repository.CredentialRefreshRepository
import ch.admin.foitt.wallet.platform.credential.domain.usecase.EvaluateBatchSize
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchAndSaveCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchAndUpdateDeferredCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchTrustForIssuance
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataClaimDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAllAnyCredentials
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAllAnyCredentialsByCredentialId
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetCredentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleDeferredCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshDeferredCredentials
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshVerifiableCredentials
import ch.admin.foitt.wallet.platform.credential.domain.usecase.ResolveClaimTemplate
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveCredentialFromDeferred
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveVcSdJwtCredentials
import ch.admin.foitt.wallet.platform.credential.domain.usecase.UpdateDeferredCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.ValidateIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.EvaluateBatchSizeImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.FetchAndSaveCredentialImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.FetchAndUpdateDeferredCredentialImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.FetchTrustForIssuanceImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.GenerateAnyDisplaysImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.GenerateMetadataClaimDisplaysImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.GenerateMetadataDisplaysImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.GetAllAnyCredentialsByCredentialIdImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.GetAllAnyCredentialsImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.GetCredentialConfigImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.HandleBatchCredentialResultImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.HandleCredentialResultImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.HandleDeferredCredentialResultImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.MapToCredentialDisplayDataImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.RefreshCredentialImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.RefreshDeferredCredentialsImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.RefreshVerifiableCredentialsImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.ResolveClaimTemplateImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.SaveCredentialFromDeferredImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.SaveVcSdJwtCredentialsImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.UpdateDeferredCredentialImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.ValidateIssuerCredentialInfoImpl
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.implementation.GetCredentialCardStateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface CredentialModule {

    @Binds
    fun bindFetchAndSaveCredential(
        useCase: FetchAndSaveCredentialImpl
    ): FetchAndSaveCredential

    @Binds
    fun bindFetchAndUpdateDeferredCredential(
        useCase: FetchAndUpdateDeferredCredentialImpl
    ): FetchAndUpdateDeferredCredential

    @Binds
    fun bindEvaluateBatchSize(
        useCase: EvaluateBatchSizeImpl
    ): EvaluateBatchSize

    @Binds
    fun bindGetAnyCredential(
        useCase: GetAllAnyCredentialsByCredentialIdImpl
    ): GetAllAnyCredentialsByCredentialId

    @Binds
    fun bindGetAnyCredentials(
        useCase: GetAllAnyCredentialsImpl
    ): GetAllAnyCredentials

    @Binds
    fun bindGetCredentialConfig(
        useCase: GetCredentialConfigImpl
    ): GetCredentialConfig

    @Binds
    fun bindRefreshDeferredCredentials(
        useCase: RefreshDeferredCredentialsImpl
    ): RefreshDeferredCredentials

    @Binds
    @ActivityRetainedScoped
    fun bindRefreshCredentials(
        useCase: RefreshCredentialImpl
    ): RefreshCredential

    @Binds
    fun bindRefreshVerifiableCredentials(
        useCase: RefreshVerifiableCredentialsImpl
    ): RefreshVerifiableCredentials

    @Binds
    fun bindGetCredentialState(
        adapter: GetCredentialCardStateImpl
    ): GetCredentialCardState

    @Binds
    fun bindMapToCredentialDisplayData(
        useCase: MapToCredentialDisplayDataImpl
    ): MapToCredentialDisplayData

    @Binds
    fun bindGenerateAnyCredentialDisplays(
        useCase: GenerateAnyDisplaysImpl
    ): GenerateAnyDisplays

    @Binds
    fun bindGenerateMetadataDisplays(
        useCase: GenerateMetadataDisplaysImpl
    ): GenerateMetadataDisplays

    @Binds
    fun bindGenerateMetadataClaimDisplays(
        useCase: GenerateMetadataClaimDisplaysImpl
    ): GenerateMetadataClaimDisplays

    @Binds
    fun bindHandleVcSdJwtCredentials(
        useCase: SaveVcSdJwtCredentialsImpl
    ): SaveVcSdJwtCredentials

    @Binds
    fun bindHandleCredentialResult(
        useCase: HandleCredentialResultImpl
    ): HandleCredentialResult

    @Binds
    fun bindHandleBatchCredentialResult(
        useCase: HandleBatchCredentialResultImpl
    ): HandleBatchCredentialResult

    @Binds
    fun bindHandleDeferredCredential(
        useCase: HandleDeferredCredentialResultImpl
    ): HandleDeferredCredentialResult

    @Binds
    fun bindFetchTrustForIssuance(
        useCase: FetchTrustForIssuanceImpl
    ): FetchTrustForIssuance

    @Binds
    fun bindValidateIssuerCredentialInfo(
        useCase: ValidateIssuerCredentialInfoImpl
    ): ValidateIssuerCredentialInfo

    @Binds
    fun bindSaveCredentialFromDeferred(
        useCase: SaveCredentialFromDeferredImpl
    ): SaveCredentialFromDeferred

    @Binds
    fun bindUpdateDeferredCredential(
        useCase: UpdateDeferredCredentialImpl
    ): UpdateDeferredCredential

    @Binds
    fun bindResolveClaimTemplate(
        useCase: ResolveClaimTemplateImpl
    ): ResolveClaimTemplate

    @Binds
    @ActivityRetainedScoped
    fun bindCredentialRefreshManager(
        manager: CredentialRefreshRepositoryImpl
    ): CredentialRefreshRepository
}
