package ch.admin.foitt.wallet.platform.batch.domain.model

data class BatchRefreshParams(
    val credentialId: Long,
    val presentableCredentialCount: Int,
    val oldBatchSize: Int
)
