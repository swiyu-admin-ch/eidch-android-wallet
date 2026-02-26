package ch.admin.foitt.wallet.platform.nonCompliance.di

import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponent
import ch.admin.foitt.wallet.platform.navigation.DestinationsScoped
import ch.admin.foitt.wallet.platform.nonCompliance.data.repository.NonComplianceFormRepositoryImpl
import ch.admin.foitt.wallet.platform.nonCompliance.data.repository.NonComplianceRepositoryImpl
import ch.admin.foitt.wallet.platform.nonCompliance.data.repository.NonComplianceTrustRepositoryImpl
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceTextInputConstraints
import ch.admin.foitt.wallet.platform.nonCompliance.domain.repository.NonComplianceFormRepository
import ch.admin.foitt.wallet.platform.nonCompliance.domain.repository.NonComplianceRepository
import ch.admin.foitt.wallet.platform.nonCompliance.domain.repository.NonComplianceTrustRepository
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.FetchNonComplianceData
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendNonComplianceReport
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.ValidateEmail
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.ValidateTextLength
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation.FetchNonComplianceDataImpl
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation.SendNonComplianceReportImpl
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation.ValidateEmailImpl
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation.ValidateTextLengthImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class NonComplianceModule {

    @Provides
    fun provideNonComplianceTextInputConstrains(): NonComplianceTextInputConstraints = NonComplianceTextInputConstraints()
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface NonComplianceBindingsModule {
    @Binds
    @ActivityRetainedScoped
    fun bindNonComplianceRepository(
        repo: NonComplianceRepositoryImpl
    ): NonComplianceRepository

    @Binds
    @ActivityRetainedScoped
    fun bindNonComplianceTrustRepository(
        repo: NonComplianceTrustRepositoryImpl
    ): NonComplianceTrustRepository

    @Binds
    fun bindFetchNonComplianceData(
        useCase: FetchNonComplianceDataImpl
    ): FetchNonComplianceData

    @Binds
    fun bindValidateTextLength(
        useCase: ValidateTextLengthImpl
    ): ValidateTextLength

    @Binds
    fun bindValidateEmail(
        useCase: ValidateEmailImpl
    ): ValidateEmail

    @Binds
    fun bindSendNonComplianceReport(
        useCase: SendNonComplianceReportImpl
    ): SendNonComplianceReport
}

@Module
@InstallIn(DestinationScopedComponent::class)
internal interface NonComplianceRepositoryModule {
    @Binds
    @DestinationsScoped
    fun bindNonComplianceFormRepository(
        repo: NonComplianceFormRepositoryImpl
    ): NonComplianceFormRepository
}

@EntryPoint
@InstallIn(DestinationScopedComponent::class)
interface NonComplianceEntryPoint {
    fun nonComplianceFormRepository(): NonComplianceFormRepository
}
