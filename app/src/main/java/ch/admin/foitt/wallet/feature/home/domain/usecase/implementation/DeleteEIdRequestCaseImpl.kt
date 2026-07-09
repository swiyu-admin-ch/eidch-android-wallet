package ch.admin.foitt.wallet.feature.home.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.home.domain.model.DeleteEIdRequestCaseError
import ch.admin.foitt.wallet.feature.home.domain.model.toDeleteEIdRequestCaseError
import ch.admin.foitt.wallet.feature.home.domain.usecase.DeleteEIdRequestCase
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.pushNotification.domain.model.DeletePushIdError
import ch.admin.foitt.wallet.platform.pushNotification.domain.usecase.DeletePushId
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class DeleteEIdRequestCaseImpl @Inject constructor(
    private val eIdRequestCaseRepository: EIdRequestCaseRepository,
    private val deletePushId: DeletePushId
) : DeleteEIdRequestCase {
    override suspend fun invoke(caseId: String): Result<Unit, DeleteEIdRequestCaseError> = coroutineBinding {
        val case = eIdRequestCaseRepository.getEIdRequestCase(caseId)
            .mapError(EIdRequestCaseRepositoryError::toDeleteEIdRequestCaseError).bind()
        val pushId = case.pushId

        eIdRequestCaseRepository.deleteEIdRequestCase(caseId)
            .mapError(EIdRequestCaseRepositoryError::toDeleteEIdRequestCaseError)

        if (pushId != null) {
            deletePushId(pushId)
                .mapError(DeletePushIdError::toDeleteEIdRequestCaseError).bind()
        }
    }
}
