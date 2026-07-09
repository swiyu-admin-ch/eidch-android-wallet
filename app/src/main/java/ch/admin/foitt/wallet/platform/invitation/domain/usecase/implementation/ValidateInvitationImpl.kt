package ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.Invitation
import ch.admin.foitt.wallet.BuildConfig
import ch.admin.foitt.wallet.platform.invitation.domain.model.GetCredentialOfferError
import ch.admin.foitt.wallet.platform.invitation.domain.model.GetPresentationRequestError
import ch.admin.foitt.wallet.platform.invitation.domain.model.GetProximityPresentationRequestError
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ValidateInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.toValidateInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.GetCredentialOfferFromUri
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.GetPresentationRequestFromUri
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.GetProximityPresentationRequestFromUri
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ValidateInvitation
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import timber.log.Timber
import java.net.URI
import javax.inject.Inject

internal class ValidateInvitationImpl @Inject constructor(
    private val getCredentialOfferFromUri: GetCredentialOfferFromUri,
    private val getPresentationRequestFromUri: GetPresentationRequestFromUri,
    private val getProximityPresentationRequestFromUri: GetProximityPresentationRequestFromUri,
) : ValidateInvitation {

    @CheckResult
    override suspend fun invoke(input: String): Result<Invitation, ValidateInvitationError> = coroutineBinding {
        val invitationUri = parseUri(input).bind()
        when (invitationUri.scheme) {
            BuildConfig.SCHEME_PRESENTATION_REQUEST_OID,
            BuildConfig.SCHEME_PRESENTATION_REQUEST_SWIYU,
            BuildConfig.SCHEME_PRESENTATION_REQUEST -> getPresentationRequestFromUri(invitationUri)
                .mapError(GetPresentationRequestError::toValidateInvitationError)
                .bind()
            BuildConfig.SCHEME_PRESENTATION_REQUEST_PROXIMITY -> getProximityPresentationRequestFromUri(invitationUri)
                .mapError(GetProximityPresentationRequestError::toValidateInvitationError)
                .bind()
            BuildConfig.SCHEME_CREDENTIAL_OFFER,
            BuildConfig.SCHEME_CREDENTIAL_OFFER_SWIYU -> getCredentialOfferFromUri(invitationUri)
                .mapError(GetCredentialOfferError::toValidateInvitationError)
                .bind()
            else -> {
                Timber.d("Unknown schema of uri: $input")
                Err(InvitationError.UnknownSchema)
                    .bind<Invitation>()
            }
        }
    }

    private fun parseUri(input: String) =
        runSuspendCatching {
            URI(input)
        }.mapError {
            Timber.d("Invalid uri: $input")
            InvitationError.InvalidUri
        }
}
