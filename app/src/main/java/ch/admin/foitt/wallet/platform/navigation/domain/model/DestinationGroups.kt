package ch.admin.foitt.wallet.platform.navigation.domain.model

interface DestinationGroups {
    /**
     * Set of [DestinationGroup]s a Destination belongs to.
     * Can be used to [ch.admin.foitt.wallet.platform.navigation.NavigationManager.navigateOutOf] a whole group of Destinations
     */
    fun destinationGroups(): Set<DestinationGroup> = emptySet()
}

enum class DestinationGroup {
    ONBOARDING,
    CREDENTIAL_OFFER,
    EID_APPLICATION_PROCESS,
    EID_REQUEST_VERIFICATION
}
