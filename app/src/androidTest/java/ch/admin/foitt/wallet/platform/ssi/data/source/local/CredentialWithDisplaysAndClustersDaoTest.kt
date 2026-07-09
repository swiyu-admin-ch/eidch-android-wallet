package ch.admin.foitt.wallet.platform.ssi.data.source.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimClusterDisplayEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimClusterEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialWithDisplaysAndClustersDao
import ch.admin.foitt.wallet.platform.database.domain.model.ClusterWithDisplaysAndClaims
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClusterWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithDisplaysAndClusters
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.cluster1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.cluster2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.clusterDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.clusterDisplay2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaim1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaim2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaimDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaimDisplay2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaimDisplay3
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialDisplay2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.verifiableCredential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.verifiableCredential2
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CredentialWithDisplaysAndClustersDaoTest {
    private lateinit var database: AppDatabase

    private lateinit var credentialDao: CredentialDao
    private lateinit var verifiableCredentialDao: VerifiableCredentialDao
    private lateinit var credentialDisplayDao: CredentialDisplayDao
    private lateinit var credentialClaimClusterEntityDao: CredentialClaimClusterEntityDao
    private lateinit var credentialClaimClusterDisplayEntityDao: CredentialClaimClusterDisplayEntityDao
    private lateinit var credentialClaimDao: CredentialClaimDao
    private lateinit var credentialClaimDisplayDao: CredentialClaimDisplayDao
    private lateinit var credentialWithDisplaysAndClustersDao: VerifiableCredentialWithDisplaysAndClustersDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        credentialDao = database.credentialDao()
        verifiableCredentialDao = database.verifiableCredentialDao()
        credentialDisplayDao = database.credentialDisplayDao()
        credentialClaimClusterEntityDao = database.credentialClaimClusterEntityDao()
        credentialClaimClusterDisplayEntityDao = database.credentialClaimClusterDisplayEntityDao()
        credentialClaimDao = database.credentialClaimDao()
        credentialClaimDisplayDao = database.credentialClaimDisplayDao()
        credentialWithDisplaysAndClustersDao = database.verifiableCredentialWithDisplaysAndClustersDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun getCredentialWithDisplaysAndClaimsFlowByIdTest() = runTest {
        credentialDao.insert(credential1)
        verifiableCredentialDao.insert(verifiableCredential1)
        credentialDisplayDao.insertAll(listOf(credentialDisplay1))
        credentialClaimClusterEntityDao.insert(cluster1)
        credentialClaimClusterDisplayEntityDao.insert(clusterDisplay1)
        credentialClaimDao.insert(credentialClaim1)
        credentialClaimDisplayDao.insertAll(listOf(credentialClaimDisplay1, credentialClaimDisplay3))

        val credentialWithDisplaysAndClaims =
            credentialWithDisplaysAndClustersDao.getVerifiableCredentialWithDisplaysAndClustersFlowById(credential1.id).firstOrNull()

        val expected = VerifiableCredentialWithDisplaysAndClusters(
            verifiableCredential = verifiableCredential1,
            credential = credential1,
            credentialDisplays = listOf(credentialDisplay1),
            clusters = listOf(
                ClusterWithDisplaysAndClaims(
                    claimsWithDisplays = listOf(
                        CredentialClaimWithDisplays(
                            claim = credentialClaim1,
                            displays = listOf(credentialClaimDisplay1, credentialClaimDisplay3)
                        )
                    ),
                    clusterWithDisplays = CredentialClusterWithDisplays(
                        cluster = cluster1,
                        displays = listOf(clusterDisplay1)
                    )
                )
            ),
        )

        assertEquals(expected, credentialWithDisplaysAndClaims)
    }

    @Test
    fun getNullableCredentialWithDisplaysAndClaimsFlowByIdTest() = runTest {
        credentialDao.insert(credential1)
        verifiableCredentialDao.insert(verifiableCredential1)
        credentialDisplayDao.insertAll(listOf(credentialDisplay1))
        credentialClaimClusterEntityDao.insert(cluster1)
        credentialClaimClusterDisplayEntityDao.insert(clusterDisplay1)
        credentialClaimDao.insert(credentialClaim1)
        credentialClaimDisplayDao.insertAll(listOf(credentialClaimDisplay1, credentialClaimDisplay3))

        val flow = credentialWithDisplaysAndClustersDao.getNullableVerifiableCredentialWithDisplaysAndClustersFlowById(credential1.id)

        val collectedCredentialWithDisplaysAndClusters = mutableListOf<VerifiableCredentialWithDisplaysAndClusters?>()

        val job = launch {
            flow.take(2).collect { credentialWithDisplaysAndClusters ->
                collectedCredentialWithDisplaysAndClusters.add(credentialWithDisplaysAndClusters)
                if (credentialWithDisplaysAndClusters != null) {
                    credentialDao.deleteById(credential1.id)
                }

            }
        }

        job.join()

        val expected = VerifiableCredentialWithDisplaysAndClusters(
            verifiableCredential = verifiableCredential1,
            credential = credential1,
            credentialDisplays = listOf(credentialDisplay1),
            clusters = listOf(
                ClusterWithDisplaysAndClaims(
                    claimsWithDisplays = listOf(
                        CredentialClaimWithDisplays(
                            claim = credentialClaim1,
                            displays = listOf(credentialClaimDisplay1, credentialClaimDisplay3)
                        )
                    ),
                    clusterWithDisplays = CredentialClusterWithDisplays(
                        cluster = cluster1,
                        displays = listOf(clusterDisplay1)
                    )
                )
            ),
        )

        assertEquals(2, collectedCredentialWithDisplaysAndClusters.size)
        assertEquals(expected, collectedCredentialWithDisplaysAndClusters[0])
        assertEquals(null, collectedCredentialWithDisplaysAndClusters[1])
    }

    @Test
    fun getCredentialsWithDisplaysAndClustersFlowTest() = runTest {
        credentialDao.insert(credential1)
        verifiableCredentialDao.insert(verifiableCredential1)
        credentialDisplayDao.insertAll(listOf(credentialDisplay1))
        credentialClaimClusterEntityDao.insert(cluster1)
        credentialClaimClusterDisplayEntityDao.insert(clusterDisplay1)
        credentialClaimDao.insert(credentialClaim1)
        credentialClaimDisplayDao.insertAll(listOf(credentialClaimDisplay1, credentialClaimDisplay3))

        credentialDao.insert(credential2)
        verifiableCredentialDao.insert(verifiableCredential2)
        credentialDisplayDao.insertAll(listOf(credentialDisplay2))
        credentialClaimClusterEntityDao.insert(cluster2)
        credentialClaimClusterDisplayEntityDao.insert(clusterDisplay2)
        credentialClaimDao.insert(credentialClaim2)
        credentialClaimDisplayDao.insertAll(listOf(credentialClaimDisplay2))

        val credentialWithDisplaysAndClusters = credentialWithDisplaysAndClustersDao.getVerifiableCredentialsWithDisplaysAndClustersFlow().firstOrNull()

        val expected1 = VerifiableCredentialWithDisplaysAndClusters(
            verifiableCredential = verifiableCredential1,
            credential = credential1,
            credentialDisplays = listOf(credentialDisplay1),
            clusters = listOf(
                ClusterWithDisplaysAndClaims(
                    claimsWithDisplays = listOf(
                        CredentialClaimWithDisplays(
                            claim = credentialClaim1,
                            displays = listOf(credentialClaimDisplay1, credentialClaimDisplay3)
                        )
                    ),
                    clusterWithDisplays = CredentialClusterWithDisplays(
                        cluster = cluster1,
                        displays = listOf(clusterDisplay1)
                    )
                )
            ),
        )

        val expected2 = VerifiableCredentialWithDisplaysAndClusters(
            verifiableCredential = verifiableCredential2,
            credential = credential2,
            credentialDisplays = listOf(credentialDisplay2),
            clusters = listOf(
                ClusterWithDisplaysAndClaims(
                    claimsWithDisplays = listOf(
                        CredentialClaimWithDisplays(
                            claim = credentialClaim2,
                            displays = listOf(credentialClaimDisplay2)
                        )
                    ),
                    clusterWithDisplays = CredentialClusterWithDisplays(
                        cluster = cluster2,
                        displays = listOf(clusterDisplay2)
                    )
                )
            ),
        )

        assertEquals(2, credentialWithDisplaysAndClusters?.size)
        // query has order by asc
        assertEquals(expected2, credentialWithDisplaysAndClusters?.get(0))
        assertEquals(expected1, credentialWithDisplaysAndClusters?.get(1))
    }
}
