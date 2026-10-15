package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.threshold
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAllAnyCredentialsByCredentialId
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshVerifiableCredentials
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import ch.admin.foitt.wallet.platform.database.domain.model.BatchRefreshDataEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithAuthentication
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.SwissProfile
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetSwissProfileStatus
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onErr
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class RefreshVerifiableCredentialsImpl @Inject constructor(
    private val credentialRefreshDataRepository: CredentialRefreshDataRepository,
    private val bundleItemRepository: BundleItemRepository,
    private val getAllAnyCredentialsByCredentialId: GetAllAnyCredentialsByCredentialId,
    private val getSwissProfileStatus: GetSwissProfileStatus,
    private val refreshCredential: RefreshCredential,
    private val environmentSetupRepository: EnvironmentSetupRepository,
) : RefreshVerifiableCredentials {

    override suspend fun invoke() {
        val credentials = credentialRefreshDataRepository.getAllCredentialRefreshData()
            .onErr { error -> Timber.w(message = "Getting credentials for refresh failed: $error") }
            .get()
            ?: return

        credentials.forEach { credential ->
            if (canBeRefreshed(credential) && needsRefresh(credential)) {
                val credentialId = credential.credential.id
                Timber.d(message = "Refreshing verifiable credential $credentialId")
                refreshCredential(credential.credential.id).onErr { error ->
                    Timber.w(message = "Failed refreshing verifiable credential $credentialId: $error")
                }
            }
        }
    }

    private fun canBeRefreshed(credential: VerifiableCredentialWithAuthentication): Boolean =
        credential.verifiableCredential.progressionState == VerifiableProgressionState.ACCEPTED &&
            credential.authentication?.refreshToken != null

    private suspend fun needsRefresh(credential: VerifiableCredentialWithAuthentication): Boolean {
        if (isBatchBelowThreshold(credential.credential.id, credential.batchData)) {
            return true
        }
        val anyCredential = getAnyCredential(credential.credential.id)
        // No profile_version or an unknown version is handled like end-of-life
        val status = getSwissProfileStatus(anyCredential?.profileVersion) ?: SwissProfile.Status.END_OF_LIFE
        if (!isAllowedToRefresh(credential, status)) {
            return false
        }
        return isSwissProfileOutdated(status) || isNearExpiry(anyCredential?.validUntilInstant)
    }

    private suspend fun isBatchBelowThreshold(credentialId: Long, batchData: BatchRefreshDataEntity?): Boolean {
        if (!environmentSetupRepository.batchIssuanceEnabled || batchData == null) return false
        val presentableCount = bundleItemRepository.getNeverPresentedCount(credentialId)
            .onErr { error ->
                Timber.w(
                    message = "Failed getting item count for ${batchData.credentialId}: $error"
                )
            }
            .get()
        return presentableCount != null && presentableCount <= batchData.batchSize.threshold
    }

    private suspend fun getAnyCredential(credentialId: Long): AnyCredential? =
        getAllAnyCredentialsByCredentialId(credentialId).get()
            ?.firstOrNull()

    private fun isAllowedToRefresh(
        credential: VerifiableCredentialWithAuthentication,
        status: SwissProfile.Status,
    ): Boolean {
        val lastAttempt = credential.verifiableCredential.lastRefreshAttemptAt ?: return true
        val lastAttemptInstant = Instant.ofEpochSecond(lastAttempt)
        val nextAllowedAttempt = lastAttemptInstant.plus(Duration.ofDays(status.refreshAttemptIntervalDays))
        return Instant.now().isAfter(nextAllowedAttempt)
    }

    private fun isSwissProfileOutdated(status: SwissProfile.Status): Boolean =
        status != SwissProfile.Status.CURRENT

    private fun isNearExpiry(exp: Instant?): Boolean =
        exp != null && exp <= Instant.now().plus(EXPIRY_REFRESH_RANGE_DAYS, ChronoUnit.DAYS)

    private companion object {
        const val EXPIRY_REFRESH_RANGE_DAYS = 30L
    }
}
