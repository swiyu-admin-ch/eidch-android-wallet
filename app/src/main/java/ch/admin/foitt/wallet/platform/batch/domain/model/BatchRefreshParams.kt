package ch.admin.foitt.wallet.platform.batch.domain.model

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationWithDpopBinding

data class BatchRefreshParams(
    val credentialId: Long,
    val presentableCredentialCount: Int,
    val oldBatchSize: Int,
    val authentication: CredentialAuthenticationWithDpopBinding
)
