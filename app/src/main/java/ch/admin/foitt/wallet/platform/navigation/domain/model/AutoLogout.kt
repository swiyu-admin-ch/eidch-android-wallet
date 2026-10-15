package ch.admin.foitt.wallet.platform.navigation.domain.model

interface AutoLogout {
    /**
     * If true auto logout can be triggered on this Destination.
     * Defaults to true
     * Override in a Destination to change default value
     */
    fun canTriggerAutoLogout(): Boolean = true
}
