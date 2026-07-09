package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class DeferredCredentialWithAuthenticationAndKeyBinding(
    @Embedded
    val deferredCredential: DeferredCredentialEntity,

    @Relation(
        entity = Credential::class,
        parentColumn = "credentialId",
        entityColumn = "id",
    )
    val credential: Credential,

    @Relation(
        entity = CredentialAuthenticationEntity::class,
        parentColumn = "credentialId",
        entityColumn = "credentialId",
    )
    val authentication: CredentialAuthenticationWithDpopBinding,

    @Relation(
        entity = CredentialKeyBindingEntity::class,
        parentColumn = "credentialId",
        entityColumn = "credentialId"
    )
    val keyBindings: List<CredentialKeyBindingEntity>,
)
