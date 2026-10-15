package ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.errorhandling.domain.model.ErrorCodes
import ch.admin.foitt.errorhandling.domain.model.ErrorOccurrence
import ch.admin.foitt.errorhandling.domain.model.LogLevel
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.GetCipherForEncryptionError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.GetOrCreateSecretKeyError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.KeystoreError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.KeystoreKeyConfig
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.toGetCipherForEncryptionError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.GetCipherForEncryption
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.GetOrCreateSecretKey
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

internal class GetCipherForEncryptionImpl @Inject constructor(
    private val getOrCreateSecretKey: GetOrCreateSecretKey,
) : GetCipherForEncryption {
    @CheckResult
    override suspend fun invoke(
        keystoreKeyConfig: KeystoreKeyConfig,
        initializationVector: ByteArray?,
    ): Result<Cipher, GetCipherForEncryptionError> = coroutineBinding {
        if (keystoreKeyConfig.randomizedEncryptionRequired && initializationVector != null) {
            return@coroutineBinding Err(generalKeystoreError("IV is provided while key config does not allow it")).bind<Cipher>()
        }

        runSuspendCatching {
            val cipher = Cipher.getInstance(keystoreKeyConfig.encryptionTransformation)
            val secretKey = getOrCreateSecretKey(keystoreKeyConfig)
                .mapError(GetOrCreateSecretKeyError::toGetCipherForEncryptionError).bind()

            if (initializationVector != null) {
                val gcmParameterSpec = GCMParameterSpec(keystoreKeyConfig.gcmAuthTagLength, initializationVector)
                cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    gcmParameterSpec,
                )
            } else {
                cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                )
            }
            cipher
        }.mapError { throwable ->
            throwable.toGetCipherForEncryptionError()
        }.bind()
    }

    private fun generalKeystoreError(message: String) = KeystoreError.General(
        ErrorOccurrence(
            errorCode = ErrorCodes.Keystore.generalKeystoreError(),
            logLevel = LogLevel.DYNATRACE,
            details = message,
        )
    )
}
