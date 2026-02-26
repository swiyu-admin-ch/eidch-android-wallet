package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class BundleItemWithKeyBinding(
    @Embedded
    val bundleItem: BundleItemEntity,
    @Relation(
        entity = CredentialKeyBindingEntity::class,
        parentColumn = "id",
        entityColumn = "bundleItemId"
    )
    val keyBinding: CredentialKeyBindingEntity?
)
