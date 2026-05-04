package ch.admin.foitt.wallet.platform.database.migrations

import androidx.room.util.useCursor
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.util.getLongColumn
import ch.admin.foitt.wallet.platform.database.util.getStringColumn
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.IOException

class Migration18To19 : BaseDBMigrationTest() {

    @Test
    @Throws(IOException::class)
    fun migrate18To19() {
        // create db with schema version 18
        var db = helper.createDatabase(testDbName, 18)

        val expectedCredentialId = 1L
        val expectedFormat = CredentialFormat.VC_SD_JWT
        val expectedIssuer = "https://issuer-agent.domain.ch"
        val expectedSelectedConfigurationId = "configId"
        val expectedCreatedAt = 1000L

        // insert a credential
        db.execSQL(
            "INSERT INTO `Credential` (`id`,`format`,`issuerUrl`,`selectedConfigurationId`,`createdAt`) " +
                "VALUES (" +
                "$expectedCredentialId," +
                "'${Credential.Converters().fromCredentialFormat(expectedFormat)}'," +
                "'$expectedIssuer'," +
                "'$expectedSelectedConfigurationId'," +
                "$expectedCreatedAt" +
                ")"
        )

        val expectedActivityId = 1L
        val expectedType = "ISSUANCE"
        val expectedActorTrust = "TRUSTED"
        val expectedVcSchemaTrust = "TRUSTED"
        val expectedNonComplianceData = "nonCompliance data"
        val expectedActivityCreatedAt = 1234L

        // insert a credentialActivity
        db.execSQL(
            "INSERT INTO `CredentialActivityEntity` (`id`,`credentialId`,`type`,`actorTrust`,`vcSchemaTrust`,`nonComplianceData`,`createdAt`) " +
                "VALUES (" +
                "$expectedActivityId,"+
                "$expectedCredentialId," +
                "'$expectedType'," +
                "'$expectedActorTrust'," +
                "'$expectedVcSchemaTrust'," +
                "'$expectedNonComplianceData'," +
                "$expectedActivityCreatedAt" +
                ")"
        )

        db.close()

        // re-open db with version 19
        db = helper.runMigrationsAndValidate(testDbName, 19, true)

        val expectedActorCompliance = "UNKNOWN"

        // manually validate migrated credentialActivity
        db.query("SELECT * FROM `CredentialActivityEntity`").useCursor { cursor ->
            cursor.moveToFirst()

            assertEquals(expectedActivityId, cursor.getLongColumn("id"))
            assertEquals(expectedCredentialId, cursor.getLongColumn("credentialId"))
            assertEquals(expectedType, cursor.getStringColumn("type"))
            assertEquals(expectedActorTrust, cursor.getStringColumn("actorTrust"))
            assertEquals(expectedVcSchemaTrust, cursor.getStringColumn("vcSchemaTrust"))
            assertEquals(expectedActorCompliance, cursor.getStringColumn("actorCompliance"))
            assertEquals(expectedNonComplianceData, cursor.getStringColumn("nonComplianceData"))
            assertEquals(expectedActivityCreatedAt, cursor.getLongColumn("createdAt"))
        }

        db.query("SELECT * FROM `NonComplianceReasonDisplayEntity`").useCursor { cursor ->
            // make sure table is empty
            assertEquals(0, cursor.count)
            // make sure table has the 4 columns we want
            assertEquals(4, cursor.columnCount)
        }

        db.close()
    }
}
