package ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.model.AuthenticateWithPassphraseError
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.model.toAuthenticateWithPassphraseError
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.usecase.AuthenticateWithPassphrase
import ch.admin.foitt.wallet.platform.database.domain.model.OpenDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.usecase.CheckDatabasePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.model.HashPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class AuthenticateWithPassphraseImpl @Inject constructor(
    private val hashPassphrase: HashPassphrase,
    private val pepperPassphrase: PepperPassphrase,
    private val checkDatabasePassphrase: CheckDatabasePassphrase,
) : AuthenticateWithPassphrase {

    @CheckResult
    override suspend fun invoke(
        passphrase: String,
    ): Result<Unit, AuthenticateWithPassphraseError> = coroutineBinding {
        val passphraseHash = hashPassphrase(
            passphrase = passphrase,
            initializeSalt = false
        ).mapError(HashPassphraseError::toAuthenticateWithPassphraseError).bind()

        val pepperedPassphraseHash = pepperPassphrase(
            passphrase = passphraseHash.hash,
            initializePepper = false,
        ).mapError(PepperPassphraseError::toAuthenticateWithPassphraseError).bind()

        checkDatabasePassphrase(
            passphrase = pepperedPassphraseHash.hash
        ).mapError(OpenDatabaseError::toAuthenticateWithPassphraseError).bind()
    }
}
