package ch.admin.foitt.wallet.platform.database.migrations

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import org.junit.Rule

abstract class BaseDBMigrationTest {
    val testDbName = "swiyu-migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )
}
