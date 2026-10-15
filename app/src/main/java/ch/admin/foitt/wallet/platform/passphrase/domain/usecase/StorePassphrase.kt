package ch.admin.foitt.wallet.platform.passphrase.domain.usecase

import ch.admin.foitt.wallet.platform.passphrase.domain.model.StorePassphraseError
import com.github.michaelbull.result.Result
import javax.crypto.Cipher

fun interface StorePassphrase {
    suspend operator fun invoke(
        passphrase: String,
        encryptionCipher: Cipher,
    ): Result<Unit, StorePassphraseError>
}
