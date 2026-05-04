package ch.admin.foitt.wallet.platform.database.migrations

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration11to12
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration14to15
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration15to16
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration16to17
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration19to20
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration1to2
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration21to22
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration2to3
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration5to6
import ch.admin.foitt.wallet.platform.database.data.migrations.Migration6to7
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.IOException

class MigrateAllTest : BaseDBMigrationTest() {

    private val manualMigrations =
        arrayOf(
            Migration1to2,
            Migration2to3,
            Migration5to6,
            Migration6to7,
            Migration11to12,
            Migration14to15,
            Migration15to16,
            Migration16to17,
            Migration19to20,
            Migration21to22,
        )

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Create earliest version of the database.
        helper.createDatabase(testDbName, 1).apply {
            close()
        }

        // Open latest version of the database. Room validates the schema
        // once all migrations execute.
        val db = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
            testDbName
        ).addMigrations(*manualMigrations).build().apply {
            openHelper.writableDatabase.close()
        }

        // Assert it tested the latest version
        assertEquals(AppDatabase.DATABASE_VERSION, db.openHelper.readableDatabase.version)
    }
}
