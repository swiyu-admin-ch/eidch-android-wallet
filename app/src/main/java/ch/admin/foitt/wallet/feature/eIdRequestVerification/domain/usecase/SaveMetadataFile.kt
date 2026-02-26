package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.SaveEIdRequestFileError
import com.github.michaelbull.result.Result

interface SaveMetadataFile {
    suspend operator fun invoke(caseId: String): Result<Unit, SaveEIdRequestFileError>
}
