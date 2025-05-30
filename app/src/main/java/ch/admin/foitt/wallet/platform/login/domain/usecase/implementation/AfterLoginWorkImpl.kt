package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateAllCredentialStatuses
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseState
import ch.admin.foitt.wallet.platform.database.domain.repository.DatabaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateAllSIdStatuses
import ch.admin.foitt.wallet.platform.login.domain.usecase.AfterLoginWork
import timber.log.Timber
import javax.inject.Inject

class AfterLoginWorkImpl @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val updateAllCredentialStatuses: UpdateAllCredentialStatuses,
    private val updateAllSIdStatuses: UpdateAllSIdStatuses
) : AfterLoginWork {
    override suspend fun invoke() {
        databaseRepository.databaseState.collect { dbState ->
            when (dbState) {
                DatabaseState.OPEN -> {
                    Timber.d("After login work: Start updating the credential statuses...")
                    updateAllCredentialStatuses()
                    Timber.d("And update SId statuses...")
                    updateAllSIdStatuses()
                }
                DatabaseState.CLOSED -> Timber.d("After login work: DB closed")
            }
        }
    }
}
