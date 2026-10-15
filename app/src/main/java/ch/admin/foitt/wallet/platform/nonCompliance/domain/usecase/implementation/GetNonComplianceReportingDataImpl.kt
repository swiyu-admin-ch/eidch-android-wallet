package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorMetadataDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.GetLocalizedActorImageData
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.GetNonComplianceReportingDataError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportingData
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.GetNonComplianceReportingData
import ch.admin.foitt.wallet.platform.ssi.domain.repository.RawCredentialDataRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import ch.admin.foitt.wallet.platform.utils.decompress
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class GetNonComplianceReportingDataImpl @Inject constructor(
    private val rawCredentialDataRepository: RawCredentialDataRepository,
    private val verifiableCredentialRepository: VerifiableCredentialRepository,
    private val getLocalizedDisplay: GetLocalizedDisplay,
    private val getLocalizedActorImageData: GetLocalizedActorImageData,
) : GetNonComplianceReportingData {
    override suspend fun invoke(
        credentialId: Long,
        actorDisplayData: ActorDisplayData,
        activityType: ActivityType,
        activityId: Long?,
    ): Result<NonComplianceReportingData, GetNonComplianceReportingDataError> = coroutineBinding {
        val rawData = rawCredentialDataRepository.getByCredentialId(credentialId)
            .mapError { NonComplianceError.Unexpected(IllegalStateException("rawOIDMetadata must not be null")) }
            .bind()
            .rawOIDMetadata?.decompress()?.decodeToString() ?: ""

        val issuerDid = verifiableCredentialRepository.getById(credentialId)
            .mapError { NonComplianceError.Unexpected(IllegalStateException("credential must not be null")) }
            .bind()
            .issuer ?: ""

        val localizedName = getLocalizedDisplay(
            actorDisplayData.name ?: emptyList(),
            actorDisplayData.preferredLanguage
        )?.value ?: ""

        val iconBytes = getLocalizedActorImageData(actorDisplayData)

        NonComplianceReportingData(
            actorDisplayData = ActorMetadataDisplayData(
                activityId = activityId,
                localizedActorName = localizedName,
                actorImageData = iconBytes
            ),
            rawData = rawData,
            issuerDid = issuerDid,
            activityType = activityType,
        )
    }
}
