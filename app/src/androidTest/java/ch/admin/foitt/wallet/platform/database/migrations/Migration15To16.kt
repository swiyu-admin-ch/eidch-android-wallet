package ch.admin.foitt.wallet.platform.database.migrations

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration15to16
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.util.getLongColumn
import ch.admin.foitt.wallet.platform.database.util.getStringColumn
import ch.admin.foitt.wallet.platform.utils.compress
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.IOException

class Migration15To16 : BaseDBMigrationTest() {

    @Test
    @Throws(IOException::class)
    fun migrate15To16() {
        // create db with schema version 15
        var db = helper.createDatabase(testDbName, 15)

        val expectedCredentialId = 1L
        val expectedFormat = CredentialFormat.VC_SD_JWT
        val expectedIssuer = "https://issuer-agent.domain.ch"
        val expectedSelectedConfigurationId = "configId"
        val expectedCreatedAt = 1000L
        val expectedRawCredentialDataId = 1L

        // insert a credential
        db.execSQL(
            "INSERT INTO `Credential` (`id`,`format`,`selectedConfigurationId`,`createdAt`) " +
                "VALUES ($expectedCredentialId,'${Credential.Converters().fromCredentialFormat(expectedFormat)}','$expectedSelectedConfigurationId',$expectedCreatedAt)"
        )
        // insert raw credential data
        val minimalMetadata = """
            {
               "credential_endpoint":"https://issuer-agent.domain.ch/credential",
               "credential_issuer":"$expectedIssuer",
               "credential_configurations_supported":{
                  "elfa-sdjwt":{
                     "format":"vc+sd-jwt",
                     "credential_signing_alg_values_supported":[
                        "Ed25519VerificationKey2020"
                     ],
                     "vct":"elfa-sdjwt"
                  }
               }
            }
        """.trimIndent()
        val compressedMetadata = minimalMetadata.toByteArray().compress()
        val rawOcaBundle = byteArrayOf(0, 1)

        db.execSQL(
            "INSERT INTO `RawCredentialData` (`id`,`credentialId`,`rawOIDMetadata`,`rawOcaBundle`) " +
                "VALUES (?, ?, ?, ?)",
            arrayOf(
                expectedRawCredentialDataId,
                expectedCredentialId,
                compressedMetadata,
                rawOcaBundle,
            )
        )
        db.close()

        // re-open db with version 16 and provide MIGRATION_15_16 as migration path
        db = helper.runMigrationsAndValidate(testDbName, 16, true, Migration15to16)

        // manually validate migrated credential
        val cursor = db.query("SELECT * FROM `Credential`")
        cursor.moveToFirst()

        assertEquals(expectedCredentialId, cursor.getLongColumn("id"))
        val format = cursor.getStringColumn("format")
        assertEquals(expectedFormat, Credential.Converters().toCredentialFormat(format))
        assertEquals(expectedIssuer, cursor.getStringColumn("issuerUrl"))
        assertEquals(expectedSelectedConfigurationId, cursor.getStringColumn("selectedConfigurationId"))
        assertEquals(expectedCreatedAt, cursor.getLongColumn("createdAt"))

        db.close()
    }
}
