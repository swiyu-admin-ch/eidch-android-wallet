package ch.admin.foitt.wallet.platform.database.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// DB schema v6.7 to v6.6
internal val Migration21to22 = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Rename "key" column to "path"
        db.execSQL(
            "ALTER TABLE `CredentialClaim` RENAME COLUMN `key` to `path`"
        )

        // Migrate the data
        db.execSQL(
            "UPDATE `CredentialClaim` SET path = '[\"' || path || '\"]'"
        )
    }
}
