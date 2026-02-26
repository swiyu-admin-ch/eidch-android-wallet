package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import com.github.michaelbull.result.Result

interface EIdRequestCaseRepository {
    suspend fun saveEIdRequestCase(case: EIdRequestCase): Result<Unit, EIdRequestCaseRepositoryError>
    suspend fun deleteEIdRequestCase(caseId: String): Result<Unit, EIdRequestCaseRepositoryError>
    suspend fun setEIdRequestCaseCredentialId(
        caseId: String,
        credentialId: Long,
    ): Result<Unit, EIdRequestCaseRepositoryError>
    suspend fun getEIdRequestCase(caseId: String): Result<EIdRequestCase, EIdRequestCaseRepositoryError>
}
