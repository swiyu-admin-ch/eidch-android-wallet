package ch.admin.foitt.wallet.feature.sessionTimeout.domain.implementation

import ch.admin.foitt.wallet.feature.sessionTimeout.domain.SessionTimeoutNavigation
import ch.admin.foitt.wallet.platform.login.domain.usecase.NavigateToLogin
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.canTriggerAutoLogout
import timber.log.Timber
import javax.inject.Inject

class SessionTimeoutNavigationImpl @Inject constructor(
    private val navManager: NavigationManager,
    private val navigateToLogin: NavigateToLogin,
) : SessionTimeoutNavigation {
    override suspend fun invoke(): Destination? {
        return if (navManager.currentDestination.canTriggerAutoLogout()) {
            Timber.d("Session timeout from ${navManager.currentDestination} -> nav to login screen")
            navigateToLogin()
        } else {
            Timber.d("Session timeout disabled for ${navManager.currentDestination}")
            null
        }
    }
}
