package ch.admin.foitt.wallet.platform.ssi.domain.usecase

import ch.admin.foitt.wallet.platform.credential.domain.model.DeferredCredentialDisplayData
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialsWithDetailsFlowError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface GetDeferredCredentialWithDetailFlow {
    suspend operator fun invoke(credentialId: Long): Flow<Result<DeferredCredentialDisplayData, GetCredentialsWithDetailsFlowError>>
}
