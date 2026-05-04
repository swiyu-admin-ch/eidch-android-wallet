package ch.admin.foitt.wallet.platform.database.data.dao

import kotlinx.coroutines.flow.StateFlow

interface DaoProvider {
    val credentialDaoFlow: StateFlow<CredentialDao?>
    val verifiableCredentialDaoFlow: StateFlow<VerifiableCredentialDao?>
    val verifiableCredentialWithDisplaysAndClustersDaoFlow: StateFlow<VerifiableCredentialWithDisplaysAndClustersDao?>
    val verifiableCredentialWithBundleItemsWithKeyBindingDaoFlow:
        StateFlow<VerifiableCredentialWithBundleItemsWithKeyBindingDao?>
    val bundleItemEntityDaoFlow: StateFlow<BundleItemEntityDao?>
    val bundleItemWithKeyBindingDaoFlow: StateFlow<BundleItemWithKeyBindingDao?>
    val deferredCredentialDao: StateFlow<DeferredCredentialDao?>
    val deferredCredentialWithDisplaysDao: StateFlow<DeferredCredentialWithDisplaysDao?>
    val credentialDisplayDaoFlow: StateFlow<CredentialDisplayDao?>
    val credentialClaimDaoFlow: StateFlow<CredentialClaimDao?>
    val credentialClaimDisplayDaoFlow: StateFlow<CredentialClaimDisplayDao?>
    val credentialIssuerDisplayDaoFlow: StateFlow<CredentialIssuerDisplayDao?>
    val credentialClaimClusterEntityDao: StateFlow<CredentialClaimClusterEntityDao?>
    val credentialClaimClusterDisplayEntityDao: StateFlow<CredentialClaimClusterDisplayEntityDao?>
    val credentialKeyBindingEntityDaoFlow: StateFlow<CredentialKeyBindingEntityDao?>
    val credentialActivityEntityDao: StateFlow<CredentialActivityEntityDao?>
    val nonComplianceReasonDisplayEntityDao: StateFlow<NonComplianceReasonDisplayEntityDao?>
    val activityClaimEntityDao: StateFlow<ActivityClaimEntityDao?>
    val activityActorDisplayEntityDao: StateFlow<ActivityActorDisplayEntityDao?>
    val activityActorDisplayWithImageDao: StateFlow<ActivityActorDisplayWithImageDao?>
    val activityWithDetailsDao: StateFlow<ActivityWithDetailsDao?>
    val activityWithDisplaysDao: StateFlow<ActivityWithDisplaysDao?>
    val imageEntityDao: StateFlow<ImageEntityDao?>
    val eIdRequestCaseDaoFlow: StateFlow<EIdRequestCaseDao?>
    val eIdRequestStateDaoFlow: StateFlow<EIdRequestStateDao?>
    val eIdRequestCaseWithStateDaoFlow: StateFlow<EIdRequestCaseWithStateDao?>
    val eIdRequestFileDaoFlow: StateFlow<EIdRequestFileDao?>
    val rawCredentialDataDao: StateFlow<RawCredentialDataDao?>
    val clientAttestationDaoFlow: StateFlow<ClientAttestationDao?>
    val batchRefreshDataDao: StateFlow<BatchRefreshDataDao?>
}
