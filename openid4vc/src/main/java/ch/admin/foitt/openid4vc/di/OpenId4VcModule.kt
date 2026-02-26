package ch.admin.foitt.openid4vc.di

import ch.admin.foitt.openid4vc.data.CredentialOfferRepositoryImpl
import ch.admin.foitt.openid4vc.data.FetchDidLogRepositoryImpl
import ch.admin.foitt.openid4vc.data.PresentationRequestRepositoryImpl
import ch.admin.foitt.openid4vc.data.TypeMetadataRepositoryImpl
import ch.admin.foitt.openid4vc.data.VcSchemaRepositoryImpl
import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_DEFAULT_ENGINE
import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_GZIP_ENGINE
import ch.admin.foitt.openid4vc.di.ExternalOpenId4VcModule.Companion.NAMED_GZIP_HTTP_CLIENT
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.repository.FetchDidLogRepository
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.domain.repository.TypeMetadataRepository
import ch.admin.foitt.openid4vc.domain.repository.VcSchemaRepository
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyDescriptorMaps
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyVerifiablePresentation
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequest
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequestProofsJwt
import ch.admin.foitt.openid4vc.domain.usecase.CreateJwk
import ch.admin.foitt.openid4vc.domain.usecase.DeclinePresentation
import ch.admin.foitt.openid4vc.domain.usecase.DeleteKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchIssuerConfiguration
import ch.admin.foitt.openid4vc.domain.usecase.FetchPresentationRequest
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.FetchVerifiableCredential
import ch.admin.foitt.openid4vc.domain.usecase.GetHardwareKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.GetPresentationRequestType
import ch.admin.foitt.openid4vc.domain.usecase.GetSoftwareKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.usecase.ResolveDid
import ch.admin.foitt.openid4vc.domain.usecase.SubmitAnyCredentialPresentation
import ch.admin.foitt.openid4vc.domain.usecase.ValidateIssuerMetadataJwt
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.implementation.CreateAnyDescriptorMapsImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.CreateAnyVerifiablePresentationImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.CreateCredentialRequestImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.CreateCredentialRequestProofsJwtImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.CreateJwkImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.DeclinePresentationImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.DeleteKeyPairImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.FetchCredentialByConfigImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.FetchIssuerConfigurationImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.FetchPresentationRequestImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.FetchRawAndParsedIssuerCredentialInfoImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.FetchVerifiableCredentialImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.GetHardwareKeyPairImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.GetPresentationRequestTypeImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.GetSoftwareKeyPairImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.GetVerifiableCredentialParamsImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.ResolveDidImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.SubmitAnyCredentialPresentationImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.ValidateIssuerMetadataJwtImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.VerifyJwtSignatureImpl
import ch.admin.foitt.openid4vc.domain.usecase.jwe.CreateJWE
import ch.admin.foitt.openid4vc.domain.usecase.jwe.DecryptJWE
import ch.admin.foitt.openid4vc.domain.usecase.jwe.implementation.CreateJWEImpl
import ch.admin.foitt.openid4vc.domain.usecase.jwe.implementation.DecryptJWEImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.CreateVcSdJwtDescriptorMap
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.CreateVcSdJwtVerifiablePresentation
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchTypeMetadata
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSchema
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyPublicKey
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyVcSdJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.CreateVcSdJwtDescriptorMapImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.CreateVcSdJwtVerifiablePresentationImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.FetchTypeMetadataImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.FetchVcSchemaImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.FetchVcSdJwtCredentialImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.VerifyPublicKeyImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.VerifyVcSdJwtSignatureImpl
import ch.admin.foitt.openid4vc.utils.SafeJson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.time.Clock
import javax.inject.Named

