package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwe.CreateJWEError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.GetPresentationRequestTypeError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestBody
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestType
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationResponseMode
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.toGetPresentationRequestTypeError
import ch.admin.foitt.openid4vc.domain.usecase.GetPresentationRequestType
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

internal class GetPresentationRequestTypeImpl @Inject constructor(
    private val safeJson: SafeJson,
    private val createJWE: CreateJWE,
) : GetPresentationRequestType {
    override fun invoke(
        presentationRequest: PresentationRequest,
        presentationRequestBody: PresentationRequestBody,
        usePayloadEncryption: Boolean,
    ): Result<PresentationRequestType, GetPresentationRequestTypeError> = binding {
        val presentationSubmissionJson = safeJson.safeEncodeObjectToString(
            objectToEncode = presentationRequestBody.presentationSubmission,
        ).mapError(JsonParsingError::toGetPresentationRequestTypeError)
            .bind()

        val requestType = if (!usePayloadEncryption) {
            PresentationRequestType.Json(
                vpToken = presentationRequestBody.vpToken,
                presentationSubmission = presentationSubmissionJson,
            )
        } else {
            when (presentationRequest.responseMode) {
                PresentationResponseMode.DIRECT_POST.value -> PresentationRequestType.Json(
                    vpToken = presentationRequestBody.vpToken,
                    presentationSubmission = presentationSubmissionJson,
                )

                PresentationResponseMode.DIRECT_POST_JWT.value -> {
                    val clientMetadata = presentationRequest.clientMetaData
                    val jwk = clientMetadata?.jwks?.keys?.firstOrNull { jwk ->
                        jwk.kty in SUPPORTED_KEY_TYPES && jwk.use in SUPPORTED_USES &&
                            jwk.alg in SUPPORTED_ALGORITHMS && jwk.crv in SUPPORTED_CURVES
                    }

                    if (jwk == null) {
                        return@binding Err(
                            PresentationRequestError.Unexpected(IllegalStateException("no valid jwk provided"))
                        ).bind<PresentationRequestType>()
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
                        ).bind<PresentationRequestType>()
                    }

                    /*
                    TODO: Omni does not support payload encryption for presentation (as acc. to them payload encryption needs DCQL and does
                     not work with the DIF spec), it kind of works by accident. Meaning, they expect the payload to be two strings (because
                     without payload encryption we send both as string)
                     */
                    val payloadJson = buildJsonObject {
                        put("vp_token", presentationRequestBody.vpToken)
                        put("presentation_submission", presentationSubmissionJson)
                    }

                    val payloadString = safeJson.safeEncodeObjectToString(
                        objectToEncode = payloadJson,
                    ).mapError(JsonParsingError::toGetPresentationRequestTypeError)
                        .bind()

                    val jwe = createJWE(
                        algorithm = jwk.alg ?: "", // default can not happen because of validation above
                        encryptionMethod = encValue,
                        payload = payloadString,
                        encryptionKey = jwk,
                    ).mapError(CreateJWEError::toGetPresentationRequestTypeError)
                        .bind()

                    PresentationRequestType.Jwt(response = jwe)
                }

                else -> return@binding Err(
                    PresentationRequestError.Unexpected(IllegalStateException("invalid response mode"))
                ).bind<PresentationRequestType>()
            }
        }

        requestType
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
