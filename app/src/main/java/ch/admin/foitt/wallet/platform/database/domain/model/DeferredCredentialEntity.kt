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
data class DeferredCredentialEntity(
    @PrimaryKey
    val credentialId: Long, // Foreign key
    val progressionState: DeferredProgressionState = DeferredProgressionState.IN_PROGRESS,
    val transactionId: String,
    val accessToken: String,
    val refreshToken: String?,
    val endpoint: String,
    val pollInterval: Int = 5,
    val createdAt: Long = Instant.now().epochSecond,
    val polledAt: Long? = null,
)
