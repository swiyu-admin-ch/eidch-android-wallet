package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class VerifiableCredentialWithBatchDataAndAuthentication(
    @Embedded
    val verifiableCredential: VerifiableCredentialEntity,

    @Relation(
        entity = Credential::class,
        parentColumn = "credentialId",
        entityColumn = "id",
    )
    val credential: Credential,

    @Relation(
        entity = BatchRefreshDataEntity::class,
        parentColumn = "credentialId",
        entityColumn = "credentialId",
    )
    val batchData: BatchRefreshDataEntity,

    @Relation(
        entity = CredentialAuthenticationEntity::class,
        parentColumn = "credentialId",
        entityColumn = "credentialId",
    )
    val authentication: CredentialAuthenticationWithDpopBinding,
)
