package ch.admin.foitt.wallet.feature.otp.di

import ch.admin.foitt.wallet.feature.otp.data.repository.OtpEmailRepositoryImpl
import ch.admin.foitt.wallet.feature.otp.data.repository.OtpRepositoryImpl
import ch.admin.foitt.wallet.feature.otp.data.repository.OtpStateCompletionRepositoryImpl
import ch.admin.foitt.wallet.feature.otp.data.repository.OtpToastRepositoryImpl
import ch.admin.foitt.wallet.feature.otp.domain.repository.OtpEmailRepository
import ch.admin.foitt.wallet.feature.otp.domain.repository.OtpRepository
import ch.admin.foitt.wallet.feature.otp.domain.repository.OtpStateCompletionRepository
import ch.admin.foitt.wallet.feature.otp.domain.repository.OtpToastRepository
import ch.admin.foitt.wallet.feature.otp.domain.usecase.RequestOtp
import ch.admin.foitt.wallet.feature.otp.domain.usecase.ValidateCodeLength
import ch.admin.foitt.wallet.feature.otp.domain.usecase.ValidateEmail
import ch.admin.foitt.wallet.feature.otp.domain.usecase.VerifyOtp
import ch.admin.foitt.wallet.feature.otp.domain.usecase.implementation.RequestOtpImpl
import ch.admin.foitt.wallet.feature.otp.domain.usecase.implementation.ValidateCodeLengthImpl
import ch.admin.foitt.wallet.feature.otp.domain.usecase.implementation.ValidateEmailImpl
import ch.admin.foitt.wallet.feature.otp.domain.usecase.implementation.VerifyOtpImpl
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponent
import ch.admin.foitt.wallet.platform.navigation.DestinationsScoped
import dagger.Binds
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface OtpModule {

    @Binds
    fun bindOtpRepository(
        repo: OtpRepositoryImpl
    ): OtpRepository

    @Binds
    fun bindValidateEmail(
        useCase: ValidateEmailImpl
    ): ValidateEmail

    @Binds
    fun bindRequestOtp(
        useCase: RequestOtpImpl
    ): RequestOtp

    @Binds
    fun bindVerifyOtp(
        useCase: VerifyOtpImpl
    ): VerifyOtp

    @Binds
    fun bindValidateCodeLength(
        useCase: ValidateCodeLengthImpl
    ): ValidateCodeLength

    @Binds
    fun bindOtpStateCompletionRepository(
        repo: OtpStateCompletionRepositoryImpl
    ): OtpStateCompletionRepository
}

@Module
@InstallIn(DestinationScopedComponent::class)
internal interface OtpRepositoryModule {
    @Binds
    @DestinationsScoped
    fun bindOtpEmailRepository(
        repo: OtpEmailRepositoryImpl
    ): OtpEmailRepository

    @Binds
    @DestinationsScoped
    fun bindOtpToastRepository(
        repo: OtpToastRepositoryImpl
    ): OtpToastRepository
}

@EntryPoint
@InstallIn(DestinationScopedComponent::class)
interface OtpEntryPoint {
    fun otpEmailRepository(): OtpEmailRepository
    fun otpToastRepository(): OtpToastRepository
}
