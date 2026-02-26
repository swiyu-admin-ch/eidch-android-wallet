package ch.admin.foitt.wallet.feature.login.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.feature.login.domain.usecase.IsDeviceSecureLockScreenConfigured
import ch.admin.foitt.wallet.feature.login.domain.usecase.LockTrigger
import ch.admin.foitt.wallet.platform.appLifecycleRepository.domain.model.AppLifecycleState
import ch.admin.foitt.wallet.platform.appLifecycleRepository.domain.usecase.GetAppLifecycleState
import ch.admin.foitt.wallet.platform.database.domain.usecase.CloseAppDatabase
import ch.admin.foitt.wallet.platform.database.domain.usecase.IsAppDatabaseOpen
import ch.admin.foitt.wallet.platform.di.IoDispatcherScope
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.NavigationAction
import ch.admin.foitt.wallet.platform.navigation.domain.model.canTriggerAutoLogout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

internal class LockTriggerImpl @Inject constructor(
    private val navManager: NavigationManager,
    private val closeAppDatabase: CloseAppDatabase,
    private val getAppLifecycleState: GetAppLifecycleState,
    private val isAppDatabaseOpen: IsAppDatabaseOpen,
    private val isDeviceSecureLockScreenConfigured: IsDeviceSecureLockScreenConfigured,
    @param:IoDispatcherScope private val ioDispatcherScope: CoroutineScope,
) : LockTrigger {
    @OptIn(ExperimentalCoroutinesApi::class)
    @CheckResult
    override suspend fun invoke(): StateFlow<NavigationAction> =
        combine(
            getAppLifecycleState(),
            navManager.backstackFlow
        ) { appLifecycleState, backstack ->

            val currentDestination = backstack.lastOrNull() ?: return@combine NavigationAction {}
            when {
                !isDeviceSecureLockScreenConfigured() -> {
                    Timber.d("!isDeviceSecureLockScreenConfigured: ${currentDestination::class.simpleName}")
                    closeAppDatabase()
                    navigateToNoDevicePinSet()
                }

                !currentDestination.canTriggerAutoLogout() -> {
                    Timber.d("!currentDestination.canTriggerAutoLogout: ${currentDestination::class.simpleName}")
                    closeAppDatabase()
                    NavigationAction {}
                }

                appLifecycleState is AppLifecycleState.Foreground && isAppDatabaseOpen() -> {
                    Timber.d("Foreground && db is open: ${currentDestination::class.simpleName}")
                    NavigationAction {}
                }

                else -> {
                    // The app is in background, or is in foreground in an inconsistent state.
                    Timber.d(
                        "Background or inconsistent state:${currentDestination::class.simpleName}, " +
                            "$appLifecycleState, $isAppDatabaseOpen()"
                    )
                    closeAppDatabase()
                    navigateToLockScreen()
                }
            }
        }.stateIn(
            scope = ioDispatcherScope,
        )

    private fun navigateToLockScreen() = NavigationAction {
        Timber.d("LockTrigger: lock navigation triggered")
        navManager.navigateTo(
            Destination.LockScreen
        )
    }

    private fun navigateToNoDevicePinSet() = NavigationAction {
        if (navManager.currentDestination != Destination.UnsecuredDeviceScreen) {
            navManager.navigateTo(Destination.UnsecuredDeviceScreen)
        }
    }
}
