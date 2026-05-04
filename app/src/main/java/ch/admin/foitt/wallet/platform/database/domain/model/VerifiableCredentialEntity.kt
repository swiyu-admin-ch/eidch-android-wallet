package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Credential::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("credentialId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("credentialId"),
    ]
)
data class VerifiableCredentialEntity(
    @PrimaryKey
    val credentialId: Long, // Foreign key
    val progressionState: VerifiableProgressionState = VerifiableProgressionState.UNACCEPTED,
    val issuer: String?,
    val validFrom: Long?,
    val validUntil: Long?,
    val createdAt: Long = Instant.now().epochSecond,
    val updatedAt: Long? = null,
    val nextPresentableBundleItemId: Long,
)
