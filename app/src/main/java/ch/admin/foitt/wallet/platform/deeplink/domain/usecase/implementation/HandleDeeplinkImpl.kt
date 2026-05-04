package ch.admin.foitt.wallet.platform.deeplink.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.deeplink.domain.repository.DeepLinkIntentRepository
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.HandleDeeplink
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import ch.admin.foitt.wallet.platform.invitation.domain.model.toErrorDestination
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ProcessInvitation
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.CredentialOfferEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialOfferEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination.CredentialOfferScreen
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination.PresentationCredentialListScreen
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination.PresentationRequestScreen
import ch.admin.foitt.wallet.platform.navigation.domain.model.NavigationAction
import com.github.michaelbull.result.mapBoth
import timber.log.Timber
import javax.inject.Inject

class HandleDeeplinkImpl @Inject constructor(
    private val navManager: NavigationManager,
    private val deepLinkIntentRepository: DeepLinkIntentRepository,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val processInvitation: ProcessInvitation,
    private val credentialOfferEventRepository: CredentialOfferEventRepository,
) : HandleDeeplink {

    @CheckResult
    override suspend operator fun invoke(fromOnboarding: Boolean): NavigationAction {
        val deepLink = deepLinkIntentRepository.get()

        return if (deepLink == null) {
            handleStandardNavigation(fromOnboarding)
        } else {
            handleDeepLinkNavigation(
                deepLink = deepLink,
                fromOnboarding = fromOnboarding,
            )
        }
    }

    private fun handleStandardNavigation(
        fromOnboarding: Boolean,
    ) = if (fromOnboarding) {
        if (environmentSetupRepository.eIdRequestEnabled) {
            // Temporarily disabling the navigate to EidIntroScreen after onboarding while the OTP last
            navigateTo(
                direction = Destination.HomeScreen,
                fromOnboarding = true
            )
        } else {
            navigateTo(
                direction = Destination.HomeScreen,
                fromOnboarding = true
            )
        }
    } else {
        NavigationAction {
            navManager.popBackStackOrToRoot()
        }
    }

    private suspend fun handleDeepLinkNavigation(
        deepLink: String,
        fromOnboarding: Boolean,
    ): NavigationAction {
        Timber.d("Deeplink read: $deepLink")
        deepLinkIntentRepository.reset()

        val nextDirection = processInvitation(deepLink)
            .mapBoth(
                success = { invitation ->
                    handleSuccess(invitation)
                },
                failure = { invitationError ->
                    handleFailure(invitationError)
                },
            )

        return nextDirection?.let {
            navigateTo(
                direction = nextDirection,
                fromOnboarding = fromOnboarding,
            )
        } ?: NavigationAction {}
    }

    private fun handleSuccess(invitation: ProcessInvitationResult): Destination? = when (invitation) {
        is ProcessInvitationResult.CredentialOffer -> CredentialOfferScreen(credentialId = invitation.credentialId)
        is ProcessInvitationResult.DeferredCredential -> {
            // Currently, receiving a deferred credential has a silent success
            credentialOfferEventRepository.setEvent(CredentialOfferEvent.ACCEPTED)
            null
        }

        is ProcessInvitationResult.PresentationRequest -> PresentationRequestScreen(
            compatibleCredential = invitation.credential,
            presentationRequestWithRaw = invitation.request,
        )

        is ProcessInvitationResult.PresentationRequestCredentialList -> PresentationCredentialListScreen(
            compatibleCredentials = invitation.credentials,
            presentationRequestWithRaw = invitation.request,
        )
    }

    private fun handleFailure(invitationError: ProcessInvitationError): Destination = invitationError.toErrorDestination(null)

    private fun navigateTo(direction: Destination, fromOnboarding: Boolean) = NavigationAction {
        if (fromOnboarding) {
            // registration -> pop whole onboarding
            navManager.popUpToAndNavigate(
                popToInclusive = Destination.OnboardingSuccessScreen::class,
                destination = direction
            )
        } else {
            // login -> pop login
            navManager.replaceCurrentWith(
                destination = direction,
            )
        }
    }
}
