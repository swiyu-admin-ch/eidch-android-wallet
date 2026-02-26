package ch.admin.foitt.wallet.platform.credential.domain.model

sealed interface FetchCredentialResult {
    data class Credential(
        val credentialId: Long
    ) : FetchCredentialResult

    data class DeferredCredential(
        val credentialId: Long
    ) : FetchCredentialResult
}
