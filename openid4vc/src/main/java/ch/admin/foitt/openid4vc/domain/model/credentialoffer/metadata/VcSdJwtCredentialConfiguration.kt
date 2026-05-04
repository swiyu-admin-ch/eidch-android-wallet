package ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VcSdJwtCredentialConfiguration(
    override val identifier: String,
    override val format: CredentialFormat = CredentialFormat.VC_SD_JWT,
    override val scope: String? = null,

    @Serializable
    @SerialName("cryptographic_binding_methods_supported")
    override val cryptographicBindingMethodsSupported: List<String>? = null,
    @Serializable(with = SigningAlgorithmsSerializer::class)
    @SerialName("credential_signing_alg_values_supported")
    override val credentialSigningAlgValuesSupported: List<SigningAlgorithm>,
    @Serializable
    @SerialName("proof_types_supported")
    override val proofTypesSupported: Map<ProofType, ProofTypeConfig> = emptyMap(),
    @SerialName("credential_metadata")
    override val credentialMetadata: CredentialMetadata? = null,

    @SerialName("vct")
    val vct: String,
    @SerialName("vct#integrity")
    val vctIntegrity: String?,
    @SerialName("vct_metadata_uri")
    val vctMetadataUri: String?,
    @SerialName("vct_metadata_uri#integrity")
    val vctMetadataUriIntegrity: String?,
) : AnyCredentialConfiguration()
