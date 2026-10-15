package ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.model

import ch.admin.foitt.errorhandling.domain.model.AppError
import ch.admin.foitt.errorhandling.domain.model.ErrorOccurrence
import ch.admin.foitt.errorhandling.domain.model.mapTo
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.database.domain.model.OpenDatabaseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.HashPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError

sealed interface AuthWithPassphraseError : AppError {
    data class InvalidPassphrase(override val errorOccurrence: ErrorOccurrence) : AuthenticateWithPassphraseError
    data class General(override val errorOccurrence: ErrorOccurrence) : AuthenticateWithPassphraseError
}
sealed interface AuthenticateWithPassphraseError : AuthWithPassphraseError

fun HashPassphraseError.toAuthenticateWithPassphraseError(): AuthenticateWithPassphraseError = when (this) {
    is PassphraseError.General -> mapTo(AuthWithPassphraseError::General)
}

fun PepperPassphraseError.toAuthenticateWithPassphraseError(): AuthenticateWithPassphraseError = when (this) {
    is PassphraseError.General -> mapTo(AuthWithPassphraseError::General)
}

fun OpenDatabaseError.toAuthenticateWithPassphraseError(): AuthenticateWithPassphraseError = when (this) {
    is DatabaseError.WrongPassphrase -> mapTo(AuthWithPassphraseError::InvalidPassphrase)
    is DatabaseError.AlreadyOpen,
    is DatabaseError.SetupFailed -> mapTo(AuthWithPassphraseError::General)
}
