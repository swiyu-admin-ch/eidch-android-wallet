package ch.admin.foitt.wallet.platform.invitation.di

import ch.admin.foitt.wallet.platform.invitation.domain.usecase.GetCredentialOfferFromUri
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.GetPresentationRequestFromUri
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.GetProximityPresentationRequestFromUri
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.HandleInvitationProcessingSuccess
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ProcessInvitation
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ValidateInvitation
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation.GetCredentialOfferFromUriImpl
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation.GetPresentationRequestFromUriImpl
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation.GetProximityPresentationRequestFromUriImpl
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation.HandleInvitationProcessingSuccessImpl
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation.ProcessInvitationImpl
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation.ValidateInvitationImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface InvitationModule {
    @Binds
    fun bindProcessInvitation(
        useCase: ProcessInvitationImpl
    ): ProcessInvitation

    @Binds
    fun bindValidateInvitation(
        useCase: ValidateInvitationImpl
    ): ValidateInvitation

    @Binds
    fun bindGetPresentationRequestFromUri(
        useCase: GetPresentationRequestFromUriImpl
    ): GetPresentationRequestFromUri

    @Binds
    fun bindGetProximityPresentationRequestFromUri(
        useCase: GetProximityPresentationRequestFromUriImpl
    ): GetProximityPresentationRequestFromUri

    @Binds
    fun bindGetCredentialOfferFromUri(
        useCase: GetCredentialOfferFromUriImpl
    ): GetCredentialOfferFromUri

    @Binds
    fun bindHandleInvitationProcessing(
        useCase: HandleInvitationProcessingSuccessImpl
    ): HandleInvitationProcessingSuccess
}
