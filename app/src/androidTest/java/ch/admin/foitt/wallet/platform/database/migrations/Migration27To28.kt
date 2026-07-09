package ch.admin.foitt.wallet.platform.database.migrations

import androidx.room.util.useCursor
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration27to28
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.util.getStringColumn
import ch.admin.foitt.wallet.platform.database.util.getStringOrNullColumn
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.IOException

class Migration27To28 : BaseDBMigrationTest() {

    @Test
    @Throws(IOException::class)
    fun migrate27To28() {
        // create db with schema version 27
        var db = helper.createDatabase(testDbName, 27)

        val credentialId = 1L
        val credentialFormat = CredentialFormat.VC_SD_JWT
        val issuerUrl = "https://example.org"
        val selectedConfigurationId = "id"
        val createdAt = 1000L

        db.execSQL(
            "INSERT INTO `Credential` (`id`,`format`,`issuerUrl`,`selectedConfigurationId`,`createdAt`) VALUES " +
                "(" +
                "$credentialId," +
                "'${Credential.Converters().fromCredentialFormat(credentialFormat)}'," +
                "'$issuerUrl'," +
                "'$selectedConfigurationId'," +
                "$createdAt" +
                ")"
        )

        val displayId = 1L
        val locale = "locale"
        val description = "Test {{$.path}}"
        val displayId2 = 2L
        val description2 = "Test"
        val displayId3 = 3L
        val description3 = null

        db.execSQL(
            "INSERT INTO `CredentialDisplay` (`id`,`credentialId`,`locale`,`description`) VALUES " +
                "($displayId,$credentialId,'$locale','$description')," +
                "($displayId2,$credentialId,'$locale','$description2')," +
                "($displayId3,$credentialId,'$locale',NULL)"
        )

        db.close()

        db = helper.runMigrationsAndValidate(testDbName, 28, true, Migration27to28)

        db.query("SELECT `description` FROM `CredentialDisplay`").useCursor { cursor ->
            cursor.moveToFirst()
            val expectedPath = listOf(ClaimsPathPointerComponent.String("path"))
            val expected = "Test {{${expectedPath.toPointerString()}}}"
            assertEquals(expected, cursor.getStringColumn("description"))

            cursor.moveToNext()
            assertEquals(description2, cursor.getStringColumn("description"))

            cursor.moveToNext()
            assertEquals(null, cursor.getStringOrNullColumn("description"))

        }
    }
}
