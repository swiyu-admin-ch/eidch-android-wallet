package ch.admin.foitt.wallet.platform.biometricPrompt.domain.model

import ch.admin.foitt.errorhandling.domain.model.AppError
import ch.admin.foitt.errorhandling.domain.model.ErrorCodes
import ch.admin.foitt.errorhandling.domain.model.ErrorOccurrence
import ch.admin.foitt.errorhandling.domain.model.LogLevel

sealed interface BiometricAuthenticationError : AppError {
    data class PromptCancelled(override val errorOccurrence: ErrorOccurrence) : BiometricAuthenticationError
    data class PromptLocked(override val errorOccurrence: ErrorOccurrence) : BiometricAuthenticationError
    data class PromptFailure(override val errorOccurrence: ErrorOccurrence) : BiometricAuthenticationError
    data class General(override val errorOccurrence: ErrorOccurrence) : BiometricAuthenticationError

    companion object {
        fun createPromptCancelledError(message: String? = null) = PromptCancelled(
            ErrorOccurrence(
                errorCode = ErrorCodes.BiometricAuthentication.promptCancelledError(),
                logLevel = LogLevel.DEBUG,
                details = message,
            )
        )

        fun createPromptLockedError(message: String? = null) = PromptLocked(
            ErrorOccurrence(
                errorCode = ErrorCodes.BiometricAuthentication.promptLockedError(),
                logLevel = LogLevel.DYNATRACE,
                details = message,
            )
        )

        fun createPromptFailureError(message: String) = PromptFailure(
            ErrorOccurrence(
                errorCode = ErrorCodes.BiometricAuthentication.promptFailureError(),
                logLevel = LogLevel.DYNATRACE,
                details = message,
            )
        )

        fun createGeneralError(throwable: Throwable) = General(
            ErrorOccurrence(
                errorCode = ErrorCodes.BiometricAuthentication.generalError(),
                logLevel = LogLevel.DYNATRACE,
                throwable = throwable,
                details = throwable.message,
            )
        )

        fun createGeneralError(message: String) = General(
            ErrorOccurrence(
                errorCode = ErrorCodes.BiometricAuthentication.generalError(),
                logLevel = LogLevel.DYNATRACE,
                details = message,
            )
        )
    }
}
