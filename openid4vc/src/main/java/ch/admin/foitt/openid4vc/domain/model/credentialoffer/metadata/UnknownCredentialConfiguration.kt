package ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UnknownCredentialConfiguration(
    override val identifier: String = "",
    override val format: CredentialFormat = CredentialFormat.UNKNOWN,
    override val scope: String? = null,

    @Serializable
    @SerialName("cryptographic_binding_methods_supported")
    override val cryptographicBindingMethodsSupported: List<String>? = null,
    @Serializable(with = SigningAlgorithmsSerializer::class)
    @SerialName("credential_signing_alg_values_supported")
    override val credentialSigningAlgValuesSupported: List<SigningAlgorithm> = emptyList(),
    @Serializable
    @SerialName("proof_types_supported")
    override val proofTypesSupported: Map<ProofType, ProofTypeConfig> =
        mapOf(ProofType.UNKNOWN to ProofTypeConfig(proofSigningAlgValuesSupported = emptyList())),
    @SerialName("credential_metadata")
    override val credentialMetadata: CredentialMetadata? = null,
) : AnyCredentialConfiguration()