@Module(includes = [OpenId4VcModule::class])
@InstallIn(ActivityRetainedComponent::class)
class ExternalOpenId4VcModule {
    @Provides
    @ActivityRetainedScoped
    internal fun provideCredentialOfferRepository(
        @Named(NAMED_DEFAULT_HTTP_CLIENT) httpClient: HttpClient,
        safeJson: SafeJson,
        decryptJWE: DecryptJWE,
    ): CredentialOfferRepository = CredentialOfferRepositoryImpl(httpClient, safeJson, decryptJWE)

    @Provides
    internal fun providesPresentationRequestRepository(
        @Named(NAMED_DEFAULT_HTTP_CLIENT) httpClient: HttpClient,
        safeJson: SafeJson,
    ): PresentationRequestRepository = PresentationRequestRepositoryImpl(httpClient, safeJson)

    companion object {
        const val NAMED_DEFAULT_HTTP_CLIENT = "defaultHttpClient"
        const val NAMED_DEFAULT_ENGINE = "defaultEngine"
        const val NAMED_GZIP_HTTP_CLIENT = "gzipHttpClient"
        const val NAMED_GZIP_ENGINE = "gzipEngine"
    }
}

@Module(includes = [OpenId4VCBindings::class])
@InstallIn(ActivityRetainedComponent::class)
interface ExternalOpenId4VcBindings {
    @Binds
    fun bindVerifyJwtSignature(
        useCase: VerifyJwtSignatureImpl
    ): VerifyJwtSignature

