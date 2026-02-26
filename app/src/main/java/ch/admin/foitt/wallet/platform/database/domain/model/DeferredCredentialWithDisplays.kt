package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class DeferredCredentialWithDisplays(
    @Embedded
    val deferredCredential: DeferredCredentialEntity,
    @Relation(
        entity = CredentialDisplay::class,
        parentColumn = "credentialId",
        entityColumn = "credentialId",
    )
    val credentialDisplays: List<CredentialDisplay>
)
