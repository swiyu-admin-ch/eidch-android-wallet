package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CredentialClaimClusterEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("clusterId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("clusterId")
    ]
)
data class CredentialClaim(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clusterId: Long, // Foreign key
    override val path: String,
    override val value: String?,
    override val valueType: String?,
    val valueDisplayInfo: String? = null,
    val order: Int = -1,
    @ColumnInfo(defaultValue = "false")
    val isSensitive: Boolean = false
) : Claim
