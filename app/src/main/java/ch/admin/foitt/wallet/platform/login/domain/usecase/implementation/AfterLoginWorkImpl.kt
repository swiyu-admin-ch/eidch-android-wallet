package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.batch.domain.usecase.RefreshBatchCredentials
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshDeferredCredentials
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateAllCredentialStatuses
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseState
import ch.admin.foitt.wallet.platform.database.domain.repository.DatabaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateAllSIdStatuses
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.login.domain.usecase.AfterLoginWork
import timber.log.Timber
import javax.inject.Inject

class AfterLoginWorkImpl @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val updateAllCredentialStatuses: UpdateAllCredentialStatuses,
    private val updateAllSIdStatuses: UpdateAllSIdStatuses,
    private val refreshDeferredCredentials: RefreshDeferredCredentials,
    private val refreshBatchCredentials: RefreshBatchCredentials,
    private val environmentSetupRepository: EnvironmentSetupRepository,
) : AfterLoginWork {
    override suspend fun invoke() {
        databaseRepository.databaseState.collect { dbState ->
            when (dbState) {
                DatabaseState.OPEN -> {
                    Timber.d("After login work: updating the deferred credentials statuses")
                    refreshDeferredCredentials()
                    if (environmentSetupRepository.batchIssuanceEnabled) {
                        Timber.d("After login work: refreshing the batch credentials...")
                        refreshBatchCredentials()
                    }
                    Timber.d("After login work: Start updating SId statuses")
                    updateAllSIdStatuses()
                    Timber.d("After login work: updating the credentials statuses...")
                    updateAllCredentialStatuses()
                }
                DatabaseState.CLOSED -> Timber.d("After login work: DB is closed")
            }
        }
    }
}
