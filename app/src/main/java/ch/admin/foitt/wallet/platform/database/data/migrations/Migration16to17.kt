package ch.admin.foitt.wallet.platform.database.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// DB schema v6.4 to v6.5
internal val Migration16to17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = OFF")
        // Add a new BundleItemEntity table
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `BundleItemEntity` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`presented` INTEGER NOT NULL DEFAULT 0, " +
                "`status` TEXT NOT NULL DEFAULT 'UNKNOWN', " +
                "`credentialId` INTEGER NOT NULL, " +
                "`payload` TEXT NOT NULL, " +
                "FOREIGN KEY(`credentialId`) REFERENCES VerifiableCredentialEntity(`credentialId`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ")"
        )

        db.execSQL(
            "INSERT INTO BundleItemEntity (" +
                "`status`, `credentialId`, `payload`)" +
                "SELECT status, credentialId, payload " +
                "FROM VerifiableCredentialEntity "
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_BundleItemEntity_credentialId` ON `BundleItemEntity` (`credentialId`)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `BatchRefreshDataEntity` (" +
                "`credentialId` INTEGER PRIMARY KEY NOT NULL, " +
                "`batchSize` INTEGER NOT NULL, " +
                "`refreshToken` TEXT NOT NULL" +
                ")"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `CredentialKeyBindingEntity_new` (" +
                "`id` TEXT NOT NULL, " +
                "`credentialId` INTEGER NOT NULL, " +
                "`bundleItemId` INTEGER, " +
                "`algorithm` TEXT NOT NULL, " +
                "`bindingType` TEXT NOT NULL, " +
                "`publicKey` BLOB, " +
                "`privateKey` BLOB, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`credentialId`) REFERENCES Credential(`id`) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "FOREIGN KEY(`bundleItemId`) REFERENCES BundleItemEntity(`id`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ")"
        )

        db.execSQL(
            "INSERT INTO `CredentialKeyBindingEntity_new` (" +
                "`id`, `credentialId`, `algorithm`, `bindingType`, `publicKey`, `privateKey`, `bundleItemId`) " +
                "SELECT ckb.`id`, ckb.`credentialId`, ckb.`algorithm`, ckb.`bindingType`, ckb.`publicKey`, ckb.`privateKey`, " +
                "(SELECT bie.`id` FROM BundleItemEntity AS bie WHERE bie.`credentialId` = ckb.`credentialId` LIMIT 1) " +
                "AS `bundleItemId` " +
                "FROM `CredentialKeyBindingEntity` AS ckb"
        )

        db.execSQL("DROP TABLE IF EXISTS `CredentialKeyBindingEntity`")
        db.execSQL(
            "ALTER TABLE `CredentialKeyBindingEntity_new` RENAME TO `CredentialKeyBindingEntity`"
        )

        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_CredentialKeyBindingEntity_credentialId` ON `CredentialKeyBindingEntity` (`credentialId`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_CredentialKeyBindingEntity_bundleItemId` ON `CredentialKeyBindingEntity` (`bundleItemId`)"
        )

        db.execSQL("PRAGMA foreign_key_check")
        db.execSQL("PRAGMA foreign_keys = ON")
    }
}
