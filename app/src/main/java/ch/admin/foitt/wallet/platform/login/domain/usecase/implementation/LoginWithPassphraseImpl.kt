package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.database.domain.model.OpenDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.usecase.OpenAppDatabase
import ch.admin.foitt.wallet.platform.login.domain.model.LoginWithPassphraseError
import ch.admin.foitt.wallet.platform.login.domain.model.toLoginWithPassphraseError
import ch.admin.foitt.wallet.platform.login.domain.usecase.LoginWithPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.model.HashPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import ch.admin.foitt.wallet.platform.userInteraction.domain.usecase.UserInteraction
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import timber.log.Timber
import javax.inject.Inject

class LoginWithPassphraseImpl @Inject constructor(
    private val hashPassphrase: HashPassphrase,
    private val openAppDatabase: OpenAppDatabase,
    private val pepperPassphrase: PepperPassphrase,
    private val userInteraction: UserInteraction,
) : LoginWithPassphrase {

    @CheckResult
    override suspend fun invoke(passphrase: String): Result<Unit, LoginWithPassphraseError> = coroutineBinding {
        val passphraseHash = hashPassphrase(
            passphrase = passphrase,
            initializeSalt = false
        ).mapError(HashPassphraseError::toLoginWithPassphraseError).bind()

        val pepperedPassphraseHash = pepperPassphrase(
            passphrase = passphraseHash.hash,
            initializePepper = false,
        ).mapError(PepperPassphraseError::toLoginWithPassphraseError).bind()

        // touching the keyboard does apparently not count as a user interaction on some devices (f. e. samsung galaxy a53)
        // -> trigger it manually
        userInteraction()

        openAppDatabase(
            passphrase = pepperedPassphraseHash.hash
        ).onErr {
            Timber.d("AppDatabase login failed")
        }.mapError(OpenDatabaseError::toLoginWithPassphraseError).bind()

        Timber.d("Pin Authentication succeeded")
    }
}
