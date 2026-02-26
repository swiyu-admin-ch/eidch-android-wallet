package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.AreEIdDocumentsEqualError
import com.github.michaelbull.result.Result

interface AreEIdDocumentsEqual {
    suspend operator fun invoke(caseId: String, newDocument: Array<String>): Result<Boolean, AreEIdDocumentsEqualError>
}
