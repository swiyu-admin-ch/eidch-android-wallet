package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.admin.foitt.openid4vc.domain.model.BatchSize

@Entity
data class BatchRefreshDataEntity(
    @PrimaryKey
    val credentialId: Long,
    val batchSize: BatchSize,
    val refreshToken: String
)
