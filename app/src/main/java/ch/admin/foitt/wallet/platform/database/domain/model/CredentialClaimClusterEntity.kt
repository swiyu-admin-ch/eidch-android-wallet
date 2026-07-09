package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = VerifiableCredentialEntity::class,
            parentColumns = arrayOf("credentialId"),
            childColumns = arrayOf("verifiableCredentialId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CredentialClaimClusterEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("parentClusterId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("verifiableCredentialId"),
        Index("parentClusterId")
    ]
)
data class CredentialClaimClusterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val verifiableCredentialId: Long, // Foreign key
    val parentClusterId: Long?, // Foreign key (on this table)
    val order: Int,
    @ColumnInfo(defaultValue = "[]")
    val path: String = "[]",
    @ColumnInfo(defaultValue = "false")
    val isSensitive: Boolean = false,
)

fun Cluster.toCredentialClaimClusterEntity(verifiableCredentialId: Long, parentClusterId: Long? = null) = CredentialClaimClusterEntity(
    verifiableCredentialId = verifiableCredentialId,
    parentClusterId = parentClusterId,
    order = this.order,
    path = path,
    isSensitive = isSensitive,
)
