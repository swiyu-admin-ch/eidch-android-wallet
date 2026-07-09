package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class VerifiableCredentialWithBundleItemsWithKeyBinding(
    @Embedded
    val verifiableCredential: VerifiableCredentialEntity,

    @Relation(
        entity = Credential::class,
        parentColumn = "credentialId",
        entityColumn = "id",
    )
    val credential: Credential,

    @Relation(
        entity = BundleItemEntity::class,
        parentColumn = "credentialId",
        entityColumn = "credentialId"
    )
    val bundleItemsWithKeyBinding: List<BundleItemWithKeyBinding>,
) {
    val nextBundleItemWithKeyBindingToPresent
        get() = requireNotNull(
            bundleItemsWithKeyBinding.find {
                it.bundleItem.id == verifiableCredential.nextPresentableBundleItemId
            }
        )

    val nextBundleItemToPresent
        get() = nextBundleItemWithKeyBindingToPresent.bundleItem
}
