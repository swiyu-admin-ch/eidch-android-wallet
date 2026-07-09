package ch.admin.foitt.wallet.platform.passphrase.domain.model

import ch.admin.foitt.wallet.platform.crypto.domain.model.HashDataError
import ch.admin.foitt.wallet.platform.database.domain.model.CreateDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError

sealed interface InitializePassphraseError {
    val throwable: Throwable?
    data class Unexpected(override val throwable: Throwable?) : InitializePassphraseError
    data class DatabaseSetupFailed(override val throwable: Throwable?) : InitializePassphraseError
}

//region Error to Error mappings
fun PepperPassphraseError.toInitializePassphraseError(): InitializePassphraseError = when (this) {
    is PepperPassphraseError.Unexpected -> InitializePassphraseError.Unexpected(this.throwable)
}

fun HashDataError.toInitializePassphraseError(): InitializePassphraseError = when (this) {
    is HashDataError.Unexpected -> InitializePassphraseError.Unexpected(this.throwable)
}

fun CreateDatabaseError.toInitializePassphraseError(): InitializePassphraseError = when (this) {
    is DatabaseError.SetupFailed -> InitializePassphraseError.DatabaseSetupFailed(this.throwable)
    DatabaseError.AlreadyOpen -> InitializePassphraseError.Unexpected(null)
}
//endregion
