package ch.admin.foitt.wallet.platform.database.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val Migration26to27 = object : Migration(26, 27) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = OFF")

        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `CredentialAuthenticationEntity` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `credentialId` INTEGER NOT NULL,
                    `tokenType` TEXT NOT NULL,
                    `accessToken` TEXT NOT NULL,
                    `refreshToken` TEXT,
                    FOREIGN KEY(`credentialId`) REFERENCES `Credential`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
                )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_CredentialAuthenticationEntity_credentialId` " +
                "ON `CredentialAuthenticationEntity` (`credentialId`)"
        )

        db.execSQL(
            """
                INSERT INTO `CredentialAuthenticationEntity` (`credentialId`, `tokenType`, `accessToken`, `refreshToken`)
                SELECT `credentialId`, 'BEARER', `accessToken`, `refreshToken`
                FROM `DeferredCredentialEntity`
            """.trimIndent()
        )

        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `DpopBindingEntity` (
                    `id` TEXT NOT NULL,
                    `credentialAuthenticationId` INTEGER NOT NULL,
                    `algorithm` TEXT NOT NULL,
                    `bindingType` TEXT NOT NULL,
                    `publicKey` BLOB,
                    `privateKey` BLOB,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`credentialAuthenticationId`) REFERENCES `CredentialAuthenticationEntity`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
                )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_DpopBindingEntity_credentialAuthenticationId` " +
                "ON `DpopBindingEntity` (`credentialAuthenticationId`)"
        )

        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `DeferredCredentialEntity_new` (
                    `credentialId` INTEGER NOT NULL,
                    `progressionState` TEXT NOT NULL,
                    `transactionId` TEXT NOT NULL,
                    `endpoint` TEXT NOT NULL,
                    `pollInterval` INTEGER NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `polledAt` INTEGER,
                    PRIMARY KEY(`credentialId`),
                    FOREIGN KEY(`credentialId`) REFERENCES `Credential`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
                )
            """.trimIndent()
        )
        db.execSQL(
            """
                INSERT INTO `DeferredCredentialEntity_new` (
                    `credentialId`,
                    `progressionState`,
                    `transactionId`,
                    `endpoint`,
                    `pollInterval`,
                    `createdAt`,
                    `polledAt`
                )
                SELECT
                    `credentialId`,
                    `progressionState`,
                    `transactionId`,
                    `endpoint`,
                    `pollInterval`,
                    `createdAt`,
                    `polledAt`
                FROM `DeferredCredentialEntity`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `DeferredCredentialEntity`")
        db.execSQL("ALTER TABLE `DeferredCredentialEntity_new` RENAME TO `DeferredCredentialEntity`")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_DeferredCredentialEntity_credentialId` " +
                "ON `DeferredCredentialEntity` (`credentialId`)"
        )

        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `BatchRefreshDataEntity_new` (
                    `credentialId` INTEGER NOT NULL,
                    `batchSize` INTEGER NOT NULL,
                    PRIMARY KEY(`credentialId`)
                )
            """.trimIndent()
        )
        db.execSQL(
            """
                INSERT INTO `BatchRefreshDataEntity_new` (`credentialId`, `batchSize`)
                SELECT `credentialId`, `batchSize`
                FROM `BatchRefreshDataEntity`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `BatchRefreshDataEntity`")
        db.execSQL("ALTER TABLE `BatchRefreshDataEntity_new` RENAME TO `BatchRefreshDataEntity`")

        db.execSQL("PRAGMA foreign_key_check")
        db.execSQL("PRAGMA foreign_keys = ON")
    }
}
