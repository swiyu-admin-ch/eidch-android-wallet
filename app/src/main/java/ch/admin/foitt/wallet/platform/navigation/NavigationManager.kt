package ch.admin.foitt.wallet.platform.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.DestinationGroup
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

interface NavigationManager {

    val backstack: SnapshotStateList<Destination>
    val backstackFlow: Flow<List<Destination>>

    val currentDestination: Destination

    fun replaceCurrentWith(
        destination: Destination,
    )

    fun popBackStack()

    fun <T : Destination> popBackStackTo(
        destination: KClass<out T>,
        inclusive: Boolean
    ): Boolean

    fun <T : Destination> navigateBackToHomeScreen(popUntil: KClass<out T>)
    fun navigateTo(destination: Destination)

    fun <T : Destination> popUpToAndNavigate(popToInclusive: KClass<out T>, destination: Destination)

    fun popBackStackOrToRoot()

    fun <T : DestinationGroup> navigateOutOf(destinationGroup: KClass<T>)
    fun <T : DestinationGroup> navigateOutAndTo(destinationGroup: KClass<T>, destination: Destination)
}
