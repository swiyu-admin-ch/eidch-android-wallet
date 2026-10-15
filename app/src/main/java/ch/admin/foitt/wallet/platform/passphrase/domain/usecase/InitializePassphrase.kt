package ch.admin.foitt.wallet.platform.passphrase.domain.usecase

import ch.admin.foitt.wallet.platform.passphrase.domain.model.InitializePassphraseError
import com.github.michaelbull.result.Result
import javax.crypto.Cipher

fun interface InitializePassphrase {
    suspend operator fun invoke(
        passphrase: String,
        encryptionCipher: Cipher?,
    ): Result<Unit, InitializePassphraseError>
}
