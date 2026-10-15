package ch.admin.foitt.wallet.platform.navigation.domain.model

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GuardianConsentResultState
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationErrorScreenState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportReason
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportingData
import ch.admin.foitt.wallet.platform.verification.domain.model.VerificationMode
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.EnforcementType
import kotlinx.serialization.Serializable

typealias EntryProviderInstaller = EntryProviderScope<NavKey>.() -> Unit

/**
 * Navigation destinations. Must be @Serializable.
 * Use `data objects` for destinations without arguments and `data class` for destinations with arguments
 *
 * Destinations can implement more interfaces:
 * - sealed interfaces in [ScopedComponentGroup] to group destinations for a composition scope, e.g. [ScopedComponentGroup.CredentialIssuer]
 */
sealed interface Destination : NavKey, AutoLogout, DestinationGroups {

    // region app

    @Serializable
    data object StartScreen : Destination {
        override fun canTriggerAutoLogout() = false
    }

    // endregion app
    // region feature/changeLogin

    @Serializable
    data class ConfirmNewPassphraseScreen(val originalPassphrase: String) : Destination

    @Serializable
    data object EnterCurrentPassphraseScreen : Destination

    @Serializable
    data object EnterNewPassphraseScreen : Destination

    // endregion feature/changeLogin
    // region feature/credentialDetail

    @Serializable
    data class CredentialDetailScreen(val credentialId: Long) : Destination

    @Serializable
    data class UpdateCredentialScreen(val credentialId: Long) : Destination

    @Serializable
    data object IssuanceInfoScreen : Destination

    @Serializable
    data class BatchIssuanceInfoScreen(val credentialId: Long) : Destination

    // endregion feature/credentialDetail
    // region feature/deferredDetail

    @Serializable
    data class DeferredDetailScreen(val credentialId: Long) : Destination

    // endregion feature/deferredDetail
    // region feature/settings

    @Serializable
    data object SettingsScreen : Destination

    @Serializable
    data class AuthWithPassphraseScreen(val enableBiometrics: Boolean) : Destination

    @Serializable
    data class EnableBiometricsScreen(val passphrase: String) : Destination

    @Serializable
    data class EnableBiometricsErrorScreen(val errorCode: String) : Destination

    @Serializable
    data object ImpressumScreen : Destination

    @Serializable
    data object LottieViewerScreen : Destination

    @Serializable
    data object LanguageScreen : Destination

    @Serializable
    data object AccessibilityScreen : Destination

    @Serializable
    data object LicencesScreen : Destination

    @Serializable
    data class EnableBiometricsLockoutScreen(val errorCode: String) : Destination

    @Serializable
    data object SecuritySettingsScreen : Destination

    @Serializable
    data object DataAnalysisScreen : Destination

    @Serializable
    data object ActivityListSettingsScreen : Destination

    // endregion feature/settings
    // region feature/onboarding

    @Serializable
    data object OnboardingIntroScreen : Destination {
        override fun canTriggerAutoLogout() = false
        override fun destinationGroups() = setOf(DestinationGroup.ONBOARDING)
    }

    @Serializable
    data object OnboardingLocalDataScreen : Destination {
        override fun canTriggerAutoLogout() = false
        override fun destinationGroups() = setOf(DestinationGroup.ONBOARDING)
    }

    @Serializable
    data object OnboardingActivityScreen : Destination {
        override fun canTriggerAutoLogout() = false
        override fun destinationGroups() = setOf(DestinationGroup.ONBOARDING)
    }

    @Serializable
    data object OnboardingPresentScreen : Destination {
        override fun canTriggerAutoLogout() = false
        override fun destinationGroups() = setOf(DestinationGroup.ONBOARDING)
    }

    @Serializable
    data object OnboardingPrivacyPolicyScreen : Destination {
        override fun canTriggerAutoLogout() = false
        override fun destinationGroups() = setOf(DestinationGroup.ONBOARDING)
    }

    @Serializable
    data object OnboardingPassphraseExplanationScreen : Destination {
        override fun canTriggerAutoLogout() = false
        override fun destinationGroups() = setOf(DestinationGroup.ONBOARDING)
    }

    @Serializable
    data object OnboardingSetupPassphraseScreen : Destination {
        override fun canTriggerAutoLogout() = false
        override fun destinationGroups() = setOf(DestinationGroup.ONBOARDING)
    }

