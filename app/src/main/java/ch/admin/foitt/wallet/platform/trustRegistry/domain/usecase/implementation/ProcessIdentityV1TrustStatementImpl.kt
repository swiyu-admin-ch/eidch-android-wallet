package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.GetTrustUrlFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessIdentityV1TrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementRepositoryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementType
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toProcessIdentityV1TrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.repository.TrustStatementRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustUrlFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatement
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class ProcessIdentityV1TrustStatementImpl @Inject constructor(
    private val getTrustUrlFromDid: GetTrustUrlFromDid,
    private val trustStatementRepository: TrustStatementRepository,
    private val validateTrustStatement: ValidateTrustStatement,
    private val safeJson: SafeJson,
) : ProcessIdentityV1TrustStatement {
    override suspend fun invoke(
        did: String
    ): Result<IdentityV1TrustStatement, ProcessIdentityV1TrustStatementError> = coroutineBinding {
        val trustUrl = getTrustUrlFromDid(
            trustStatementType = TrustStatementType.IDENTITY,
            actorDid = did,
            vcSchemaId = null,
        )
            .mapError(GetTrustUrlFromDidError::toProcessIdentityV1TrustStatementError)
            .bind()

        val trustStatementsRaw = trustStatementRepository.fetchTrustStatements(trustUrl)
            .mapError(TrustStatementRepositoryError::toProcessIdentityV1TrustStatementError)
            .bind()

        val trustStatements = runSuspendCatching {
            trustStatementsRaw.map { VcSdJwt(it) }
        }.mapError { throwable ->
            throwable.toProcessIdentityV1TrustStatementError(message = "trust statement vc sd jwt creation failed")
        }.bind()

        val identityStatements = trustStatements.filter { it.vct == IDENTITY_VCT }

        val validIdentityStatements = identityStatements.mapNotNull {
            validateTrustStatement(
                trustStatement = it,
                actorDid = did,
            ).get()
        }

        if (validIdentityStatements.size != 1) {
            return@coroutineBinding Err(TrustRegistryError.InvalidTrustStatus).bind<IdentityV1TrustStatement>()
        }

        val identityTrustStatement = safeJson.safeDecodeElementTo<IdentityV1TrustStatement>(
            validIdentityStatements.first().processedJson
        ).mapError(JsonParsingError::toProcessIdentityV1TrustStatementError)
            .bind()

        identityTrustStatement
    }

    private companion object {
        const val IDENTITY_VCT = "TrustStatementIdentityV1"
    }
}
