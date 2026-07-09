package ch.admin.foitt.wallet.platform.database.migrations

import androidx.room.util.useCursor
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration26to27
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.openid4vc.domain.model.TokenType
import ch.admin.foitt.wallet.platform.database.util.getIntColumn
import ch.admin.foitt.wallet.platform.database.util.getLongColumn
import ch.admin.foitt.wallet.platform.database.util.getStringColumn
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.IOException

class Migration26To27 : BaseDBMigrationTest() {

    @Test
    @Throws(IOException::class)
    fun migrate26to27() {
        var db = helper.createDatabase(testDbName, 26)

        val deferredCredentialId = 1L
        val createdAt = 1000L
        val credentialFormat = CredentialFormat.VC_SD_JWT
        val issuerUrl = "https://issuer.example"
        val selectedConfigurationId = "config-id"

        db.execSQL(
            "INSERT INTO `Credential` (`id`,`format`,`issuerUrl`,`createdAt`,`selectedConfigurationId`) VALUES " +
                "($deferredCredentialId,'${Credential.Converters().fromCredentialFormat(credentialFormat)}','$issuerUrl',$createdAt,'$selectedConfigurationId')"
        )

        val deferredAccessToken = "deferred-access-token"
        val deferredRefreshToken = "deferred-refresh-token"
        val deferredTransactionId = "transaction-id"
        val deferredEndpoint = "https://issuer.example/deferred"
        val deferredPolledAt = 1001L

        db.execSQL(
            "INSERT INTO `DeferredCredentialEntity` " +
                "(`credentialId`,`progressionState`,`transactionId`,`accessToken`,`refreshToken`,`endpoint`,`pollInterval`,`createdAt`,`polledAt`) VALUES (" +
                "$deferredCredentialId," +
                "'${DeferredProgressionState.IN_PROGRESS.name}'," +
                "'$deferredTransactionId'," +
                "'$deferredAccessToken'," +
                "'$deferredRefreshToken'," +
                "'$deferredEndpoint'," +
                "5," +
                "$createdAt," +
                "$deferredPolledAt" +
                ")"
        )

        db.close()

        db = helper.runMigrationsAndValidate(testDbName, 27, true, Migration26to27)

        db.query(
            "SELECT `credentialId`,`tokenType`,`accessToken`,`refreshToken` FROM `CredentialAuthenticationEntity` ORDER BY `credentialId`"
        ).useCursor { cursor ->
            assertEquals(1, cursor.count)

            cursor.moveToFirst()
            assertEquals(deferredCredentialId, cursor.getLongColumn("credentialId"))
            assertEquals(TokenType.BEARER.name, cursor.getStringColumn("tokenType"))
            assertEquals(deferredAccessToken, cursor.getStringColumn("accessToken"))
            assertEquals(deferredRefreshToken, cursor.getStringColumn("refreshToken"))
        }

        db.query("SELECT * FROM `DeferredCredentialEntity`").useCursor { cursor ->
            assertEquals(1, cursor.count)
            cursor.moveToFirst()

            assertEquals(deferredCredentialId, cursor.getLongColumn("credentialId"))
            assertEquals(DeferredProgressionState.IN_PROGRESS.name, cursor.getStringColumn("progressionState"))
            assertEquals(deferredTransactionId, cursor.getStringColumn("transactionId"))
            assertEquals(deferredEndpoint, cursor.getStringColumn("endpoint"))
            assertEquals(5, cursor.getIntColumn("pollInterval"))
            assertEquals(createdAt, cursor.getLongColumn("createdAt"))
            assertEquals(deferredPolledAt, cursor.getLongColumn("polledAt"))
            assertTrue(cursor.getColumnIndex("accessToken") < 0)
            assertTrue(cursor.getColumnIndex("refreshToken") < 0)
        }

        db.close()
    }
}
