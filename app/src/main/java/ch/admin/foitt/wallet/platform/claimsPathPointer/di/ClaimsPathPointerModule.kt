package ch.admin.foitt.wallet.platform.claimsPathPointer.di

import ch.admin.foitt.wallet.platform.claimsPathPointer.domain.usecase.GetClaimsPathPointers
import ch.admin.foitt.wallet.platform.claimsPathPointer.domain.usecase.implementation.GetClaimsPathPointersImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface ClaimsPathPointerModule {

    @Binds
    fun bindGetClaimsPathPointers(
        useCase: GetClaimsPathPointersImpl
    ): GetClaimsPathPointers
}
