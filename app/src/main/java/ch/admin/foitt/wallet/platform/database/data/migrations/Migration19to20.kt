package ch.admin.foitt.wallet.platform.database.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// DB schema v6.4 to v6.5
internal val Migration19to20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE VerifiableCredentialEntity ADD COLUMN nextPresentableBundleItemId INTEGER NOT NULL DEFAULT 0"
        )
        // This is fine, since batch issuance is disabled at the time of this migration, so any credential only has one bundle item
        db.execSQL(
            "UPDATE VerifiableCredentialEntity SET nextPresentableBundleItemId = " +
                "(SELECT id FROM BundleItemEntity WHERE credentialId = VerifiableCredentialEntity.credentialId LIMIT 1)"
        )
    }
}
