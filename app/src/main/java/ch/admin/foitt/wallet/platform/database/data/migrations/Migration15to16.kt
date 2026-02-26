package ch.admin.foitt.wallet.platform.database.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.wallet.platform.utils.decompress
import com.github.michaelbull.result.annotation.UnsafeResultErrorAccess
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import com.github.michaelbull.result.runCatching
import kotlinx.serialization.json.Json
import timber.log.Timber

// DB schema v6.1 to v6.2
internal val Migration15to16 = Migration15To16()

@OptIn(UnsafeResultErrorAccess::class, UnsafeResultValueAccess::class)
internal class Migration15To16 : Migration(15, 16) {
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    @Suppress("NestedBlockDepth")
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = OFF")
        // Add new credential table that contains the issuerUrl field (not null and without default -> reason why we re-create the table)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `new_Credential` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`format` TEXT NOT NULL, " +
                "`issuerUrl` TEXT NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, " +
                "`selectedConfigurationId` TEXT" +
                ")"
        )

        db.query("SELECT * FROM `Credential`").use { credentialCursor ->
            while (credentialCursor.moveToNext()) {
                val credentialId = credentialCursor.getLong(credentialCursor.getColumnIndexOrThrow("id"))
                val format = credentialCursor.getString(credentialCursor.getColumnIndexOrThrow("format"))
                val selectedConfigurationId = credentialCursor.getString(credentialCursor.getColumnIndexOrThrow("selectedConfigurationId"))
                val createdAt = credentialCursor.getLong(credentialCursor.getColumnIndexOrThrow("createdAt"))

                db.query("SELECT `rawOIDMetadata` FROM `RawCredentialData` WHERE `credentialId` = $credentialId").use { cursor ->
                    while (cursor.moveToNext()) {
                        val compressedRawOIDMetadata = cursor.getBlob(cursor.getColumnIndexOrThrow("rawOIDMetadata"))
                        val metadataResult = runCatching {
                            val rawOIDMetadata = compressedRawOIDMetadata.decompress().decodeToString()
                            json.decodeFromString<IssuerCredentialInfo>(rawOIDMetadata)
                        }
                        // skip this credential if error during metadata parsing
                        if (metadataResult.isErr) {
                            Timber.e(t = metadataResult.error, message = "Credential could not be migrated due to invalid metadata")
                            continue
                        }
                        val issuerUrl = metadataResult.value.credentialIssuer

                        db.execSQL(
                            "INSERT INTO `new_Credential` (`id`, `format`, `issuerUrl`, `selectedConfigurationId`, `createdAt`) " +
                                "VALUES ($credentialId, '$format', '$issuerUrl', '$selectedConfigurationId', $createdAt)"
                        )
                    }
                }
            }
        }

        db.execSQL("DROP TABLE `Credential`")
        db.execSQL("ALTER TABLE `new_Credential` RENAME TO `Credential`")
        db.execSQL("PRAGMA foreign_key_check")
        db.execSQL("PRAGMA foreign_keys = ON")
    }
}
