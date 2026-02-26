package ch.admin.foitt.wallet.platform.navigation.implementation

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.DestinationGroup
import io.ktor.util.reflect.instanceOf
import timber.log.Timber
import javax.inject.Inject
import kotlin.reflect.KClass

class NavigationManagerImpl @Inject constructor() : NavigationManager {
    override val backstack: SnapshotStateList<Destination> = mutableStateListOf(Destination.StartScreen)
    override val backstackFlow = snapshotFlow { backstack.toList() }
    override val currentDestination: Destination
        get() = backstack.last()

    private val rootDestination: Destination = Destination.HomeScreen

    /**
     * Pushes a new destination onto the top of the back‑stack.
     *
     * - `[A, B] -> navigateTo(C) -> [A, B, C]`
     *
     * @param destination The destination to push.
     */
    override fun navigateTo(destination: Destination) {
        // ["A","B"] -> navigateTo(C) -> ["A","B","C"]
        backstack.add(destination)
        printBackStack("navigateTo($destination). Added to backstack: $destination")
    }

    /**
     * Pops the top entry from the back stack.
     *
     * - `[A, B, C] -> popBackStack() -> [A, B]`
     */
    override fun popBackStack() {
        backstack.removeLastOrNull()?.also {
            printBackStack("popBackstack()")
        } ?: printBackStack("popBackstack(): backstack was empty, did not pop anything")
    }

    /**
     * Pops back to the specified destination.
     *
     * - `[A, B(1), B(2), C] -> popBackstackTo(B, false) -> [A, B(1), B(2)]`
     * - `[A, B(1), B(2), C] -> popBackstackTo(B, true) -> [A, B(1)]`
     * - `[A, B, C] -> popBackstackTo(D, true) -> [A, B, C]`
     *
     * @param destination Remove all destinations back to a destination implementing this class.
     * @param inclusive If true, [destination] itself is popped as well.
     */
    override fun <T : Destination> popBackStackTo(destination: KClass<out T>, inclusive: Boolean): Boolean {
        Timber.d("Start popBackstackTo()")
        val idx = backstack.indexOfLast { it::class == destination }
        if (idx == -1) {
            return false
        }

        val offset = if (inclusive) 0 else 1
        backstack.removeRange(idx + offset, backstack.size)
        return true
    }

    // ALL OTHER HELPERS ARE JUST COMBINATIONS OF navigateTo, popBackstack and popBackstackTo

    /**
     * Replace current destination with new destination
     * - `[A, B, C] -> replaceCurrentWith(D) -> [A, B , E]`
     *
     * @param destination new [Destination]
     */
    override fun replaceCurrentWith(destination: Destination) {
        if (backstack.isNotEmpty()) {
            popUpToAndNavigate(backstack.last()::class, destination)
            printBackStack("replaceCurrentWith($destination)")
        } else {
            navigateTo(destination)
        }
    }

    /**
     * Pop the backstack if possible, replace with root otherwise
     *
     * - ```[A, B, C] -> popBackStackOrToRoot() -> [A, B]```
     * - ```[A] -> popBackStackOrToRoot() -> ["ROOT"]```
     */
    override fun popBackStackOrToRoot() {
        if (backstack.size > 1) {
            popBackStack()
        } else {
            replaceCurrentWith(rootDestination)
        }
        printBackStack("popBackStackOrToRoot()")
    }

    /**
     * Pop the backstack up to and including a certain destination, then navigate to the new destination.
     *
     * - `[A, B, C] -> popUpToAndNavigate(popUpToInclusive: B, destination: D) -> [A, D]`
     * - `[A, B, C] -> popUpToAndNavigate(popUpToInclusive: X, destination: D) -> [A, B, C, D]`
     *
     * @param destination new [Destination]
     * @param popToInclusive remove all destinations from the backstack until and including this destination
     */
    override fun <T : Destination> popUpToAndNavigate(popToInclusive: KClass<out T>, destination: Destination) {
        popBackStackTo(destination = popToInclusive, inclusive = true)
        navigateTo(destination)
        printBackStack("navigateToAndPopUpTo(destination=$destination, popToInclusive=$popToInclusive")
    }

    /**
     * Pop backstack to [Destination.HomeScreen] if it's in the backstack.
     * Otherwise [popUpAndNavigate] to [popUntil] before navigating to [Destination.HomeScreen]
     *
     * @param popUntil The destination type to pop up to before navigating to the home screen.
     */
    override fun <T : Destination> navigateBackToHomeScreen(popUntil: KClass<out T>) {
        if (backstack.any { it is Destination.HomeScreen }) {
            popBackStackTo(Destination.HomeScreen::class, false)
            printBackStack("navigateBackToHome(popUntil=$popUntil)")
        } else {
            popUpToAndNavigate(popUntil, Destination.HomeScreen)
            printBackStack("navigateBackToHome(popUntil=$popUntil)")
        }
    }

    /**
     * Navigates back to last screen in backstack not in [destinationGroup]
     *
     * - `[A:X, B, C:X, D:X] -> navigateOutOf(X::class) -> [A:X, B]`
     *
     * @param destinationGroup KClass of [DestinationGroup] to navigate out
     */
    override fun <T : DestinationGroup> navigateOutOf(destinationGroup: KClass<T>) {
        if (destinationGroup.qualifiedName?.endsWith("Screen") == true) {
            error("$destinationGroup must be a scope, not a screen")
        }

        val remove = backstack.takeLastWhile { it.instanceOf(destinationGroup) }
        backstack.removeAll(remove)
    }

    /**
     * Navigates back to last screen in backstack not in [destinationGroup] and add new destination
     *
     * - `[A:X, B, C:X, D:X] -> navigateOutAndTo(X::class, E) -> [A:X, B, E]`
     * - `[A:X, B, E, C:X, D:X] -> navigateOutAndTo(X::class, E) -> [A:X, B, E]`
     *
     * @param destinationGroup KClass of [DestinationGroup] to navigate out
     * @param destination new [Destination]
     */
    override fun <T : DestinationGroup> navigateOutAndTo(destinationGroup: KClass<T>, destination: Destination) {
        navigateOutOf(destinationGroup)
        val isDestinationAlreadyOnTop = backstack.lastOrNull()?.let { backstack.last()::class == destination::class } ?: false
        if (!isDestinationAlreadyOnTop) {
            navigateTo(destination)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun printBackStack(action: String) {
        Timber.d(
            "NavManager::$action.\nCurrent backstack: ${backstack.joinToString(
                " -> "
            ) { it::class.qualifiedName?.split(".")?.last() ?: "Unknown" }}"
        )
    }
}
