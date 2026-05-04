package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class ActivityWithActorDisplays(
    @Embedded val activity: CredentialActivityEntity,
    @Relation(
        entity = ActivityActorDisplayEntity::class,
        parentColumn = "id",
        entityColumn = "activityId",
    )
    val actorDisplays: List<ActivityActorDisplayWithImage>,
)
