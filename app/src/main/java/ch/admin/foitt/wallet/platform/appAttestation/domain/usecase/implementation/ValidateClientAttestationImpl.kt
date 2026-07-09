package ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.implementation

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwk.hasSameCurveAs
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationAlgorithm
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationResponse
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.ValidateClientAttestation
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import ch.admin.foitt.wallet.platform.utils.toBase64StringUrlEncodedWithoutPadding
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOrThrow
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.erdtman.jcs.JsonCanonicalizer
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

class ValidateClientAttestationImpl @Inject constructor(
    private val didResolverHelper: DidResolverHelper,
    private val environmentSetupRepo: EnvironmentSetupRepository,
    private val verifyJwtSignatureFromDid: VerifyJwtSignatureFromDid,
    private val safeJson: SafeJson,
) : ValidateClientAttestation {
    override suspend operator fun invoke(
        keyStoreAlias: String,
        originalJwk: Jwk,
        clientAttestationResponse: ClientAttestationResponse,
    ): Result<ClientAttestation, AttestationError.ValidationError> = coroutineBinding {
        runSuspendCatching {
            val attestation = Jwt(clientAttestationResponse.clientAttestation)
            val keyId = checkNotNull(attestation.keyId) { "kid is missing" }

            val did = didResolverHelper.getDidStringFromAbsoluteKeyId(keyId)
                .mapError { throwable ->
                    Timber.w(t = throwable, message = "Client attestation validation failed")
                    AttestationError.ValidationError(throwable.message)
                }.bind()

            check(did in environmentSetupRepo.attestationsServiceTrustedDids)

            check(attestation.type == SUPPORTED_ATTESTATION_TYPE) {
                "type is unsupported"
            }

            val attestationSignatureAlgorithm = AttestationAlgorithm.fromJwt(attestation)
            checkNotNull(attestationSignatureAlgorithm) {
                "algorithm is unsupported"
            }

            val verificationResult = verifyJwtSignatureFromDid(
                kid = keyId,
                jwt = attestation,
            )
            check(verificationResult.isOk) {
                "signature verification failed"
            }

            val expiredAt = checkNotNull(attestation.expInstant) { "exp is missing" }
            check(Instant.now().isBefore(expiredAt)) {
                "attestation is expired"
            }

            checkNotNull(attestation.nbfInstant) {
                "attestation nbf is missing"
            }

            check(attestation.payloadJson[KEY_WALLET_NAME]?.jsonPrimitive?.content == environmentSetupRepo.appId) {
                "wallet name is not ${environmentSetupRepo.appId}"
            }

            val attestedJwkValue = checkNotNull(attestation.payloadJson[KEY_CONFIRMATION]?.jsonObject?.get(KEY_JWK)) {
                "$KEY_CONFIRMATION $KEY_JWK is missing"
            }
            val attestedJwk = safeJson.safeDecodeElementTo<Jwk>(attestedJwkValue).getOrThrow {
                when (it) {
                    is JsonError.Unexpected -> it.throwable
                }
            }

            val attestedDidJwk = "did:jwk:" + JsonCanonicalizer(attestedJwkValue.toString())
                .encodedUTF8
                .toBase64StringUrlEncodedWithoutPadding()

            check(attestedDidJwk == attestation.subject) {
                "subject jwk is not the same as the cnf jwk"
            }

            check(attestedJwk.hasSameCurveAs(originalJwk)) {
                "attested key is not the same as the original"
            }

            ClientAttestation(keyStoreAlias, attestation)
        }.mapError { throwable ->
            Timber.w(t = throwable, message = "Client attestation validation failed")
            AttestationError.ValidationError(throwable.message)
        }.bind()
    }

    companion object {
        private const val SUPPORTED_ATTESTATION_TYPE = "oauth-client-attestation+jwt"
        private const val KEY_WALLET_NAME = "wallet_name"
        private const val KEY_CONFIRMATION = "cnf"
        private const val KEY_JWK = "jwk"
    }
}
