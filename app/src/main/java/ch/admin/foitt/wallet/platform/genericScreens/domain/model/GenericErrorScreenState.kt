@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.genericScreens.domain.model

import ch.admin.foitt.wallet.R

sealed interface GenericErrorScreenState {

    data class Error(
        val title: Int,
        val subtitle: Int,
        val errorText: String? = null,
        val errorDescription: Int? = null,
    ) : GenericErrorScreenState

    data class PresentationError(
        val title: Int,
        val subtitle: Int,
        val errorText: String? = null,
        val errorDescription: String? = null
    ) : GenericErrorScreenState

    companion object {
        fun generic() = Error(
            title = R.string.tk_credentialOffer_error_primary,
            subtitle = R.string.tk_credentialOffer_error_secondary,
        )
    }

    object Offer {
        private val offerErrorTitle = R.string.tk_credentialOffer_error_primary
        private val offerErrorSubtitle = R.string.tk_credentialOffer_error_secondary

        fun generic() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
        )

        fun invalidRequest() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
            errorText = "invalid_request",
            errorDescription = R.string.tk_credentialOffer_error_invalidRequest_description,
        )

        fun invalidRequestBearerToken() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
            errorText = "invalid_request_bearer_token",
            errorDescription = R.string.tk_credentialOffer_error_invalidRequest_description,
        )

        fun invalidGrant() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
            errorText = "invalid_grant",
            errorDescription = R.string.tk_credentialOffer_error_invalidGrant_description,
        )

        fun invalidClient() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
            errorText = "invalid_client",
            errorDescription = R.string.tk_credentialOffer_error_invalidClient_description,
        )

        fun invalidCredentialRequest() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
            errorText = "invalid_credential_request",
            errorDescription = R.string.tk_credentialOffer_error_invalidCredentialRequest_description,
        )

        fun unknownCredentialConfiguration() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
            errorText = "invalid_credential_configuration",
            errorDescription = R.string.tk_credentialOffer_error_unknownCredentialConfiguration_description,
        )

        fun unknownCredentialIdentifier() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
            errorText = "invalid_credential_identifier",
            errorDescription = R.string.tk_credentialOffer_error_unknownCredentialIdentifier_description,
        )

        fun invalidProof() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
            errorText = "invalid_proof",
            errorDescription = R.string.tk_credentialOffer_error_invalidProof_description,
        )

        fun invalidNonce() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
            errorText = "invalid_nonce",
            errorDescription = R.string.tk_credentialOffer_error_invalidNonce_description,
        )

        fun invalidEncryptionParameters() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
            errorText = "invalid_encryption_parameters",
            errorDescription = R.string.tk_credentialOffer_error_invalidEncryptionParameters_description,
        )

        fun credentialRequestDenied() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
            errorText = "credentialRequestDenied",
            errorDescription = R.string.tk_credentialOffer_error_credentialRequestDenied_description,
        )

        fun invalidTransactionId() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
            errorText = "invalid_transaction_id",
            errorDescription = R.string.tk_credentialOffer_error_invalidTransactionId_description,
        )

        fun insufficientScope() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
            errorText = "insufficient_scope",
            errorDescription = R.string.tk_credentialOffer_error_insufficientScope_description,
        )

        fun invalidToken() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
            errorText = "invalid_token",
            errorDescription = R.string.tk_credentialOffer_error_invalidToken_description,
        )

        fun unauthorizedClient() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
            errorText = "unauthorized_client",
            errorDescription = R.string.tk_credentialOffer_error_unauthorizedClient_description,
        )

        fun unauthorizedGrantType() = Error(
            title = offerErrorTitle,
            subtitle = offerErrorSubtitle,
            errorText = "unauthorized_grant_type",
            errorDescription = R.string.tk_credentialOffer_error_unauthorizedGrantType_description,
        )
    }

    object Presentation {
        private val presentationErrorTitle = R.string.tk_present_error_primary
        private val presentationErrorSubtitle = R.string.tk_present_error_secondary

        fun generic() = Error(
            title = presentationErrorTitle,
            subtitle = presentationErrorSubtitle,
        )

        fun invalidRequest() = Error(
            title = presentationErrorTitle,
            subtitle = presentationErrorSubtitle,
            errorText = "invalid_request",
            errorDescription = R.string.tk_credentialOffer_error_invalidRequest_description
        )

        fun invalidClient() = Error(
            title = presentationErrorTitle,
            subtitle = presentationErrorSubtitle,
            errorText = "invalid_client",
            errorDescription = R.string.tk_credentialOffer_error_invalidClient_description
        )

        fun invalidTransactionData() = Error(
            title = presentationErrorTitle,
            subtitle = presentationErrorSubtitle,
            errorText = "invalid_request",
            errorDescription = R.string.tk_present_error_invalidTransactionData_secondary
        )

        fun presentationError(
            errorText: String?,
            errorDescription: String?,
        ) = PresentationError(
            title = presentationErrorTitle,
            subtitle = presentationErrorSubtitle,
            errorText = errorText,
            errorDescription = errorDescription,
        )
    }
}
