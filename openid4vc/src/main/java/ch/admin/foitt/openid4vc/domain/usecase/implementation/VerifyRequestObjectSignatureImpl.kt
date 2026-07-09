package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureError
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureFromDidError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientIdentifier
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObject
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObjectVerificationOutcome
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyRequestObjectSignatureError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.toVerifyRequestObjectSignatureError
import ch.admin.foitt.openid4vc.domain.usecase.VerifyRequestObjectSignature
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import ch.admin.foitt.openid4vc.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

internal class VerifyRequestObjectSignatureImpl @Inject constructor(
    private val didResolverHelper: DidResolverHelper,
    private val verifyJwtSignatureFromDid: VerifyJwtSignatureFromDid,
    private val verifyJwtSignature: VerifyJwtSignature,
    private val safeJson: SafeJson,
) : VerifyRequestObjectSignature {

    override suspend fun invoke(
        requestObject: RequestObject,
        trustedAttestationDids: List<String>,
    ): Result<RequestObjectVerificationOutcome, VerifyRequestObjectSignatureError> = coroutineBinding {
        val clientIdentifier = ClientIdentifier.fromRequestObject(requestObject)
            .mapError { throwable ->
                VcSdJwtError.Unexpected(throwable)
            }.bind()
        val jwt = requestObject.jwt

        when (clientIdentifier.clientIdPrefix) {
            ClientIdentifier.ClientIdPrefix.DecentralizedIdentifier -> {
                verifyWithDid(
                    jwt = jwt,
                    clientIdentifier = clientIdentifier
                ).bind()
                RequestObjectVerificationOutcome.DID_PATH
            }

            ClientIdentifier.ClientIdPrefix.VerifierAttestationJwt -> {
                verifyWithAttestation(
                    jwt = jwt,
                    clientIdentifier = clientIdentifier,
                    redirectUri = requestObject.redirectUri,
                    trustedAttestationDids = trustedAttestationDids,
                ).bind()
            }
        }
    }

    private suspend fun verifyWithDid(
        jwt: Jwt,
        clientIdentifier: ClientIdentifier,
    ): Result<Unit, VerifyRequestObjectSignatureError> = coroutineBinding {
        val did = clientIdentifier.clientId
        if (!did.matches(DID_REGEX)) return@coroutineBinding Err(VcSdJwtError.InvalidRequestObject).bind<Unit>()

        val jwtKid = runSuspendCatching {
            checkNotNull(jwt.keyId)
        }.mapError { throwable -> VcSdJwtError.Unexpected(throwable) }
            .bind()
        val jwtDid = didResolverHelper.getDidStringFromAbsoluteKeyId(jwtKid)
            .mapError { throwable -> VcSdJwtError.Unexpected(throwable) }
            .bind()
        if (jwtDid != did) return@coroutineBinding Err(VcSdJwtError.InvalidRequestObject).bind<Unit>()

        verifyJwtSignatureFromDid(kid = jwtKid, jwt = jwt)
            .mapError(VerifyJwtSignatureFromDidError::toVerifyRequestObjectSignatureError)
            .bind()
    }

    private suspend fun verifyWithAttestation(
        jwt: Jwt,
        clientIdentifier: ClientIdentifier,
        redirectUri: String?,
        trustedAttestationDids: List<String>,
    ): Result<RequestObjectVerificationOutcome, VerifyRequestObjectSignatureError> = coroutineBinding {
        val attestationJwtRawString = jwt.signedJwt.header.getCustomParam("jwt") as? String
            ?: Err(VcSdJwtError.InvalidJwt).bind()

        val attestationJwt = runSuspendCatching {
            val attestationJwt = Jwt(attestationJwtRawString)

            val sub = checkNotNull(attestationJwt.subject) { "Subject must not be null" }
            check(sub == clientIdentifier.clientId) { "sub claim does not match clientId" }
            check(attestationJwt.jwtValidity == Validity.Valid) { "jwt is not valid" }
            check(attestationJwt.type == ATTESTATION_HEADER_TYP) { "jwt has incorrect type" }

            val attestationRedirectUris = attestationJwt.payloadJson["redirect_uris"]?.jsonArray?.toList()
            attestationRedirectUris?.let { uris ->
                uris.any { it.jsonPrimitive.content == redirectUri }
            }

            attestationJwt
        }.mapError { throwable ->
            VcSdJwtError.Unexpected(throwable)
        }.bind()

        val (attestationSignatureDid, kid) = runSuspendCatching {
            val keyId = checkNotNull(attestationJwt.signedJwt.header.keyID) {
                "kid is missing"
            }
            val did = didResolverHelper.getDidStringFromAbsoluteKeyId(keyId)
                .mapError { throwable -> VcSdJwtError.Unexpected(throwable) }
                .bind()

            did to keyId
        }.mapError { throwable -> VcSdJwtError.Unexpected(throwable) }
            .bind()

        // validate signature of attestation jwt
        verifyJwtSignatureFromDid(
            kid = kid,
            jwt = attestationJwt,
        ).mapError(VerifyJwtSignatureFromDidError::toVerifyRequestObjectSignatureError)
            .bind()

        val requestObjectPublicKey = resolvePublicKeyFromAttestation(attestationJwt = attestationJwt).bind()

        // validate signature of request object jwt with the cnf of the attestation jwt
        verifyJwtSignature(jwt = jwt, publicKey = requestObjectPublicKey)
            .mapError(VerifyJwtSignatureError::toVerifyRequestObjectSignatureError)
            .bind()

        if (attestationSignatureDid in trustedAttestationDids) {
            RequestObjectVerificationOutcome.ATTESTATION_TRUSTED
        } else {
            RequestObjectVerificationOutcome.ATTESTATION_UNTRUSTED
        }
    }

    private suspend fun resolvePublicKeyFromAttestation(
        attestationJwt: Jwt,
    ): Result<Jwk, VerifyRequestObjectSignatureError> = coroutineBinding {
        val jwkJson = runSuspendCatching {
            checkNotNull(attestationJwt.payloadJson["cnf"]?.jsonObject["jwk"]?.jsonObject) { "No confirmation key found" }
        }.mapError { VcSdJwtError.InvalidJwt }.bind()

        safeJson.safeDecodeFromJsonElement<Jwk>(jwkJson).mapError {
            VcSdJwtError.InvalidJwt
        }.bind()
    }

    private companion object {
        const val ATTESTATION_HEADER_TYP = "verifier-attestation+jwt"
        val DID_REGEX = Regex("^did:[a-z0-9]+:[a-zA-Z0-9.\\-_:]+$")
    }
}
