package ch.admin.foitt.wallet.platform.database.domain.model

import ch.admin.foitt.errorhandling.domain.model.AppError
import ch.admin.foitt.errorhandling.domain.model.ErrorCode
import ch.admin.foitt.errorhandling.domain.model.ErrorCodes
import ch.admin.foitt.errorhandling.domain.model.ErrorOccurrence
import ch.admin.foitt.errorhandling.domain.model.LogLevel

sealed interface DatabaseError : AppError {
    data class AlreadyOpen(override val errorOccurrence: ErrorOccurrence) : CreateAppDatabaseError, OpenDatabaseError
    data class SetupFailed(override val errorOccurrence: ErrorOccurrence) : CreateAppDatabaseError, OpenDatabaseError
    data class ReKeyFailed(override val errorOccurrence: ErrorOccurrence) : ChangeDatabasePassphraseError
    data class WrongPassphrase(override val errorOccurrence: ErrorOccurrence) : OpenDatabaseError

    companion object {
        fun createAlreadyOpenError() = AlreadyOpen(
            errorOccurrence = createError(
                code = ErrorCodes.Database.alreadyOpenError(),
            )
        )

        fun createSetupFailedError(throwable: Throwable) = SetupFailed(
            errorOccurrence = createError(
                code = ErrorCodes.Database.setupFailedError(),
                throwable = throwable,
            )
        )

        fun createReKeyFailed(throwable: Throwable) = ReKeyFailed(
            errorOccurrence = createError(
                code = ErrorCodes.Database.reKeyError(),
                throwable = throwable,
            )
        )

        fun createReKeyFailed(message: String) = ReKeyFailed(
            errorOccurrence = createError(
                code = ErrorCodes.Database.reKeyError(),
                message = message,
            )
        )

        fun createWrongPassphraseError(throwable: Throwable) = WrongPassphrase(
            errorOccurrence = createError(
                code = ErrorCodes.Database.wrongPassphraseError(),
                throwable = throwable,
            )
        )

        private fun createError(code: ErrorCode, throwable: Throwable? = null, message: String? = null) = ErrorOccurrence(
            errorCode = code,
            logLevel = LogLevel.DYNATRACE,
            throwable = throwable,
            details = throwable?.message ?: message,
        )
    }
}

sealed interface ChangeDatabasePassphraseError : DatabaseError
sealed interface CreateAppDatabaseError : DatabaseError
sealed interface OpenDatabaseError : DatabaseError
