package ch.admin.foitt.wallet.feature.changePassphrase.domain.usecase

import ch.admin.foitt.wallet.feature.changePassphrase.domain.model.ChangePassphraseError
import com.github.michaelbull.result.Result

fun interface ChangePassphrase {
    suspend operator fun invoke(newPassphrase: String): Result<Unit, ChangePassphraseError>
}
