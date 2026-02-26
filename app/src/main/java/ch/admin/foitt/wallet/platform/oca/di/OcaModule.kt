package ch.admin.foitt.wallet.platform.oca.di

import ch.admin.foitt.wallet.platform.oca.data.OcaRepositoryImpl
import ch.admin.foitt.wallet.platform.oca.domain.repository.OcaRepository
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchDeferredVcMetadataByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchVcMetadataByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaClaimData
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaClaimDisplays
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaCredentialData
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaDisplays
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GetRootCaptureBase
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaBundler
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaCaptureBaseValidator
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaCesrHashValidator
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaOverlayValidator
import ch.admin.foitt.wallet.platform.oca.domain.usecase.TransformOcaOverlays
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.FetchDeferredVcMetadataByFormatImpl
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.FetchOcaBundleImpl
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.FetchVcMetadataByFormatImpl
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.GenerateOcaClaimDataImpl
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.GenerateOcaClaimDisplaysImpl
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.GenerateOcaCredentialDataImpl
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.GenerateOcaDisplaysImpl
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.GetRootCaptureBaseImpl
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.OcaBundlerImpl
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.OcaCaptureBaseValidatorImpl
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.OcaCesrHashValidatorImpl
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.OcaOverlayValidatorImpl
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.TransformOcaOverlaysImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface OcaModule {
    @Binds
    @ActivityRetainedScoped
    fun bindOcaRepository(
        repo: OcaRepositoryImpl
    ): OcaRepository

    @Binds
    fun bindOCADigestValidator(
        useCase: OcaCesrHashValidatorImpl
    ): OcaCesrHashValidator

    @Binds
    fun bindFetchVcMetadataByFormat(
        useCase: FetchVcMetadataByFormatImpl
    ): FetchVcMetadataByFormat

    @Binds
    fun bindFetchDeferredVcMetadataByFormat(
        useCase: FetchDeferredVcMetadataByFormatImpl
    ): FetchDeferredVcMetadataByFormat

    @Binds
    fun bindFetchOcaBundle(
        useCase: FetchOcaBundleImpl
    ): FetchOcaBundle

    @Binds
    fun bindOcaBundler(
        useCase: OcaBundlerImpl
    ): OcaBundler

    @Binds
    fun bindOcaCaptureBaseValidator(
        useCase: OcaCaptureBaseValidatorImpl
    ): OcaCaptureBaseValidator

    @Binds
    fun bindOcaOverlayValidator(
        useCase: OcaOverlayValidatorImpl
    ): OcaOverlayValidator

    @Binds
    fun bindGenerateOcaDisplays(
        useCase: GenerateOcaDisplaysImpl
    ): GenerateOcaDisplays

    @Binds
    fun bindGenerateOcaClaimDisplays(
        useCase: GenerateOcaClaimDisplaysImpl
    ): GenerateOcaClaimDisplays

    @Binds
    fun bindTransformOcaOverlays(
        useCase: TransformOcaOverlaysImpl
    ): TransformOcaOverlays

    @Binds
    fun bindGenerateOcaClaimData(
        useCase: GenerateOcaClaimDataImpl
    ): GenerateOcaClaimData

    @Binds
    fun bindGenerateOcaCredentialData(
        useCase: GenerateOcaCredentialDataImpl
    ): GenerateOcaCredentialData

    @Binds
    fun bindGetRootCaptureBase(
        useCase: GetRootCaptureBaseImpl
    ): GetRootCaptureBase
}
