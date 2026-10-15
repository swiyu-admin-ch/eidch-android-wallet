package ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model

import ch.admin.foitt.errorhandling.domain.model.AppError
import ch.admin.foitt.errorhandling.domain.model.ErrorCodes
import ch.admin.foitt.errorhandling.domain.model.ErrorOccurrence
import ch.admin.foitt.errorhandling.domain.model.LogLevel
import ch.admin.foitt.errorhandling.domain.model.mapTo
import java.security.InvalidKeyException

sealed interface KeystoreError : AppError {

    data class InvalidatedKey(override val errorOccurrence: ErrorOccurrence) : GetCipherForDecryptionError
    data class InvalidKey(override val errorOccurrence: ErrorOccurrence) : GetCipherForEncryptionError
    data class General(override val errorOccurrence: ErrorOccurrence) :
        GetCipherForDecryptionError,
        GetCipherForEncryptionError,
        GetOrCreateSecretKeyError

    companion object {
        fun createGeneralError() = General(
            errorOccurrence = ErrorOccurrence(
                errorCode = ErrorCodes.Keystore.generalKeystoreError(),
                logLevel = LogLevel.DYNATRACE,
            )
        )
    }
}

sealed interface GetCipherForDecryptionError : KeystoreError
sealed interface GetCipherForEncryptionError : KeystoreError
sealed interface GetOrCreateSecretKeyError : KeystoreError

internal fun GetOrCreateSecretKeyError.toGetCipherForDecryptionError(): GetCipherForDecryptionError = when (this) {
    is KeystoreError.General -> this.mapTo(KeystoreError::General)
}

internal fun Throwable.toGetCipherForDecryptionError(): GetCipherForDecryptionError = when (this) {
    is InvalidKeyException -> KeystoreError.InvalidatedKey(
        ErrorOccurrence(
            errorCode = ErrorCodes.Keystore.invalidatedKeyError(),
            logLevel = LogLevel.DYNATRACE,
            throwable = this,
            details = "invalided key: ${this.message}",
        )
    )

    else -> KeystoreError.General(
        ErrorOccurrence(
            errorCode = ErrorCodes.Keystore.generalKeystoreError(),
            logLevel = LogLevel.DYNATRACE,
            throwable = this,
            details = this.message,
        )
    )
}

internal fun GetOrCreateSecretKeyError.toGetCipherForEncryptionError(): GetCipherForEncryptionError = when (this) {
    is KeystoreError.General -> this.mapTo(KeystoreError::General)
}

internal fun Throwable.toGetCipherForEncryptionError(): GetCipherForEncryptionError = when (this) {
    is InvalidKeyException -> KeystoreError.InvalidKey(
        ErrorOccurrence(
            errorCode = ErrorCodes.Keystore.invalidKeyError(),
            logLevel = LogLevel.DYNATRACE,
            throwable = this,
            details = "invalid key: ${this.message}",
        )
    )

    else -> KeystoreError.General(
        ErrorOccurrence(
            errorCode = ErrorCodes.Keystore.generalKeystoreError(),
            logLevel = LogLevel.DYNATRACE,
            throwable = this,
            details = this.message,
        )
    )
}

internal fun Throwable.toGetOrCreateSecretKeyError(): GetOrCreateSecretKeyError = KeystoreError.General(
    ErrorOccurrence(
        errorCode = ErrorCodes.Keystore.generalKeystoreError(),
        logLevel = LogLevel.DYNATRACE,
        throwable = this,
        details = this.message,
    )
)
