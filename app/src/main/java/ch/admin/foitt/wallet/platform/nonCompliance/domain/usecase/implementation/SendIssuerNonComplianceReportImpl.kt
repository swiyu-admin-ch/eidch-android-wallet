package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.CredentialActivityRepositoryError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.CredentialActivityRepository
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceIssuerMetadata
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceIssuerReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceMetadata
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRequest
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.SendNonComplianceReportError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.toSendNonComplianceReportError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendIssuerNonComplianceReport
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SubmitNonComplianceReport
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.RawCredentialDataRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.VerifiableCredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.platform.ssi.domain.repository.RawCredentialDataRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SendIssuerNonComplianceReportImpl @Inject constructor(
    private val credentialActivityRepository: CredentialActivityRepository,
    private val verifiableCredentialRepo: VerifiableCredentialRepository,
    private val credentialRepo: CredentialRepo,
    private val rawCredentialDataRepo: RawCredentialDataRepository,
    private val bundleItemRepo: BundleItemRepository,
    private val getCurrentAppLocale: GetCurrentAppLocale,
    private val submitNonComplianceReport: SubmitNonComplianceReport
) : SendIssuerNonComplianceReport {
    override suspend fun invoke(
        activityId: Long?,
        reportReason: NonComplianceReportReason,
        description: String?,
        context: String?,
        email: String?,
    ): Result<Unit, SendNonComplianceReportError> = coroutineBinding {
        val activity = activityId?.let { id ->
            credentialActivityRepository.getById(id)
                .mapError(CredentialActivityRepositoryError::toSendNonComplianceReportError)
                .bind()
        } ?: return@coroutineBinding Err(
            NonComplianceError.Unexpected(IllegalStateException("activityId must not be null"))
        ).bind<Unit>()

        val credentialId = activity.credentialId

        val vcEntity = verifiableCredentialRepo.getById(credentialId)
            .mapError(VerifiableCredentialRepositoryError::toSendNonComplianceReportError)
            .bind()

        val credential = credentialRepo.getById(credentialId)
            .mapError(CredentialRepositoryError::toSendNonComplianceReportError)
            .bind()

        val rawData = rawCredentialDataRepo.getByCredentialId(credentialId)
            .mapError(RawCredentialDataRepositoryError::toSendNonComplianceReportError)
            .bind()

        val createdAt = Instant.ofEpochSecond(activity.createdAt)
        val formattedCreatedAt = DateTimeFormatter.ISO_INSTANT.format(createdAt)

        val vcIssued = if (reportReason == NonComplianceIssuerReportReason.ILLEGAL_CONTENT_OR_PURPOSE) {
            bundleItemRepo.getAllByCredentialId(credentialId)
                .mapError(BundleItemRepositoryError::toSendNonComplianceReportError)
                .bind()
                .firstOrNull()?.payload
        } else {
            null
        }

        val nonComplianceMetadata = NonComplianceMetadata(
            issuer = NonComplianceIssuerMetadata(
                issuerDid = vcEntity.issuer,
                issuerUrl = credential.issuerUrl.toString(),
                issuanceCreatedAt = formattedCreatedAt,
                signedMetadataJwt = rawData.rawOIDMetadata?.let { String(it) },
                ocaBundle = rawData.rawOcaBundle?.let { String(it) },
                vcIssued = vcIssued,
            )
        )

        val nonComplianceRequest = NonComplianceRequest(
            type = reportReason.type,
            description = description,
            email = email,
            context = context,
            language = getCurrentAppLocale().language,
            metadata = nonComplianceMetadata
        )

        submitNonComplianceReport(nonComplianceRequest).bind()
    }
}
