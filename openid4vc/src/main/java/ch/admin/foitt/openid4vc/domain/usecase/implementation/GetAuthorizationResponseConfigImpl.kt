package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.jwe.CreateJWEError
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.EncryptionAlgorithm
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponse
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseParam
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseType
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.CreateAnyVerifiablePresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.GetAuthorizationResponseConfigError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationResponseMode
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.toGetAuthorizationResponseConfigError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.toGetPresentationRequestConfigError
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyVerifiablePresentation
import ch.admin.foitt.openid4vc.domain.usecase.GetAuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.usecase.jwe.CreateJWE
import ch.admin.foitt.openid4vc.utils.JsonParsingError
import ch.admin.foitt.openid4vc.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

internal class GetAuthorizationResponseConfigImpl @Inject constructor(
    private val createAnyVerifiablePresentation: CreateAnyVerifiablePresentation,
    private val safeJson: SafeJson,
    private val createJWE: CreateJWE,
) : GetAuthorizationResponseConfig {
    override suspend fun invoke(
        anyCredential: AnyCredential,
        presentationPaths: List<ClaimsPathPointer>,
        authorizationRequest: AuthorizationRequest,
        usePayloadEncryption: Boolean,
        dcqlQueryId: String?,
    ): Result<AuthorizationResponseConfig, GetAuthorizationResponseConfigError> = coroutineBinding {
        val verifiablePresentation = createAnyVerifiablePresentation(
            anyCredential = anyCredential,
            presentationPaths = presentationPaths,
            authorizationRequest = authorizationRequest,
        ).mapError(CreateAnyVerifiablePresentationError::toGetAuthorizationResponseConfigError)
            .bind()

        val dcqlId = dcqlQueryId ?: return@coroutineBinding Err(
            PresentationRequestError.Unexpected(IllegalStateException("No dcql query provided"))
        ).bind<AuthorizationResponseConfig>()

        val authorizationResponse = getAuthorizationResponseByDcqlId(
            dcqlQueryId = dcqlId,
            verifiablePresentation = verifiablePresentation,
            state = authorizationRequest.state,
        )

        when {
            usePayloadEncryption && authorizationRequest.responseMode == PresentationResponseMode.DIRECT_POST_JWT.value -> {
                getJWEAuthorizationResponseConfig(authorizationRequest, authorizationResponse)
            }

            authorizationRequest.responseMode == PresentationResponseMode.DIRECT_POST.value ||
                authorizationRequest.responseMode == PresentationResponseMode.DC_API_JWT.value -> {
                getDCQLAuthorizationResponseConfig(authorizationResponse)
            }

            else -> {
                Err(PresentationRequestError.Unexpected(IllegalStateException("invalid response mode")))
            }
        }.bind()
    }

    private fun getAuthorizationResponseByDcqlId(
        dcqlQueryId: String,
        verifiablePresentation: String,
        state: String?,
    ) = AuthorizationResponse.Dcql(
        vpToken = mapOf(dcqlQueryId to listOf(verifiablePresentation)),
        state = state,
    )

    private fun getDCQLAuthorizationResponseConfig(
        authorizationResponse: AuthorizationResponse.Dcql
    ): Result<AuthorizationResponseConfig, GetAuthorizationResponseConfigError> = binding {
        val value = safeJson.safeEncodeObjectToString(authorizationResponse.vpToken)
            .mapError(JsonParsingError::toGetPresentationRequestConfigError)
            .bind()
        AuthorizationResponseConfig(
            type = AuthorizationResponseType.DCQL,
            params = buildMap {
                put(AuthorizationResponseParam.VP_TOKEN, value)

                authorizationResponse.state?.let {
                    put(AuthorizationResponseParam.STATE, it)
                }
            }
        )
    }

    private fun getJWEAuthorizationResponseConfig(
        authorizationRequest: AuthorizationRequest,
        authorizationResponse: AuthorizationResponse
    ): Result<AuthorizationResponseConfig, GetAuthorizationResponseConfigError> = binding {
        val clientMetadata = authorizationRequest.clientMetaData
        val jwk = clientMetadata?.jwks?.keys?.firstOrNull { jwk ->
            jwk.kty in SUPPORTED_KEY_TYPES &&
                jwk.alg in SUPPORTED_ALGORITHMS && jwk.crv in SUPPORTED_CURVES
        }

        if (jwk == null) {
            return@binding Err(
                PresentationRequestError.Unexpected(IllegalStateException("no valid jwk provided"))
            ).bind<AuthorizationResponseConfig>()
        }

        val encValuesSupported = clientMetadata.encryptedResponseEncValuesSupported
        // acc. to spec:
        // if field is not provided -> use A256GCM as default
        // if field is provided -> take first algo that we support -> if we support none return error
        val encValue = if (encValuesSupported == null) {
            DEFAULT_ENCRYPTION_ALGORITHM
        } else {
            encValuesSupported.firstOrNull { enc ->
                enc in SUPPORTED_ENC_VALUES
            } ?: return@binding Err(
                PresentationRequestError.Unexpected(IllegalStateException("no valid enc value provided"))
            ).bind<AuthorizationResponseConfig>()
        }

        /* TODO: Omni does not support payload encryption for presentation
             (as acc. to them payload encryption needs DCQL and does not work with the DIF spec), it kind of works by accident.
              Meaning, they expect the payload to be two strings (because without payload encryption we send both as string) */
        val payloadJson = when (authorizationResponse) {
            is AuthorizationResponse.Dcql -> {
                val value = safeJson.safeEncodeObjectToString(authorizationResponse.vpToken)
                    .mapError(JsonParsingError::toGetPresentationRequestConfigError)
                    .bind()
                buildJsonObject {
                    put(AuthorizationResponseParam.VP_TOKEN.jsonName, value)
                    authorizationRequest.state?.let {
                        put(AuthorizationResponseParam.STATE.jsonName, it)
                    }
                }
            }
        }

        val payloadString = safeJson.safeEncodeObjectToString(
            objectToEncode = payloadJson,
        ).mapError(JsonParsingError::toGetPresentationRequestConfigError)
            .bind()

        val jwe = createJWE(
            algorithm = jwk.alg ?: "", // default can not happen because of validation above
            encryptionMethod = encValue,
            payload = payloadString,
            encryptionKey = jwk,
        ).mapError(CreateJWEError::toGetPresentationRequestConfigError)
            .bind()

        AuthorizationResponseConfig(
            type = AuthorizationResponseType.DCQL,
            params = mapOf(AuthorizationResponseParam.RESPONSE to jwe)
        )
    }

    private companion object {
        private val SUPPORTED_KEY_TYPES = listOf("EC")
        private val SUPPORTED_ALGORITHMS = listOf("ECDH-ES")
        private val SUPPORTED_CURVES = listOf("P-256")
        private val DEFAULT_ENCRYPTION_ALGORITHM = EncryptionAlgorithm.A256GCM.name
        private val SUPPORTED_ENC_VALUES = listOf(DEFAULT_ENCRYPTION_ALGORITHM, EncryptionAlgorithm.A128GCM.name)
    }
}
