package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class DeferredCredentialWithKeyBinding(
    @Embedded
    val deferredCredential: DeferredCredentialEntity,

    @Relation(
        entity = Credential::class,
        parentColumn = "credentialId",
        entityColumn = "id",
    )
    val credential: Credential,

    @Relation(
        entity = CredentialKeyBindingEntity::class,
        parentColumn = "credentialId",
        entityColumn = "credentialId"
    )
    val keyBinding: CredentialKeyBindingEntity?
)
