package ch.admin.foitt.wallet.platform.login.domain.model

import ch.admin.foitt.errorhandling.domain.model.AppError
import ch.admin.foitt.errorhandling.domain.model.ErrorOccurrence
import ch.admin.foitt.errorhandling.domain.model.mapTo
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricAuthenticationError
import ch.admin.foitt.wallet.platform.biometrics.domain.model.BiometricsError
import ch.admin.foitt.wallet.platform.biometrics.domain.model.GetBiometricsCipherError
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.database.domain.model.OpenDatabaseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.HashPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.LoadAndDecryptPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError

interface LoginError : AppError {
    data class InvalidPassphrase(override val errorOccurrence: ErrorOccurrence) : LoginWithPassphraseError, LoginWithBiometricsError
    data class BiometricsChanged(override val errorOccurrence: ErrorOccurrence) : LoginWithBiometricsError
    data class Cancelled(override val errorOccurrence: ErrorOccurrence) : LoginWithBiometricsError
    data class BiometricsLocked(override val errorOccurrence: ErrorOccurrence) : LoginWithBiometricsError
    data class General(override val errorOccurrence: ErrorOccurrence) : LoginWithPassphraseError, LoginWithBiometricsError
}

sealed interface LoginWithPassphraseError : LoginError
sealed interface LoginWithBiometricsError : LoginError

//region Error to Error mappings
fun HashPassphraseError.toLoginWithPassphraseError(): LoginWithPassphraseError = when (this) {
    is PassphraseError.General -> mapTo(LoginError::General)
}

fun PepperPassphraseError.toLoginWithPassphraseError(): LoginWithPassphraseError = when (this) {
    is PassphraseError.General -> mapTo(LoginError::General)
}

fun OpenDatabaseError.toLoginWithPassphraseError(): LoginWithPassphraseError = when (this) {
    is DatabaseError.WrongPassphrase -> mapTo(LoginError::InvalidPassphrase)
    is DatabaseError.AlreadyOpen,
    is DatabaseError.SetupFailed -> mapTo(LoginError::General)
}
fun GetBiometricsCipherError.toLoginWithBiometricsError(): LoginWithBiometricsError = when (this) {
    is BiometricsError.InvalidatedKey -> mapTo(LoginError::BiometricsChanged)
    is BiometricsError.General -> mapTo(LoginError::General)
}

fun BiometricAuthenticationError.toLoginWithBiometricsError(): LoginWithBiometricsError = when (this) {
    is BiometricAuthenticationError.PromptCancelled -> mapTo(LoginError::Cancelled)
    is BiometricAuthenticationError.PromptLocked -> mapTo(LoginError::BiometricsLocked)
    is BiometricAuthenticationError.PromptFailure -> mapTo(LoginError::General)
    is BiometricAuthenticationError.General -> mapTo(LoginError::General)
}

fun LoadAndDecryptPassphraseError.toLoginWithBiometricsError(): LoginWithBiometricsError = when (this) {
    is PassphraseError.General -> mapTo(LoginError::General)
}

fun OpenDatabaseError.toLoginWithBiometricsError(): LoginWithBiometricsError = when (this) {
    is DatabaseError.WrongPassphrase -> mapTo(LoginError::InvalidPassphrase)
    is DatabaseError.AlreadyOpen,
    is DatabaseError.SetupFailed -> mapTo(LoginError::General)
}
//endregion
