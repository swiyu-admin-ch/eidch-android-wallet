package ch.admin.foitt.wallet.platform.database.migrations

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration16to17
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.database.util.getBlobOrNullColumn
import ch.admin.foitt.wallet.platform.database.util.getIntColumn
import ch.admin.foitt.wallet.platform.database.util.getLongColumn
import ch.admin.foitt.wallet.platform.database.util.getLongOrNullColumn
import ch.admin.foitt.wallet.platform.database.util.getStringColumn
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.IOException

class Migration16To17 : BaseDBMigrationTest() {

    @Test
    @Throws(IOException::class)
    fun migrate16to17() {
        // create db with schema version 16
        var db = helper.createDatabase(testDbName, 16)

        val expectedCredentialId = 1L
        val expectedFormat = CredentialFormat.VC_SD_JWT
        val expectedCreatedAt = 1000L
        val expectedUpdatedAt = 2000L
        val expectedPayload = "payload-16"
        val expectedIssuer = "issuer-16"
        val expectedIssuerUrl = "https://issuer-16.example"
        val expectedStatus = CredentialStatus.VALID
        val expectedValidFrom = 111L
        val expectedValidUntil = 222L

        // Insert a Credential (required FK for others)
        db.execSQL(
            "INSERT INTO `Credential` (`id`, `format`, `issuerUrl`, `createdAt`) VALUES (" +
                "$expectedCredentialId, '" +
                Credential.Converters().fromCredentialFormat(expectedFormat) + "', '" +
                expectedIssuerUrl + "', " +
                "$expectedCreatedAt)"
        )

        // Insert a VerifiableCredentialEntity row that should be copied to BundleItemEntity
        db.execSQL(
            "INSERT INTO `VerifiableCredentialEntity` (" +
                "`credentialId`,`progressionState`,`status`,`payload`,`issuer`,`validFrom`,`validUntil`,`createdAt`,`updatedAt`) " +
                "VALUES (" +
                "$expectedCredentialId, '" + VerifiableProgressionState.ACCEPTED.name + "', '" +
                expectedStatus.name + "', '" + expectedPayload + "', '" + expectedIssuer + "', " +
                "$expectedValidFrom, $expectedValidUntil, $expectedCreatedAt, $expectedUpdatedAt)"
        )

        // Insert a CredentialKeyBindingEntity row that should gain a nullable bundleItemId after migration
        val keyBindingId = "kb-1"
        val keyBindingAlgorithm = "EdDSA"
        db.execSQL(
            "INSERT INTO `CredentialKeyBindingEntity` (" +
                "`id`, `credentialId`, `algorithm`, `bindingType`, `publicKey`, `privateKey`) VALUES (" +
                "'" + keyBindingId + "', $expectedCredentialId, '" + keyBindingAlgorithm + "', '" +
                KeyBindingType.HARDWARE.name + "', NULL, NULL)"
        )

        db.close()

        // Run migration 16 -> 17
        db = helper.runMigrationsAndValidate(testDbName, 17, true, Migration16to17)

        // Validate BundleItemEntity has an entry copied from VerifiableCredentialEntity
        val bundleCursor = db.query("SELECT * FROM `BundleItemEntity`")
        assertEquals(1, bundleCursor.count)
        bundleCursor.moveToFirst()
        // presented default should be 0/false
        assertEquals(0, bundleCursor.getIntColumn("presented"))
        assertEquals(expectedStatus.name, bundleCursor.getStringColumn("status"))
        assertEquals(expectedCredentialId, bundleCursor.getLongColumn("credentialId"))
        assertEquals(expectedPayload, bundleCursor.getStringColumn("payload"))

        // Validate BatchRefreshDataEntity table exists (should be empty)
        val batchCursor = db.query("SELECT * FROM `BatchRefreshDataEntity`")
        assertEquals(0, batchCursor.count)

        // Validate CredentialKeyBindingEntity structure and data preserved, bundleItemId is bundleItemEntity.id
        val kbCursor = db.query("SELECT * FROM `CredentialKeyBindingEntity`")
        assertEquals(1, kbCursor.count)
        kbCursor.moveToFirst()
        assertEquals(keyBindingId, kbCursor.getStringColumn("id"))
        assertEquals(expectedCredentialId, kbCursor.getLongColumn("credentialId"))
        assertEquals(keyBindingAlgorithm, kbCursor.getStringColumn("algorithm"))
        assertEquals(KeyBindingType.HARDWARE.name, kbCursor.getStringColumn("bindingType"))
        assertNull(kbCursor.getBlobOrNullColumn("publicKey"))
        assertNull(kbCursor.getBlobOrNullColumn("privateKey"))
        assertTrue(kbCursor.getColumnIndex("bundleItemId") >= 0)
        assertTrue(kbCursor.getLongOrNullColumn("bundleItemId") == bundleCursor.getLongOrNullColumn("id"))

        db.close()
    }
}
