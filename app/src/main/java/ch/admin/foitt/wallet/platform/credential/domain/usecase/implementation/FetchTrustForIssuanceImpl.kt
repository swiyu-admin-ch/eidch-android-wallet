package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchTrustForIssuance
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustCheckResult
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementActor
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchVcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityV1TrustStatement
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getOrElse
import javax.inject.Inject

class FetchTrustForIssuanceImpl @Inject constructor(
    private val getActorEnvironment: GetActorEnvironment,
    private val processIdentityV1TrustStatement: ProcessIdentityV1TrustStatement,
    private val fetchVcSchemaTrustStatus: FetchVcSchemaTrustStatus,
) : FetchTrustForIssuance {

    override suspend operator fun invoke(
        issuerDid: String,
        vcSchemaId: String,
    ): TrustCheckResult {
        val environment = getActorEnvironment(issuerDid)

        val identityTrustStatement = when (environment) {
            ActorEnvironment.PRODUCTION, ActorEnvironment.BETA -> processIdentityV1TrustStatement(issuerDid).get()
            ActorEnvironment.EXTERNAL -> null
        }

        val issuanceTrustStatus = fetchVcSchemaTrustStatus(
            trustStatementActor = TrustStatementActor.ISSUER,
            actorDid = issuerDid,
            vcSchemaId = vcSchemaId,
        ).getOrElse { VcSchemaTrustStatus.UNPROTECTED }

        return TrustCheckResult(
            actorEnvironment = environment,
            actorTrustStatement = identityTrustStatement,
            vcSchemaTrustStatus = issuanceTrustStatus,
        )
    }
}
