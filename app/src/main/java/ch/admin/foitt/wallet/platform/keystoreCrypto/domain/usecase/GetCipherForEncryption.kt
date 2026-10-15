package ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.GetCipherForEncryptionError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.KeystoreKeyConfig
import com.github.michaelbull.result.Result
import javax.crypto.Cipher

fun interface GetCipherForEncryption {
    @CheckResult
    suspend operator fun invoke(
        keystoreKeyConfig: KeystoreKeyConfig,
        initializationVector: ByteArray?,
    ): Result<Cipher, GetCipherForEncryptionError>
}
