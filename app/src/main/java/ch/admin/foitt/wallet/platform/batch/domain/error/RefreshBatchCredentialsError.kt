package ch.admin.foitt.wallet.platform.batch.domain.error

import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.model.VerifiableCredentialRepositoryError

sealed interface RefreshBatchCredentialsError {
    class Unexpected(val cause: Throwable?) : RefreshBatchCredentialsError
}

fun VerifiableCredentialRepositoryError.toRefreshBatchCredentialsError() = when (this) {
    is SsiError.Unexpected -> RefreshBatchCredentialsError.Unexpected(cause = cause)
}
