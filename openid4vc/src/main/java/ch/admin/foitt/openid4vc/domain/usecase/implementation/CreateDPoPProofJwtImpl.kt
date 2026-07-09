package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.CreateJwkError
import ch.admin.foitt.openid4vc.domain.model.DigestAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateDPoPProofJwtError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toCreateDPoPProofJwtError
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.toCurve
import ch.admin.foitt.openid4vc.domain.model.toJWSAlgorithm
import ch.admin.foitt.openid4vc.domain.usecase.CreateDPoPProofJwt
import ch.admin.foitt.openid4vc.domain.usecase.CreateJwk
import ch.admin.foitt.openid4vc.utils.Constants
import ch.admin.foitt.openid4vc.utils.toBase64StringUrlEncodedWithoutPadding
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.net.URL
import java.security.MessageDigest
import java.util.Date
import java.util.UUID
import javax.inject.Inject

internal class CreateDPoPProofJwtImpl @Inject constructor(
    private val createJwk: CreateJwk,
) : CreateDPoPProofJwt {
    override suspend fun invoke(
        method: String,
        url: URL,
        keyPair: JWSKeyPair,
        nonce: String?,
        accessToken: String?,
        keyAttestationJwt: Jwt?,
    ): Result<String, CreateDPoPProofJwtError> = coroutineBinding {
        val jwk = createJwk(keyPair = keyPair.keyPair, algorithm = keyPair.algorithm)
            .mapError(CreateJwkError::toCreateDPoPProofJwtError)
            .bind()

        val header = JWSHeader.Builder(keyPair.algorithm.toJWSAlgorithm())
            .jwk(JWK.parse(jwk))
            .type(JOSEObjectType(Constants.DPOP_JWT_PROOF_HEADER_TYPE))
            .customParam(Constants.DPOP_SWISS_PROFILE_HEADER, Constants.DPOP_SWISS_PROFILE_VERSION)
            .apply {
                keyAttestationJwt?.let { customParam("key_attestation", it.rawJwt) }
            }
            .build()

        val payload = JWTClaimsSet.Builder()
            .jwtID(UUID.randomUUID().toString())
            .claim("htm", method.uppercase())
            .claim("htu", normalizeTargetUri(url))
            .issueTime(Date((System.currentTimeMillis() / 1000) * 1000))
            .apply {
                nonce?.let { claim("nonce", it) }
                accessToken?.let { claim("ath", createAccessTokenHash(it)) }
            }
            .build()

        runSuspendCatching {
            SignedJWT(header, payload).apply {
                sign(ECDSASigner(keyPair.keyPair.private, keyPair.algorithm.toCurve()))
            }.serialize()
        }.mapError { throwable ->
            CredentialOfferError.Unexpected(throwable)
        }.bind()
    }

    private fun normalizeTargetUri(url: URL): String {
        val protocol = url.protocol
        val host = url.host
        val port = if (url.port == -1) "" else ":${url.port}"
        val path = url.path.ifEmpty { "/" }
        return "$protocol://$host$port$path"
    }

    private fun createAccessTokenHash(accessToken: String): String {
        val digest = MessageDigest.getInstance(DigestAlgorithm.SHA256.stdName)
        val hash = digest.digest(accessToken.toByteArray(Charsets.US_ASCII))
        // Workaround for a backend issue: RFC 9449 expects an unpadded base64url-encoded `ath`,
        // but the current issuer implementation only accepts the value with trailing `=` padding.
        // Once the backend is fixed, the `+ "="` should be removed.
        return hash.toBase64StringUrlEncodedWithoutPadding() + "="
    }
}
