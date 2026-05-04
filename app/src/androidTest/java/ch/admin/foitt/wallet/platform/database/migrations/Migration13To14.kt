package ch.admin.foitt.wallet.platform.database.migrations

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.util.getBooleanColumn
import ch.admin.foitt.wallet.platform.database.util.getStringOrNullColumn
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull

class Migration13To14 : BaseDBMigrationTest() {

    /** changes:
     * - add new field `val selectedConfigurationId: String? = null` to [ch.admin.foitt.wallet.platform.database.domain.model.Credential]
     * - add new field `@ColumnInfo(defaultValue = "false") val isSensitive: Boolean = false` to [ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim]
     */
    @Test
    fun migrate13to14() {
        var db = helper.createDatabase(testDbName, 13)

        // insert a Credential
        val expectedCredentialId = 1L
        val expectedFormat = CredentialFormat.VC_SD_JWT
        val expectedCreatedAt = 1000L

        // add Credential
        db.execSQL(
            "INSERT INTO `Credential` (`id`, `format`, `createdAt`) " +
                "VALUES (" +
                "$expectedCredentialId," +
                "'${Credential.Converters().fromCredentialFormat(expectedFormat)}'," +
                "$expectedCreatedAt" +
                ")"
        )

        // add CredentialClaim
        val expectedClaimId = 2L
        val expectedClusterId = 2L
        val expectedKey = "key"
        val expectedValue = "value"
        val expectedValueType = "valueType"
        val expectedValueDisplayInfo = "valueDisplayInfo"
        val expectedOrder = -1
        db.execSQL(
            "INSERT INTO `CredentialClaim` (`id`, `clusterId`, `key`, `value`, `valueType`, `valueDisplayInfo`, `order`) " +
                "VALUES (" +
                "$expectedClaimId, " +
                "$expectedClusterId, " +
                "'$expectedKey', " +
                "'$expectedValue', " +
                "'$expectedValueType', " +
                "'$expectedValueDisplayInfo', " +
                "$expectedOrder" +
                ")"
        )

        db.close()

        // re-open db with version 14
        db = helper.runMigrationsAndValidate(testDbName, 14, true)

        // Validate that Credential has new field with default value null
        val credentialCursor = db.query("SELECT * FROM `Credential`")
        credentialCursor.moveToFirst()
        assertNull(credentialCursor.getStringOrNullColumn("selectedConfigurationId"))

        // Validate that CredentialClaim has new field with default value false
        val claimCursor = db.query("SELECT * FROM `CredentialClaim`")
        claimCursor.moveToFirst()
        assertFalse(claimCursor.getBooleanColumn("isSensitive"))

        db.close()
    }
}
