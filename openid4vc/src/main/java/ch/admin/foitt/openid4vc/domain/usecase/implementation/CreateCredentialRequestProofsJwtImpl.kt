package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.CreateJwkError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialRequestProofsJwt
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.toCurve
import ch.admin.foitt.openid4vc.domain.model.toJWSAlgorithm
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequestProofsJwt
import ch.admin.foitt.openid4vc.domain.usecase.CreateJwk
import ch.admin.foitt.openid4vc.utils.Constants
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.util.Date
import javax.inject.Inject

internal class CreateCredentialRequestProofsJwtImpl @Inject constructor(
    private val createJwk: CreateJwk,
) : CreateCredentialRequestProofsJwt {
    override suspend operator fun invoke(
        keyPairs: List<BindingKeyPair>,
        issuer: String,
        cNonce: String?,
    ) = coroutineBinding {
        val attestationJwts = keyPairs.map { bindingKeyPair ->
            val keyPair = bindingKeyPair.keyPair
            val attestationJwt = bindingKeyPair.attestationJwt
            val jwk = createJwk(keyPair = keyPair.keyPair, algorithm = keyPair.algorithm, asDid = false)
                .mapError(CreateJwkError::toFetchVerifiableCredentialError)
                .bind()
            val header = createHeader(keyPair, jwk, attestationJwt)
            val payload = createPayload(issuer, cNonce)
            createJwt(
                header = header,
                payload = payload,
                keyPair = keyPair
            ).bind()
        }
        CredentialRequestProofsJwt(attestationJwts)
    }

    private fun createHeader(
        keyPair: JWSKeyPair,
        jwk: String,
        attestationJwt: Jwt?,
    ) = JWSHeader
        .Builder(
            keyPair.algorithm.toJWSAlgorithm()
        )
        .jwk(JWK.parse(jwk))
        .type(JOSEObjectType(Constants.OID4VCI_JWT_PROOF_HEADER_TYPE))
        .apply {
            attestationJwt?.let { customParam("key_attestation", attestationJwt.rawJwt) }
        }
        .build()

    private fun createPayload(
        issuer: String,
        cNonce: String?,
    ) = JWTClaimsSet
        .Builder()
        .audience(issuer)
        .apply {
            cNonce?.let { claim("nonce", cNonce) }
        }
        .issueTime(Date())
        .build()

    private fun createJwt(
        header: JWSHeader,
        payload: JWTClaimsSet,
        keyPair: JWSKeyPair
    ) = runSuspendCatching {
        val jwt = SignedJWT(header, payload)
        val signer = ECDSASigner(keyPair.keyPair.private, keyPair.algorithm.toCurve())
        jwt.sign(signer)
        jwt.serialize()
    }.mapError { throwable ->
        CredentialOfferError.Unexpected(throwable)
    }
}
