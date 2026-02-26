package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.wallet.platform.credential.domain.model.RefreshDeferredCredentialsError
import com.github.michaelbull.result.Result

interface RefreshDeferredCredentials {
    suspend operator fun invoke(): Result<Unit, RefreshDeferredCredentialsError>
}
