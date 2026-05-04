package ch.admin.foitt.wallet.platform.navigation.domain.model

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GuardianConsentResultState
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationErrorScreenState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportReason
import kotlinx.serialization.Serializable

typealias EntryProviderInstaller = EntryProviderScope<NavKey>.() -> Unit

/**
 * Destinations implementing [NoAutoLogout] do not trigger a session timeout/a lock screen
 */
sealed interface NoAutoLogout

/**
 * Navigation destinations. Must be @Serializable.
 * Use `data objects` for destinations without arguments and `data class` for destinations with arguments
 *
 * Destinations can implement more interfaces:
 * - [NoAutoLogout] to disable auto-logout on this destination
 * - sealed interfaces in [ScopedComponentGroup] to group destinations for a composition scope, e.g. [ScopedComponentGroup.CredentialIssuer]
 * - sealed interfaces in [DestinationGroup] to create a destinations scope used [ch.admin.foitt.wallet.platform.navigation.NavigationManager.navigateOutOf]
 */
sealed interface Destination : NavKey {

    // region app

    @Serializable
    data object StartScreen : Destination, NoAutoLogout

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
    data object CredentialDetailWrongDataScreen : Destination

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
    data class EnableBiometricsScreen(val pin: String) : Destination

    @Serializable
    data object EnableBiometricsErrorScreen : Destination

    @Serializable
    data object ImpressumScreen : Destination

    @Serializable
    data object LottieViewerScreen : Destination

    @Serializable
    data object LanguageScreen : Destination

    @Serializable
    data object LicencesScreen : Destination

    @Serializable
    data object EnableBiometricsLockoutScreen : Destination

    @Serializable
    data object SecuritySettingsScreen : Destination

    @Serializable
    data object DataAnalysisScreen : Destination

    @Serializable
    data object ActivityListSettingsScreen : Destination

    // endregion feature/settings
    // region feature/onboarding

    @Serializable
    data object OnboardingIntroScreen : Destination, DestinationGroup.Onboarding, NoAutoLogout

    @Serializable
    data object OnboardingLocalDataScreen : Destination, DestinationGroup.Onboarding, NoAutoLogout

    @Serializable
    data object OnboardingActivityScreen : Destination, DestinationGroup.Onboarding, NoAutoLogout

    @Serializable
    data object OnboardingPresentScreen : Destination, DestinationGroup.Onboarding, NoAutoLogout

    @Serializable
    data object OnboardingPrivacyPolicyScreen : Destination, DestinationGroup.Onboarding, NoAutoLogout

    @Serializable
    data object OnboardingPassphraseExplanationScreen : Destination, DestinationGroup.Onboarding, NoAutoLogout

    @Serializable
    data object OnboardingSetupPassphraseScreen : Destination, DestinationGroup.Onboarding, NoAutoLogout

    @Serializable
    data class OnboardingConfirmPassphraseScreen(val passphrase: String) : Destination, DestinationGroup.Onboarding, NoAutoLogout

    @Serializable
    data object OnboardingConfirmPassphraseFailureScreen : Destination, DestinationGroup.Onboarding, NoAutoLogout

    @Serializable
    data class OnboardingRegisterBiometricsScreen(val passphrase: String) : Destination, DestinationGroup.Onboarding, NoAutoLogout

    @Serializable
    data object OnboardingErrorScreen : Destination, DestinationGroup.Onboarding, NoAutoLogout

    // OnboardingSuccessScreen is outside of the NavigationScope.Onboarding.
    @Serializable
    data object OnboardingSuccessScreen : Destination, ScopedComponentGroup.CredentialIssuer

    // endregion feature/onboarding
    // region feature/login

    @Serializable
    data object BiometricLoginScreen : Destination, NoAutoLogout, ScopedComponentGroup.CredentialIssuer, ScopedComponentGroup.Verifier

    @Serializable
    data object LockScreen : Destination, NoAutoLogout

    @Serializable
    data object LockoutScreen : Destination, NoAutoLogout

    @Serializable
    data class PassphraseLoginScreen(
        val biometricsLocked: Boolean
    ) : Destination, NoAutoLogout, ScopedComponentGroup.CredentialIssuer, ScopedComponentGroup.Verifier

    @Serializable
    data object UnsecuredDeviceScreen : Destination, NoAutoLogout

    // endregion feature/login
    // region feature/home

    @Serializable
    data object HomeScreen : Destination

    @Serializable
    data object BetaIdScreen : Destination

    // endregion feature/home
    // region feature/eIdApplicationProcess

    @Serializable
    data object EIdIntroScreen : Destination, DestinationGroup.EIdApplicationProcess

    @Serializable
    data object EIdAttestationScreen : Destination, DestinationGroup.EIdApplicationProcess

