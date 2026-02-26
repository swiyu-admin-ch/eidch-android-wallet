package ch.admin.foitt.wallet.platform.batch.domain.usecase

import ch.admin.foitt.wallet.platform.batch.domain.error.RefreshBatchCredentialsError
import com.github.michaelbull.result.Result

interface RefreshBatchCredentials {
    suspend operator fun invoke(): Result<Unit, RefreshBatchCredentialsError>
}
