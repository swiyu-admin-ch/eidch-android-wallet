package ch.admin.foitt.wallet.platform.activityList

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.activityActorDisplay4
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.activityActorDisplayWithImage
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.credential1
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.credentialActivity2
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityActorDisplayEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityWithDisplaysDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialActivityEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityWithActorDisplays
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class ActivityWithDisplaysDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var credentialDao: CredentialDao
    private lateinit var credentialActivityEntityDao: CredentialActivityEntityDao
    private lateinit var activityActorDisplayEntityDao: ActivityActorDisplayEntityDao
    private lateinit var activityWithDisplaysDao: ActivityWithDisplaysDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        credentialDao = database.credentialDao()
        credentialActivityEntityDao = database.credentialActivityEntityDao()
        activityActorDisplayEntityDao = database.activityActorDisplayEntityDao()
        activityWithDisplaysDao = database.activityWithDisplaysDao()

        credentialDao.insert(credential1)
        credentialActivityEntityDao.insert(credentialActivity2)
        activityActorDisplayEntityDao.insert(activityActorDisplay4)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun getActivityWithDisplaysTest() = runTest {
        val activityWithDisplays = activityWithDisplaysDao.getActivitiesByCredentialIdFlow(credential1.id).firstOrNull()

        val expected = listOf(
            ActivityWithActorDisplays(
                activity = credentialActivity2,
                actorDisplays = listOf(activityActorDisplayWithImage)
            )
        )

        assertEquals(expected, activityWithDisplays)
    }
}
