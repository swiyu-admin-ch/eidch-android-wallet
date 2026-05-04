package ch.admin.foitt.wallet.platform.database.migrations

import ch.admin.foitt.wallet.platform.database.data.migrations.Migration2to3
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState
import ch.admin.foitt.wallet.platform.database.util.getLongColumn
import ch.admin.foitt.wallet.platform.database.util.getLongOrNullColumn
import ch.admin.foitt.wallet.platform.database.util.getStringColumn
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.LegalRepresentativeConsent
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.IOException

class Migration2To3Test : BaseDBMigrationTest() {

    @Test
    @Throws(IOException::class)
    fun migrate2to3() {
        val eIdRequestStateTable = "EIdRequestState"
        val defaultConsentState = LegalRepresentativeConsent.NOT_REQUIRED

        var db = helper.createDatabase(testDbName, 2)

        val states = listOf(
            EIdRequestState(
                id = 42L,
                eIdRequestCaseId = "CaseId42",
                state = EIdRequestQueueState.IN_QUEUING,
                lastPolled = 2L,
                onlineSessionStartOpenAt = 3L,
                onlineSessionStartTimeoutAt = 4L,
                legalRepresentativeConsent = defaultConsentState,
            ),
            EIdRequestState(
                id = 43L,
                eIdRequestCaseId = "CaseId43",
                state = EIdRequestQueueState.READY_FOR_ONLINE_SESSION,
                lastPolled = 6L,
                onlineSessionStartOpenAt = null,
                onlineSessionStartTimeoutAt = 8L,
                legalRepresentativeConsent = defaultConsentState,
            ),
            EIdRequestState(
                id = 44L,
                eIdRequestCaseId = "",
                state = EIdRequestQueueState.IN_TARGET_WALLET_PAIRING,
                lastPolled = 0L,
                onlineSessionStartOpenAt = 7L,
                onlineSessionStartTimeoutAt = null,
                legalRepresentativeConsent = defaultConsentState,
            ),
        )

        for (requestState in states) {
            db.execSQL(
                "INSERT INTO `$eIdRequestStateTable` (`id`,`eIdRequestCaseId`,`state`,`lastPolled`,`onlineSessionStartOpenAt`,`onlineSessionStartTimeoutAt`) " +
                    "VALUES ('${requestState.id}','${requestState.eIdRequestCaseId}','${requestState.state}',${requestState.lastPolled},${requestState.onlineSessionStartOpenAt},${requestState.onlineSessionStartTimeoutAt})"
            )
        }
        db.close()

        // re-open db with version 3 and provide MIGRATION_2_3 as migration path
        db = helper.runMigrationsAndValidate(testDbName, 3, true, Migration2to3)

        // manually validate migrated states
        val cursor = db.query("SELECT * FROM `$eIdRequestStateTable`")

        for (requestState in states) {
            cursor.moveToNext()

            assertEquals(requestState.id, cursor.getLongColumn("id"))
            assertEquals(requestState.eIdRequestCaseId, cursor.getStringColumn("eIdRequestCaseId"))
            assertEquals(requestState.state.name, cursor.getStringColumn("state"))
            assertEquals(requestState.lastPolled, cursor.getLongColumn("lastPolled"))
            assertEquals(requestState.onlineSessionStartOpenAt, cursor.getLongOrNullColumn("onlineSessionStartOpenAt"))
            assertEquals(requestState.onlineSessionStartTimeoutAt, cursor.getLongOrNullColumn("onlineSessionStartTimeoutAt"))
            assertEquals(defaultConsentState.name, cursor.getStringColumn("legalRepresentativeConsent"))
        }
    }
}
