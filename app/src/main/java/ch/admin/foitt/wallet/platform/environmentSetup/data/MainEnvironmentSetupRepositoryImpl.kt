package ch.admin.foitt.wallet.platform.environmentSetup.data

import ch.admin.foitt.wallet.BuildConfig
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import javax.inject.Inject

class MainEnvironmentSetupRepositoryImpl @Inject constructor() : EnvironmentSetupRepository {
    override val appVersionEnforcementUrl: String = "https://wallet-ve.trust-infra.swiyu.admin.ch/v1/android"

    override val attestationsServiceUrl: String = "https://attestations.trust-infra.swiyu.admin.ch"

    @Suppress("MaximumLineLength")
    override val attestationsServiceTrustedDids: List<String> = listOf(
        "did:tdw:QmVxp7q4pFKRp8zf7KftJBRroNRF6dVzHns3Sq7EdjxQep:identifier-reg.trust-infra.swiyu.admin.ch:api:v1:did:9f94645c-2b23-4f7d-9c8c-21c77e9995a5",
    )

    val prodTrustRegistryIdentifier = "identifier-reg.trust-infra.swiyu.admin.ch"
    val intTrustRegistryIdentifier = "identifier-reg.trust-infra.swiyu-int.admin.ch"

    val prodTrustRegistryUrl = "trust-reg.trust-infra.swiyu.admin.ch"
    val intTrustRegistryUrl = "trust-reg.trust-infra.swiyu-int.admin.ch"

    override val trustRegistryMapping: Map<String, String> = mapOf(
        prodTrustRegistryIdentifier to prodTrustRegistryUrl,
        intTrustRegistryIdentifier to intTrustRegistryUrl,
    )

    @Suppress("MaximumLineLength")
    val prodTrustRegistryDid =
        "did:tdw:QmerEFUx69M5AB7oyoPQG6P17MbZQUHoe2Jxz9tXk7cSdf:identifier-reg.trust-infra.swiyu.admin.ch:api:v1:did:02ee8aca-041f-4683-b878-8c6efa977292"

    @Suppress("MaximumLineLength")
    val intTrustRegistryDid =
        "did:tdw:QmWrXWFEDenvoYWFXxSQGFCa6Pi22Cdsg2r6weGhY2ChiQ:identifier-reg.trust-infra.swiyu-int.admin.ch:api:v1:did:2e246676-209a-4c21-aceb-721f8a90b212"

    override val trustRegistryTrustedDids: Map<String, List<String>> = mapOf(
        prodTrustRegistryUrl to listOf(prodTrustRegistryDid),
        intTrustRegistryUrl to listOf(intTrustRegistryDid),
    )

    override val trustEnvironmentDidRegex: String = "^did:(?:tdw|webvh):[^:]+:identifier-reg\\.trust-infra\\.swiyu\\.admin\\.ch:.*"

    override val demoTrustEnvironmentDidRegex: String = "^did:(?:tdw|webvh):[^:]+:identifier-reg\\.trust-infra\\.swiyu-int\\.admin\\.ch:.*"

    override val baseTrustDomainRegex =
        Regex("^did:tdw:[^:]+:([^:]+\\.swiyu(-int)?\\.admin\\.ch):[^:]+", setOf(RegexOption.MULTILINE))

    override val betaIdRequestEnabled = true

    override val eIdRequestEnabled = false

    override val eIdMockMrzEnabled = false

    override val sidBackendUrl: String = "https://eid.admin.ch"

    override val avBackendUrl: String = "https://av.admin.ch/"

    override val eIdNfcWebSocketUrl: String = "wss://av.admin.ch/nfc/ws1/validate"

    override val appId: String = BuildConfig.APPLICATION_ID

    override val avBeamLoggingEnabled: Boolean = false

    override val nonComplianceEnabled: Boolean = false

    override val nonComplianceBaseUrl: String = "https://noncompliance.trust-infra.swiyu.admin.ch/non-compliance-service"

    override val batchIssuanceEnabled: Boolean = false

    override val payloadEncryptionEnabled: Boolean = false

    override val allowBypassOtp: Boolean = false

    override val isLottieViewerEnabled: Boolean = false

    override val devsSettingsEnabled: Boolean = false
    override val isImageValidationEnabled: Boolean = false
}
