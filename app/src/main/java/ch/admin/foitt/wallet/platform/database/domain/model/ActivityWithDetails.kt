package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class ActivityWithDetails(
    @Embedded val activity: CredentialActivityEntity,
    @Relation(
        entity = ActivityActorDisplayEntity::class,
        parentColumn = "id",
        entityColumn = "activityId",
    )
    val actorDisplays: List<ActivityActorDisplayWithImage>,
    @Relation(
        entity = NonComplianceReasonDisplayEntity::class,
        parentColumn = "id",
        entityColumn = "activityId",
    )
    val nonComplianceReasonDisplays: List<NonComplianceReasonDisplayEntity>,
    @Relation(
        entity = ActivityClaimEntity::class,
        parentColumn = "id",
        entityColumn = "activityId"
    )
    val claims: List<ActivityClaimEntity>,
)
