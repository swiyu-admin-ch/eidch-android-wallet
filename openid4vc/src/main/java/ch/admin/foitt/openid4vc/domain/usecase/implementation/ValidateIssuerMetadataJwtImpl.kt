package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.anycredential.getValidity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.ValidateIssuerMetadataJwtError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toValidateIssuerMetadataJwtError
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureFromDidError
import ch.admin.foitt.openid4vc.domain.usecase.ValidateIssuerMetadataJwt
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class ValidateIssuerMetadataJwtImpl @Inject constructor(
    private val verifyJwtSignatureFromDid: VerifyJwtSignatureFromDid,
) : ValidateIssuerMetadataJwt {
    override suspend fun invoke(credentialIssuerIdentifier: String, jwt: Jwt, type: String?): Result<Unit, ValidateIssuerMetadataJwtError> =
        coroutineBinding {
            runSuspendCatching {
                check(jwt.algorithm == SigningAlgorithm.ES256.stdName) { "Unsupported JWT algorithm: ${jwt.algorithm}" }
                type?.let {
                    check(jwt.type == type) { "Unsupported JWT type: ${jwt.type}" }
                }

                checkNotNull(jwt.issuedAt) { "iat is missing" }
                val subject = checkNotNull(jwt.subject) { "sub is missing" }
                check(subject == credentialIssuerIdentifier) {
                    "sub ('$subject') is not matching credential issuer identifier ('$credentialIssuerIdentifier')"
                }

                val validity = getValidity(jwt.issuedAt?.epochSecond, jwt.expInstant?.epochSecond)
                check(validity == Validity.Valid) { "JWT not in validity period (iat or exp)" }

                val issuer = checkNotNull(jwt.issuer) { "iss is missing and kid parameter does not contain issuer did" }
                val keyId = checkNotNull(jwt.keyId) { "keyId is missing" }

                verifyJwtSignatureFromDid(
                    did = issuer,
                    kid = keyId,
                    jwt = jwt,
                ).mapError(VerifyJwtSignatureFromDidError::toValidateIssuerMetadataJwtError)
                    .bind()
            }.mapError {
                CredentialOfferError.InvalidSignedMetadata(it.localizedMessage ?: "Unknown")
            }.bind()
        }

    private val Jwt.issuer: String?
        get() = iss ?: keyId?.split("#")?.firstOrNull()
}