    @Serializable
    data object EIdDocumentSelectionScreen :
        Destination,
        ScopedComponentGroup.EidApplicationProcess,
        DestinationGroup.EIdApplicationProcess

    @Serializable
    data class EIdGuardianConsentResultScreen(
        val rawDeadline: String?,
        val screenState: GuardianConsentResultState,
    ) : Destination, DestinationGroup.EIdApplicationProcess

    @Serializable
    data class EIdGuardianConsentScreen(val caseId: String) : Destination, DestinationGroup.EIdApplicationProcess

    @Serializable
    data class EIdGuardianSelectionScreen(val caseId: String) : Destination, DestinationGroup.EIdApplicationProcess

    @Serializable
    data object EIdGuardianshipScreen : Destination, ScopedComponentGroup.EidApplicationProcess, DestinationGroup.EIdApplicationProcess

    @Serializable
    data class EIdGuardianVerificationScreen(val caseId: String) : Destination, DestinationGroup.EIdApplicationProcess

    @Serializable
    data object EIdPrivacyPolicyScreen : Destination, DestinationGroup.EIdApplicationProcess

    @Serializable
    data class EIdProcessDataScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.EidOnlineSession,
        DestinationGroup.EIdRequestVerification

    @Serializable
    data class EIdQueueScreen(val rawDeadline: String?) : Destination, DestinationGroup.EIdApplicationProcess

    @Serializable
    data object EIdReadyForAvScreen : Destination, DestinationGroup.EIdRequestVerification

    @Serializable
    data class EIdStartAvSessionScreen(val caseId: String) : Destination, DestinationGroup.EIdRequestVerification

    @Serializable
    data class EIdStartSelfieVideoScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.EidOnlineSession,
        DestinationGroup.EIdRequestVerification

    @Serializable
    data object EIdProcessDataConfirmationScreen :
        Destination,
        DestinationGroup.EIdRequestVerification

    // endregion feature/eIdApplicationProcess
    // region feature/eIDRequestVerification

    @Serializable
    data class EIdDocumentRecordingScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.EidOnlineSession,
        DestinationGroup.EIdRequestVerification

    @Serializable
    data class EIdDocumentScannerScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.EidApplicationProcess,
        ScopedComponentGroup.EidDocumentScan,
        ScopedComponentGroup.EidOnlineSession,
        DestinationGroup.EIdApplicationProcess,
        DestinationGroup.EIdRequestVerification

    @Serializable
    data class EIdDocumentScanSummaryScreen(
        val caseId: String,
    ) :
        Destination,
        ScopedComponentGroup.EidApplicationProcess,
        ScopedComponentGroup.EidDocumentScan,
        ScopedComponentGroup.EidOnlineSession,
        DestinationGroup.EIdApplicationProcess,
        DestinationGroup.EIdRequestVerification

    @Serializable
    data class EIdDocumentScannerInfoScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.EidOnlineSession,
        DestinationGroup.EIdApplicationProcess,
        DestinationGroup.EIdRequestVerification

    @Serializable
    data class EIdDocumentRecordingInfoScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.EidOnlineSession,
        DestinationGroup.EIdRequestVerification

    @Serializable
    data class EIdFaceScannerScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.EidOnlineSession,
        DestinationGroup.EIdRequestVerification

    @Serializable
    data class EIdNfcScannerScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.EidOnlineSession,
        DestinationGroup.EIdRequestVerification

    @Suppress("ArrayInDataClass")
    @Serializable
    data class EIdNfcSummaryScreen(
        val caseId: String,
        val picture: ByteArray,
        val givenName: String,
        val surname: String,
        val documentId: String,
        val expiryDate: String,
    ) : Destination, ScopedComponentGroup.EidOnlineSession, DestinationGroup.EIdRequestVerification

    @Serializable
    data class EIdStartAutoVerificationScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.EidOnlineSession,
        DestinationGroup.EIdRequestVerification

    @Serializable
    data object MrzChooserScreen :
        Destination,
        ScopedComponentGroup.EidApplicationProcess,
        ScopedComponentGroup.EidDocumentScan,
        DestinationGroup.EIdApplicationProcess

    @Serializable
    data class MrzScanPermissionScreen(val caseId: String) :
        Destination,
        ScopedComponentGroup.EidOnlineSession,
        DestinationGroup.EIdApplicationProcess,
        DestinationGroup.EIdRequestVerification

    @Serializable
    data class MrzSubmissionScreen(val mrzLines: List<String>) :
        Destination,
        ScopedComponentGroup.EidApplicationProcess,
        ScopedComponentGroup.EidDocumentScan,
        ScopedComponentGroup.EidOnlineSession,
        DestinationGroup.EIdApplicationProcess,
        DestinationGroup.EIdRequestVerification

