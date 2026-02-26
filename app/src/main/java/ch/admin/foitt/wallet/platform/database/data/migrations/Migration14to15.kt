package ch.admin.foitt.wallet.platform.database.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val Migration14to15 = object : Migration(startVersion = 14, endVersion = 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // All existing credentials are considered accepted
        db.execSQL(
            """
                UPDATE VerifiableCredentialEntity SET progressionState = 'ACCEPTED'
            """.trimIndent()
        )
    }
}
