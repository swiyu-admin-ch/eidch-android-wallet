package ch.admin.foitt.wallet.platform.ssi.data.source.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.DeferredCredentialDao
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.deferredCredential1
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class DeferredCredentialDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var credentialDao: CredentialDao
    private lateinit var deferredCredentialDao: DeferredCredentialDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        credentialDao = database.credentialDao()
        deferredCredentialDao = database.deferredCredentialDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertDeferredCredentialTest() = runTest {
        credentialDao.insert(credential1)
        val id = deferredCredentialDao.insert(deferredCredential1)

        val deferredCredential = deferredCredentialDao.getById(id)

        assertEquals(deferredCredential1, deferredCredential.deferredCredential)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertDeferredCredentialWithoutCredentialShouldThrow() = runTest {
        deferredCredentialDao.insert(deferredCredential1)
    }

    @Test
    fun deleteCredentialTest() = runTest {
        credentialDao.insert(credential1)
        deferredCredentialDao.insert(deferredCredential1)

        credentialDao.deleteById(credential1.id)

        assertThrows<IllegalStateException> {
            deferredCredentialDao.getById(credential1.id)
        }
    }
}
