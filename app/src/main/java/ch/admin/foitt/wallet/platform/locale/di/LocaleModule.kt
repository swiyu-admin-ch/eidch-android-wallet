package ch.admin.foitt.wallet.platform.locale.di

import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedAndThemedDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedCredentialInformationDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetSupportedAppLocales
import ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation.GetCurrentAppLocaleImpl
import ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation.GetLocalizedAndThemedDisplayImpl
import ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation.GetLocalizedCredentialInformationDisplayImpl
import ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation.GetLocalizedDisplayImpl
import ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation.GetSupportedAppLocalesImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface LocaleModule {
    @Binds
    fun bindGetCurrentAppLocale(
        useCase: GetCurrentAppLocaleImpl
    ): GetCurrentAppLocale

    @Binds
    fun bindGetSupportedLocales(
        useCase: GetSupportedAppLocalesImpl
    ): GetSupportedAppLocales

    @Binds
    fun bindGetLocalizedDisplay(
        useCase: GetLocalizedDisplayImpl
    ): GetLocalizedDisplay

    @Binds
    fun bindGetLocalizedAndThemedDisplay(
        useCase: GetLocalizedAndThemedDisplayImpl
    ): GetLocalizedAndThemedDisplay

    @Binds
    fun bindGetLocalizedCredentialInformationDisplay(
        useCase: GetLocalizedCredentialInformationDisplayImpl
    ): GetLocalizedCredentialInformationDisplay
}
