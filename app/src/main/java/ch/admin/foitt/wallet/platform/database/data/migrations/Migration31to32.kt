package ch.admin.foitt.wallet.platform.database.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val Migration31to32 = object : Migration(31, 32) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `VerifiableCredentialEntity` ADD COLUMN `lastRefreshAttemptAt` INTEGER")
    }
}
