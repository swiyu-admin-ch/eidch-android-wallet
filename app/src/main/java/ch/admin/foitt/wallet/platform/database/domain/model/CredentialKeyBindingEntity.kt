package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Credential::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("credentialId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = BundleItemEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("bundleItemId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("credentialId"),
        Index("bundleItemId"),
    ]
)
data class CredentialKeyBindingEntity(
    @PrimaryKey
    val id: String,
    val credentialId: Long, // Foreign key
    val bundleItemId: Long? = null,
    val algorithm: String,
    val bindingType: KeyBindingType,
    val publicKey: ByteArray? = null,
    val privateKey: ByteArray? = null,
) {
    @Suppress("CyclomaticComplexMethod")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CredentialKeyBindingEntity

        if (id != other.id) return false
        if (credentialId != other.credentialId) return false
        if (algorithm != other.algorithm) return false
        if (bindingType != other.bindingType) return false
        if (publicKey != null) {
            if (other.publicKey == null) return false
            if (!publicKey.contentEquals(other.publicKey)) return false
        } else if (other.publicKey != null) return false
        if (privateKey != null) {
            if (other.privateKey == null) return false
            if (!privateKey.contentEquals(other.privateKey)) return false
        } else if (other.privateKey != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + credentialId.hashCode()
        result = 31 * result + algorithm.hashCode()
        result = 31 * result + bindingType.hashCode()
        result = 31 * result + (publicKey?.contentHashCode() ?: 0)
        result = 31 * result + (privateKey?.contentHashCode() ?: 0)
        return result
    }
}
