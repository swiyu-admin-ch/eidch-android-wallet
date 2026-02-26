package ch.admin.foitt.openid4vc.domain.model.credentialoffer

import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface CredentialRequest

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class VerifiableCredentialRequest(
    @SerialName("credential_configuration_id")
    val credentialConfigurationId: String,
    // Only send the field proof in the request if it contains a proof
    @EncodeDefault(NEVER)
    @SerialName("proofs")
    val proofs: CredentialRequestProofs? = null,
    // Only send the field in the request if it contains a proof
    @EncodeDefault(NEVER)
    @SerialName("credential_response_encryption")
    val credentialResponseEncryption: CredentialRequestCredentialResponseEncryption? = null
) : CredentialRequest

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CredentialRequestCredentialResponseEncryption(
    @SerialName("jwk")
    val jwk: Jwk,
    @SerialName("alg")
    val alg: String,
    @SerialName("enc")
    val enc: String,
    @EncodeDefault(NEVER)
    @SerialName("zip")
    val zip: String? = null,
)
