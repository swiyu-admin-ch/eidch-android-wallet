package ch.admin.foitt.openid4vc.di

import ch.admin.foitt.openid4vc.data.CredentialOfferRepositoryImpl
import ch.admin.foitt.openid4vc.data.FetchDidLogRepositoryImpl
import ch.admin.foitt.openid4vc.data.PresentationRequestRepositoryImpl
import ch.admin.foitt.openid4vc.data.TypeMetadataRepositoryImpl
import ch.admin.foitt.openid4vc.data.VcSchemaRepositoryImpl
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.repository.FetchDidLogRepository
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.domain.repository.TypeMetadataRepository
import ch.admin.foitt.openid4vc.domain.repository.VcSchemaRepository
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyVerifiablePresentation
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequest
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequestProofsJwt
import ch.admin.foitt.openid4vc.domain.usecase.CreateDPoPProofJwt
import ch.admin.foitt.openid4vc.domain.usecase.CreateJwk
import ch.admin.foitt.openid4vc.domain.usecase.DeclinePresentation
import ch.admin.foitt.openid4vc.domain.usecase.DeleteKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchIssuerConfiguration
import ch.admin.foitt.openid4vc.domain.usecase.FetchPresentationRequest
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.FetchVerifiableCredential
import ch.admin.foitt.openid4vc.domain.usecase.GetAuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.usecase.GetHardwareKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.GetKeyPairForKeyBinding
import ch.admin.foitt.openid4vc.domain.usecase.GetSoftwareKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.usecase.ResolveDid
import ch.admin.foitt.openid4vc.domain.usecase.ResolvePublicKey
import ch.admin.foitt.openid4vc.domain.usecase.SubmitAnyCredentialNetworkPresentation
import ch.admin.foitt.openid4vc.domain.usecase.ValidateIssuerMetadataJwt
import ch.admin.foitt.openid4vc.domain.usecase.VerifyRequestObjectSignature
import ch.admin.foitt.openid4vc.domain.usecase.implementation.CreateAnyVerifiablePresentationImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.CreateCredentialRequestImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.CreateCredentialRequestProofsJwtImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.CreateDPoPProofJwtImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.CreateJwkImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.DeclinePresentationImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.DeleteKeyPairImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.FetchCredentialByConfigImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.FetchIssuerConfigurationImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.FetchPresentationRequestImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.FetchRawAndParsedIssuerCredentialInfoImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.FetchVerifiableCredentialImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.GetAuthorizationResponseConfigImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.GetHardwareKeyPairImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.GetKeyPairForKeyBindingImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.GetSoftwareKeyPairImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.GetVerifiableCredentialParamsImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.ResolveDidImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.ResolvePublicKeyImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.SubmitAnyCredentialNetworkPresentationImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.ValidateIssuerMetadataJwtImpl
import ch.admin.foitt.openid4vc.domain.usecase.implementation.VerifyRequestObjectSignatureImpl
import ch.admin.foitt.openid4vc.domain.usecase.jwe.CreateJWE
import ch.admin.foitt.openid4vc.domain.usecase.jwe.DecryptJWE
import ch.admin.foitt.openid4vc.domain.usecase.jwe.implementation.CreateJWEImpl
import ch.admin.foitt.openid4vc.domain.usecase.jwe.implementation.DecryptJWEImpl
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import ch.admin.foitt.openid4vc.domain.usecase.jwt.implementation.VerifyJwtSignatureFromDidImpl
import ch.admin.foitt.openid4vc.domain.usecase.jwt.implementation.VerifyJwtSignatureImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.CreateVcSdJwtVerifiablePresentation
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchTypeMetadata
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSchema
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyVcSdJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.CreateVcSdJwtVerifiablePresentationImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.FetchTypeMetadataImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.FetchVcSchemaImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.FetchVcSdJwtCredentialImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.VerifyVcSdJwtSignatureImpl
import ch.admin.foitt.openid4vc.utils.ContentLengthLimiter
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

@Module
@InstallIn(ActivityRetainedComponent::class)
class OpenId4VcModule {
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