    @Serializable
    data class OnboardingConfirmPassphraseScreen(val passphrase: String) : Destination {
        override fun canTriggerAutoLogout() = false
        override fun destinationGroups() = setOf(DestinationGroup.ONBOARDING)
    }

    @Serializable
    data object OnboardingConfirmPassphraseFailureScreen : Destination {
        override fun canTriggerAutoLogout() = false
        override fun destinationGroups() = setOf(DestinationGroup.ONBOARDING)
    }

    @Serializable
    data class OnboardingRegisterBiometricsScreen(val passphrase: String) : Destination {
        override fun canTriggerAutoLogout() = false
        override fun destinationGroups() = setOf(DestinationGroup.ONBOARDING)
    }

    @Serializable
    data class OnboardingFatalErrorScreen(
        val primaryTextRes: Int,
        val secondaryTextRes: Int,
        val errorCode: String,
    ) : Destination {
        override fun canTriggerAutoLogout() = false
        override fun destinationGroups() = setOf(DestinationGroup.ONBOARDING)
    }

    // OnboardingSuccessScreen is outside of the NavigationScope.Onboarding.
    @Serializable
    data object OnboardingSuccessScreen : Destination, ScopedComponentGroup.CredentialIssuer, ScopedComponentGroup.Verifier

    // endregion feature/onboarding
    // region feature/login

    @Serializable
    data object BiometricLoginScreen : Destination, ScopedComponentGroup.CredentialIssuer, ScopedComponentGroup.Verifier {
        override fun canTriggerAutoLogout() = false
    }

    @Serializable
    data object LockScreen : Destination {
        override fun canTriggerAutoLogout() = false
    }

    @Serializable
    data object LockoutScreen : Destination {
        override fun canTriggerAutoLogout() = false
    }

    @Serializable
    data class PassphraseLoginScreen(
        val biometricsLocked: Boolean
    ) : Destination, ScopedComponentGroup.CredentialIssuer, ScopedComponentGroup.Verifier {
        override fun canTriggerAutoLogout() = false
    }

    @Serializable
    data object UnsecuredDeviceScreen : Destination {
        override fun canTriggerAutoLogout() = false
    }

    // endregion feature/login
    // region feature/home

    @Serializable
    data object HomeScreen : Destination

    @Serializable
    data object BetaIdScreen : Destination

    // endregion feature/home
    // region feature/eIdApplicationProcess

