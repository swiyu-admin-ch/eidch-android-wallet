package ch.admin.foitt.wallet.platform.database.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt
import ch.admin.foitt.wallet.platform.database.domain.model.Credential

// DB schema v3.1 to v3.3
internal val Migration1to2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Credential ADD COLUMN validFrom INTEGER")
        db.execSQL("ALTER TABLE Credential ADD COLUMN validUntil INTEGER")

        val cursor = db.query("SELECT id, format, payload FROM CREDENTIAL")
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val payload = cursor.getString(cursor.getColumnIndexOrThrow("payload"))
            val formatString = cursor.getString(cursor.getColumnIndexOrThrow("format"))
            val format = Credential.Converters().toCredentialFormat(formatString)

            val (validFrom, validUntil) = when (format) {
                CredentialFormat.VC_SD_JWT -> {
                    val sdJwt = SdJwt(payload)
                    Pair(sdJwt.nbfInstant?.epochSecond, sdJwt.expInstant?.epochSecond)
                }

                else -> error("invalid format")
            }

            db.execSQL("UPDATE Credential SET validFrom = $validFrom, validUntil = $validUntil WHERE id = $id")
        }

        cursor.close()
    }
}
