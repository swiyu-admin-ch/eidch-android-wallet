package ch.admin.foitt.wallet.platform.activityList

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.activityActorDisplay2
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.activityActorDisplay3
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.activityActorDisplay4
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.credential1
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.credentialActivity2
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.credentialActivity3
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.image1
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.image2
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityActorDisplayEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityClaimEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialActivityEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimClusterEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.ImageEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialDao
import ch.admin.foitt.wallet.platform.database.domain.model.ImageEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

class ImageEntityDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var credentialDao: CredentialDao
    private lateinit var verifiableCredentialDao: VerifiableCredentialDao
    private lateinit var credentialClaimClusterEntityDao: CredentialClaimClusterEntityDao
    private lateinit var credentialClaimDao: CredentialClaimDao
    private lateinit var credentialActivityEntityDao: CredentialActivityEntityDao
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
        activityClaimEntityDao = database.activityClaimEntityDao()
        activityActorDisplayEntityDao = database.activityActorDisplayEntityDao()
        imageEntityDao = database.imageEntityDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertImageTest() = runTest {
        imageEntityDao.insert(image1)

        val image = imageEntityDao.getByHash(image1.hash)
        assertEquals(image1, image)
    }

    @Test
    fun insertImageWithSameIdTest() = runTest {
        imageEntityDao.insert(image1)

        val imageWithSameId = ImageEntity(
            id = image1.id,
            hash = "otherHash",
            image = byteArrayOf(1, 2)
        )

        val id = imageEntityDao.insert(imageWithSameId)

        assertEquals(-1, id)
    }

    @Test
    fun insertImageWithSameHashTest() = runTest {
        imageEntityDao.insert(image1)

        val imageWithSameId = ImageEntity(
            id = 2,
            hash = image1.hash,
            image = byteArrayOf(1, 2)
        )

        val id = imageEntityDao.insert(imageWithSameId)

        assertEquals(-1, id)
    }

    @Test
    fun insertIdenticalImageTest() = runTest {
        imageEntityDao.insert(image1)

        val id = imageEntityDao.insert(image1)

        assertEquals(-1, id)
    }

    @Test
    fun deleteNotLastChildOfImageTest() = runTest {
        credentialDao.insert(credential1)
        // insert first activity
        credentialActivityEntityDao.insert(credentialActivity2)
        imageEntityDao.insert(image2)
        activityActorDisplayEntityDao.insert(activityActorDisplay2)
        // insert second activity
        credentialActivityEntityDao.insert(credentialActivity3)
        imageEntityDao.insert(image2) // this is ignored on dao level
        activityActorDisplayEntityDao.insert(activityActorDisplay3)
        // delete one activity
        credentialActivityEntityDao.deleteById(activityActorDisplay2.id)
        // repo will trigger the cleanup
        imageEntityDao.deleteImagesWithoutChildren()

        // image should still exist
        assertEquals(image2, imageEntityDao.getByHash(image2.hash))
    }

    @Test
    fun deleteLastChildOfImageTest() = runTest {
        credentialDao.insert(credential1)
        // insert activity
        credentialActivityEntityDao.insert(credentialActivity2)
        imageEntityDao.insert(image2)
        activityActorDisplayEntityDao.insert(activityActorDisplay2)

        // delete  activity
        credentialActivityEntityDao.deleteById(activityActorDisplay2.id)
        // repo will trigger the cleanup
        imageEntityDao.deleteImagesWithoutChildren()

        // image should also be deleted
        assertThrows<Throwable> {
            imageEntityDao.getByHash(image2.hash)
        }
    }

    @Test
    fun deleteLastChildOfImageWhileNullHashExistsTest() = runTest {
        credentialDao.insert(credential1)
        // insert activity
        credentialActivityEntityDao.insert(credentialActivity2)
        imageEntityDao.insert(image2)
        activityActorDisplayEntityDao.insert(activityActorDisplay2)
        activityActorDisplayEntityDao.insert(activityActorDisplay4)

        // delete  activity
        credentialActivityEntityDao.deleteById(activityActorDisplay2.id)
        // repo will trigger the cleanup
        imageEntityDao.deleteImagesWithoutChildren()

        // image should also be deleted
        assertThrows<Throwable> {
            imageEntityDao.getByHash(image2.hash)
        }
    }
}
