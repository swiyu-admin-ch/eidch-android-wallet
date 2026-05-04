package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class SIdErrorResponse(
    @SerialName("errors")
    val errors: List<SIdError>,
)

@Serializable
internal data class SIdError(
    @SerialName("code")
    val code: String = "",
    @SerialName("correlationId")
    val correlationId: String = "",
    @SerialName("id")
    val id: String = "",
    @SerialName("message")
    val message: String = "",
    @SerialName("messageKey")
    val messageKey: String = "",
    @SerialName("params")
    val params: JsonObject? = null,
    @SerialName("status")
    val status: Int = 0,
    @SerialName("transferId")
    val transferId: String = "",
    @SerialName("translations")
    val translations: JsonObject? = null,
) {
    companion object {
        const val INVALID_CLIENT_ATTESTATION = "InvalidClientAttestation"
        const val INVALID_KEY_ATTESTATION = "InvalidKeyAttestation"
        const val INSUFFICIENT_KEY_STORAGE_RESISTANCE = "InsufficientKeyStorageResistance"
        const val REQUEST_IN_WRONG_STATE = "AntragInWrongStateException"
    }
}
