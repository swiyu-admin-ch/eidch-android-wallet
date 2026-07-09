package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AvUploadFilesError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface UploadAllFiles {
    operator fun invoke(caseId: String, accessToken: String): Flow<Result<UploadAllFilesProgress, AvUploadFilesError>>
}

data class UploadAllFilesProgress(
    val total: Int,
    val completed: Int,
)
