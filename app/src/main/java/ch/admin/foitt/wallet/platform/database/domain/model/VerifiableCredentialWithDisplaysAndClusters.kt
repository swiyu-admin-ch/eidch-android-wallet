package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class VerifiableCredentialWithDisplaysAndClusters(
    @Embedded
    val verifiableCredential: VerifiableCredentialEntity,
    @Relation(
        entity = Credential::class,
        parentColumn = "credentialId",
        entityColumn = "id",
    )
    val credential: Credential,
    @Relation(
        entity = CredentialDisplay::class,
        parentColumn = "credentialId",
        entityColumn = "credentialId",
    )
    val credentialDisplays: List<CredentialDisplay>,
    @Relation(
        entity = CredentialClaimClusterEntity::class,
        parentColumn = "credentialId",
        entityColumn = "verifiableCredentialId",
    )
    val clusters: List<ClusterWithDisplaysAndClaims>,
)
