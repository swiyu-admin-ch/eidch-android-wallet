package ch.admin.foitt.wallet.platform.credentialPresentation.di

import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetCompatibleCredentials
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ProcessPresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation.GetCompatibleCredentialsImpl
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation.ProcessPresentationRequestImpl
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation.ValidatePresentationRequestImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
interface CredentialPresentationModule {

    @Binds
    fun bindProcessPresentationRequest(
        useCase: ProcessPresentationRequestImpl
    ): ProcessPresentationRequest

    @Binds
    fun bindValidatePresentationRequest(
        useCase: ValidatePresentationRequestImpl
    ): ValidatePresentationRequest

    @Binds
    fun bindGetCompatibleCredentials(
        useCase: GetCompatibleCredentialsImpl
    ): GetCompatibleCredentials
}
