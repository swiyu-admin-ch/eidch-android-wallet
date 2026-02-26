package ch.admin.foitt.wallet.platform.nonCompliance.domain.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NonComplianceRequest(
    @SerialName("type")
    val type: String,
    @SerialName("description")
    val description: String,
    @SerialName("email")
    val email: String? = null,
    @SerialName("metadata")
    val metadata: NonComplianceMetadata
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class NonComplianceMetadata(
    @SerialName("verifier_did")
    val verifierDid: String? = null,

    @SerialName("verifier_url")
    val verifierUrl: String? = null,

    @SerialName("presentation_action_created_at")
    val presentationActionCreatedAt: String? = null,

    @EncodeDefault(NEVER)
    @SerialName("presented_credential_issuer_did")
    val presentedCredentialIssuerDid: String? = null,

    @EncodeDefault(NEVER)
    @SerialName("presentation_request_jwt")
    val presentationRequestJwt: String? = null,

    @EncodeDefault(NEVER)
    @SerialName("presentation_request_fields")
    val presentationRequestFields: List<NonComplianceRequestField> = emptyList()
) {
    companion object {
        val EMPTY = NonComplianceMetadata(
            verifierDid = "",
            verifierUrl = "",
            presentationActionCreatedAt = "",
            presentedCredentialIssuerDid = "",
            presentationRequestJwt = "",
            presentationRequestFields = emptyList(),
        )
    }
}

@Serializable
data class NonComplianceRequestField(
    @SerialName("name")
    val name: String,
    @SerialName("constraint")
    val constraint: String? = null
)
