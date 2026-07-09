package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import ch.admin.foitt.openid4vc.domain.model.TokenType

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
        Index(value = ["credentialId"], unique = true),
    ]
)
@TypeConverters(CredentialAuthenticationEntity.Converters::class)
data class CredentialAuthenticationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val credentialId: Long,
    val tokenType: TokenType,
    val accessToken: String,
    val refreshToken: String? = null,
) {

    class Converters {
        @TypeConverter
        fun toTokenType(value: String): TokenType = TokenType.valueOf(value)

        @TypeConverter
        fun fromTokenType(value: TokenType): String = value.name
    }
}
