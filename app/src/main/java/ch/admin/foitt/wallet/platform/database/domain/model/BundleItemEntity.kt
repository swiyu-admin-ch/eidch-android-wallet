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
            childColumns = arrayOf("credentialId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("credentialId"),
    ]
)
data class BundleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(defaultValue = "0")
    val presented: Boolean = false,
    @ColumnInfo(defaultValue = "UNKNOWN")
    val status: CredentialStatus = CredentialStatus.UNKNOWN,
    val credentialId: Long,
    val payload: String,
)
