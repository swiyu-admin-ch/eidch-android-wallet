package ch.admin.foitt.wallet.platform.ssi.data.source.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialDao
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.verifiableCredential1
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class VerifiableCredentialDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var credentialDao: CredentialDao
    private lateinit var verifiableCredentialDao: VerifiableCredentialDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        credentialDao = database.credentialDao()
        verifiableCredentialDao = database.verifiableCredentialDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertVerifiableCredentialTest() = runTest {
        credentialDao.insert(credential1)
        val id = verifiableCredentialDao.insert(verifiableCredential1)

        val verifiableCredential = verifiableCredentialDao.getById(id)

        assertEquals(verifiableCredential1, verifiableCredential)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertVerifiableCredentialWithoutCredentialShouldThrow() = runTest {
        verifiableCredentialDao.insert(verifiableCredential1)
    }

    @Test
    fun updateVerifiableCredentialByCredentialIdTest() = runTest {
        val id = credentialDao.insert(credential1)
        verifiableCredentialDao.insert(verifiableCredential1)
        val updatedAt = 2L

        verifiableCredentialDao.updatedAt(id, updatedAt)

        val verifiableCredential = verifiableCredentialDao.getById(id)
        TestCase.assertEquals("UpdatedAt should be updated", updatedAt, verifiableCredential.updatedAt)
    }

    @Test
    fun deleteCredentialTest() = runTest {
        credentialDao.insert(credential1)
        verifiableCredentialDao.insert(verifiableCredential1)

        credentialDao.deleteById(credential1.id)

        assertThrows<IllegalStateException> {
            verifiableCredentialDao.getById(credential1.id)
        }
    }
}
