package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credential.domain.repository.CredentialRefreshRepository
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshDeferredCredentials
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshVerifiableCredentials
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateAllCredentialStatuses
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseState
import ch.admin.foitt.wallet.platform.database.domain.repository.DatabaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateAllSIdStatuses
import ch.admin.foitt.wallet.platform.login.domain.usecase.AfterLoginWork
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.UpdatePushToken
import timber.log.Timber
import javax.inject.Inject

class AfterLoginWorkImpl @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val updateAllCredentialStatuses: UpdateAllCredentialStatuses,
    private val updateAllSIdStatuses: UpdateAllSIdStatuses,
    private val refreshDeferredCredentials: RefreshDeferredCredentials,
    private val refreshVerifiableCredentials: RefreshVerifiableCredentials,
    private val credentialRefreshRepository: CredentialRefreshRepository,
    private val updatePushToken: UpdatePushToken
) : AfterLoginWork {
    override suspend fun invoke() {
        databaseRepository.databaseState.collect { dbState ->
            when (dbState) {
                DatabaseState.OPEN -> {
                    // Claim the shared cooldown so a Home resume right after login does not refresh again.
                    credentialRefreshRepository.markRefreshed()
                    Timber.d("After login work: updating the deferred credentials statuses")
                    refreshDeferredCredentials()
                    Timber.d("After login work: refreshing the verifiable credentials...")
                    refreshVerifiableCredentials()
                    Timber.d("After login work: Start updating SId statuses")
                    updateAllSIdStatuses()
                    Timber.d("After login work: updating the push token...")
                    updatePushToken()
                    Timber.d("After login work: updating the credentials statuses...")
                    updateAllCredentialStatuses()
                }
                DatabaseState.CLOSED -> Timber.d("After login work: DB is closed")
            }
        }
    }
}
