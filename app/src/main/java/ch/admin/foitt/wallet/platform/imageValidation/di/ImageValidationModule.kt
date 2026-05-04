package ch.admin.foitt.wallet.platform.imageValidation.di

import ch.admin.foitt.wallet.platform.imageValidation.domain.usecase.ValidateImage
import ch.admin.foitt.wallet.platform.imageValidation.domain.usecase.implementation.ValidateImageImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
class ImageValidationModule

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface ImageValidationBindingsModule {
    @Binds
    fun bindValidateImage(
        useCase: ValidateImageImpl
    ): ValidateImage
}
