package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.AreEIdDocumentsEqualError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.toAreEIdDocumentsEqualError
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.AreEIdDocumentsEqual
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.GetDocumentScanData
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class AreEIdDocumentsEqualImpl @Inject constructor(
    private val getDocumentData: GetDocumentScanData,
) : AreEIdDocumentsEqual {
    override suspend fun invoke(
        caseId: String,
        newDocument: Array<String>
    ): Result<Boolean, AreEIdDocumentsEqualError> = coroutineBinding {
        if (caseId.isEmpty()) {
            return@coroutineBinding true
        }

        val previousDocument = getDocumentData(caseId).mapError { error ->
            error.toAreEIdDocumentsEqualError()
        }.bind()

        return@coroutineBinding (newDocument.contentEquals(previousDocument.mrzValues.toTypedArray()))
    }
}
