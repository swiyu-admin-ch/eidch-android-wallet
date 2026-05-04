package ch.admin.foitt.wallet.platform.database.migrations

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration14to15
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.database.util.getLongColumn
import ch.admin.foitt.wallet.platform.database.util.getStringColumn
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class Migration14To15 : BaseDBMigrationTest() {

    @Test
    fun migrate14to15() {
        var db = helper.createDatabase(testDbName, 14)

        val expectedCredentialId = 1L
        val expectedFormat = CredentialFormat.VC_SD_JWT
        val expectedCreatedAt = 1000L
        val expectedPayload = "payload"
        val expectedIssuer = "issuer"
        val expectedStatus = CredentialStatus.VALID
        val expectedValidFrom = 123456L
        val expectedValidUntil = 234567L
        val expectedUpdatedAt = 1000L

        val initialProgressionState = VerifiableProgressionState.UNACCEPTED
        val expectedProgressionState = VerifiableProgressionState.ACCEPTED

        // insert a Credential
        db.execSQL(
            "INSERT INTO `Credential` (`id`, `format`, `createdAt`) " +
                "VALUES (" +
                "$expectedCredentialId," +
                "'${Credential.Converters().fromCredentialFormat(expectedFormat)}'," +
                "$expectedCreatedAt" +
                ")"
        )

        // add Credential
        db.execSQL(
            "INSERT INTO `VerifiableCredentialEntity` (`credentialId`,`progressionState`,`status`,`payload`,`issuer`,`validFrom`,`validUntil`,`createdAt`,`updatedAt`) " +
                "VALUES (" +
                "$expectedCredentialId," +
                "'${initialProgressionState.name}'," +
                "'${expectedStatus.name}'," +
                "'$expectedPayload'," +
                "'$expectedIssuer'," +
                "$expectedValidFrom," +
                "$expectedValidUntil," +
                "$expectedCreatedAt," +
                "$expectedUpdatedAt" +
                ")"
        )

        db.close()

        // re-open with version 15
        db = helper.runMigrationsAndValidate(testDbName, 15, validateDroppedTables = true, Migration14to15)

        // Validate that VerifiableCredentialEntity progression state was updated
        val cursor = db.query("SELECT * FROM `VerifiableCredentialEntity`")
        cursor.moveToFirst()

        assertEquals(expectedCredentialId, cursor.getLongColumn("credentialId"))
        val progressionState = cursor.getStringColumn("progressionState")
        assertEquals(expectedProgressionState, VerifiableProgressionState.valueOf(progressionState))
        val status = cursor.getStringColumn("status")
        assertEquals(expectedStatus, CredentialStatus.valueOf(status))
        assertEquals(expectedPayload, cursor.getStringColumn("payload"))
        assertEquals(expectedIssuer, cursor.getStringColumn("issuer"))
        assertEquals(expectedValidFrom, cursor.getLongColumn("validFrom"))
        assertEquals(expectedValidUntil, cursor.getLongColumn("validUntil"))
        assertEquals(expectedCreatedAt, cursor.getLongColumn("createdAt"))
        assertEquals(expectedUpdatedAt, cursor.getLongColumn("updatedAt"))

        db.close()
    }
}
