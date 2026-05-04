package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import java.time.Instant

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Credential::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("credentialId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("credentialId")
    ]
)
data class CredentialActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val credentialId: Long, // ForeignKey
    val type: ActivityType,
    val actorTrust: TrustStatus,
    val vcSchemaTrust: VcSchemaTrustStatus,
    @ColumnInfo(defaultValue = "UNKNOWN")
    val actorCompliance: ActorComplianceState,
    val nonComplianceData: String?,
    val createdAt: Long = Instant.now().epochSecond,
)
