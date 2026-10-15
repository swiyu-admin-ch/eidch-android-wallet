package ch.admin.foitt.wallet.platform.environmentSetup.data

import ch.admin.foitt.wallet.BuildConfig
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationQueryPublicStatement
import javax.inject.Inject

class MainEnvironmentSetupRepositoryImpl @Inject constructor() : EnvironmentSetupRepository {
    val prodTrustRegistryIdentifier = "identifier-reg.trust-infra.swiyu.admin.ch"
    val prodIntTrustRegistryIdentifier = "identifier-reg.trust-infra.swiyu-int.admin.ch"

    override val userAgent: String = "swiyuWallet"

    @Suppress("MaximumLineLength")
    override val appVersionEnforcementUrl: String = "https://versioning.trust-infra.swiyu.admin.ch/api/versioning?platform=android&app_id=wallet"

    override val defaultAttestationServiceUrl = "https://attestations.trust-infra.swiyu.admin.ch"

    override val attestationServiceMapping: Map<String, String> = mapOf(
        prodTrustRegistryIdentifier to defaultAttestationServiceUrl,
        prodIntTrustRegistryIdentifier to defaultAttestationServiceUrl,
    )

    @Suppress("MaximumLineLength")
    override val attestationsServiceTrustedDids: List<String> = listOf(
        "did:tdw:QmVxp7q4pFKRp8zf7KftJBRroNRF6dVzHns3Sq7EdjxQep:identifier-reg.trust-infra.swiyu.admin.ch:api:v1:did:9f94645c-2b23-4f7d-9c8c-21c77e9995a5",
        "did:webvh:QmSnE8nCxzoFuXcJS9GoowDjX8rG3vsWy4fYbpvYEZpKEa:identifier-reg.trust-infra.swiyu.admin.ch:api:v1:did:0e547c8b-64bd-467e-a21f-8b959a1d38b4",
    )

    val prodTrustRegistryUrl = "trust-reg.trust-infra.swiyu.admin.ch"
    val prodIntTrustRegistryUrl = "trust-reg.trust-infra.swiyu-int.admin.ch"

    override val trustRegistryMapping: Map<String, String> = mapOf(
        prodTrustRegistryIdentifier to prodTrustRegistryUrl,
        prodIntTrustRegistryIdentifier to prodIntTrustRegistryUrl,
    )

    val prodStatusListUrl = "status-reg.trust-infra.swiyu.admin.ch"
    val prodIntStatusListUrl = "status-reg.trust-infra.swiyu-int.admin.ch"

    override val statusListMapping: Map<String, String> = mapOf(
        prodTrustRegistryIdentifier to prodStatusListUrl,
        prodIntTrustRegistryIdentifier to prodIntStatusListUrl,
    )

    // ---------------------------- trust protocol V1 ----------------------------
    @Suppress("MaximumLineLength")
    private val prodTrustV1TrustRegistryDid =
        "did:tdw:QmerEFUx69M5AB7oyoPQG6P17MbZQUHoe2Jxz9tXk7cSdf:identifier-reg.trust-infra.swiyu.admin.ch:api:v1:did:02ee8aca-041f-4683-b878-8c6efa977292"

    @Suppress("MaximumLineLength")
    private val prodIntTrustV1TrustRegistryDid =
        "did:tdw:QmWrXWFEDenvoYWFXxSQGFCa6Pi22Cdsg2r6weGhY2ChiQ:identifier-reg.trust-infra.swiyu-int.admin.ch:api:v1:did:2e246676-209a-4c21-aceb-721f8a90b212"

    override val trustV1TrustRegistryTrustedDids: Map<String, List<String>> = mapOf(
        prodTrustRegistryUrl to listOf(prodTrustV1TrustRegistryDid),
        prodIntTrustRegistryUrl to listOf(prodIntTrustV1TrustRegistryDid),
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
    private val prodIntTrustStatementIssuer =
        "did:webvh:QmdVPcfEJgvQAJKEjaTWAhskT1kc59KZQiXNenqHBB7iH5:identifier-reg.trust-infra.swiyu-int.admin.ch:api:v1:did:4c131dc4-ced1-454b-bbd4-9401c7512e37"

    @Suppress("MaximumLineLength")
    private val prodIntPublicTransparencyStatementIssuer =
        "did:webvh:QmNTHuhETA3u2ypoujoaEMaZGKf5HpPwkV6ktfgzu7JzMp:identifier-reg.trust-infra.swiyu-int.admin.ch:api:v1:did:5e5de412-0e7d-4982-a0ed-bd55a0f25a04"

    override val trustRegistryTrustedDids: Map<String, Map<String, List<String>>> = mapOf(
        prodTrustRegistryUrl to mapOf(
            VerificationQueryPublicStatement.TYPE to listOf(prodPublicTransparencyStatementIssuer),
            IdentityV2TrustStatement.TYPE to listOf(prodTrustStatementIssuer),
            ProtectedIssuanceTrustListStatement.TYPE to listOf(prodTrustStatementIssuer),
            ProtectedIssuanceAuthorizationTrustStatement.TYPE to listOf(prodTrustStatementIssuer),
            NonComplianceTrustListStatement.TYPE to listOf(prodTrustStatementIssuer),
            VerificationAuthorizationTrustStatement.TYPE to listOf(prodTrustStatementIssuer),
        ),
        prodIntTrustRegistryUrl to mapOf(
            VerificationQueryPublicStatement.TYPE to listOf(prodIntPublicTransparencyStatementIssuer),
            IdentityV2TrustStatement.TYPE to listOf(prodIntTrustStatementIssuer),
            ProtectedIssuanceTrustListStatement.TYPE to listOf(prodIntTrustStatementIssuer),
            ProtectedIssuanceAuthorizationTrustStatement.TYPE to listOf(prodIntTrustStatementIssuer),
            NonComplianceTrustListStatement.TYPE to listOf(prodIntTrustStatementIssuer),
            VerificationAuthorizationTrustStatement.TYPE to listOf(prodIntTrustStatementIssuer),
        ),
    )
    // ---------------------------- end trust protocol V2 ----------------------------

    @Suppress("MaximumLineLength")
    private val prodMobileVersioningServiceIssuerV2 =
        "did:webvh:QmdQUxfqpECnuHK5b3wNmHiF6zUH41nRx9RVCzEpcKZwCE:identifier-reg.trust-infra.swiyu.admin.ch:api:v1:did:c6e1fa56-248d-45c8-bfc7-42f681539f10"

    @Suppress("MaximumLineLength")
    private val prodMobileVersioningServiceIssuerV1 =
        "did:tdw:Qmet5dXTKHmvArLVJTxrg1e9rfgQmMFxgBtf2zp4B7YTGq:identifier-reg.trust-infra.swiyu.admin.ch:api:v1:did:da06c198-8544-44eb-a558-1ac87fd89f21"

    override val trustEnvironmentDidRegex: String = "^did:(?:tdw|webvh):[^:]+:identifier-reg\\.trust-infra\\.swiyu\\.admin\\.ch:.*"

    @Suppress("MaximumLineLength")
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

    override val allowBypassOtp: Boolean = false

    override val isLottieViewerEnabled: Boolean = false

    override val devsSettingsEnabled: Boolean = false
    override val isProximityEngagementEnabled: Boolean = false

    override val isVersionEnforcementEnabled: Boolean = true

    override val versionEnforcementTrustedDids: List<String> = listOf(
        prodMobileVersioningServiceIssuerV1,
        prodMobileVersioningServiceIssuerV2
    )

    override val terminateOnInvalidIdTSEnabled: Boolean = false
}