    @Serializable
    data object EIdIntroScreen : Destination {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data object EIdAttestationScreen :
        Destination,
        ScopedComponentGroup.AvBeamSdkSession {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data object EIdDocumentSelectionScreen :
        Destination,
        ScopedComponentGroup.EidApplicationProcess,
        ScopedComponentGroup.AvBeamSdkSession {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data class EIdGuardianConsentResultScreen(
        val rawDeadline: String?,
        val screenState: GuardianConsentResultState,
    ) : Destination {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data class EIdGuardianConsentScreen(val caseId: String) : Destination {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data class EIdGuardianSelectionScreen(val caseId: String) : Destination {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data object EIdGuardianshipScreen :
        Destination,
        ScopedComponentGroup.EidApplicationProcess,
        ScopedComponentGroup.AvBeamSdkSession {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data class EIdGuardianVerificationScreen(val caseId: String) : Destination {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data object EIdPrivacyPolicyScreen :
        Destination,
        ScopedComponentGroup.AvBeamSdkSession {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data class EIdProcessDataScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.EidOnlineSession,
        ScopedComponentGroup.AvBeamSdkSession {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION)
    }

    @Serializable
    data class EIdQueueScreen(val rawDeadline: String?) : Destination {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data object EIdReadyForAvScreen : Destination {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION)
    }

    @Serializable
    data class EIdStartAvSessionScreen(val caseId: String) : Destination {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION)
    }

    @Serializable
    data class EIdStartSelfieVideoScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.EidOnlineSession,
        ScopedComponentGroup.AvBeamSdkSession {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION)
    }

    @Serializable
    data object EIdProcessDataConfirmationScreen :
        Destination {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION)
    }

    @Serializable
    data object EIdNotSupportedDeviceScreen : Destination {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data object EIdExternalDeviceDetectedScreen : Destination {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }
    // endregion feature/eIdApplicationProcess
    // region feature/eIDRequestVerification

    @Serializable
    data class EIdDocumentRecordingScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.AvBeamSdkSession,
        ScopedComponentGroup.EidOnlineSession {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION)
    }

    @Serializable
    data class EIdDocumentScannerScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.EidApplicationProcess,
        ScopedComponentGroup.AvBeamSdkSession,
        ScopedComponentGroup.EidDocumentScan,
        ScopedComponentGroup.EidOnlineSession {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION, DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data class EIdDocumentScanSummaryScreen(
        val caseId: String,
    ) :
        Destination,
        ScopedComponentGroup.EidApplicationProcess,
        ScopedComponentGroup.AvBeamSdkSession,
        ScopedComponentGroup.EidDocumentScan,
        ScopedComponentGroup.EidOnlineSession {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION, DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data class EIdDocumentScannerInfoScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.AvBeamSdkSession,
        ScopedComponentGroup.EidOnlineSession,
        ScopedComponentGroup.EidApplicationProcess {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION, DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data class EIdDocumentRecordingInfoScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.AvBeamSdkSession,
        ScopedComponentGroup.EidOnlineSession {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION)
    }

    @Serializable
    data class EIdFaceScannerScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.AvBeamSdkSession,
        ScopedComponentGroup.EidOnlineSession {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION)
    }

    @Serializable
    data class EIdNfcScannerScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.AvBeamSdkSession,
        ScopedComponentGroup.EidOnlineSession {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION)
    }

    @Suppress("ArrayInDataClass")
    @Serializable
    data class EIdNfcSummaryScreen(
        val caseId: String,
        val picture: ByteArray,
        val givenName: String,
        val surname: String,
        val documentId: String,
        val expiryDate: String,
    ) : Destination,
        ScopedComponentGroup.AvBeamSdkSession,
        ScopedComponentGroup.EidOnlineSession {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION)
    }

    @Serializable
    data class EIdStartAutoVerificationScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.EidOnlineSession {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION)
    }

    @Serializable
    data object MrzChooserScreen :
        Destination,
        ScopedComponentGroup.EidApplicationProcess,
        ScopedComponentGroup.AvBeamSdkSession,
        ScopedComponentGroup.EidDocumentScan {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data class MrzSubmissionScreen(val mrzLines: List<String>) :
        Destination,
        ScopedComponentGroup.EidApplicationProcess,
        ScopedComponentGroup.AvBeamSdkSession,
        ScopedComponentGroup.EidDocumentScan,
        ScopedComponentGroup.EidOnlineSession {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION, DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data class EIdPushNotificationScreen(val caseId: String) : Destination {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    // endregion feature/eIDRequestVerification
    // region feature/walletPairing

    @Serializable
    data class EIdPairingOverviewScreen(val caseId: String) : Destination {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION)
    }

    @Serializable
    data class EIdWalletPairingQrCodeScreen(val caseId: String) : Destination {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION)
    }

    @Serializable
    data class EIdWalletPairingScreen(val caseId: String) : Destination {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION)
    }

    @Serializable
    data object EIdWalletPairingTimeoutScreen : Destination {
        override fun destinationGroups() = setOf(DestinationGroup.EID_REQUEST_VERIFICATION)
    }

    // endregion feature/walletPairing
    // region feature/qrscan

    @Serializable
    data class ShowOrScanQrCodeScreen(
        val verificationMode: VerificationMode
    ) : Destination, ScopedComponentGroup.CredentialIssuer, ScopedComponentGroup.Verifier

    @Serializable
    data object QrScannerScreen : Destination, ScopedComponentGroup.CredentialIssuer, ScopedComponentGroup.Verifier

    // endregion feature/qrscan
    // region feature/credentialOffer

    @Serializable
    data class CredentialOfferScreen(
        val credentialId: Long,
    ) : Destination, ScopedComponentGroup.CredentialIssuer {
        override fun destinationGroups() = setOf(DestinationGroup.CREDENTIAL_OFFER)
    }

    @Serializable
    data class DeclineCredentialOfferScreen(
        val credentialId: Long,
    ) : Destination, ScopedComponentGroup.CredentialIssuer {
        override fun destinationGroups() = setOf(DestinationGroup.CREDENTIAL_OFFER)
    }

    // endregion feature/credentialOffer
    // region feature/presentationRequest

    @Serializable
    data class PresentationCredentialListScreen(
        val compatibleCredentials: Set<CompatibleCredential>,
        val presentationRequestWithRaw: PresentationRequestWithRaw,
    ) : Destination, ScopedComponentGroup.Verifier {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data class PresentationDeclinedScreen(
        val redirectUri: String?,
        val credentialId: Long,
        val activityId: Long?,

    ) : Destination, ScopedComponentGroup.Verifier {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data class PresentationFailureScreen(
        val compatibleCredential: CompatibleCredential,
        val presentationRequestWithRaw: PresentationRequestWithRaw,
    ) : Destination, ScopedComponentGroup.Verifier {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data class PresentationRequestScreen(
        val compatibleCredential: CompatibleCredential,
        val presentationRequestWithRaw: PresentationRequestWithRaw,
    ) : Destination, ScopedComponentGroup.Verifier {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data class PresentationRequestReviewScreen(
        val compatibleCredentials: Set<CompatibleCredential>,
        val presentationRequestWithRaw: PresentationRequestWithRaw,
    ) : Destination, ScopedComponentGroup.Verifier {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data class PresentationSuccessScreen(
        val redirectUri: String?,
    ) : Destination, ScopedComponentGroup.Verifier {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data object PresentationRequestBlockedScreen : Destination, ScopedComponentGroup.Verifier {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    @Serializable
    data object PresentationVerificationErrorScreen :
        Destination,
        ScopedComponentGroup.Verifier {
        override fun destinationGroups() = setOf(DestinationGroup.EID_APPLICATION_PROCESS)
    }

    // endregion feature/presentationRequest
    // region platform/activityList

    @Serializable
    data class ActivityDetailScreen(val credentialId: Long, val activityId: Long) : Destination, ScopedComponentGroup.Verifier

    @Serializable
    data class ActivityListScreen(val credentialId: Long) : Destination, ScopedComponentGroup.Verifier

    // endregion platform/activityList
    // region platform/nonCompliance
    @Serializable
    data object NonComplianceDescriptionInputScreen : Destination, ScopedComponentGroup.NonComplianceFormInput

    @Serializable
    data object NonComplianceEmailInputScreen : Destination, ScopedComponentGroup.NonComplianceFormInput

    @Serializable
    data object NonComplianceContextInputScreen : Destination, ScopedComponentGroup.NonComplianceFormInput

    @Serializable
    data class NonComplianceFormScreen(
        val reportingData: NonComplianceReportingData,
        val titleId: Int?,
        val reportReason: NonComplianceReportReason,
        val goBackToRoot: Boolean,
    ) : Destination, ScopedComponentGroup.NonComplianceFormInput

    @Serializable
    data class NonComplianceListScreen(
        val reportingData: NonComplianceReportingData,
        val goBackToRoot: Boolean = false,
    ) : Destination

    // endregion platform/nonCompliance
    // region platform/invitation

    @Serializable
    data class InvitationFailureScreen(
        val invitationError: InvitationErrorScreenState,
        val responseUri: String?,
        val state: String?,
    ) : Destination, ScopedComponentGroup.Verifier

    // endregion platform/invitation
    // region platform/screens
    @Serializable
    data class GenericErrorScreen(val error: GenericErrorScreenState) : Destination

    // endregion platform/screens
    // region platform/versionEnforcement

    @Serializable
    data class AppVersionBlockedScreen(
        val title: String?,
        val text: String?,
        val playStoreUrl: String,
        val enforcedType: EnforcementType
    ) : Destination {
        override fun canTriggerAutoLogout() = false
    }

    // endregion platform/versionEnforcement
    // region platform/otp

    @Serializable
    data object OtpIntroScreen : Destination

    @Serializable
    data object OtpLegalScreen : Destination

    @Serializable
    data object OtpEmailInputScreen : Destination, ScopedComponentGroup.Otp

    @Serializable
    data object OtpCodeInputScreen : Destination, ScopedComponentGroup.Otp

    // endregion platform/otp
}