    // endregion feature/eIDRequestVerification
    // region feature/walletPairing

    @Serializable
    data class EIdPairingOverviewScreen(val caseId: String) : Destination, DestinationGroup.EIdRequestVerification

    @Serializable
    data class EIdWalletPairingQrCodeScreen(val caseId: String) : Destination, DestinationGroup.EIdRequestVerification

    @Serializable
    data class EIdWalletPairingScreen(val caseId: String) : Destination, DestinationGroup.EIdRequestVerification

    @Serializable
    data object EIdWalletPairingTimeoutScreen : Destination, DestinationGroup.EIdRequestVerification

    // endregion feature/walletPairing
    // region feature/qrscan

    @Serializable
    data class QrScannerScreen(val firstCredentialWasAdded: Boolean) : Destination, ScopedComponentGroup.CredentialIssuer

    @Serializable
    data object QrScanPermissionScreen : Destination

    // endregion feature/qrscan
    // region feature/credentialOffer

    @Serializable
    data class CredentialOfferScreen(
        val credentialId: Long,
    ) : Destination, ScopedComponentGroup.CredentialIssuer, DestinationGroup.CredentialOffer

    @Serializable
    data class DeclineCredentialOfferScreen(
        val credentialId: Long,
    ) : Destination, ScopedComponentGroup.CredentialIssuer, DestinationGroup.CredentialOffer

    // endregion feature/credentialOffer
    // region feature/presentationRequest

    @Serializable
    data class PresentationCredentialListScreen(
        val compatibleCredentials: Set<CompatibleCredential>,
        val presentationRequestWithRaw: PresentationRequestWithRaw,
    ) : Destination, ScopedComponentGroup.Verifier, DestinationGroup.EIdApplicationProcess

    @Serializable
    data object PresentationDeclinedScreen : Destination, ScopedComponentGroup.Verifier, DestinationGroup.EIdApplicationProcess

    @Serializable
    data class PresentationFailureScreen(
        val compatibleCredential: CompatibleCredential,
        val presentationRequestWithRaw: PresentationRequestWithRaw,
    ) : Destination, ScopedComponentGroup.Verifier, DestinationGroup.EIdApplicationProcess

    @Serializable
    data class PresentationInvalidCredentialErrorScreen(val sentFields: List<String>) :
        Destination,
        ScopedComponentGroup.Verifier,
        DestinationGroup.EIdApplicationProcess

    @Serializable
    data class PresentationRequestScreen(
        val compatibleCredential: CompatibleCredential,
        val presentationRequestWithRaw: PresentationRequestWithRaw,
    ) : Destination, ScopedComponentGroup.Verifier, DestinationGroup.EIdApplicationProcess

    @Serializable
    data class PresentationSuccessScreen(val sentFields: List<String>) :
        Destination,
        ScopedComponentGroup.Verifier,
        DestinationGroup.EIdApplicationProcess

    @Serializable
    data object PresentationValidationErrorScreen :
        Destination,
        ScopedComponentGroup.Verifier,
        DestinationGroup.EIdApplicationProcess

    @Serializable
    data object PresentationVerificationErrorScreen :
        Destination,
        ScopedComponentGroup.Verifier,
        DestinationGroup.EIdApplicationProcess

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
    data class NonComplianceFormScreen(
        val activityId: Long,
        val titleId: Int?,
        val reportReason: NonComplianceReportReason
    ) : Destination, ScopedComponentGroup.NonComplianceFormInput

    @Serializable
    data class NonComplianceInfoScreen(
        val activityId: Long,
        val reportReason: NonComplianceReportReason
    ) : Destination

    @Serializable
    data class NonComplianceListScreen(
        val activityId: Long,
        val activityType: ActivityType
    ) : Destination

    // endregion platform/nonCompliance
    // region platform/invitation

    @Serializable
    data class InvitationFailureScreen(val invitationError: InvitationErrorScreenState, val uri: String?) : Destination

    // endregion platform/invitation
    // region platform/screens
    @Serializable
    data class GenericErrorScreen(val error: GenericErrorScreenState) : Destination

    // endregion platform/screens
    // region platform/versionEnforcement

    @Serializable
    data class AppVersionBlockedScreen(val title: String?, val text: String?) : Destination, NoAutoLogout

    // endregion platform/versionEnforcement
    // region platform/reportWrongData

    @Serializable
    data object ReportWrongDataScreen : Destination

    // endregion platform/reportWrongData
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

fun Destination?.canTriggerAutoLogout(): Boolean {
    return when (this) {
        is NoAutoLogout -> false
        else -> true
    }
}
