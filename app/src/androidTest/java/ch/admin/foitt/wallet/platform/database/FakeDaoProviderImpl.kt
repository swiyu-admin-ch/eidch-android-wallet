package ch.admin.foitt.wallet.platform.database

import ch.admin.foitt.wallet.platform.database.data.dao.ActivityActorDisplayEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityActorDisplayWithImageDao
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityClaimEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityWithDetailsDao
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityWithDisplaysDao
import ch.admin.foitt.wallet.platform.database.data.dao.BatchRefreshDataDao
import ch.admin.foitt.wallet.platform.database.data.dao.BundleItemEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.BundleItemWithKeyBindingDao
import ch.admin.foitt.wallet.platform.database.data.dao.ClientAttestationDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialActivityEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimClusterDisplayEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimClusterEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialIssuerDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialKeyBindingEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.data.dao.DeferredCredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.DeferredCredentialWithDisplaysDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestCaseDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestCaseWithStateDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestFileDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestStateDao
import ch.admin.foitt.wallet.platform.database.data.dao.ImageEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.RawCredentialDataDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialWithBundleItemsWithKeyBindingDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialWithDisplaysAndClustersDao
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithState
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialWithPayload
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.verifiableCredential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.verifiableCredential2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.verifiableCredentialWithPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeDaoProviderImpl : DaoProvider {
    override val credentialDaoFlow: StateFlow<CredentialDao?>
        get() = MutableStateFlow(object : CredentialDao {
            override fun insert(credential: Credential): Long {
                return 1L
            }

            override fun deleteById(id: Long) {
                // No-op in the mock
            }

            override fun getById(id: Long): Credential {
                return credentialWithPayload
            }

            override fun getAll(): List<Credential> {
                return listOf(
                    credential1,
                    credential2,
                    credentialWithPayload
                )
            }
        })
    override val credentialDisplayDaoFlow: StateFlow<CredentialDisplayDao?>
        get() = MutableStateFlow(null)
    override val credentialClaimDaoFlow: StateFlow<CredentialClaimDao?>
        get() = MutableStateFlow(null)
    override val credentialClaimDisplayDaoFlow: StateFlow<CredentialClaimDisplayDao?>
        get() = MutableStateFlow(null)
    override val credentialIssuerDisplayDaoFlow: StateFlow<CredentialIssuerDisplayDao?>
        get() = MutableStateFlow(null)
    override val credentialClaimClusterDisplayEntityDao: StateFlow<CredentialClaimClusterDisplayEntityDao?>
        get() = MutableStateFlow(null)
    override val credentialClaimClusterEntityDao: StateFlow<CredentialClaimClusterEntityDao?>
        get() = MutableStateFlow(null)

    override val verifiableCredentialDaoFlow: StateFlow<VerifiableCredentialDao?>
        get() = MutableStateFlow(object : VerifiableCredentialDao {
            override fun insert(verifiableCredential: VerifiableCredentialEntity): Long {
                return 1L
            }

            override fun updateProgressStateByCredentialId(id: Long, progressionState: VerifiableProgressionState): Int {
                return 1
            }

            override fun deleteById(id: Long) {
                // No-op in the mock
            }

            override fun getById(id: Long): VerifiableCredentialEntity {
                return verifiableCredentialWithPayload
            }

            override fun getAllIds(): List<Long> {
                return listOf(1L, 2L, 3L)
            }

            override fun getAll(): List<VerifiableCredentialEntity> {
                return listOf(verifiableCredential1, verifiableCredential2, verifiableCredentialWithPayload)
            }

            override fun updatedAt(id: Long, updatedAt: Long): Int {
                return 3
            }
        })

    override val verifiableCredentialWithDisplaysAndClustersDaoFlow: StateFlow<VerifiableCredentialWithDisplaysAndClustersDao?>
        get() = MutableStateFlow(null)
    override val verifiableCredentialWithBundleItemsWithKeyBindingDaoFlow: StateFlow<VerifiableCredentialWithBundleItemsWithKeyBindingDao?>
        get() = MutableStateFlow(null)
    override val bundleItemEntityDaoFlow: StateFlow<BundleItemEntityDao?>
        get() = MutableStateFlow(null)
    override val bundleItemWithKeyBindingDaoFlow: StateFlow<BundleItemWithKeyBindingDao?>
        get() = MutableStateFlow(null)
    override val credentialKeyBindingEntityDaoFlow: StateFlow<CredentialKeyBindingEntityDao?>
        get() = MutableStateFlow(null)

    override val credentialActivityEntityDao: StateFlow<CredentialActivityEntityDao?>
        get() = MutableStateFlow(null)
    override val activityClaimEntityDao: StateFlow<ActivityClaimEntityDao?>
        get() = MutableStateFlow(null)
    override val activityActorDisplayEntityDao: StateFlow<ActivityActorDisplayEntityDao?>
        get() = MutableStateFlow(null)
    override val activityActorDisplayWithImageDao: StateFlow<ActivityActorDisplayWithImageDao?>
        get() = MutableStateFlow(null)
    override val activityWithDetailsDao: StateFlow<ActivityWithDetailsDao?>
        get() = MutableStateFlow(null)
    override val activityWithDisplaysDao: StateFlow<ActivityWithDisplaysDao?>
        get() = MutableStateFlow(null)

    override val imageEntityDao: StateFlow<ImageEntityDao?>
        get() = MutableStateFlow(null)

    override val eIdRequestCaseDaoFlow: StateFlow<EIdRequestCaseDao?>
        get() = MutableStateFlow(null)
    override val eIdRequestStateDaoFlow: StateFlow<EIdRequestStateDao?>
        get() = MutableStateFlow(null)
    override val deferredCredentialDao: StateFlow<DeferredCredentialDao?>
        get() = MutableStateFlow(null)
    override val deferredCredentialWithDisplaysDao: StateFlow<DeferredCredentialWithDisplaysDao?>
        get() = MutableStateFlow(null)
    override val eIdRequestFileDaoFlow: StateFlow<EIdRequestFileDao?>
        get() = MutableStateFlow(null)
    override val eIdRequestCaseWithStateDaoFlow: StateFlow<EIdRequestCaseWithStateDao?>
        get() = MutableStateFlow(object : EIdRequestCaseWithStateDao {
            override fun getEIdCasesWithStatesFlow(): Flow<List<EIdRequestCaseWithState>> {
                return MutableStateFlow(listOf())
            }
        })
    override val rawCredentialDataDao: StateFlow<RawCredentialDataDao?>
        get() = MutableStateFlow(null)
    override val clientAttestationDaoFlow: StateFlow<ClientAttestationDao?>
        get() = MutableStateFlow(null)
    override val batchRefreshDataDao: StateFlow<BatchRefreshDataDao?>
        get() = MutableStateFlow(null)
}
