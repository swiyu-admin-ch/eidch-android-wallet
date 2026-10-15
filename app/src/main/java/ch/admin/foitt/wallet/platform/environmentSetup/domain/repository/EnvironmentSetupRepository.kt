package ch.admin.foitt.wallet.platform.environmentSetup.domain.repository

interface EnvironmentSetupRepository {
    val userAgent: String
    val appVersionEnforcementUrl: String
    val defaultAttestationServiceUrl: String
    val attestationServiceMapping: Map<String, String>
    val attestationsServiceTrustedDids: List<String>
    val trustRegistryMapping: Map<String, String>
    val statusListMapping: Map<String, String>
    val trustV1TrustRegistryTrustedDids: Map<String, List<String>>
    val trustRegistryTrustedDids: Map<String, Map<String, List<String>>>
    val trustEnvironmentDidRegex: String
    val demoTrustEnvironmentDidRegex: String
    val baseTrustDomainRegex: Regex
    val notificationBackendUrl: String
    val betaIdRequestEnabled: Boolean
    val eIdRequestEnabled: Boolean
    val eIdMockMrzEnabled: Boolean
    val sidBackendUrl: String
    val avBackendUrl: String
    val eIdNfcWebSocketUrl: String
    val appId: String
    val avBeamLoggingEnabled: Boolean
    val nonComplianceEnabled: Boolean
    val nonComplianceBaseUrl: String
    val batchIssuanceEnabled: Boolean
    val allowBypassOtp: Boolean
    val isLottieViewerEnabled: Boolean
    val devsSettingsEnabled: Boolean
    val isProximityEngagementEnabled: Boolean
    val isVersionEnforcementEnabled: Boolean
    val versionEnforcementTrustedDids: List<String>
    val terminateOnInvalidIdTSEnabled: Boolean
}
