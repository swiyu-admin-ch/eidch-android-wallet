package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.IdentityType
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
data class EIdRequestCase(
    @PrimaryKey
    val id: String, // caseId from the api
    @ColumnInfo(defaultValue = "NULL")
    val credentialId: Long? = null, // Foreign key
    val rawMrz: String,
    val documentNumber: String,
    @ColumnInfo(defaultValue = "SWISS_IDK")
    val selectedDocumentType: IdentityType,
    val firstName: String,
    val lastName: String,
    @ColumnInfo(defaultValue = "false")
    val filesSubmitted: Boolean = false,
    val createdAt: Long = Instant.now().epochSecond,
    @ColumnInfo(defaultValue = "NULL")
    val pushId: String? = null,
)
