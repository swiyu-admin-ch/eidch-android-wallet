package ch.admin.foitt.wallet.platform.database.data

import android.content.Context
import androidx.room.Room
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
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import dagger.hilt.android.qualifiers.ApplicationContext
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SqlCipherDatabaseInitializer @Inject constructor(
    @param:ApplicationContext private val appContext: Context
) : DatabaseInitializer {

    override fun create(password: ByteArray): Result<AppDatabase, DatabaseError.SetupFailed> =
        runSuspendCatching {
            Room.databaseBuilder(appContext, AppDatabase::class.java, DATABASE_NAME)
                .openHelperFactory(SupportOpenHelperFactory(password))
                // see also auto migrations in AppDatabase
                .addMigrations(
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
                .build()
        }.mapError { throwable ->
            DatabaseError.SetupFailed(throwable)
        }

    companion object {
        private const val DATABASE_NAME = "app_database.db"

        init {
            System.loadLibrary("sqlcipher")
        }
    }
}
