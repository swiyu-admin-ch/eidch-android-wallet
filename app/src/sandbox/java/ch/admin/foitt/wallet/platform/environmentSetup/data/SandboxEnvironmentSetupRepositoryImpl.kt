package ch.admin.foitt.wallet.platform.environmentSetup.data

import ch.admin.foitt.wallet.BuildConfig
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import javax.inject.Inject

class SandboxEnvironmentSetupRepositoryImpl @Inject constructor() : EnvironmentSetupRepository {
    override val appVersionEnforcementUrl: String = "https://wallet-ve.trust-infra.swiyu.admin.ch/v1/android"

    override val attestationsServiceUrl: String = "https://attestations.trust-infra.swiyu.admin.ch"

    @Suppress("MaximumLineLength")
    override val attestationsServiceTrustedDids: List<String> = listOf()

    val intTrustRegistryUrl = "trust-reg.trust-infra.swiyu-int.admin.ch"

    val intTrustRegistryIdentifier = "identifier-reg.trust-infra.swiyu-int.admin.ch"

    override val trustRegistryMapping: Map<String, String> = mapOf(
        intTrustRegistryIdentifier to intTrustRegistryUrl,
    )

    @Suppress("MaximumLineLength")
    private val trustV1IntTrustRegistryDid =
        "did:tdw:QmWrXWFEDenvoYWFXxSQGFCa6Pi22Cdsg2r6weGhY2ChiQ:identifier-reg.trust-infra.swiyu-int.admin.ch:api:v1:did:2e246676-209a-4c21-aceb-721f8a90b212"

    override val trustV1TrustRegistryTrustedDids: Map<String, List<String>> = mapOf(
        intTrustRegistryUrl to listOf(trustV1IntTrustRegistryDid),
    )

    @Suppress("MaximumLineLength")
    private val intTrustStatementIssuer =
        "did:webvh:QmdVPcfEJgvQAJKEjaTWAhskT1kc59KZQiXNenqHBB7iH5:identifier-reg.trust-infra.swiyu-int.admin.ch:api:v1:did:4c131dc4-ced1-454b-bbd4-9401c7512e37"

    @Suppress("MaximumLineLength")
    private val intPublicTransparencyStatementIssuer =
        "did:webvh:QmNTHuhETA3u2ypoujoaEMaZGKf5HpPwkV6ktfgzu7JzMp:identifier-reg.trust-infra.swiyu-int.admin.ch:api:v1:did:5e5de412-0e7d-4982-a0ed-bd55a0f25a04"

    override val trustRegistryTrustedDids: Map<String, Map<String, List<String>>> = mapOf(
        intTrustRegistryUrl to mapOf(
            "swiyu-verification-query-public-statement+jwt" to listOf(intPublicTransparencyStatementIssuer),
            "swiyu-identity-trust-statement+jwt" to listOf(intTrustStatementIssuer),
            "swiyu-protected-issuance-trust-list-statement+jwt" to listOf(intTrustStatementIssuer),
            "swiyu-protected-issuance-authorization-trust-statement+jwt" to listOf(intTrustStatementIssuer),
            "swiyu-non-compliance-trust-list-statement+jwt" to listOf(intTrustStatementIssuer),
            "swiyu-protected-verification-authorization-trust-statement+jwt" to listOf(intTrustStatementIssuer),
        )
    )

    override val trustEnvironmentDidRegex: String = "^did:(?:tdw|webvh):[^:]+:identifier-reg\\.trust-infra\\.swiyu\\.admin\\.ch:.*"

    override val demoTrustEnvironmentDidRegex: String = "^did:(?:tdw|webvh):[^:]+:identifier-reg\\.trust-infra\\.swiyu-int\\.admin\\.ch:.*"

    override val baseTrustDomainRegex =
        Regex("^did:tdw:[^:]+:([^:]+\\.swiyu(-int)?\\.admin\\.ch):[^:]+", setOf(RegexOption.MULTILINE))

    override val notificationBackendUrl = "https://push-api.trust-infra.swiyu.admin.ch"

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

    override val payloadEncryptionEnabled: Boolean = true

    override val allowBypassOtp: Boolean = false

    override val isLottieViewerEnabled: Boolean = false

    override val devsSettingsEnabled: Boolean = false

    override val isProximityEngagementEnabled: Boolean = false

    override val verifyRequestObjectSignature: Boolean = true

    override val isVersionEnforcementEnabled: Boolean = false

    override val isDPopEnabled: Boolean = false
}
