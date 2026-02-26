package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.GetEIdRequestCaseError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.toGetEIdRequestCaseError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetEIdRequestCase
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class GetEIdRequestCaseImpl @Inject constructor(
    private val eIdRequestCaseRepository: EIdRequestCaseRepository,
) : GetEIdRequestCase {
    override suspend fun invoke(caseId: String): Result<EIdRequestCase, GetEIdRequestCaseError> = coroutineBinding {
        eIdRequestCaseRepository.getEIdRequestCase(caseId)
            .mapError(EIdRequestCaseRepositoryError::toGetEIdRequestCaseError).bind()
    }
}
