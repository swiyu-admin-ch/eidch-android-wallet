package ch.admin.foitt.wallet.platform.batch.domain.error

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError

sealed interface BatchRefreshDataRepositoryError {
    data class Unexpected(val cause: Throwable?) : BatchRefreshDataRepositoryError

    fun toFetchCredentialError() = when (this) {
        is Unexpected -> CredentialError.Unexpected(cause)
    }

    fun toRefreshBatchCredentialsError() = when (this) {
        is Unexpected -> RefreshBatchCredentialsError.Unexpected(cause)
    }
}
