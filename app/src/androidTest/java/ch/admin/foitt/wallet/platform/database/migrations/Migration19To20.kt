package ch.admin.foitt.wallet.platform.database.migrations

import ch.admin.foitt.wallet.platform.database.data.migrations.Migration19to20
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.database.util.getLongColumn
import ch.admin.foitt.wallet.platform.database.util.getStringColumn
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.IOException

class Migration19To20 : BaseDBMigrationTest() {

    @Test
    @Throws(IOException::class)
    fun migrate19to20() {
        // create db with schema version 19
        var db = helper.createDatabase(testDbName, 19)

        val expectedCredentialId = 1L
        val expectedCreatedAt = 1000L
        val expectedUpdatedAt = 2000L
        val expectedPayload = "payload-19"
        val expectedIssuer = "issuer-19"
        val expectedValidFrom = 111L
        val expectedValidUntil = 222L
        val expectedProgressionState = VerifiableProgressionState.ACCEPTED.name

        // Insert a VerifiableCredentialEntity row that should be copied to BundleItemEntity
        db.execSQL(
            "INSERT INTO `VerifiableCredentialEntity` (" +
                "`credentialId`,`progressionState`,`issuer`,`validFrom`,`validUntil`,`createdAt`,`updatedAt`) " +
                "VALUES (" +
                "$expectedCredentialId, '" + expectedProgressionState + "', '" +
                expectedIssuer + "', " + "$expectedValidFrom, $expectedValidUntil, $expectedCreatedAt, $expectedUpdatedAt)"
        )

        val expectedBundleItemId = 1L
        db.execSQL(
            "INSERT INTO `BundleItemEntity` (" +
                "`id`, `credentialId`,`payload`) " +
                "VALUES (" +
                "$expectedBundleItemId, $expectedCredentialId, '$expectedPayload')"
        )

        db.close()

        // Run migration 19 -> 20
        db = helper.runMigrationsAndValidate(testDbName, 20, true, Migration19to20)

        // Validate VerifiableCredentialEntity
        val credentialCursor = db.query("SELECT * FROM `VerifiableCredentialEntity`")
        assertEquals(1, credentialCursor.count)
        credentialCursor.moveToFirst()
        // presented default should be 0/false
        assertEquals(expectedCredentialId, credentialCursor.getLongColumn("credentialId"))
        assertEquals(expectedProgressionState, credentialCursor.getStringColumn("progressionState"))
        assertEquals(expectedIssuer, credentialCursor.getStringColumn("issuer"))
        assertEquals(expectedValidFrom, credentialCursor.getLongColumn("validFrom"))
        assertEquals(expectedValidUntil, credentialCursor.getLongColumn("validUntil"))
        assertEquals(expectedCreatedAt, credentialCursor.getLongColumn("createdAt"))
        assertEquals(expectedUpdatedAt, credentialCursor.getLongColumn("updatedAt"))
        assertEquals(expectedBundleItemId, credentialCursor.getLongColumn("nextPresentableBundleItemId"))

        db.close()
    }
}
