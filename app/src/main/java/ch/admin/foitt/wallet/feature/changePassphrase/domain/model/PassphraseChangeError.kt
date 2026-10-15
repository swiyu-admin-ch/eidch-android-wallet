package ch.admin.foitt.wallet.feature.changePassphrase.domain.model

import ch.admin.foitt.errorhandling.domain.model.AppError
import ch.admin.foitt.errorhandling.domain.model.ErrorOccurrence
import ch.admin.foitt.errorhandling.domain.model.mapTo
import ch.admin.foitt.wallet.platform.database.domain.model.ChangeDatabasePassphraseError
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.HashPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError

sealed interface PassphraseChangeError : AppError {
    data class General(override val errorOccurrence: ErrorOccurrence) : ChangePassphraseError
}

sealed interface ChangePassphraseError : PassphraseChangeError

fun HashPassphraseError.toChangePassphraseError(): ChangePassphraseError = when (this) {
    is PassphraseError.General -> mapTo(PassphraseChangeError::General)
}

fun PepperPassphraseError.toChangePassphraseError(): ChangePassphraseError = when (this) {
    is PassphraseError.General -> mapTo(PassphraseChangeError::General)
}

fun ChangeDatabasePassphraseError.toChangePassphraseError(): ChangePassphraseError = when (this) {
    is DatabaseError.ReKeyFailed -> mapTo(PassphraseChangeError::General)
}
