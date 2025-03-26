package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import timber.log.Timber

interface EIdRequestError {
    data class Unexpected(val cause: Throwable?) :
        EIdRequestCaseRepositoryError,
        EIdRequestStateRepositoryError,
        EIdRequestCaseWithStateRepositoryError,
        ApplyRequestError,
        StateRequestError
}

sealed interface EIdRequestCaseRepositoryError
sealed interface EIdRequestStateRepositoryError
sealed interface EIdRequestCaseWithStateRepositoryError
sealed interface ApplyRequestError
sealed interface StateRequestError

internal fun EIdRequestStateRepositoryError.toUpdateEIdRequestStateError() = when (this) {
    is EIdRequestError.Unexpected -> this
}

internal fun Throwable.toEIdRequestCaseRepositoryError(message: String): EIdRequestCaseRepositoryError {
    Timber.e(t = this, message = message)
    return EIdRequestError.Unexpected(this)
}

internal fun Throwable.toEIdRequestStateRepositoryError(message: String): EIdRequestStateRepositoryError {
    Timber.e(t = this, message = message)
    return EIdRequestError.Unexpected(this)
}

internal fun Throwable.toEIdRequestCaseWithStateRepositoryError(message: String): EIdRequestCaseWithStateRepositoryError {
    Timber.e(t = this, message = message)
    return EIdRequestError.Unexpected(this)
}

internal fun Throwable.toFetchSIdStateError(message: String): StateRequestError {
    Timber.e(t = this, message = message)
    return EIdRequestError.Unexpected(this)
}

internal fun Throwable.toFetchSIdCaseError(message: String): ApplyRequestError {
    Timber.e(t = this, message = message)
    return EIdRequestError.Unexpected(this)
}
