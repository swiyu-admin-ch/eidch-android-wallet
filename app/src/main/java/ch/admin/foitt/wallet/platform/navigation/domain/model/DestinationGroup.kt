package ch.admin.foitt.wallet.platform.navigation.domain.model

sealed interface DestinationGroup {
    sealed interface Onboarding : DestinationGroup
    sealed interface CredentialOffer : DestinationGroup
    sealed interface EIdApplicationProcess : DestinationGroup
    sealed interface EIdRequestVerification : DestinationGroup
}
