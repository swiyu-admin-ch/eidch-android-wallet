package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureFromDidError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwt
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusProperties
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchCredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toValidateTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatement
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class ValidateTrustStatementImpl @Inject constructor(
    private val didResolverHelper: DidResolverHelper,
    private val getTrustDomainFromDid: GetTrustDomainFromDid,
    private val environmentSetupRepo: EnvironmentSetupRepository,
    private val verifyJwtSignatureFromDid: VerifyJwtSignatureFromDid,
    private val fetchCredentialStatus: FetchCredentialStatus,
    private val safeJson: SafeJson,
) : ValidateTrustStatement {
    override suspend fun invoke(
        trustStatement: VcSdJwt,
        actorDid: String,
    ): Result<VcSdJwt, ValidateTrustStatementError> = coroutineBinding {
        val trustStatementDid = didResolverHelper.getDidStringFromAbsoluteKeyId(trustStatement.kid)
            .mapError { throwable ->
                throwable.toValidateTrustStatementError("ValidateTrustStatement error")
            }.bind()

        runSuspendCatching {
            check(hasTrustedDid(actorDid, trustStatementDid)) {
                errorMessageStart + "issuer did is not trusted"
            }

            // Header checks
            check(trustStatement.type == VCSDJWT_TYPE_VALUE) {
                errorMessageStart + "type is unsupported"
            }
            check(trustStatement.algorithm == SigningAlgorithm.ES256.stdName) {
                errorMessageStart + "algorithm is unsupported"
            }

            verifyJwtSignatureFromDid(
                kid = trustStatement.kid,
                jwt = trustStatement,
            ).mapError(VerifyJwtSignatureFromDidError::toValidateTrustStatementError)
                .bind()

            // Claim checks
            checkNotNull(trustStatement.issuedAt) { "$errorMessageStart iat is missing" }
            checkNotNull(trustStatement.subject) { "$errorMessageStart sub is missing" }
            check(trustStatement.subject == actorDid) { "trust statement is not from did we requested it for" }
            check(trustStatement.jwtValidity == Validity.Valid) {
                "$errorMessageStart is ${trustStatement.jwtValidity}"
            }

            // Status of trust statement
            val statusJsonElement = checkNotNull(trustStatement.status)

            val statusProperties =
                checkNotNull(safeJson.safeDecodeElementTo<CredentialStatusProperties>(statusJsonElement).get()) {
                    "$errorMessageStart has no status"
                }

            val trustStatementStatus = fetchCredentialStatus(trustStatementDid, statusProperties).get()
            check(trustStatementStatus == CredentialStatus.VALID) {
                "$errorMessageStart status is not valid"
            }
        }.mapError { throwable ->
            throwable.toValidateTrustStatementError("ValidateTrustStatement error")
        }.bind()

        trustStatement
    }

    private fun hasTrustedDid(actorDid: String, trustStatementDid: String): Boolean {
        val trustDomain = getTrustDomainFromDid(actorDid).get() ?: return false
        return environmentSetupRepo.trustV1TrustRegistryTrustedDids[trustDomain]?.contains(trustStatementDid) ?: false
    }

    private val errorMessageStart = "Trust statement "

    companion object {
        const val VCSDJWT_TYPE_VALUE = "vc+sd-jwt"
    }
}
