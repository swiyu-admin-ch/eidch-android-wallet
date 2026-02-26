package ch.admin.foitt.wallet.openid4vc

import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcBindings
import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyVcSdJwtSignature
import ch.admin.foitt.wallet.platform.utils.SafeJson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [ActivityRetainedComponent::class],
    replaces = [ExternalOpenId4VcModule::class]
)
class FakeOpenId4VCModule {
    @Provides
    fun provideCredentialOfferRepository(
        safeJson: SafeJson,
    ): CredentialOfferRepository = FakeCredentialOfferRepositoryImpl(safeJson)

    @Provides
    fun providePresentationRequestRepository(
    ): PresentationRequestRepository = FakePresentationRequestRepositoryImpl()
}

@Module
@TestInstallIn(
    components = [ActivityRetainedComponent::class],
    replaces = [ExternalOpenId4VcBindings::class]
)
interface FakeOpenId4VcBindings {
    @Binds
    fun bindVerifyJwtSignature(
        useCase: FakeVerifyJwtSignatureImpl
    ): VerifyJwtSignature

    @Binds
    fun bindVerifyVcSdJwtSignature(
        useCase: FakeVerifyVcSdJwtSignatureImpl
    ): VerifyVcSdJwtSignature
}
