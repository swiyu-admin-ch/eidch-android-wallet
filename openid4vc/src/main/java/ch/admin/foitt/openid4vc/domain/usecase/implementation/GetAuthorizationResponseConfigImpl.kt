package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwe.CreateJWEError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponse
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseParam
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseType
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.GetAuthorizationResponseConfigError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationResponseMode
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.toGetPresentationRequestConfigError
import ch.admin.foitt.openid4vc.domain.usecase.GetAuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.usecase.jwe.CreateJWE
import ch.admin.foitt.openid4vc.utils.JsonParsingError
import ch.admin.foitt.openid4vc.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

internal class GetAuthorizationResponseConfigImpl @Inject constructor(
    private val safeJson: SafeJson,
    private val createJWE: CreateJWE,
) : GetAuthorizationResponseConfig {
    override fun invoke(
        authorizationRequest: AuthorizationRequest,
        authorizationResponse: AuthorizationResponse,
        usePayloadEncryption: Boolean,
    ): Result<AuthorizationResponseConfig, GetAuthorizationResponseConfigError> = binding {
        if (!usePayloadEncryption || authorizationRequest.responseMode == PresentationResponseMode.DIRECT_POST.value) {
            when (authorizationResponse) {
                is AuthorizationResponse.Dif -> getDifAuthorizationResponseConfig(authorizationResponse)
                is AuthorizationResponse.Dcql -> getDCQLAuthorizationResponseConfig(authorizationResponse)
            }
        } else {
            when (authorizationRequest.responseMode) {
                PresentationResponseMode.DIRECT_POST_JWT.value -> {
                    getJWEAuthorizationResponseConfig(authorizationRequest, authorizationResponse)
                }

                else -> return@binding Err(
                    PresentationRequestError.Unexpected(IllegalStateException("invalid response mode"))
                ).bind<AuthorizationResponseConfig>()
            }
        }.bind()
    }

    private fun getDifAuthorizationResponseConfig(
        authorizationResponse: AuthorizationResponse.Dif
    ): Result<AuthorizationResponseConfig, GetAuthorizationResponseConfigError> = binding {
        val presentationSubmissionJson = safeJson.safeEncodeObjectToString(
            objectToEncode = authorizationResponse.presentationSubmission,
        ).mapError(JsonParsingError::toGetPresentationRequestConfigError)
            .bind()
        AuthorizationResponseConfig(
            type = AuthorizationResponseType.DIF,
            params = buildMap {
                put(AuthorizationResponseParam.VP_TOKEN, authorizationResponse.vpToken)
                put(AuthorizationResponseParam.PRESENTATION_SUBMISSION, presentationSubmissionJson)

                authorizationResponse.state?.let {
                    put(AuthorizationResponseParam.STATE, it)
                }
            }
        )
    }

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
            jwk.kty in SUPPORTED_KEY_TYPES && jwk.use in SUPPORTED_USES &&
                jwk.alg in SUPPORTED_ALGORITHMS && jwk.crv in SUPPORTED_CURVES
        }

        if (jwk == null) {
            return@binding Err(
                PresentationRequestError.Unexpected(IllegalStateException("no valid jwk provided"))
            ).bind<AuthorizationResponseConfig>()
        }

        val encValuesSupported = clientMetadata.encryptedResponseEncValuesSupported
        // acc. to spec:
        // if field is not provided -> use A128GCM as default
        // if field is provided -> A128GCM does not need be part of the array, but can
        // -> take first algo that we support -> if we support none return error
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
            is AuthorizationResponse.Dif -> {
                val presentationSubmissionJson = safeJson.safeEncodeObjectToString(
                    objectToEncode = authorizationResponse.presentationSubmission,
                ).mapError(JsonParsingError::toGetPresentationRequestConfigError)
                    .bind()
                buildJsonObject {
                    put(AuthorizationResponseParam.VP_TOKEN.jsonName, authorizationResponse.vpToken)
                    put(AuthorizationResponseParam.PRESENTATION_SUBMISSION.jsonName, presentationSubmissionJson)

                    authorizationRequest.state?.let {
                        put(AuthorizationResponseParam.STATE.jsonName, it)
                    }
                }
            }

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
            type = if (authorizationResponse is AuthorizationResponse.Dif) {
                AuthorizationResponseType.DIF
            } else {
                AuthorizationResponseType.DCQL
            },
            params = mapOf(AuthorizationResponseParam.RESPONSE to jwe)
        )
    }

    private companion object {
        private val SUPPORTED_KEY_TYPES = listOf("EC")
        private val SUPPORTED_USES = listOf("enc")
        private val SUPPORTED_ALGORITHMS = listOf("ECDH-ES")
        private val SUPPORTED_CURVES = listOf("P-256")
        private const val DEFAULT_ENCRYPTION_ALGORITHM = "A128GCM"
        private val SUPPORTED_ENC_VALUES = listOf(DEFAULT_ENCRYPTION_ALGORITHM)
    }
}
