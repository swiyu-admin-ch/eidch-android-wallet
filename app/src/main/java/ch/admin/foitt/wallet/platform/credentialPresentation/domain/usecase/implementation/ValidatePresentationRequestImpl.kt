package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestContainer
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ValidatePresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.toValidatePresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import javax.inject.Inject

class ValidatePresentationRequestImpl @Inject constructor(
    private val safeJson: SafeJson,
    private val verifyJwtSignature: VerifyJwtSignature,
) : ValidatePresentationRequest {
    override suspend fun invoke(
        presentationRequestContainer: PresentationRequestContainer
    ): Result<PresentationRequestWithRaw, ValidatePresentationRequestError> = coroutineBinding {
        val presentationRequest = when (presentationRequestContainer) {
            is PresentationRequestContainer.Jwt -> validateJwtPresentationRequest(presentationRequestContainer).bind()
            is PresentationRequestContainer.Json -> presentationRequestContainer.toPresentationRequest().bind()
        }
        validatePresentationRequest(
            presentationRequest = presentationRequest.presentationRequest,
            clientId = presentationRequestContainer.clientId
        ).bind()

        presentationRequest
    }

    private fun validatePresentationRequest(
        presentationRequest: PresentationRequest,
        clientId: String?,
    ): Result<Unit, ValidatePresentationRequestError> {
        val validationError = Err(CredentialPresentationError.InvalidPresentation(presentationRequest.responseUri))
        return when {
            clientId != null && clientId != presentationRequest.clientId -> validationError
            presentationRequest.responseType != VP_TOKEN -> validationError
            presentationRequest.responseMode !in SUPPORTED_RESPONSE_MODES -> validationError
            presentationRequest.responseMode == DIRECT_POST_JWT && presentationRequest.clientMetaData == null -> validationError
            presentationRequest.clientIdScheme == null -> validationError
            presentationRequest.clientIdScheme != ID_SCHEME_DID -> validationError
            !presentationRequest.clientId.matches(DID_REGEX) -> validationError
            presentationRequest.isFieldsEmpty() -> validationError
            presentationRequest.hasInvalidConstraintsPath() -> validationError
            else -> Ok(Unit)
        }
    }

    private fun PresentationRequest.hasInvalidConstraintsPath(): Boolean {
        // JsonPath filter expressions in path are not allowed.
        // The filter expression starts with "[?" and may contain whitespace between these characters
        val invalidConstrainPath = """.*\[\s*\?.*""".toRegex()
        return presentationDefinition.inputDescriptors.any { inputDescriptor ->
            inputDescriptor.constraints.fields.any { field ->
                field.path.any { path ->
                    invalidConstrainPath.matches(path)
                }
            }
        }
    }

    private fun PresentationRequest.isFieldsEmpty() = presentationDefinition.inputDescriptors.any { inputDescriptor ->
        inputDescriptor.formats.any { format ->
            format is InputDescriptorFormat.VcSdJwt &&
                inputDescriptor.constraints.fields.isEmpty()
        }
    }

    private suspend fun validateJwtPresentationRequest(
        container: PresentationRequestContainer.Jwt,
    ): Result<PresentationRequestWithRaw, ValidatePresentationRequestError> = coroutineBinding {
        val jwt = container.jwt
        val responseUri = runSuspendCatching {
            checkNotNull(jwt.payloadJson[CLAIM_RESPONSE_URI]?.jsonPrimitive?.content)
        }.mapError { throwable ->
            CredentialPresentationError.Unexpected(throwable)
        }.bind()

        val validationError = Err(CredentialPresentationError.InvalidPresentation(responseUri))

        if (jwt.algorithm != SigningAlgorithm.ES256.stdName) {
            validationError.bind<ValidatePresentationRequestError>()
        }

        runSuspendCatching {
            val issuerDid = checkNotNull(jwt.iss) { "issuer is missing" }
            val clientId = jwt.payloadJson["client_id"]?.jsonPrimitive?.content

            check(issuerDid == clientId) { "jwt issuer did does not match request object clientId" }

            val keyId = checkNotNull(jwt.keyId) { "keyId is missing" }

            check(jwt.jwtValidity == Validity.Valid) { "jwt is not yet valid or expired" }

            verifyJwtSignature(
                did = issuerDid,
                kid = keyId,
                jwt = jwt,
            )
                .mapError { error -> error.toValidatePresentationRequestError(responseUri) }
                .bind()
        }.mapError { throwable ->
            Timber.w(t = throwable)
            throwable.toValidatePresentationRequestError(responseUri = responseUri, message = "validateJwtPresentationRequest error")
        }.bind()

        val presentationRequest = safeJson.safeDecodeElementTo<PresentationRequest>(jwt.payloadJson).mapError {
            CredentialPresentationError.Unexpected(null)
        }.bind()

        PresentationRequestWithRaw(
            presentationRequest = presentationRequest,
            rawPresentationRequest = jwt.rawJwt,
        )
    }

    private fun PresentationRequestContainer.Json.toPresentationRequest():
        Result<PresentationRequestWithRaw, ValidatePresentationRequestError> = binding {
        val presentationRequest = safeJson.safeDecodeElementTo<PresentationRequest>(json)
            .mapError(JsonParsingError::toValidatePresentationRequestError)
            .bind()

        PresentationRequestWithRaw(
            presentationRequest = presentationRequest,
            rawPresentationRequest = json.toString()
        )
    }

    private companion object {
        const val ID_SCHEME_DID = "did"
        const val VP_TOKEN = "vp_token"
        val DID_REGEX = Regex("^did:[a-z0-9]+:[a-zA-Z0-9.\\-_:]+$")
        const val DIRECT_POST = "direct_post"
        const val DIRECT_POST_JWT = "direct_post.jwt"
        val SUPPORTED_RESPONSE_MODES = listOf(DIRECT_POST, DIRECT_POST_JWT)
        const val CLAIM_RESPONSE_URI = "response_uri"
    }
}
