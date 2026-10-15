package ch.admin.foitt.wallet.platform.nonCompliance.di

import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponent
import ch.admin.foitt.wallet.platform.navigation.DestinationsScoped
import ch.admin.foitt.wallet.platform.nonCompliance.data.repository.NonComplianceFormRepositoryImpl
import ch.admin.foitt.wallet.platform.nonCompliance.data.repository.NonComplianceRepositoryImpl
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceTextInputConstraints
import ch.admin.foitt.wallet.platform.nonCompliance.domain.repository.NonComplianceFormRepository
import ch.admin.foitt.wallet.platform.nonCompliance.domain.repository.NonComplianceRepository
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.GetNonComplianceReportingData
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendIssuerNonComplianceReport
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendNonComplianceReport
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendVerifierNonComplianceReport
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SubmitNonComplianceReport
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.ValidateEmail
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.ValidateTextLength
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation.GetNonComplianceReportingDataImpl
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation.SendIssuerNonComplianceReportImpl
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation.SendNonComplianceReportImpl
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation.SendVerifierNonComplianceReportImpl
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation.SubmitNonComplianceReportImpl
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
    fun bindValidateTextLength(
        useCase: ValidateTextLengthImpl
    ): ValidateTextLength

    @Binds
    fun bindValidateEmail(
        useCase: ValidateEmailImpl
    ): ValidateEmail

    @Binds
    fun bindSendVerifierNonComplianceReport(
        useCase: SendVerifierNonComplianceReportImpl
    ): SendVerifierNonComplianceReport

    @Binds
    fun bindSendIssuerNonComplianceReport(
        useCase: SendIssuerNonComplianceReportImpl
    ): SendIssuerNonComplianceReport

    @Binds
    fun bindSubmitNonComplianceReport(
        useCase: SubmitNonComplianceReportImpl
    ): SubmitNonComplianceReport

    @Binds
    fun bindSendNonComplianceReport(
        useCase: SendNonComplianceReportImpl
    ): SendNonComplianceReport

    @Binds
    fun bindGetNonComplianceReportingData(
        useCase: GetNonComplianceReportingDataImpl
    ): GetNonComplianceReportingData
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
