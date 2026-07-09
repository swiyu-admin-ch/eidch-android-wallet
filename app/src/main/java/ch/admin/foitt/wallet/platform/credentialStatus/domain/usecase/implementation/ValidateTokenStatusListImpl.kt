package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureFromDidError
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListResponse
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.ValidateTokenStatusStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.toValidateTokenStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.toValidateTokenStatusStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.ValidateTokenStatusList
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import java.time.Instant
import javax.inject.Inject

class ValidateTokenStatusListImpl @Inject constructor(
    private val didResolverHelper: DidResolverHelper,
    private val verifyJwtSignatureFromDid: VerifyJwtSignatureFromDid,
    private val safeJson: SafeJson,
) : ValidateTokenStatusList {
    override suspend fun invoke(
        credentialIssuer: String,
        statusListJwt: String,
        subject: String,
    ): Result<TokenStatusListResponse, ValidateTokenStatusStatusListError> = coroutineBinding {
        val jwt = runSuspendCatching {
            val jwt = Jwt(statusListJwt)

            check(jwt.type == SUPPORTED_STATUS_TYPE) { "Status list token is not of proper type" }
            checkNotNull(jwt.issuedAt) { "Status list token iat claim is missing" }
            check(subject == jwt.subject) { "Subject does not match" }
            jwt.expInstant?.let { expirationTime ->
                check(expirationTime.isAfter(Instant.now())) { "Status list token is expired" }
            }

            jwt
        }.mapError { throwable ->
            throwable.toValidateTokenStatusStatusListError("ValidateTokenStatusList error")
        }.bind()

        val keyId = runSuspendCatching {
            checkNotNull(jwt.keyId) { "keyId is missing" }
        }.mapError { throwable ->
            throwable.toValidateTokenStatusStatusListError("ValidateTokenStatusList error")
        }.bind()

        val did = didResolverHelper.getDidStringFromAbsoluteKeyId(keyId)
            .mapError { throwable ->
                throwable.toValidateTokenStatusStatusListError("ValidateTokenStatusList error")
            }.bind()

        runSuspendCatching {
            check(credentialIssuer == did) { "Issuers do not match" }
        }.mapError { throwable ->
            throwable.toValidateTokenStatusStatusListError("ValidateTokenStatusList error")
        }.bind()

        verifyJwtSignatureFromDid(
            kid = keyId,
            jwt = jwt,
        ).mapError(VerifyJwtSignatureFromDidError::toValidateTokenStatusListError)
            .bind()

        parseResponse(jwt.payloadString).bind()
    }

    private fun parseResponse(payload: String) =
        safeJson.safeDecodeStringTo<TokenStatusListResponse>(string = payload)
            .mapError(JsonParsingError::toValidateTokenStatusListError)

    companion object {
        private const val SUPPORTED_STATUS_TYPE = "statuslist+jwt"
    }
}
