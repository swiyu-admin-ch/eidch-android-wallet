package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusProperties
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchCredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toValidateTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatement
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class ValidateTrustStatementImpl @Inject constructor(
    private val environmentSetupRepo: EnvironmentSetupRepository,
    private val verifyJwtSignature: VerifyJwtSignature,
    private val fetchCredentialStatus: FetchCredentialStatus,
    private val safeJson: SafeJson,
) : ValidateTrustStatement {
    override suspend fun invoke(
        trustStatement: VcSdJwt,
        actorDid: String,
    ): Result<VcSdJwt, ValidateTrustStatementError> = coroutineBinding {
        runSuspendCatching {
            check(trustStatement.hasTrustedDid()) {
                errorMessageStart + "issuer did is not trusted"
            }

            // Header checks
            check(trustStatement.type == VCSDJWT_TYPE_VALUE) {
                errorMessageStart + "type is unsupported"
            }
            check(trustStatement.algorithm == SigningAlgorithm.ES256.stdName) {
                errorMessageStart + "algorithm is unsupported"
            }

            verifyJwtSignature(
                did = trustStatement.vcIssuer,
                kid = trustStatement.kid,
                jwt = trustStatement,
            )
                .mapError(VerifyJwtError::toValidateTrustStatementError)
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

            val trustStatementStatus = fetchCredentialStatus(trustStatement.vcIssuer, statusProperties).get()

            check(trustStatementStatus == CredentialStatus.VALID) {
                "$errorMessageStart status is not valid"
            }

            trustStatement
        }.mapError { throwable ->
            throwable.toValidateTrustStatementError("ValidateTrustStatement error")
        }.bind()
    }

    private fun VcSdJwt.hasTrustedDid() = environmentSetupRepo.trustRegistryTrustedDids.contains(vcIssuer)

    private val errorMessageStart = "Trust statement "

    private companion object {
        const val VCSDJWT_TYPE_VALUE = "vc+sd-jwt"
    }
}
