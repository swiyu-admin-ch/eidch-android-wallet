package ch.admin.foitt.openid4vc.domain.usecase.implementation.mock

import ch.admin.foitt.openid4vc.domain.model.KeyStorageSecurityLevel
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.Claim
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialMetadata
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.KeyAttestationConfig
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.ProofType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.ProofTypeConfig
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.CREDENTIAL_IDENTIFIER

object MockIssuerCredentialConfiguration {
    private const val VCT = "vct"
    private const val VCT_INTEGRITY = "vct integrity"
    private const val VCT_METADATA_URI = "vct metadata uri"
    private const val VCT_METADATA_URI_INTEGRITY = "vct metadata uri integrity"
    private val CLAIMS = emptyList<Claim>()
    private val SUPPORTED_CRYPTOGRAPHIC_SUITE = SigningAlgorithm.ES512
    const val JWK_BINDING_METHOD = "jwk"
    val SUPPORTED_PROOF_TYPE = ProofType.JWT
    private val UNKNOWN_PROOF_TYPE = ProofType.UNKNOWN
    private val SIGNING_ALG = SigningAlgorithm.ES256
    private val PROOF_SIGNING_ALG_VALUES_SUPPORTED = listOf(SIGNING_ALG)
    val strongboxKeyStorage = listOf(KeyStorageSecurityLevel.HIGH)
    val teeKeyStorage = listOf(KeyStorageSecurityLevel.ENHANCED_BASIC)

    val proofTypeConfigSoftwareBinding = ProofTypeConfig(PROOF_SIGNING_ALG_VALUES_SUPPORTED)
    val proofTypeConfigHardwareBinding = ProofTypeConfig(
        proofSigningAlgValuesSupported = PROOF_SIGNING_ALG_VALUES_SUPPORTED,
        keyAttestationsRequired = KeyAttestationConfig(
            keyStorage = strongboxKeyStorage,
            userAuthentication = listOf(KeyStorageSecurityLevel.HIGH)
        )
    )

    val vcSdJwtCredentialConfiguration = VcSdJwtCredentialConfiguration(
        identifier = CREDENTIAL_IDENTIFIER,
        credentialSigningAlgValuesSupported = listOf(SUPPORTED_CRYPTOGRAPHIC_SUITE),
        cryptographicBindingMethodsSupported = listOf(JWK_BINDING_METHOD),
        format = CredentialFormat.VC_SD_JWT,
        proofTypesSupported = mapOf(SUPPORTED_PROOF_TYPE to proofTypeConfigSoftwareBinding),
        vct = VCT,
        vctIntegrity = VCT_INTEGRITY,
        vctMetadataUri = VCT_METADATA_URI,
        vctMetadataUriIntegrity = VCT_METADATA_URI_INTEGRITY,
        credentialMetadata = CredentialMetadata(
            claims = CLAIMS,
        )
    )
    val credentialConfigurationWithHardwareKeyBinding = vcSdJwtCredentialConfiguration.copy(
        proofTypesSupported = mapOf(SUPPORTED_PROOF_TYPE to proofTypeConfigHardwareBinding)
    )
    val credentialConfigurationWithoutProofTypesSupported = vcSdJwtCredentialConfiguration.copy(
        proofTypesSupported = emptyMap(),
    )
    val credentialConfigurationWithOtherProofTypeSigningAlgorithms = vcSdJwtCredentialConfiguration.copy(
        proofTypesSupported = mapOf(UNKNOWN_PROOF_TYPE to ProofTypeConfig(PROOF_SIGNING_ALG_VALUES_SUPPORTED))
    )
}
