package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.GetEIdRequestCaseError
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import com.github.michaelbull.result.Result

interface GetEIdRequestCase {
    suspend operator fun invoke(caseId: String): Result<EIdRequestCase, GetEIdRequestCaseError>
}
