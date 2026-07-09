package ch.admin.foitt.didResolver.di

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.didResolver.domain.implementation.DidResolverHelperImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface DidResolverModule {
    @Binds
    fun bindDidResolverHelper(
        resolver: DidResolverHelperImpl
    ): DidResolverHelper
}
