package ch.admin.foitt.wallet.platform.navigation.implementation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.DestinationGroup
import timber.log.Timber
import javax.inject.Inject
import kotlin.reflect.KClass

class NavigationManagerImpl @Inject constructor() : NavigationManager {
    override val backstack: SnapshotStateList<Destination> = mutableStateListOf(Destination.StartScreen)
    override val backstackFlow = snapshotFlow { backstack.toList() }
    override val currentDestination: Destination
        get() = backstack.lastOrNull() ?: rootDestination

    private val rootDestination: Destination = Destination.HomeScreen

    /**
     * Pushes a new destination onto the top of the back stack.
     *
     * `[A, B] → navigateTo(C) → [A, B, C]`
     */
    override fun navigateTo(destination: Destination) {
        backstack.add(destination)
        logBackStack("navigateTo($destination)")
    }

    /**
     * Pops the top entry from the back stack.
     * Returns `true` if an entry was popped, `false` if the stack was empty.
     *
     * `[A, B, C] → popBackStack() → [A, B]`
     */
    override fun popBackStack(): Boolean {
        val popped = backstack.removeLastOrNull() != null
        if (!popped) {
            logBackStack("popBackStack(): backstack was empty")
        } else {
            logBackStack("popBackStack()")
        }
        return popped
    }

    /**
     * Pops back to the specified destination type.
     *
     * - `[A, B(1), B(2), C] → popBackStackTo(B, false) → [A, B(1), B(2)]`
     * - `[A, B(1), B(2), C] → popBackStackTo(B, true)  → [A, B(1)]`
     * - `[A, B, C] → popBackStackTo(D, true) → [A, B, C]`  (not found → no-op)
     *
     * @param destination The destination **type** to pop back to.
     * @param inclusive If `true`, [destination] itself is also popped.
     * @return `true` if the destination was found and the stack was modified.
     */
    override fun <T : Destination> popBackStackTo(
        destination: KClass<out T>,
        inclusive: Boolean,
    ): Boolean = Snapshot.withMutableSnapshot {
        val idx = backstack.indexOfLast { it::class == destination }
        if (idx == -1) {
            logBackStack("popBackStackTo(${destination.java.simpleName}, inclusive=$inclusive): destination not in back stack")
            return@withMutableSnapshot false
        }

        val cutIndex = if (inclusive) idx else idx + 1
        if (cutIndex < backstack.size) {
            backstack.removeRange(cutIndex, backstack.size)
            logBackStack("popBackStackTo(${destination.java.simpleName}, inclusive=$inclusive)")
            true
        } else {
            logBackStack("popBackStackTo(${destination.java.simpleName}, inclusive=$inclusive): already on top, no-op")
            false
        }
    }

    /**
     * Replaces the current destination with [destination].
     *
     * `[A, B, C] → replaceCurrentWith(D) → [A, B, D]`
     */
    override fun replaceCurrentWith(destination: Destination) {
        Snapshot.withMutableSnapshot {
            backstack.removeLastOrNull()
            backstack.add(destination)
        }
        logBackStack("replaceCurrentWith($destination)")
    }

    /**
     * Pops the back stack if more than one entry exists;
     * otherwise replaces the single entry with the root destination.
     *
     * - `[A, B, C] → popBackStackOrToRoot() → [A, B]`
     * - `[A]       → popBackStackOrToRoot() → [rootDestination]`
     */
    override fun popBackStackOrToRoot() {
        if (backstack.size > 1) {
            popBackStack()
        } else if (backstack.firstOrNull() != rootDestination) {
            replaceCurrentWith(rootDestination)
        }
        // else: stack is [rootDestination] — nothing to do.
        logBackStack("popBackStackOrToRoot()")
    }

    /**
     * Pops the back stack up to and including [popToInclusive],
     * then pushes [destination]. If [popToInclusive] is not found,
     * simply pushes [destination] on top.
     *
     * - `[A, B, C] → popUpToAndNavigate(B, D) → [A, D]`
     * - `[A, B, C] → popUpToAndNavigate(X, D) → [A, B, C, D]`  (X not found)
     */
    override fun <T : Destination> popUpToAndNavigate(
        popToInclusive: KClass<out T>,
        destination: Destination,
    ) {
        Snapshot.withMutableSnapshot {
            val idx = backstack.indexOfLast { it::class == popToInclusive }
            if (idx != -1) {
                // Remove target (inclusive) and everything above it
                backstack.removeRange(idx, backstack.size)
            }

            backstack.add(destination)
            logBackStack("popUpToAndNavigate(${popToInclusive.java.simpleName}, destination=$destination)")
        }
    }

    /**
     * Navigates back to [Destination.HomeScreen].
     *
     * If `HomeScreen` is already on the stack, pops to it (non-inclusive).
     * Otherwise, pops up to [popUntil] (inclusive) and pushes `HomeScreen`.
     *
     * @param popUntil The destination type to pop up to when HomeScreen is
     *                 not already present on the stack.
     */
    override fun <T : Destination> navigateBackToHomeScreen(popUntil: KClass<out T>) {
        if (backstack.any { it is Destination.HomeScreen }) {
            popBackStackTo(Destination.HomeScreen::class, inclusive = false)
        } else {
            popUpToAndNavigate(popUntil, Destination.HomeScreen)
        }
        logBackStack("navigateBackToHomeScreen(popUntil=$popUntil)")
    }

    /**
     * Removes the trailing run of destinations belonging to [destinationGroup].
     * If this empties the stack, restores [rootDestination].
     *
     * `[A:X, B, C:X, D:X] → navigateOutOf(X::class) → [A:X, B]`
     */
    override fun navigateOutOf(destinationGroup: DestinationGroup) {
        Snapshot.withMutableSnapshot {
            val toRemove = backstack.takeLastWhile { destinationGroup in it.destinationGroups() }
            backstack.removeAll(toRemove)
            if (backstack.isEmpty()) {
                backstack.add(rootDestination)
            }
        }
        logBackStack("navigateOutOf($destinationGroup)")
    }

    /**
     * Navigates out of [destinationGroup] and pushes [destination],
     * unless [destination] is already on top after the pop.
     *
     * - `[A:X, B, C:X, D:X] → navigateOutAndTo(X, E) → [A:X, B, E]`
     * - `[A:X, B, E, C:X, D:X] → navigateOutAndTo(X, E) → [A:X, B, E]`
     */
    override fun navigateOutAndTo(
        destinationGroup: DestinationGroup,
        destination: Destination
    ) {
        Snapshot.withMutableSnapshot {
            navigateOutOf(destinationGroup)
            val alreadyOnTop = backstack.lastOrNull()?.let { it::class == destination::class } ?: false
            if (!alreadyOnTop) {
                navigateTo(destination)
            }
        }
        logBackStack("navigateOutAndTo($destinationGroup, $destination)")
    }

    private fun logBackStack(action: String) {
        Timber.d(
            "NavManager::$action.\nCurrent backstack: ${
                backstack.joinToString(" -> ") { it::class.java.simpleName ?: "Unknown" }
            }"
        )
    }
}
