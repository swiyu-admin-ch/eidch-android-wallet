package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.usecase.ResolvePublicKey
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignature
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.utils.SafeJson
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.FetchVersionEnforcementError
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.JwsDetachedHeader
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcementError
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.VerifyDetachedJws
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import com.nimbusds.jose.JWSObject
import timber.log.Timber
import javax.inject.Inject

internal class VerifyDetachedJwsImpl @Inject constructor(
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val didResolverHelper: DidResolverHelper,
    private val resolvePublicKey: ResolvePublicKey,
    private val verifyJwtSignature: VerifyJwtSignature,
    private val safeJson: SafeJson,
) : VerifyDetachedJws {

    private val supportedAlgorithms = setOf("ES256", "ES512")

    override suspend fun invoke(jws: String, body: String): Result<Unit, FetchVersionEnforcementError> = coroutineBinding {
        if (!jws.contains("..")) {
            return@coroutineBinding Err(VersionEnforcementError.SignatureValidationError).bind<Unit>()
        }
        val jwsObject = runCatching {
            JWSObject.parse(jws)
        }.getOrElse {
            return@coroutineBinding Err(VersionEnforcementError.SignatureValidationError).bind<Unit>()
        }

        val headerString = jwsObject.header.toBase64URL().decodeToString()
        val header = safeJson.safeDecodeStringTo<JwsDetachedHeader>(headerString)
            .mapError { VersionEnforcementError.SignatureValidationError }
            .bind()

        validateRfc7797Compliance(header).bind()

        if (header.alg !in supportedAlgorithms) {
            return@coroutineBinding Err(VersionEnforcementError.SignatureValidationError).bind<Unit>()
        }

        val kid = header.kid ?: return@coroutineBinding Err(VersionEnforcementError.SignatureValidationError).bind<Unit>()

        val did = didResolverHelper.getDidStringFromAbsoluteKeyId(kid)
            .mapError { VersionEnforcementError.SignatureValidationError }
            .bind()

        if (did !in environmentSetupRepository.versionEnforcementTrustedDids) {
            return@coroutineBinding Err(VersionEnforcementError.SignatureValidationError).bind<Unit>()
        }

        val publicKey = resolvePublicKey(kid)
            .mapError { VersionEnforcementError.SignatureValidationError }
            .bind()

        verifyDetachedSignature(jwsObject, body, publicKey).bind()
    }

    private fun validateRfc7797Compliance(header: JwsDetachedHeader): Result<Unit, VersionEnforcementError.SignatureValidationError> {
        if (header.b64 != false) {
            return Err(VersionEnforcementError.SignatureValidationError)
        }
        if (header.crit == null || ("b64" !in header.crit)) {
            return Err(VersionEnforcementError.SignatureValidationError)
        }
        return Ok(Unit)
    }

    private fun verifyDetachedSignature(
        jwsObject: JWSObject,
        body: String,
        publicKey: Jwk,
    ): Result<Unit, VersionEnforcementError.SignatureValidationError> = binding {
        val signingInput = (jwsObject.header.toBase64URL().toString() + ".")
            .toByteArray(Charsets.US_ASCII) + body.toByteArray(Charsets.UTF_8)

        verifyJwtSignature(
            header = jwsObject.header,
            signingInput = signingInput,
            signature = jwsObject.signature,
            publicKey = publicKey,
        ).mapError { error ->
            when (error) {
                is JwtError.InvalidJwt -> VersionEnforcementError.SignatureValidationError
                is JwtError.Unexpected -> {
                    Timber.e(error.throwable)
                    VersionEnforcementError.SignatureValidationError
                }
            }
        }.bind()
    }
}