    @ActivityRetainedScoped
    @Provides
    @Named(NAMED_DEFAULT_HTTP_CLIENT)
    internal fun provideDefaultHttpClient(@Named(NAMED_DEFAULT_ENGINE) engine: HttpClientEngine, jsonSerializer: Json): HttpClient {
        return HttpClient(engine) {
            expectSuccess = true
            install(ContentLengthLimiter) {
                limitInBytes = MAX_CONTENT_SIZE
            }
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
    internal fun provideGzipHttpClient(@Named(NAMED_GZIP_ENGINE) engine: HttpClientEngine, jsonSerializer: Json): HttpClient {
        return HttpClient(engine) {
            expectSuccess = true
            install(ContentLengthLimiter) {
                limitInBytes = MAX_CONTENT_SIZE
            }
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
    internal fun provideHttpClientEngine(): HttpClientEngine = OkHttp.create()

    @Provides
    @Named(NAMED_GZIP_ENGINE)
    internal fun provideGzipHttpClientEngine(): HttpClientEngine = OkHttp.create()

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    internal fun provideJsonSerializer(): Json {
        return Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
            classDiscriminatorMode = ClassDiscriminatorMode.NONE
        }
    }

    @Provides
    internal fun provideSafeJson(json: Json) = SafeJson(json)

    @ActivityRetainedScoped
    @Provides
    internal fun provideClock(): Clock = Clock.systemUTC()

    companion object {
        const val NAMED_DEFAULT_HTTP_CLIENT = "defaultHttpClient"
        const val NAMED_DEFAULT_ENGINE = "defaultEngine"
        const val NAMED_GZIP_HTTP_CLIENT = "gzipHttpClient"
        const val NAMED_GZIP_ENGINE = "gzipEngine"

        private const val SOCKET_TIMEOUT_MILLIS = 30 * 1000L
        private const val REQUEST_TIMEOUT_MILLIS = 60 * 1000L
        private const val MAX_CONTENT_SIZE = 150 * 1024 * 1024L // limit http responses to 150MB
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
    fun bindCreateDPoPProofJwt(
        useCase: CreateDPoPProofJwtImpl
    ): CreateDPoPProofJwt

    @Binds
    fun bindFetchPresentationRequest(
        useCase: FetchPresentationRequestImpl
    ): FetchPresentationRequest

    @Binds
    fun bindSubmitAnyCredentialPresentation(
        useCase: SubmitAnyCredentialNetworkPresentationImpl
    ): SubmitAnyCredentialNetworkPresentation

    @Binds
    fun bindCreateAnyVerifiablePresentation(
        useCase: CreateAnyVerifiablePresentationImpl
    ): CreateAnyVerifiablePresentation

    @Binds
    fun bindCreateVcSdJwtVerifiablePresentation(
        useCase: CreateVcSdJwtVerifiablePresentationImpl
    ): CreateVcSdJwtVerifiablePresentation

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
    fun bindGetKeyPairForKeyBinding(
        useCase: GetKeyPairForKeyBindingImpl
    ): GetKeyPairForKeyBinding

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
    fun bindGetAuthorizationResponseConfig(
        useCase: GetAuthorizationResponseConfigImpl
    ): GetAuthorizationResponseConfig

    @Binds
    fun bindVerifyRequestObjectSignature(
        useCase: VerifyRequestObjectSignatureImpl
    ): VerifyRequestObjectSignature

    @Binds
    fun bindResolvePublicKey(
        useCase: ResolvePublicKeyImpl
    ): ResolvePublicKey

    @Binds
    fun bindVerifyJwtSignatureFromDid(
        useCase: VerifyJwtSignatureFromDidImpl
    ): VerifyJwtSignatureFromDid

    @Binds
    fun bindVerifyJwtSignature(
        useCase: VerifyJwtSignatureImpl
    ): VerifyJwtSignature

    @Binds
    fun bindVerifySdJwtCredentialSignature(
        useCase: VerifyVcSdJwtSignatureImpl
    ): VerifyVcSdJwtSignature
}
