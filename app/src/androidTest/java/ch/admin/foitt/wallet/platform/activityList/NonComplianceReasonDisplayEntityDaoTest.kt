package ch.admin.foitt.wallet.platform.activityList

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.claim1
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.cluster1
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.credential1
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.credentialActivity2
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.nonComplianceReasonDisplay1
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.verifiableCredential1
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialActivityEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimClusterEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.NonComplianceReasonDisplayEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialDao
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class NonComplianceReasonDisplayEntityDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var credentialDao: CredentialDao
    private lateinit var verifiableCredentialDao: VerifiableCredentialDao
    private lateinit var credentialClaimClusterEntityDao: CredentialClaimClusterEntityDao
    private lateinit var credentialClaimDao: CredentialClaimDao
    private lateinit var credentialActivityEntityDao: CredentialActivityEntityDao
    private lateinit var nonComplianceReasonDisplayEntityDao: NonComplianceReasonDisplayEntityDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        credentialDao = database.credentialDao()
        verifiableCredentialDao = database.verifiableCredentialDao()
        credentialClaimClusterEntityDao = database.credentialClaimClusterEntityDao()
        credentialClaimDao = database.credentialClaimDao()
        credentialActivityEntityDao = database.credentialActivityEntityDao()
        nonComplianceReasonDisplayEntityDao = database.nonComplianceReasonDisplayEntityDao()

        credentialDao.insert(credential1)
        verifiableCredentialDao.insert(verifiableCredential1)
        credentialClaimClusterEntityDao.insert(cluster1)
        credentialClaimDao.insert(claim1)
        credentialActivityEntityDao.insert(credentialActivity2)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertNonComplianceReasonDisplayTest() = runTest {
        val id = nonComplianceReasonDisplayEntityDao.insert(nonComplianceReasonDisplay1)

        val nonComplianceReasonDisplay = nonComplianceReasonDisplayEntityDao.getById(id)
        assertEquals(nonComplianceReasonDisplay1, nonComplianceReasonDisplay)
    }
}
