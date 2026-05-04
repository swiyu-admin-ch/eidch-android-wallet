package ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.KeyStorageSecurityLevel
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwk.hasSameCurveAs
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationAlgorithm
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.KeyAttestationJwt
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ValidateKeyAttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.ValidateKeyAttestation
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOrThrow
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

internal class ValidateKeyAttestationImpl @Inject constructor(
    private val environmentSetupRepo: EnvironmentSetupRepository,
    private val verifyJwtSignatureFromDid: VerifyJwtSignatureFromDid,
    private val safeJson: SafeJson,
) : ValidateKeyAttestation {
    override suspend fun invoke(
        originalJwk: Jwk,
        keyAttestationJwt: KeyAttestationJwt,
    ): Result<Jwt, ValidateKeyAttestationError> = runSuspendCatching {
        val attestation = Jwt(keyAttestationJwt.value)

        val issuer = checkNotNull(attestation.iss) { "issuer did is null" }
        check(issuer in environmentSetupRepo.attestationsServiceTrustedDids) {
            "issuer did is not from the trusted attestations service"
        }

        val keyId = checkNotNull(attestation.keyId) {
            "kid is missing"
        }

        checkKidDid(keyId)

        check(attestation.type == SUPPORTED_ATTESTATION_TYPE) {
            "type is unsupported"
        }

        val attestationSignatureAlgorithm = AttestationAlgorithm.fromJwt(attestation)

        checkNotNull(attestationSignatureAlgorithm) {
            "algorithm is unsupported"
        }

        val verificationResult = verifyJwtSignatureFromDid(
            did = issuer,
            kid = keyId,
            jwt = attestation,
        )

        check(verificationResult.isOk) {
            "signature verification failed"
        }

        checkNotNull(attestation.issuedAt) { "iat is missing" }
        val expiredAt = checkNotNull(attestation.expInstant) { "exp is missing" }
        check(Instant.now().isBefore(expiredAt)) {
            "attestation is expired"
        }

        val attestedKeys: JsonArray = checkNotNull(attestation.payloadJson[KEY_ATTESTED_KEYS] as? JsonArray) {
            "attested key is missing"
        }

        val attestedJwk = safeJson.safeDecodeElementTo<Jwk>(attestedKeys.first())
            .getOrThrow {
                when (it) {
                    is JsonError.Unexpected -> it.throwable
                }
            }

        check(attestedJwk.hasSameCurveAs(originalJwk)) {
            "attested key is not the same as the original"
        }

        attestation.checkKeyStorageValues()

        attestation
    }.mapError { throwable ->
        Timber.w(t = throwable, message = "Key attestation validation failed")
        AttestationError.ValidationError(throwable.message)
    }

    private fun Jwt.checkKeyStorageValues() {
        val keyStorage: JsonArray = checkNotNull(payloadJson[KEY_KEY_STORAGE] as? JsonArray) {
            "key storage is missing"
        }

        keyStorage.map { value ->
            checkNotNull(KeyStorageSecurityLevel.get(value.jsonPrimitive.content)) {
                "key storage value is unsupported"
            }
        }
    }

    private fun checkKidDid(kid: String) {
        val did = kid.split("#").first()

        check(did in environmentSetupRepo.attestationsServiceTrustedDids) {
            "kid did is not from the trusted attestations service"
        }
    }

    companion object {
        private const val SUPPORTED_ATTESTATION_TYPE = "key-attestation+jwt"
        private const val KEY_ATTESTED_KEYS = "attested_keys"
        private const val KEY_KEY_STORAGE = "key_storage"
    }
}
