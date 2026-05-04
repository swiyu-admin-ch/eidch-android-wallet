package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CredentialActivityEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("activityId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("activityId")
    ]
)
data class NonComplianceReasonDisplayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityId: Long, // ForeignKey
    override val locale: String,
    val reason: String,
) : LocalizedDisplay
