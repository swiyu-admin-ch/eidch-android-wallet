package ch.admin.foitt.wallet.platform.environmentSetup.domain.repository

interface EnvironmentSetupRepository {
    val appVersionEnforcementUrl: String
    val attestationsServiceUrl: String
    val attestationsServiceTrustedDids: List<String>
    val trustRegistryMapping: Map<String, String>
    val trustRegistryTrustedDids: Map<String, List<String>>
    val trustEnvironmentDidRegex: String
    val demoTrustEnvironmentDidRegex: String
    val baseTrustDomainRegex: Regex
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
    val payloadEncryptionEnabled: Boolean
    val allowBypassOtp: Boolean
    val isLottieViewerEnabled: Boolean
    val devsSettingsEnabled: Boolean
    val isImageValidationEnabled: Boolean
}
