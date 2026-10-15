package ch.admin.foitt.wallet.platform.passphrase.domain.model

import ch.admin.foitt.errorhandling.domain.model.AppError
import ch.admin.foitt.errorhandling.domain.model.ErrorCodes
import ch.admin.foitt.errorhandling.domain.model.ErrorOccurrence
import ch.admin.foitt.errorhandling.domain.model.LogLevel
import ch.admin.foitt.errorhandling.domain.model.mapTo
import ch.admin.foitt.wallet.platform.crypto.domain.model.CryptoError
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashDataWithSaltError
import ch.admin.foitt.wallet.platform.database.domain.model.CreateAppDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.GetCipherForEncryptionError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.KeystoreError

sealed interface PassphraseError : AppError {
    data class DatabaseSetupFailed(override val errorOccurrence: ErrorOccurrence) : InitializePassphraseError
    data class General(override val errorOccurrence: ErrorOccurrence) :
        DeleteSecretKeyError,
        HashPassphraseError,
        PepperPassphraseError,
        InitializePassphraseError,
        EncryptAndSavePassphraseError,
        LoadAndDecryptPassphraseError,
        StorePassphraseError

    companion object {
        fun createGeneralError(throwable: Throwable) = General(
            ErrorOccurrence(
                errorCode = ErrorCodes.Passphrase.generalPassphraseError(),
                logLevel = LogLevel.DYNATRACE,
                throwable = throwable,
                details = throwable.message,
            )
        )
    }
}

sealed interface DeleteSecretKeyError : PassphraseError
sealed interface EncryptAndSavePassphraseError : PassphraseError
sealed interface HashPassphraseError : PassphraseError
sealed interface InitializePassphraseError : PassphraseError
sealed interface LoadAndDecryptPassphraseError : PassphraseError
sealed interface PepperPassphraseError : PassphraseError
sealed interface StorePassphraseError : PassphraseError

fun HashDataWithSaltError.toHashPassphraseError(): HashPassphraseError = when (this) {
    is CryptoError.HashError -> mapTo(PassphraseError::General)
}

fun HashPassphraseError.toInitializePassphraseError(): InitializePassphraseError = when (this) {
    is PassphraseError.General -> mapTo(PassphraseError::General)
}

fun PepperPassphraseError.toInitializePassphraseError(): InitializePassphraseError = when (this) {
    is PassphraseError.General -> mapTo(PassphraseError::General)
}

fun CreateAppDatabaseError.toInitializePassphraseError(): InitializePassphraseError = when (this) {
    is DatabaseError.SetupFailed -> mapTo(PassphraseError::DatabaseSetupFailed)
    is DatabaseError.AlreadyOpen -> mapTo(PassphraseError::General)
}

fun EncryptAndSavePassphraseError.toInitializePassphraseError(): InitializePassphraseError = when (this) {
    is PassphraseError.General -> mapTo(PassphraseError::General)
}

fun GetCipherForEncryptionError.toPepperPassphraseError(): PepperPassphraseError = when (this) {
    is KeystoreError.General -> this.mapTo(PassphraseError::General)
    is KeystoreError.InvalidKey -> this.mapTo(PassphraseError::General)
}

fun HashPassphraseError.toStorePassphraseError(): StorePassphraseError = when (this) {
    is PassphraseError.General -> mapTo(PassphraseError::General)
}

fun PepperPassphraseError.toStorePassphraseError(): StorePassphraseError = when (this) {
    is PassphraseError.General -> mapTo(PassphraseError::General)
}

fun EncryptAndSavePassphraseError.toStorePassphraseError(): StorePassphraseError = when (this) {
    is PassphraseError.General -> mapTo(PassphraseError::General)
}
