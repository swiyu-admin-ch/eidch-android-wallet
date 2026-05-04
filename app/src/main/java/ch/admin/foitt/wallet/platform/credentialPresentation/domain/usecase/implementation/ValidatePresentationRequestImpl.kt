package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObject
import ch.admin.foitt.openid4vc.domain.usecase.VerifyRequestObjectSignature
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ValidatePresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.toValidatePresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class ValidatePresentationRequestImpl @Inject constructor(
    private val safeJson: SafeJson,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val verifyRequestObjectSignature: VerifyRequestObjectSignature,
) : ValidatePresentationRequest {
    override suspend fun invoke(
        requestObject: RequestObject
    ): Result<PresentationRequestWithRaw, ValidatePresentationRequestError> = coroutineBinding {
        val presentationRequestWithRaw = validateRequestObject(requestObject).bind()

        validateAuthorizationRequest(
            authorizationRequest = presentationRequestWithRaw.authorizationRequest,
        ).bind()

        presentationRequestWithRaw
    }

    private suspend fun validateRequestObject(
        requestObject: RequestObject,
    ): Result<PresentationRequestWithRaw, ValidatePresentationRequestError> = coroutineBinding {
        val jwt = requestObject.jwt

        runSuspendCatching {
            check(jwt.type == JWT_HEADER_TYP)
        }.mapError { throwable ->
            CredentialPresentationError.Unexpected(throwable)
        }.bind()

        // request object clientId must be equal to authorization request clientId
        requestObject.clientId?.let {
            runSuspendCatching {
                val authorizationRequestClientId = jwt.payloadJson["client_id"]?.jsonPrimitive?.content
                checkNotNull(authorizationRequestClientId)
                check(requestObject.clientId == authorizationRequestClientId)
            }.mapError { throwable ->
                CredentialPresentationError.Unexpected(throwable)
            }.bind()
        }

        val responseUri = runSuspendCatching {
            checkNotNull(jwt.payloadJson[CLAIM_RESPONSE_URI]?.jsonPrimitive?.content)
        }.mapError { throwable ->
            CredentialPresentationError.Unexpected(throwable)
        }.bind()

        runSuspendCatching {
            check(jwt.algorithm == SigningAlgorithm.ES256.stdName)
            checkNotNull(jwt.keyId) { "keyId is missing" }
            check(jwt.jwtValidity == Validity.Valid) { "jwt is not yet valid or expired" }
        }.mapError { throwable ->
            throwable.toValidatePresentationRequestError(responseUri = responseUri, message = "validateJwtPresentationRequest error")
        }.bind()

        verifyRequestObjectSignature(
            requestObject = requestObject,
            trustedAttestationDids = environmentSetupRepository.attestationsServiceTrustedDids,
        ).mapError { error ->
            error.toValidatePresentationRequestError(responseUri)
        }.bind()

        val authorizationRequest = safeJson.safeDecodeElementTo<AuthorizationRequest>(jwt.payloadJson).mapError {
            CredentialPresentationError.Unexpected(null)
        }.bind()

        PresentationRequestWithRaw(
            authorizationRequest = authorizationRequest,
            rawPresentationRequest = jwt.rawJwt,
        )
    }

    @Suppress("CyclomaticComplexMethod")
    private fun validateAuthorizationRequest(
        authorizationRequest: AuthorizationRequest,
    ): Result<Unit, ValidatePresentationRequestError> {
        val validationError = Err(CredentialPresentationError.InvalidPresentation(authorizationRequest.responseUri))
        return when {
            authorizationRequest.responseType != VP_TOKEN -> validationError
            authorizationRequest.responseMode !in SUPPORTED_RESPONSE_MODES -> validationError
            authorizationRequest.responseMode == DIRECT_POST_JWT && authorizationRequest.clientMetaData == null -> validationError
            authorizationRequest.isFieldsEmpty() -> validationError
            authorizationRequest.isClaimsEmpty() -> validationError
            authorizationRequest.presentationDefinition == null && authorizationRequest.dcqlQuery == null -> validationError
            authorizationRequest.hasInvalidConstraintsPath() -> validationError
            else -> Ok(Unit)
        }
    }

    private fun AuthorizationRequest.isFieldsEmpty() = presentationDefinition?.inputDescriptors?.any { inputDescriptor ->
        inputDescriptor.formats.any { format ->
            format is InputDescriptorFormat.VcSdJwt &&
                inputDescriptor.constraints.fields.isEmpty()
        }
    } ?: false

    private fun AuthorizationRequest.isClaimsEmpty() = dcqlQuery?.let { dcqlQuery ->
        dcqlQuery.credentials?.let { credentialQueries ->
            credentialQueries.any { it.format == CredentialFormat.VC_SD_JWT.format && it.claims?.isEmpty() == true }
        } ?: false
    } ?: false

    private fun AuthorizationRequest.hasInvalidConstraintsPath(): Boolean {
        // JsonPath filter expressions in path are not allowed.
        // The filter expression starts with "[?" and may contain whitespace between these characters
        val invalidConstrainPath = """.*\[\s*\?.*""".toRegex()
        return presentationDefinition?.inputDescriptors?.any { inputDescriptor ->
            inputDescriptor.constraints.fields.any { field ->
                field.path.any { path ->
                    invalidConstrainPath.matches(path)
                }
            }
        } ?: false
    }

    private companion object {
        const val JWT_HEADER_TYP = "oauth-authz-req+jwt"
        const val VP_TOKEN = "vp_token"
        const val DIRECT_POST = "direct_post"
        const val DIRECT_POST_JWT = "direct_post.jwt"
        val SUPPORTED_RESPONSE_MODES = listOf(DIRECT_POST, DIRECT_POST_JWT)
        const val CLAIM_RESPONSE_URI = "response_uri"
    }
}
