package ch.admin.foitt.wallet.platform.environmentSetup.data

import ch.admin.foitt.wallet.BuildConfig
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import javax.inject.Inject

class MainEnvironmentSetupRepositoryImpl @Inject constructor() : EnvironmentSetupRepository {
    @Suppress("MaximumLineLength")
    override val appVersionEnforcementUrl: String = "https://versioning.trust-infra.swiyu.admin.ch/api/versioning?platform=android&app_id=wallet"

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

    // ---------------------------- trust protocol V1 ----------------------------
    @Suppress("MaximumLineLength")
    private val trustV1ProdTrustRegistryDid =
        "did:tdw:QmerEFUx69M5AB7oyoPQG6P17MbZQUHoe2Jxz9tXk7cSdf:identifier-reg.trust-infra.swiyu.admin.ch:api:v1:did:02ee8aca-041f-4683-b878-8c6efa977292"

    @Suppress("MaximumLineLength")
    private val trustV1IntTrustRegistryDid =
        "did:tdw:QmWrXWFEDenvoYWFXxSQGFCa6Pi22Cdsg2r6weGhY2ChiQ:identifier-reg.trust-infra.swiyu-int.admin.ch:api:v1:did:2e246676-209a-4c21-aceb-721f8a90b212"

    override val trustV1TrustRegistryTrustedDids: Map<String, List<String>> = mapOf(
        prodTrustRegistryUrl to listOf(trustV1ProdTrustRegistryDid),
        intTrustRegistryUrl to listOf(trustV1IntTrustRegistryDid),
    )
    // ---------------------------- end trust protocol V1 ----------------------------

    // ---------------------------- trust protocol V2 ----------------------------
    @Suppress("MaximumLineLength")
    private val prodTrustStatementIssuer =
        "did:webvh:QmaemSxuZiADoV3F5aBxyvemrUgbWURCv67KU222midYSo:identifier-reg.trust-infra.swiyu.admin.ch:api:v1:did:cc6c0cc8-0743-4cf0-a6b8-c87e30c78d31"

    @Suppress("MaximumLineLength")
    private val prodPublicTransparencyStatementIssuer =
        "did:webvh:QmUtTiwizd74sn8vv2XfsjjrzjaEuPTbgeT2u3MpFCCqHu:identifier-reg.trust-infra.swiyu.admin.ch:api:v1:did:82b5f3c4-ce00-464d-bc45-b28d3a04ee73"

    @Suppress("MaximumLineLength")
    private val intTrustStatementIssuer =
        "did:webvh:QmdVPcfEJgvQAJKEjaTWAhskT1kc59KZQiXNenqHBB7iH5:identifier-reg.trust-infra.swiyu-int.admin.ch:api:v1:did:4c131dc4-ced1-454b-bbd4-9401c7512e37"

    @Suppress("MaximumLineLength")
    private val intPublicTransparencyStatementIssuer =
        "did:webvh:QmNTHuhETA3u2ypoujoaEMaZGKf5HpPwkV6ktfgzu7JzMp:identifier-reg.trust-infra.swiyu-int.admin.ch:api:v1:did:5e5de412-0e7d-4982-a0ed-bd55a0f25a04"
    override val trustRegistryTrustedDids: Map<String, Map<String, List<String>>> = mapOf(
        prodTrustRegistryUrl to mapOf(
            "swiyu-verification-query-public-statement+jwt" to listOf(prodPublicTransparencyStatementIssuer),
            "swiyu-identity-trust-statement+jwt" to listOf(prodTrustStatementIssuer),
            "swiyu-protected-issuance-trust-list-statement+jwt" to listOf(prodTrustStatementIssuer),
            "swiyu-protected-issuance-authorization-trust-statement+jwt" to listOf(prodTrustStatementIssuer),
            "swiyu-non-compliance-trust-list-statement+jwt" to listOf(prodTrustStatementIssuer),
            "swiyu-protected-verification-authorization-trust-statement+jwt" to listOf(prodTrustStatementIssuer),
        ),
        intTrustRegistryUrl to mapOf(
            "swiyu-verification-query-public-statement+jwt" to listOf(intPublicTransparencyStatementIssuer),
            "swiyu-identity-trust-statement+jwt" to listOf(intTrustStatementIssuer),
            "swiyu-protected-issuance-trust-list-statement+jwt" to listOf(intTrustStatementIssuer),
            "swiyu-protected-issuance-authorization-trust-statement+jwt" to listOf(intTrustStatementIssuer),
            "swiyu-non-compliance-trust-list-statement+jwt" to listOf(intTrustStatementIssuer),
            "swiyu-protected-verification-authorization-trust-statement+jwt" to listOf(intTrustStatementIssuer),
        )
    )
    // ---------------------------- end trust protocol V2 ----------------------------

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

    override val isVersionEnforcementEnabled: Boolean = true
    override val isDPopEnabled: Boolean = false
}
