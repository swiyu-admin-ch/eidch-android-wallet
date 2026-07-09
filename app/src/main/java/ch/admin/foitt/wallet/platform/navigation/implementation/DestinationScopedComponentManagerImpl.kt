package ch.admin.foitt.wallet.platform.navigation.implementation

import ch.admin.foitt.wallet.platform.di.IoDispatcherScope
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponent
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.DestinationsComponentBuilder
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.contains
import ch.admin.foitt.wallet.platform.scanning.di.AvBeamSdkEntryPoint
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
            val msg = "current destination ${currentDestination::class.qualifiedName} is not in scope $componentScope"
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

    private suspend fun updateComponents(backStack: List<Destination>) {
        Timber.d("Components update triggered")
        val iterator = scopedComponents.iterator()
        while (iterator.hasNext()) {
            val (scope, component) = iterator.next()
            // Only keep component if their scope is in the backstack
            if (backStack.none { scope.contains(it) }) {
                if (scope == ComponentScope.AvBeamSdkSession) {
                    EntryPoints.get(component, AvBeamSdkEntryPoint::class.java)
                        .avBeamRepository()
                        .release()
                }

                iterator.remove()
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
