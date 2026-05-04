package ch.admin.foitt.wallet.platform.activityList

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.activityActorDisplay2
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.activityClaim1
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.claim1
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.cluster1
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.credential1
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.credential2
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.credentialActivity1
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.credentialActivity2
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.credentialActivity3
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.credentialActivity4
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.image2
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.nonComplianceReasonDisplay1
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.verifiableCredential1
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityActorDisplayEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityClaimEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialActivityEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimClusterEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.ImageEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.NonComplianceReasonDisplayEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialDao
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

class CredentialActivityEntityDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var credentialDao: CredentialDao
    private lateinit var verifiableCredentialDao: VerifiableCredentialDao
    private lateinit var credentialClaimClusterEntityDao: CredentialClaimClusterEntityDao
    private lateinit var credentialClaimDao: CredentialClaimDao
    private lateinit var credentialActivityEntityDao: CredentialActivityEntityDao
    private lateinit var nonComplianceReasonDisplayEntityDao: NonComplianceReasonDisplayEntityDao
    private lateinit var activityClaimEntityDao: ActivityClaimEntityDao
    private lateinit var activityActorDisplayEntityDao: ActivityActorDisplayEntityDao
    private lateinit var imageEntityDao: ImageEntityDao

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
        activityClaimEntityDao = database.activityClaimEntityDao()
        activityActorDisplayEntityDao = database.activityActorDisplayEntityDao()
        imageEntityDao = database.imageEntityDao()

        credentialDao.insert(credential1)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertCredentialActivityTest() = runTest {
        val id = credentialActivityEntityDao.insert(credentialActivity1)

        val activity = credentialActivityEntityDao.getById(id)
        assertEquals(credentialActivity1, activity)
    }

    @Test
    fun deleteCredentialActivityTest() = runTest {
        credentialActivityEntityDao.insert(credentialActivity1)
        credentialActivityEntityDao.deleteById(credentialActivity1.id)

        assertThrows<Throwable> {
            credentialActivityEntityDao.getById(credentialActivity1.id)
        }
    }

    @Test
    fun deleteAllCredentialActivitiesTest() = runTest {
        credentialDao.insert(credential2)

        credentialActivityEntityDao.insert(credentialActivity1)
        credentialActivityEntityDao.insert(credentialActivity2)
        credentialActivityEntityDao.insert(credentialActivity3)
        credentialActivityEntityDao.insert(credentialActivity4)
        credentialActivityEntityDao.deleteAllActivities()

        assertThrows<Throwable> {
            credentialActivityEntityDao.getById(credentialActivity1.id)
        }
        assertThrows<Throwable> {
            credentialActivityEntityDao.getById(credentialActivity2.id)
        }
        assertThrows<Throwable> {
            credentialActivityEntityDao.getById(credentialActivity3.id)
        }
        assertThrows<Throwable> {
            credentialActivityEntityDao.getById(credentialActivity4.id)
        }
    }

    @Test
    fun deleteCredentialActivityCascadingTest() = runTest {
        verifiableCredentialDao.insert(verifiableCredential1)
        credentialClaimClusterEntityDao.insert(cluster1)
        credentialClaimDao.insert(claim1)

        credentialActivityEntityDao.insert(credentialActivity2)
        nonComplianceReasonDisplayEntityDao.insert(nonComplianceReasonDisplay1)
        activityClaimEntityDao.insert(activityClaim1)
        imageEntityDao.insert(image2)
        activityActorDisplayEntityDao.insert(activityActorDisplay2)

        credentialActivityEntityDao.deleteById(credentialActivity2.id)

        assertEquals(credential1, credentialDao.getById(credential1.id))
        assertEquals(verifiableCredential1, verifiableCredentialDao.getById(verifiableCredential1.credentialId))
        assertEquals(cluster1, credentialClaimClusterEntityDao.getById(cluster1.id))
        assertEquals(claim1, credentialClaimDao.getById(claim1.id))

        assertThrows<Throwable> {
            credentialActivityEntityDao.getById(credentialActivity2.id)
        }
        assertThrows<Throwable> {
            nonComplianceReasonDisplayEntityDao.getById(nonComplianceReasonDisplay1.id)
        }
        assertThrows<Throwable> {
            activityClaimEntityDao.getById(activityClaim1.id)
        }
        assertThrows<Throwable> {
            activityActorDisplayEntityDao.getById(activityActorDisplay2.id)
        }
    }
}
