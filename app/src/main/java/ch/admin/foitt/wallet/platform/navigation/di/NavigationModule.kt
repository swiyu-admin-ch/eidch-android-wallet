package ch.admin.foitt.wallet.platform.navigation.di

import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.implementation.DestinationScopedComponentManagerImpl
import ch.admin.foitt.wallet.platform.navigation.implementation.NavigationManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface NavigationModule {
    @Binds
    @ActivityRetainedScoped
    fun provideNavigation3Manager(
        manager: NavigationManagerImpl,
    ): NavigationManager

    @Binds
    @ActivityRetainedScoped
    fun provideDestinationScopedComponentManager(
        manager: DestinationScopedComponentManagerImpl,
    ): DestinationScopedComponentManager
}
