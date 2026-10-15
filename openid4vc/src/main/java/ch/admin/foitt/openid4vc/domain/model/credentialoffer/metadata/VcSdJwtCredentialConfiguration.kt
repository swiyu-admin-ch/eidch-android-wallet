package ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.jwt.JwtSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VcSdJwtCredentialConfiguration(
    override val identifier: String,
    override val format: CredentialFormat = CredentialFormat.DC_SD_JWT,
    override val scope: String? = null,
    @Serializable(with = JwtSerializer::class)
    @SerialName("protected_issuance_authorization_trust_statement")
    override val protectedIssuanceAuthorizationTrustStatement: Jwt? = null,

    @Serializable
    @SerialName("cryptographic_binding_methods_supported")
    override val cryptographicBindingMethodsSupported: List<String>? = null,
    @Serializable(with = SignatureAlgorithmsSerializer::class)
    @SerialName("credential_signing_alg_values_supported")
    override val credentialSigningAlgValuesSupported: List<SignatureAlgorithm>,
    @Serializable
    @SerialName("proof_types_supported")
    override val proofTypesSupported: Map<ProofType, ProofTypeConfig> = emptyMap(),
    @SerialName("credential_metadata")
    override val credentialMetadata: CredentialMetadata? = null,
    @SerialName("credential_refresh_disabled")
    override val credentialRefreshDisabled: Boolean = false,

    @SerialName("vct")
    val vct: String,
    @SerialName("vct#integrity")
    val vctIntegrity: String?,
    @SerialName("vct_metadata_uri")
    val vctMetadataUri: String?,
    @SerialName("vct_metadata_uri#integrity")
    val vctMetadataUriIntegrity: String?,
) : AnyCredentialConfiguration()
