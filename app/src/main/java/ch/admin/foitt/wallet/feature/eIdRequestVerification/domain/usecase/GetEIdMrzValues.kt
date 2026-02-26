package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.GetDocumentScanDataError
import com.github.michaelbull.result.Result

interface GetEIdMrzValues {
    suspend operator fun invoke(serializedDataList: String): Result<List<String>, GetDocumentScanDataError>
}
