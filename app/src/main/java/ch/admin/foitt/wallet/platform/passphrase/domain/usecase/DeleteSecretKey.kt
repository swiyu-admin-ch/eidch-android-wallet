package ch.admin.foitt.wallet.platform.passphrase.domain.usecase

import ch.admin.foitt.wallet.platform.passphrase.domain.model.DeleteSecretKeyError
import com.github.michaelbull.result.Result

interface DeleteSecretKey {
    operator fun invoke(): Result<Unit, DeleteSecretKeyError>
}
