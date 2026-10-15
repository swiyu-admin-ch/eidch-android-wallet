package ch.admin.foitt.wallet.platform.nonCompliance.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NonComplianceRequest(
    @SerialName("type")
    val type: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("language")
    val language: String? = null,
    @SerialName("metadata")
    val metadata: NonComplianceMetadata,
    @SerialName("context")
    val context: String? = null
)

@Serializable
data class NonComplianceMetadata(
    @SerialName("verifier")
    val verifier: NonComplianceVerifierMetadata? = null,

    @SerialName("issuer")
    val issuer: NonComplianceIssuerMetadata? = null,
)

@Serializable
data class NonComplianceVerifierMetadata(
    @SerialName("verifier_did")
    val verifierDid: String? = null,

    @SerialName("verifier_url")
    val verifierUrl: String? = null,

    @SerialName("presentation_action_created_at")
    val presentationActionCreatedAt: String? = null,

    @SerialName("presented_credential_issuer_did")
    val presentedCredentialIssuerDid: String? = null,

    @SerialName("presentation_request_jwt")
    val presentationRequestJwt: String? = null,

    @SerialName("presentation_request_fields")
    val presentationRequestFields: List<NonComplianceRequestField> = emptyList(),
)

@Serializable
data class NonComplianceIssuerMetadata(
    @SerialName("issuer_did")
    val issuerDid: String? = null,

    @SerialName("issuer_url")
    val issuerUrl: String? = null,

    @SerialName("issuance_created_at")
    val issuanceCreatedAt: String? = null,

    @SerialName("signed_metadata_jwt")
    val signedMetadataJwt: String? = null,

    @SerialName("oca_bundle")
    val ocaBundle: String? = null,

    @SerialName("vc_issued")
    val vcIssued: String? = null,
)

@Serializable
data class NonComplianceRequestField(
    @SerialName("name")
    val name: String,
    @SerialName("constraint")
    val constraint: String? = null
)
