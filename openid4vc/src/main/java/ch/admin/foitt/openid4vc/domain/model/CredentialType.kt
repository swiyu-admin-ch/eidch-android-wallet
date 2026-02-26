package ch.admin.foitt.openid4vc.domain.model

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialRequestProofs

sealed interface CredentialType {
    data class Verifiable(
        val verifiableCredentialParams: VerifiableCredentialParams,
        val proofs: CredentialRequestProofs?,
    ) : CredentialType

    data class Deferred(val transactionId: String) : CredentialType
}
