package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwt
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.FetchVcSchemaTrustStatusError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.GetTrustUrlFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IssuanceV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementActor
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementRepositoryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementType
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toFetchVcSchemaTrustStatusError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.repository.TrustStatementRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchVcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustUrlFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatement
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class FetchVcSchemaTrustStatusImpl @Inject constructor(
    private val getTrustUrlFromDid: GetTrustUrlFromDid,
    private val trustStatementRepository: TrustStatementRepository,
    private val getTrustDomainFromDid: GetTrustDomainFromDid,
    private val environmentSetupRepo: EnvironmentSetupRepository,
    private val validateTrustStatement: ValidateTrustStatement,
    private val safeJson: SafeJson,
) : FetchVcSchemaTrustStatus {
    override suspend fun invoke(
        trustStatementActor: TrustStatementActor,
        actorDid: String,
        vcSchemaId: String,
    ): Result<VcSchemaTrustStatus, FetchVcSchemaTrustStatusError> = coroutineBinding {
        val trustStatementType = when (trustStatementActor) {
            TrustStatementActor.ISSUER -> TrustStatementType.ISSUANCE
            TrustStatementActor.VERIFIER -> TrustStatementType.VERIFICATION
        }

        val trustUrl = getTrustUrlFromDid(
            trustStatementType = trustStatementType,
            actorDid = actorDid,
            vcSchemaId = vcSchemaId,
        )
            .mapError(GetTrustUrlFromDidError::toFetchVcSchemaTrustStatusError)
            .bind()

        val trustStatementsRaw = trustStatementRepository.fetchTrustStatements(trustUrl)
            .mapError(TrustStatementRepositoryError::toFetchVcSchemaTrustStatusError)
            .bind()

        val trustStatements = runSuspendCatching {
            trustStatementsRaw.map { VcSdJwt(it) }
        }.mapError { throwable ->
            throwable.toFetchVcSchemaTrustStatusError(message = "trust statement vc sd jwt creation failed")
        }.bind()

        val filteredStatements = trustStatements
            .filter {
                when (trustStatementActor) {
                    TrustStatementActor.ISSUER -> it.vct == ISSUANCE_VCT
                    TrustStatementActor.VERIFIER -> it.vct == VERIFICATION_VCT
                }
            }.filter { trustStatement ->
                val trustDomain = getTrustDomainFromDid(actorDid).get() ?: return@filter false

                environmentSetupRepo.trustRegistryTrustedDids[trustDomain]?.contains(trustStatement.vcIssuer) ?: false
            }

        if (filteredStatements.isEmpty()) {
            return@coroutineBinding Ok(VcSchemaTrustStatus.UNPROTECTED).bind()
        }

        val validTrustStatements = filteredStatements.mapNotNull {
            validateTrustStatement(trustStatement = it, actorDid = actorDid).get()
        }

        val vcSchemaTrustStatements = validTrustStatements.mapNotNull {
            when (trustStatementActor) {
                TrustStatementActor.ISSUER -> {
                    safeJson.safeDecodeElementTo<IssuanceV1TrustStatement>(
                        validTrustStatements.first().processedJson
                    ).get()
                }
                TrustStatementActor.VERIFIER -> {
                    safeJson.safeDecodeElementTo<VerificationV1TrustStatement>(
                        validTrustStatements.first().processedJson
                    ).get()
                }
            }
        }.filter { it.vcSchemaId == vcSchemaId }

        if (vcSchemaTrustStatements.size == 1) {
            VcSchemaTrustStatus.TRUSTED
        } else {
            VcSchemaTrustStatus.NOT_TRUSTED
        }
    }

    private companion object {
        const val ISSUANCE_VCT = "TrustStatementIssuanceV1"
        const val VERIFICATION_VCT = "TrustStatementVerificationV1"
    }
}
