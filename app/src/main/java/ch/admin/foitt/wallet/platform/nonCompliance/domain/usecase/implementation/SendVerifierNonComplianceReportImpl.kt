package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.wallet.platform.activityList.domain.model.CredentialActivityRepositoryError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.CredentialActivityRepository
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceMetadata
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRequest
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceRequestField
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceVerifierMetadata
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.SendNonComplianceReportError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.toSendNonComplianceReportError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SendVerifierNonComplianceReport
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.SubmitNonComplianceReport
import ch.admin.foitt.wallet.platform.ssi.domain.model.VerifiableCredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.mapError
import uniffi.heidi_dcql_rust.Meta
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SendVerifierNonComplianceReportImpl @Inject constructor(
    private val credentialActivityRepository: CredentialActivityRepository,
    private val verifiableCredentialRepo: VerifiableCredentialRepository,
    private val safeJson: SafeJson,
    private val getCurrentAppLocale: GetCurrentAppLocale,
    private val submitNonComplianceReport: SubmitNonComplianceReport
) : SendVerifierNonComplianceReport {
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

        val credential = verifiableCredentialRepo.getById(activity.credentialId)
            .mapError(VerifiableCredentialRepositoryError::toSendNonComplianceReportError)
            .bind()

        val authorizationRequest = getAuthorizationRequest(activity.nonComplianceData).bind()
        val createdAt = Instant.ofEpochSecond(activity.createdAt)
        val formattedCreatedAt = DateTimeFormatter.ISO_INSTANT.format(createdAt)
        val presentationRequestFields = getPresentationRequestFields(authorizationRequest)

        val nonComplianceMetadata = NonComplianceMetadata(
            verifier = NonComplianceVerifierMetadata(
                verifierDid = authorizationRequest.clientIdentifier.clientId,
                verifierUrl = authorizationRequest.responseUri,
                presentationActionCreatedAt = formattedCreatedAt,
                presentedCredentialIssuerDid = credential.issuer,
                presentationRequestJwt = activity.nonComplianceData,
                presentationRequestFields = presentationRequestFields,
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

    private fun getAuthorizationRequest(nonComplianceData: String?): Result<AuthorizationRequest, SendNonComplianceReportError> = binding {
        if (nonComplianceData == null) {
            return@binding Err(
                NonComplianceError.Unexpected(IllegalStateException("nonComplianceData must not be null"))
            ).bind<AuthorizationRequest>()
        }
        val authorizationRequest = runSuspendCatching {
            Jwt(nonComplianceData)
        }.mapBoth(
            success = { jwt ->
                safeJson.safeDecodeElementTo<AuthorizationRequest>(jwt.payloadJson)
                    .mapError(JsonParsingError::toSendNonComplianceReportError)
                    .bind()
            },
            failure = {
                safeJson.safeDecodeStringTo<AuthorizationRequest>(nonComplianceData)
                    .mapError(JsonParsingError::toSendNonComplianceReportError)
                    .bind()
            },
        )

        authorizationRequest
    }

    private fun getPresentationRequestFields(authorizationRequest: AuthorizationRequest): List<NonComplianceRequestField> {
        return authorizationRequest.dcqlQuery?.credentials?.flatMap { credential ->
            buildList {
                add(
                    NonComplianceRequestField(
                        name = "vct",
                        constraint = when (val meta = credential.meta) {
                            is Meta.SdjwtVc -> meta.vctValues.joinToString(", ")
                            else -> null
                        }
                    )
                )
                credential.claims?.map { claim ->
                    add(
                        NonComplianceRequestField(
                            name = claim.path.joinToString(", "),
                            constraint = claim.values
                                ?.mapNotNull { safeJson.safeEncodeObjectToString(it).get() }
                                ?.joinToString(", ")
                        )
                    )
                }
            }
        } ?: emptyList()
    }
}
