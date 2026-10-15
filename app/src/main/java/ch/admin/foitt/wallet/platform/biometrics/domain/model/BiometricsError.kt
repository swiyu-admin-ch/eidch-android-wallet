package ch.admin.foitt.wallet.platform.biometrics.domain.model

import ch.admin.foitt.errorhandling.domain.model.AppError
import ch.admin.foitt.errorhandling.domain.model.ErrorCodes
import ch.admin.foitt.errorhandling.domain.model.ErrorOccurrence
import ch.admin.foitt.errorhandling.domain.model.LogLevel
import ch.admin.foitt.errorhandling.domain.model.mapTo
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricAuthenticationError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.GetCipherForDecryptionError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.GetCipherForEncryptionError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.KeystoreError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.DeleteSecretKeyError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.StorePassphraseError

interface BiometricsError : AppError {
    data class InvalidatedKey(override val errorOccurrence: ErrorOccurrence) : GetBiometricsCipherError
    data class Cancelled(override val errorOccurrence: ErrorOccurrence) : InitializeCipherWithBiometricsError
    data class Locked(override val errorOccurrence: ErrorOccurrence) : InitializeCipherWithBiometricsError
    data class General(override val errorOccurrence: ErrorOccurrence) :
        GetBiometricsCipherError,
        InitializeCipherWithBiometricsError,
        ResetBiometricsError

    companion object {
        fun createInvalidatedKeyError() = InvalidatedKey(
            errorOccurrence = ErrorOccurrence(
                errorCode = ErrorCodes.Biometrics.invalidatedKeyError(),
                logLevel = LogLevel.DYNATRACE,
            )
        )

        fun createGeneralError() = General(
            errorOccurrence = ErrorOccurrence(
                errorCode = ErrorCodes.Biometrics.generalError(),
                logLevel = LogLevel.DYNATRACE,
            )
        )
    }
}

sealed interface GetBiometricsCipherError : BiometricsError
sealed interface InitializeCipherWithBiometricsError : BiometricsError
sealed interface ResetBiometricsError : BiometricsError

//region Error to Error mappings
fun ResetBiometricsError.toInitializeCipherWithBiometricsError() = when (this) {
    is BiometricsError.General -> mapTo(BiometricsError::General)
}

fun GetCipherForEncryptionError.toInitializeCipherWithBiometricsError() = when (this) {
    is KeystoreError.InvalidKey -> mapTo(BiometricsError::General)
    is KeystoreError.General -> mapTo(BiometricsError::General)
}

fun BiometricAuthenticationError.toInitializeCipherWithBiometricsError(): InitializeCipherWithBiometricsError = when (this) {
    is BiometricAuthenticationError.PromptCancelled -> mapTo(BiometricsError::Cancelled)
    is BiometricAuthenticationError.PromptLocked -> mapTo(BiometricsError::Locked)
    is BiometricAuthenticationError.PromptFailure -> mapTo(BiometricsError::General)
    is BiometricAuthenticationError.General -> mapTo(BiometricsError::General)
}

fun StorePassphraseError.toInitializeCipherWithBiometricsError() = when (this) {
    is PassphraseError.General -> mapTo(BiometricsError::General)
}

fun GetCipherForDecryptionError.toGetBiometricsCipherError() = when (this) {
    is KeystoreError.InvalidatedKey -> mapTo(BiometricsError::InvalidatedKey)
    is KeystoreError.General -> mapTo(BiometricsError::General)
}

fun DeleteSecretKeyError.toResetBiometricsError(): ResetBiometricsError = when (this) {
    is PassphraseError.General -> mapTo(BiometricsError::General)
}
