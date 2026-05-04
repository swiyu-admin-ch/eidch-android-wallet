package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.di.DefaultDispatcher
import ch.admin.foitt.openid4vc.domain.model.DigestAlgorithm
import ch.admin.foitt.openid4vc.domain.model.GetKeyPairError
import ch.admin.foitt.openid4vc.domain.model.GetSoftwareKeyPairError
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.CreateVcSdJwtVerifiablePresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.toCreateVcSdJwtVerifiablePresentationError
import ch.admin.foitt.openid4vc.domain.model.toJWSAlgorithm
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.GetHardwareKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.GetSoftwareKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.CreateVcSdJwtVerifiablePresentation
import ch.admin.foitt.openid4vc.utils.Constants.ANDROID_KEY_STORE
import ch.admin.foitt.openid4vc.utils.JsonParsingError
import ch.admin.foitt.openid4vc.utils.SafeJson
import ch.admin.foitt.openid4vc.utils.createDigest
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.toErrorIfNull
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.security.KeyPair
import java.time.Instant
import javax.inject.Inject

internal class CreateVcSdJwtVerifiablePresentationImpl @Inject constructor(
    private val safeJson: SafeJson,
    private val getHardwareKeyPair: GetHardwareKeyPair,
    private val getSoftwareKeyPair: GetSoftwareKeyPair,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : CreateVcSdJwtVerifiablePresentation {
    override suspend fun invoke(
        credential: VcSdJwtCredential,
        keyBinding: KeyBinding?,
        requestedFields: List<String>,
        authorizationRequest: AuthorizationRequest,
    ): Result<String, CreateVcSdJwtVerifiablePresentationError> = withContext(defaultDispatcher) {
        coroutineBinding {
            val sdJwtWithDisclosures = runSuspendCatching {
                credential.createVerifiableCredential(requestedFields)
            }.mapError { throwable -> throwable.toCreateVcSdJwtVerifiablePresentationError("createVerifiableCredential error") }
                .bind()

            if (keyBinding != null) {
                val base64UrlEncodedSdHash = runSuspendCatching {
                    sdJwtWithDisclosures.createDigest(HASH_ALGORITHM)
                }.mapError { throwable -> throwable.toCreateVcSdJwtVerifiablePresentationError("sdJwtWithDisclosures.createDigest error") }
                    .bind()

                val keyPair = getKeyPair(keyBinding).bind()

                val keyBindingJwt =
                    createKeyBindingJwt(keyBinding.algorithm, base64UrlEncodedSdHash, authorizationRequest)
                val jwk = getKeyBindingJwk(credential).bind()
                val signer = ECDSASigner(keyPair.private, Curve(jwk.crv))
                keyBindingJwt.sign(signer)
                val keyBindingJwtString = keyBindingJwt.serialize()

                sdJwtWithDisclosures + keyBindingJwtString
            } else {
                sdJwtWithDisclosures
            }
        }
    }

    private suspend fun getKeyPair(keyBinding: KeyBinding): Result<KeyPair, CreateVcSdJwtVerifiablePresentationError> = coroutineBinding {
        val keyPair = when (keyBinding.bindingType) {
            KeyBindingType.SOFTWARE -> {
                if (keyBinding.publicKey != null && keyBinding.privateKey != null) {
                    getSoftwareKeyPair(keyBinding.publicKey, keyBinding.privateKey)
                        .mapError(GetSoftwareKeyPairError::toCreateVcSdJwtVerifiablePresentationError)
                        .bind()
                } else {
                    Err(PresentationRequestError.InvalidKeyPairError).bind<KeyPair>()
                }
            }
            KeyBindingType.HARDWARE -> getHardwareKeyPair(keyBinding.identifier, ANDROID_KEY_STORE)
                .mapError(GetKeyPairError::toCreateVcSdJwtVerifiablePresentationError)
                .bind()
        }

        keyPair
    }

    private fun createKeyBindingJwt(
        keyBindingAlgorithm: SigningAlgorithm,
        base64UrlEncodedSdHash: String,
        authorizationRequest: AuthorizationRequest,
    ): SignedJWT {
        val jwtHeader = JWSHeader.Builder(keyBindingAlgorithm.toJWSAlgorithm())
            .type(JOSEObjectType(HEADER_TYPE))
            .build()
        val jwtBody = JWTClaimsSet.Builder()
            .claim(CLAIM_KEY_SD_HASH, base64UrlEncodedSdHash)
            .claim(CLAIM_KEY_AUD, authorizationRequest.clientId)
            .claim(CLAIM_KEY_NONCE, authorizationRequest.nonce)
            .claim(CLAIM_KEY_IAT, Instant.now().epochSecond)
            .build()

        return SignedJWT(jwtHeader, jwtBody)
    }

    private suspend fun getKeyBindingJwk(
        credential: VcSdJwtCredential
    ): Result<Jwk, CreateVcSdJwtVerifiablePresentationError> = coroutineBinding {
        val cnfJwk = runSuspendCatching {
            credential.cnfJwk
        }.mapError { throwable ->
            throwable.toCreateVcSdJwtVerifiablePresentationError("credential.cnfJwk error")
        }.toErrorIfNull {
            PresentationRequestError.Unexpected(IllegalStateException("credential cnfJwk is null"))
        }.bind()

        safeJson.safeDecodeFromJsonElement<Jwk>(cnfJwk)
            .mapError(JsonParsingError::toCreateVcSdJwtVerifiablePresentationError).bind()
    }

    companion object {
        val HASH_ALGORITHM = DigestAlgorithm.SHA256
        const val HEADER_TYPE = "kb+jwt"
        const val CLAIM_KEY_SD_HASH = "sd_hash"
        const val CLAIM_KEY_AUD = "aud"
        const val CLAIM_KEY_NONCE = "nonce"
        const val CLAIM_KEY_IAT = "iat"
    }
}
