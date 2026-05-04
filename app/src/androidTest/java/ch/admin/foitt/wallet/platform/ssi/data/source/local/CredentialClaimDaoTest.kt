package ch.admin.foitt.wallet.platform.ssi.data.source.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimClusterEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialDao
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.PATH
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.VALUE
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.cluster1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.cluster2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaim1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaim2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.verifiableCredential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.verifiableCredential2
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CredentialClaimDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var credentialDao: CredentialDao
    private lateinit var verifiableCredentialDao: VerifiableCredentialDao
    private lateinit var credentialClaimClusterDao: CredentialClaimClusterEntityDao
    private lateinit var credentialClaimDao: CredentialClaimDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        credentialDao = database.credentialDao()
        credentialDao.insert(credential1)
        credentialDao.insert(credential2)

        verifiableCredentialDao = database.verifiableCredentialDao()
        verifiableCredentialDao.insert(verifiableCredential1)
        verifiableCredentialDao.insert(verifiableCredential2)

        credentialClaimClusterDao = database.credentialClaimClusterEntityDao()
        credentialClaimClusterDao.insert(cluster1)
        credentialClaimClusterDao.insert(cluster2)

        credentialClaimDao = database.credentialClaimDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertCredentialClaimTest() = runTest {
        val id = credentialClaimDao.insert(credentialClaim1)

        assertEquals(
            credentialClaim1.copy(id = id),
            credentialClaimDao.getById(id)
        )

        val id2 = credentialClaimDao.insert(credentialClaim2)
        assertEquals(
            credentialClaim2.copy(id = id2),
            credentialClaimDao.getById(id2)
        )
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertWithoutMatchingForeignKeyShouldThrow() {
        val credentialClaim = CredentialClaim(id = 1, clusterId = -1, path = PATH, value = VALUE, valueType = null)
        credentialClaimDao.insert(credentialClaim)
    }
}
