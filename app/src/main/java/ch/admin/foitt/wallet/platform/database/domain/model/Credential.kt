package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import ch.admin.foitt.openid4vc.domain.model.HttpsURLAsStringSerializer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import kotlinx.serialization.Serializable
import java.net.URL
import java.time.Instant

@Entity
@TypeConverters(Credential.Converters::class)
data class Credential(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val format: CredentialFormat,
    @Serializable(HttpsURLAsStringSerializer::class)
    val issuerUrl: URL,
    val createdAt: Long = Instant.now().epochSecond,
    val selectedConfigurationId: String? = null,
) {
    class Converters {
        @TypeConverter
        fun toCredentialFormat(value: String) = CredentialFormat.entries.find { it.name == value } ?: CredentialFormat.UNKNOWN

        @TypeConverter
        fun fromCredentialFormat(value: CredentialFormat) = value.name

        @TypeConverter
        fun toURL(url: String) = URL(url)

        @TypeConverter
        fun fromURL(url: URL) = url.toString()
    }
}
