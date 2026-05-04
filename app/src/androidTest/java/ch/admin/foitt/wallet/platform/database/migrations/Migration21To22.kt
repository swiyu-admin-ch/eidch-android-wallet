package ch.admin.foitt.wallet.platform.database.migrations

import androidx.room.util.useCursor
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration21to22
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.database.util.getBooleanColumn
import ch.admin.foitt.wallet.platform.database.util.getIntColumn
import ch.admin.foitt.wallet.platform.database.util.getLongColumn
import ch.admin.foitt.wallet.platform.database.util.getStringColumn
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import java.io.IOException

class Migration21To22 : BaseDBMigrationTest() {

    @Test
    @Throws(IOException::class)
    fun migrate21To22() {
        // create db with schema version 21
        var db = helper.createDatabase(testDbName, 21)

        // insert a credential
        val expectedCredentialId = 1L
        val expectedFormat = CredentialFormat.VC_SD_JWT
        val expectedIssuerUrl = "https://issuer-agent.domain.ch"
        val expectedSelectedConfigurationId = "configId"
        val expectedCreatedAt = 1000L

        db.execSQL(
            "INSERT INTO `Credential` (`id`,`format`,`issuerUrl`,`selectedConfigurationId`,`createdAt`) " +
                "VALUES (" +
                "$expectedCredentialId," +
                "'${Credential.Converters().fromCredentialFormat(expectedFormat)}'," +
                "'$expectedIssuerUrl'," +
                "'$expectedSelectedConfigurationId'," +
                "$expectedCreatedAt" +
                ")"
        )

        // insert a verifiable credential
        val expectedProgressionState = VerifiableProgressionState.ACCEPTED
        val expectedIssuer = "issuer"
        val expectedValidFrom = 1000L
        val expectedValidUntil = 1001L
        val nextPresentableBundleItemId = 1L

        db.execSQL(
            "INSERT INTO `VerifiableCredentialEntity` (`credentialId`,`progressionState`,`issuer`,`validFrom`,`validUntil`,`createdAt`,`nextPresentableBundleItemId`) " +
                "VALUES (" +
                "$expectedCredentialId," +
                "'${expectedProgressionState.name}'," +
                "'$expectedIssuer'," +
                "$expectedValidFrom," +
                "$expectedValidUntil," +
                "$expectedCreatedAt," +
                "$nextPresentableBundleItemId" +
                ")"
        )

        // insert a cluster
        val expectedClusterId = 3L
        val expectedParentClusterId = null
        val expectedClusterOrder = 1L

        db.execSQL(
            "INSERT INTO `CredentialClaimClusterEntity` (`id`,`verifiableCredentialId`,`parentClusterId`,`order`) " +
                "VALUES (" +
                "$expectedClusterId," +
                "$expectedCredentialId," +
                "$expectedParentClusterId," +
                "$expectedClusterOrder" +
                ")"
        )

        val expectedClaimId = 1L
        val expectedKey = "key"
        val expectedValue = "value"
        val expectedValueType = "valueType"
        val expectedValueDisplayInfo = "valueDisplayInfo"
        val expectedClaimOrder = 1
        val expectedIsSensitive = 0

        db.execSQL(
            "INSERT INTO `CredentialClaim` (`id`,`clusterId`,`key`,`value`,`valueType`,`valueDisplayInfo`,`order`,`isSensitive`) " +
                "VALUES (" +
                "$expectedClaimId," +
                "$expectedClusterId," +
                "'$expectedKey'," +
                "'$expectedValue'," +
                "'$expectedValueType'," +
                "'$expectedValueDisplayInfo'," +
                "$expectedClaimOrder," +
                "$expectedIsSensitive" +
                ")"
        )

        db.close()

        // re-open db with version 22
        db = helper.runMigrationsAndValidate(testDbName, 22, true, Migration21to22)

        val expectedPath = "[\"$expectedKey\"]"

        // manually validate migrated claim
        db.query("SELECT * FROM `CredentialClaim`").useCursor { cursor ->
            cursor.moveToFirst()

            assertEquals(expectedClaimId, cursor.getLongColumn("id"))
            assertEquals(expectedClusterId, cursor.getLongColumn("clusterId"))
            assertEquals(expectedPath, cursor.getStringColumn("path"))
            assertEquals(expectedValue, cursor.getStringColumn("value"))
            assertEquals(expectedValueType, cursor.getStringColumn("valueType"))
            assertEquals(expectedValueDisplayInfo, cursor.getStringColumn("valueDisplayInfo"))
            assertEquals(expectedClaimOrder, cursor.getIntColumn("order"))
            assertFalse(cursor.getBooleanColumn("isSensitive"))
        }

        db.close()
    }
}
