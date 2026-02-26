package ch.admin.foitt.wallet.platform.nonCompliance.domain.repository

import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRepositoryError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceResponse
import com.github.michaelbull.result.Result

interface NonComplianceTrustRepository {
    suspend fun fetchNonComplianceData(trustRegistryDomain: String): Result<NonComplianceResponse, NonComplianceRepositoryError>
}
