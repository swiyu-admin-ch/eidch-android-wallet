package ch.admin.foitt.wallet.feature.credentialOffer.domain.model

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.model.VerifiableCredentialRepositoryError

interface CredentialOfferError {
    data class Unexpected(val throwable: Throwable?) :
        GetCredentialOfferFlowError,
        AcceptCredentialError
}

sealed interface GetCredentialOfferFlowError
sealed interface AcceptCredentialError

internal fun CredentialWithDisplaysRepositoryError.toGetCredentialOfferFlowError(): GetCredentialOfferFlowError =
    when (this) {
        is SsiError.Unexpected -> CredentialOfferError.Unexpected(cause)
    }

internal fun MapToCredentialDisplayDataError.toGetCredentialOfferFlowError(): GetCredentialOfferFlowError = when (this) {
    is CredentialError.Unexpected -> CredentialOfferError.Unexpected(cause)
}

internal fun VerifiableCredentialRepositoryError.toAcceptCredentialError(): AcceptCredentialError = when (this) {
    is SsiError.Unexpected -> CredentialOfferError.Unexpected(cause)
}
