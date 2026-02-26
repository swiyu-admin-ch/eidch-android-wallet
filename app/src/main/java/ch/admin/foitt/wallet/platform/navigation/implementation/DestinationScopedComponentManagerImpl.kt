package ch.admin.foitt.wallet.platform.navigation.implementation

import ch.admin.foitt.wallet.platform.di.IoDispatcherScope
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponent
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.DestinationsComponentBuilder
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.contains
import dagger.hilt.EntryPoints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

internal class DestinationScopedComponentManagerImpl @Inject constructor(
    private val componentBuilder: DestinationsComponentBuilder,
    private val navManager: NavigationManager,
    @param:IoDispatcherScope private val ioDispatcherScope: CoroutineScope
) : DestinationScopedComponentManager {
    private val scopedComponents: MutableMap<ComponentScope, DestinationScopedComponent> = mutableMapOf()

    override fun <T> getEntryPoint(entryPointClass: Class<T>, componentScope: ComponentScope): T {
        val currentDestination = navManager.currentDestination

        // Runtime exception in case of coding error
        require(componentScope.contains(currentDestination)) {
            val msg = "current destination ${currentDestination::class.simpleName} is not in scope $componentScope"
            Timber.e(msg)
            msg
        }

        val component = scopedComponents.getOrPut(componentScope) {
            componentBuilder.setScope(componentScope).build()
        }

        val entryPoint = EntryPoints.get(component, entryPointClass)
        Timber.d(
            """
            Component Entry Point requested:
            :: EntryClass: ${entryPointClass.simpleName}
            :: Scoped components: ${scopedComponents.entries}
            """.trimIndent()
        )

        return entryPoint
    }

    init {
        ioDispatcherScope.launch {
            navManager.backstackFlow.collect { backStack ->
                updateComponents(backStack)
            }
        }
    }

    private fun updateComponents(backStack: List<Destination>) {
        Timber.d("Components update triggered")
        scopedComponents.keys.retainAll { componentScope ->
            // Only keep component if their scope is in the backstack
            backStack.any { destination ->
                componentScope.contains(destination)
            }
        }
        Timber.d(
            """
            Components updated:
            :: Backstack: ${backStack.joinToString(", ") { it::class.simpleName ?: "" }}
            :: Scoped components: ${scopedComponents.entries}
            """.trimIndent()
        )
    }
}
