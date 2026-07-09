package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SubmitCaseId
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AvRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AvSubmitCaseError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toAvSubmitCaseError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdAvRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class SubmitCaseIdImpl @Inject constructor(
    private val eIdAvRepository: EIdAvRepository,
    private val eIdRequestCaseRepository: EIdRequestCaseRepository,
) : SubmitCaseId {
    override suspend fun invoke(
        caseId: String,
        accessToken: String
    ): Result<Unit, AvSubmitCaseError> = coroutineBinding {
        eIdAvRepository.submitCase(caseId, accessToken)
            .mapError(AvRepositoryError::toAvSubmitCaseError).bind()
        eIdRequestCaseRepository.setFilesSubmitted(caseId)
            .mapError(EIdRequestCaseRepositoryError::toAvSubmitCaseError).bind()
    }
}
