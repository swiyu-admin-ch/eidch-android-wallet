package ch.admin.foitt.wallet.platform.navigation

import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import dagger.BindsInstance
import dagger.hilt.DefineComponent
import dagger.hilt.android.components.ActivityRetainedComponent
import javax.inject.Scope

@Scope
@Retention(value = AnnotationRetention.RUNTIME)
annotation class DestinationsScoped

@DestinationsScoped
@DefineComponent(parent = ActivityRetainedComponent::class)
interface DestinationScopedComponent

@DefineComponent.Builder
interface DestinationsComponentBuilder {
    fun setScope(@BindsInstance componentScope: ComponentScope): DestinationsComponentBuilder
    fun build(): DestinationScopedComponent
}

interface DestinationScopedComponentManager {
    fun <T> getEntryPoint(entryPointClass: Class<T>, componentScope: ComponentScope): T

    fun isInScope(componentScope: ComponentScope): Boolean
}
