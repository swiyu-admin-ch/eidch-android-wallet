package ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata

import ch.admin.foitt.openid4vc.domain.model.AnyCredentialConfigurationListSerializer
import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.HttpsURLAsStringSerializer
import ch.admin.foitt.openid4vc.domain.model.KeyStorageSecurityLevel
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwks
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonArray
import java.net.URL

@Serializable
data class IssuerCredentialInfo(
    @Serializable(with = HttpsURLAsStringSerializer::class)
    @SerialName("credential_endpoint")
    val credentialEndpoint: URL,
    @Serializable(with = HttpsURLAsStringSerializer::class)
    @SerialName("deferred_credential_endpoint")
    val deferredCredentialEndpoint: URL? = null,
    @SerialName("nonce_endpoint")
    @Serializable(with = HttpsURLAsStringSerializer::class)
    val nonceEndpoint: URL? = null,
    @Serializable(with = HttpsURLAsStringSerializer::class)
    @SerialName("credential_issuer")
    val credentialIssuer: URL,
    @SerialName("credential_request_encryption")
    val credentialRequestEncryption: CredentialRequestEncryption?,
    @SerialName("credential_response_encryption")
    val credentialResponseEncryption: CredentialResponseEncryption?,
    @Serializable(with = AnyCredentialConfigurationListSerializer::class)
    @SerialName("credential_configurations_supported")
    val credentialConfigurations: List<AnyCredentialConfiguration>,
    @SerialName("display")
    val display: List<OidIssuerDisplay>?,
    @SerialName("batch_credential_issuance")
    val batchCredentialIssuance: BatchCredentialIssuance? = null,
)

@Serializable
data class BatchCredentialIssuance(
    @SerialName("batch_size")
    val batchSize: BatchSize,
)

@Serializable
data class CredentialRequestEncryption(
    @SerialName("jwks")
    val jwks: Jwks,
    @SerialName("enc_values_supported")
    val encValuesSupported: List<String>,
    @SerialName("zip_values_supported")
    val zipValuesSupported: List<String>?,
    @SerialName("encryption_required")
    val encryptionRequired: Boolean
)

@Serializable
data class CredentialResponseEncryption(
    @SerialName("alg_values_supported")
    val algValuesSupported: List<String>,
    @SerialName("enc_values_supported")
    val encValuesSupported: List<String>,
    @SerialName("zip_values_supported")
    val zipValuesSupported: List<String>?,
    @SerialName("encryption_required")
    val encryptionRequired: Boolean
)

@Serializable(with = AnyCredentialConfigurationSerializer::class)
sealed class AnyCredentialConfiguration {
    @SerialName("identifier")
    abstract val identifier: String

    @SerialName("format")
    open val format: CredentialFormat = CredentialFormat.UNKNOWN

    @SerialName("scope")
    abstract val scope: String?

    abstract val cryptographicBindingMethodsSupported: List<String>?
    abstract val credentialSigningAlgValuesSupported: List<SigningAlgorithm>

    // fixme: this should be nullable for claim based credentials
    abstract val proofTypesSupported: Map<ProofType, ProofTypeConfig>
    abstract val credentialMetadata: CredentialMetadata?
}

@Serializable
enum class CredentialFormat(val format: String) {
    @SerialName("vc+sd-jwt")
    VC_SD_JWT("vc+sd-jwt"),

    UNKNOWN("unknown"),
}

@Serializable
enum class ProofType(val type: String) {
    @SerialName("jwt")
    JWT("jwt"),

    UNKNOWN("unknown")
}

@Serializable
data class ProofTypeConfig(
    @Serializable(with = SigningAlgorithmsSerializer::class)
    @SerialName("proof_signing_alg_values_supported")
    val proofSigningAlgValuesSupported: List<SigningAlgorithm>,
    @SerialName("key_attestations_required")
    val keyAttestationsRequired: KeyAttestationConfig? = null,
)

@Serializable
data class KeyAttestationConfig(
    @SerialName("key_storage")
    val keyStorage: List<KeyStorageSecurityLevel>? = null,
    @SerialName("user_authentication")
    val userAuthentication: List<KeyStorageSecurityLevel>? = null,
)

@Serializable
data class CredentialMetadata(
    @SerialName("display")
    val display: List<OidCredentialDisplay>? = null,
    @SerialName("claims")
    val claims: List<Claim>? = null,
)

@Serializable
data class Claim(
    @SerialName("path")
    @Serializable(with = StringToJsonArraySerializer::class)
    val path: ClaimsPathPointer,
    @SerialName("mandatory")
    val mandatory: Boolean? = false,
    @SerialName("display")
    val display: List<OidClaimDisplay>? = null
)

private object StringToJsonArraySerializer : JsonTransformingSerializer<ClaimsPathPointer>(
    tSerializer = ListSerializer(
        ClaimsPathPointerComponent.serializer()
    )
) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        val array = when (element) {
            // Temporary fix for ELFA, because they send the claims path pointer as string "claimName" instead of array ["claimName"]
            is JsonPrimitive -> buildJsonArray { add(element) }
            is JsonArray -> element
            else -> error("invalid object")
        }

        return array
    }
}

// https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#section-11.2.3
@Serializable
data class OidCredentialDisplay(
    @SerialName("locale")
    override val locale: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("logo")
    val logo: Logo? = null,
    @SerialName("name")
    val name: String,
    @SerialName("background_color")
    val backgroundColor: String? = null,
    @SerialName("text_color")
    val textColor: String? = null,
) : CredentialInformationDisplay

// https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#section-11.2.3
@Serializable
data class OidIssuerDisplay(
    @SerialName("locale")
    override val locale: String? = null,
    @SerialName("logo")
    val logo: Logo? = null,
    @SerialName("name")
    val name: String? = null,
) : CredentialInformationDisplay

@Serializable
data class OidClaimDisplay(
    @SerialName("locale")
    override val locale: String? = null,
    @SerialName("name")
    val name: String,
) : CredentialInformationDisplay

interface CredentialInformationDisplay {
    val locale: String?
}

@Serializable
data class Logo(
    @SerialName("uri")
    val uri: String,
    @SerialName("alt_text")
    val altText: String? = null,
)
