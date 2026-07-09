package ch.admin.foitt.wallet.platform.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DefaultDispatcherScope

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class IoDispatcherScope

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MainDispatcherScope

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MainDispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class IoDispatcher

@InstallIn(SingletonComponent::class)
@Module
object CoroutinesModule {

    @DefaultDispatcherScope
    @Singleton
    @Provides
    fun providesCoroutineScopeWithDefaultDispatcher(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @MainDispatcherScope
    @Singleton
    @Provides
    fun providesCoroutineScopeWithMainDispatcher(
        @MainDispatcher mainDispatcher: MainCoroutineDispatcher
    ): CoroutineScope {
        return CoroutineScope(SupervisorJob() + mainDispatcher)
    }

    @IoDispatcherScope
    @Singleton
    @Provides
    fun providesCoroutineScopeWithIoDispatcher(
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): CoroutineScope {
        return CoroutineScope(SupervisorJob() + ioDispatcher)
    }

    @IoDispatcher
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun provideMainDispatcher() = Dispatchers.Main
}
