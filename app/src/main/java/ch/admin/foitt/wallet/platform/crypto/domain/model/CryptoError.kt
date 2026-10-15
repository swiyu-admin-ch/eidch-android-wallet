package ch.admin.foitt.wallet.platform.crypto.domain.model

import ch.admin.foitt.errorhandling.domain.model.AppError
import ch.admin.foitt.errorhandling.domain.model.ErrorCodes
import ch.admin.foitt.errorhandling.domain.model.ErrorOccurrence
import ch.admin.foitt.errorhandling.domain.model.LogLevel

sealed interface CryptoError : AppError {
    data class HashError(override val errorOccurrence: ErrorOccurrence) : HashDataWithSaltError

    companion object {
        fun createGeneralError(throwable: Throwable) = HashError(
            ErrorOccurrence(
                errorCode = ErrorCodes.Crypto.generalCryptoError(),
                logLevel = LogLevel.DYNATRACE,
                throwable = throwable,
                details = throwable.message,
            )
        )
    }
}

sealed interface HashDataWithSaltError : CryptoError
