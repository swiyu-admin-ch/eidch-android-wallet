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

class SandboxEnvironmentSetupRepositoryImpl @Inject constructor() : EnvironmentSetupRepository {
    val intTrustRegistryIdentifier = "identifier-reg.trust-infra.swiyu-int.admin.ch"

    override val userAgent: String = "swiyuSandboxWallet"

    override val appVersionEnforcementUrl: String = "https://wallet-ve.trust-infra.swiyu.admin.ch/v1/android"

    override val defaultAttestationServiceUrl = "https://attestations.trust-infra.swiyu.admin.ch"

    override val attestationServiceMapping: Map<String, String> = mapOf(
        intTrustRegistryIdentifier to defaultAttestationServiceUrl,
    )

    @Suppress("MaximumLineLength")
    override val attestationsServiceTrustedDids: List<String> = listOf(
        "did:tdw:QmVxp7q4pFKRp8zf7KftJBRroNRF6dVzHns3Sq7EdjxQep:identifier-reg.trust-infra.swiyu.admin.ch:api:v1:did:9f94645c-2b23-4f7d-9c8c-21c77e9995a5",
        "did:webvh:QmSnE8nCxzoFuXcJS9GoowDjX8rG3vsWy4fYbpvYEZpKEa:identifier-reg.trust-infra.swiyu.admin.ch:api:v1:did:0e547c8b-64bd-467e-a21f-8b959a1d38b4",
    )

    val intTrustRegistryUrl = "trust-reg.trust-infra.swiyu-int.admin.ch"

    override val trustRegistryMapping: Map<String, String> = mapOf(
        intTrustRegistryIdentifier to intTrustRegistryUrl,
    )

    val intStatusListUrl = "status-reg.trust-infra.swiyu-int.admin.ch"

    override val statusListMapping: Map<String, String> = mapOf(
        intTrustRegistryIdentifier to intStatusListUrl
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
            VerificationQueryPublicStatement.TYPE to listOf(intPublicTransparencyStatementIssuer),
            IdentityV2TrustStatement.TYPE to listOf(intTrustStatementIssuer),
            ProtectedIssuanceTrustListStatement.TYPE to listOf(intTrustStatementIssuer),
            ProtectedIssuanceAuthorizationTrustStatement.TYPE to listOf(intTrustStatementIssuer),
            NonComplianceTrustListStatement.TYPE to listOf(intTrustStatementIssuer),
            VerificationAuthorizationTrustStatement.TYPE to listOf(intTrustStatementIssuer),
        )
    )

    override val trustEnvironmentDidRegex: String = "^did:(?:tdw|webvh):[^:]+:identifier-reg\\.trust-infra\\.swiyu-int\\.admin\\.ch:.*"

    override val demoTrustEnvironmentDidRegex: String = "not available in sandbox env"

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

    override val isVersionEnforcementEnabled: Boolean = false

    override val versionEnforcementTrustedDids: List<String> = emptyList()

    override val terminateOnInvalidIdTSEnabled: Boolean = true
}
