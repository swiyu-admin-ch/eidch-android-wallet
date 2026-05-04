package ch.admin.foitt.wallet.platform.appAttestation.di

import ch.admin.foitt.wallet.platform.appAttestation.data.repository.AppAttestationRepositoryImpl
import ch.admin.foitt.wallet.platform.appAttestation.data.repository.CurrentClientAttestationRepositoryImpl
import ch.admin.foitt.wallet.platform.appAttestation.domain.repository.AppAttestationRepository
import ch.admin.foitt.wallet.platform.appAttestation.domain.repository.CurrentClientAttestationRepository
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.GenerateProofOfPossession
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestKeyAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.ValidateClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.ValidateKeyAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.implementation.GenerateProofOfPossessionImpl
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.implementation.RequestClientAttestationImpl
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.implementation.RequestKeyAttestationImpl
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.implementation.ValidateClientAttestationImpl
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.implementation.ValidateKeyAttestationImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface AppAttestationModule {
    @Binds
    fun bindRequestClientAttestation(
        useCase: RequestClientAttestationImpl
    ): RequestClientAttestation

    @Binds
    fun bindValidateClientAttestation(
        useCase: ValidateClientAttestationImpl
    ): ValidateClientAttestation

    @Binds
    fun bindRequestKeyAttestation(
        useCase: RequestKeyAttestationImpl
    ): RequestKeyAttestation

    @Binds
    fun bindValidateKeyAttestation(
        useCase: ValidateKeyAttestationImpl
    ): ValidateKeyAttestation

    @Binds
    fun bindAppAttestationRepository(
        repo: AppAttestationRepositoryImpl
    ): AppAttestationRepository

    @Binds
    fun bindCurrentClientAttestationRepository(
        repo: CurrentClientAttestationRepositoryImpl
    ): CurrentClientAttestationRepository

    @Binds
    fun bindGenerateProofOfPossession(
        useCase: GenerateProofOfPossessionImpl
    ): GenerateProofOfPossession
}