    @Binds
    fun bindVerifySdJwtCredentialSignature(
        useCase: VerifyVcSdJwtSignatureImpl
    ): VerifyVcSdJwtSignature
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal class OpenId4VcModule {

    @ActivityRetainedScoped
    @Provides
    @Named(NAMED_DEFAULT_HTTP_CLIENT)
    fun provideDefaultHttpClient(@Named(NAMED_DEFAULT_ENGINE) engine: HttpClientEngine, jsonSerializer: Json): HttpClient {
        return HttpClient(engine) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(jsonSerializer)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.d(message)
                    }
                }
                level = LogLevel.INFO
            }
        }
    }

    @ActivityRetainedScoped
    @Provides
    @Named(NAMED_GZIP_HTTP_CLIENT)
    fun provideGzipHttpClient(@Named(NAMED_GZIP_ENGINE) engine: HttpClientEngine, jsonSerializer: Json): HttpClient {
        return HttpClient(engine) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(jsonSerializer)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
            }
            install(ContentEncoding) {
                gzip()
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.d(message)
                    }
                }
                level = LogLevel.INFO
            }
        }
    }

    @Provides
    @Named(NAMED_DEFAULT_ENGINE)
    fun provideHttpClientEngine(): HttpClientEngine = OkHttp.create()

    @Provides
    @Named(NAMED_GZIP_ENGINE)
    fun provideGzipHttpClientEngine(): HttpClientEngine = OkHttp.create()

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    fun provideJsonSerializer(): Json {
        return Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
            classDiscriminatorMode = ClassDiscriminatorMode.NONE
        }
    }

    @Provides
    fun provideSafeJson(json: Json) = SafeJson(json)

    @ActivityRetainedScoped
    @Provides
    fun provideClock(): Clock = Clock.systemUTC()

    private companion object {
        const val SOCKET_TIMEOUT_MILLIS = 30 * 1000L
        const val REQUEST_TIMEOUT_MILLIS = 60 * 1000L
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface OpenId4VCBindings {
    @Binds
    @ActivityRetainedScoped
    fun bindFetchDidLogRepository(
        repository: FetchDidLogRepositoryImpl
    ): FetchDidLogRepository

    @Binds
    fun bindRawAndParsedFetchIssuerCredentialInformation(
        useCase: FetchRawAndParsedIssuerCredentialInfoImpl
    ): FetchRawAndParsedIssuerCredentialInfo

    @Binds
    fun bindValidateIssuerMetadataJwt(
        useCase: ValidateIssuerMetadataJwtImpl
    ): ValidateIssuerMetadataJwt

    @Binds
    fun bindFetchIssuerConfiguration(
        useCase: FetchIssuerConfigurationImpl
    ): FetchIssuerConfiguration

    @Binds
    fun bindFetchVerifiableCredential(
        useCase: FetchVerifiableCredentialImpl
    ): FetchVerifiableCredential

    @Binds
    fun bindGetVerifiableCredentialParams(
        useCase: GetVerifiableCredentialParamsImpl
    ): GetVerifiableCredentialParams

    @Binds
    fun bindCreateCredentialRequestProofsJwt(
        useCase: CreateCredentialRequestProofsJwtImpl
    ): CreateCredentialRequestProofsJwt

    @Binds
    fun bindFetchPresentationRequest(
        useCase: FetchPresentationRequestImpl
    ): FetchPresentationRequest

    @Binds
    fun bindSubmitAnyCredentialPresentation(
        useCase: SubmitAnyCredentialPresentationImpl
    ): SubmitAnyCredentialPresentation

    @Binds
    fun bindCreateAnyVerifiablePresentation(
        useCase: CreateAnyVerifiablePresentationImpl
    ): CreateAnyVerifiablePresentation

    @Binds
    fun bindCreateVcSdJwtVerifiablePresentation(
        useCase: CreateVcSdJwtVerifiablePresentationImpl
    ): CreateVcSdJwtVerifiablePresentation

    @Binds
    fun bindCreateAnyDescriptorMaps(
        useCase: CreateAnyDescriptorMapsImpl
    ): CreateAnyDescriptorMaps

    @Binds
    fun bindCreateVcSdJwtDescriptorMap(
        useCase: CreateVcSdJwtDescriptorMapImpl
    ): CreateVcSdJwtDescriptorMap

    @Binds
    fun bindDeclinePresentation(
        useCase: DeclinePresentationImpl
    ): DeclinePresentation

    @Binds
    fun bindGetHardwareKeyPair(
        useCase: GetHardwareKeyPairImpl
    ): GetHardwareKeyPair

    @Binds
    fun bindGetSoftwareKeyPair(
        useCase: GetSoftwareKeyPairImpl
    ): GetSoftwareKeyPair

    @Binds
    fun bindDeleteKeyPair(
        useCase: DeleteKeyPairImpl
    ): DeleteKeyPair

    @Binds
    fun bindCreateDidJwk(
        useCase: CreateJwkImpl
    ): CreateJwk

    @Binds
    fun bindFetchCredentialByConfig(
        useCase: FetchCredentialByConfigImpl
    ): FetchCredentialByConfig

    @Binds
    fun bindFetchVcSdJwtCredential(
        useCase: FetchVcSdJwtCredentialImpl
    ): FetchVcSdJwtCredential

    @Binds
    fun bindVerifyPublicKey(
        verifier: VerifyPublicKeyImpl
    ): VerifyPublicKey

    @Binds
    fun bindResolveDid(
        resolver: ResolveDidImpl
    ): ResolveDid

    @Binds
    @ActivityRetainedScoped
    fun bindTypeMetadataRepository(
        repo: TypeMetadataRepositoryImpl
    ): TypeMetadataRepository

    @Binds
    fun bindFetchTypeMetadataByFormat(
        useCase: FetchTypeMetadataImpl
    ): FetchTypeMetadata

    @Binds
    @ActivityRetainedScoped
    fun bindVcSchemaRepository(
        repo: VcSchemaRepositoryImpl
    ): VcSchemaRepository

    @Binds
    fun bindFetchVcSchema(
        useCase: FetchVcSchemaImpl
    ): FetchVcSchema

    @Binds
    fun bindDecryptJWE(
        useCase: DecryptJWEImpl
    ): DecryptJWE

    @Binds
    fun bindCreateJWE(
        useCase: CreateJWEImpl
    ): CreateJWE

    @Binds
    fun bindCreateCredentialRequest(
        useCase: CreateCredentialRequestImpl
    ): CreateCredentialRequest

    @Binds
    fun bindGetPresentationRequestType(
        useCase: GetPresentationRequestTypeImpl
    ): GetPresentationRequestType
}
