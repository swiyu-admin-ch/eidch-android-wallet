package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.CredentialRequestType
import ch.admin.foitt.openid4vc.domain.model.CredentialType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialRequestEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequest
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyVcSdJwtSignature
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchExistingIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchTrustForIssuance
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.RawOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.VcMetadata
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchVcMetadataByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaBundler
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.PayloadEncryptionError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryptionType
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustCheckResult
import ch.admin.foitt.wallet.platform.utils.compress
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.Instant
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository as OidCredentialOfferRepository

class RefreshDeferredCredentialsImplTest {

    @MockK
    private lateinit var mockDeferredCredentialRepository: DeferredCredentialRepository

    @MockK
    private lateinit var mockOidCredentialOfferRepository: OidCredentialOfferRepository

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockGetPayloadEncryptionType: GetPayloadEncryptionType

    @MockK
    private lateinit var mockCreateCredentialRequest: CreateCredentialRequest

    @MockK
    private lateinit var mockVerifyVcSdJwtSignature: VerifyVcSdJwtSignature

    @MockK
    private lateinit var mockFetchVcMetadataByFormat: FetchVcMetadataByFormat

    @MockK
    private lateinit var mockFetchTrustForIssuance: FetchTrustForIssuance

    @MockK
    private lateinit var mockOcaBundler: OcaBundler

    @MockK
    private lateinit var mockGenerateAnyDisplays: GenerateAnyDisplays

    @MockK
    private lateinit var mockCredentialOfferRepository: CredentialOfferRepository

    @MockK
    private lateinit var mockFetchExistingIssuerCredentialInfo: FetchExistingIssuerCredentialInfo

    @MockK
    private lateinit var mockPayloadEncryptionType: PayloadEncryptionType

    @MockK
    private lateinit var mockCredentialRequestType: CredentialRequestType

    @MockK
    private lateinit var mockVcSdJwtCredential: VcSdJwtCredential

    @MockK
    private lateinit var mockRawAndParsedIssuerCredentialInfo: RawAndParsedIssuerCredentialInfo

    @MockK
    private lateinit var mockVcMetadata: VcMetadata

    @MockK
    private lateinit var mockOcaBundle: OcaBundle

    @MockK
    private lateinit var mockTrustCheckResult: TrustCheckResult

    @MockK
    private lateinit var mockAnyDisplays: AnyDisplays

    @MockK
    private lateinit var mockAnyIssuerDisplay: AnyIssuerDisplay

    @MockK
    private lateinit var mockAnyCredentialDisplay: AnyCredentialDisplay

    @MockK
    private lateinit var mockCluster: Cluster

    @MockK
    private lateinit var mockIssuerCredentialInfo: IssuerCredentialInfo

    @MockK
    private lateinit var mockCredentialRequestEncryption: CredentialRequestEncryption

    @MockK
    private lateinit var mockCredentialResponseEncryption: CredentialResponseEncryption

    @MockK
    private lateinit var mockAnyCredentialConfiguration: AnyCredentialConfiguration

    private lateinit var useCase: RefreshDeferredCredentialsImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = RefreshDeferredCredentialsImpl(
            deferredCredentialRepository = mockDeferredCredentialRepository,
            fetchExistingIssuerCredentialInfo = mockFetchExistingIssuerCredentialInfo,
            getPayloadEncryptionType = mockGetPayloadEncryptionType,
            createCredentialRequest = mockCreateCredentialRequest,
            oidCredentialOfferRepository = mockOidCredentialOfferRepository,
            verifyVcSdJwtSignature = mockVerifyVcSdJwtSignature,
            fetchVcMetadataByFormat = mockFetchVcMetadataByFormat,
            fetchTrustForIssuance = mockFetchTrustForIssuance,
            ocaBundler = mockOcaBundler,
            generateAnyDisplays = mockGenerateAnyDisplays,
            credentialOfferRepository = mockCredentialOfferRepository,
            environmentSetupRepository = mockEnvironmentSetupRepository,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun setupDefaultMocks() {
        mockkStatic(Instant::class)
        coEvery { Instant.now().epochSecond } returns currentTime

        coEvery { mockDeferredCredentialRepository.getAll() } returns Ok(
            listOf(
                deferredCredentialEntityWithBinding01,
            )
        )

        coEvery {
            mockFetchExistingIssuerCredentialInfo(any())
        } returns Ok(mockRawAndParsedIssuerCredentialInfo)

        coEvery {
            mockGetPayloadEncryptionType(any(), any())
        } returns Ok(mockPayloadEncryptionType)

        coEvery {
            mockCreateCredentialRequest(any(), any())
        } returns Ok(mockCredentialRequestType)

        coEvery {
            mockOidCredentialOfferRepository.fetchDeferredCredential(
                issuerEndpoint = any(),
                accessToken = any(),
                credentialRequestType = any(),
                payloadEncryptionType = any(),
            )
        } returns Ok(deferredCredentialResponse01)

        coEvery {
            mockDeferredCredentialRepository.updateStatus(
                credentialId = any(),
                progressionState = any(),
                polledAt = any(),
                pollInterval = any(),
            )
        } returns Ok(1)

        coEvery { mockEnvironmentSetupRepository.payloadEncryptionEnabled } returns true

        coEvery {
            mockVerifyVcSdJwtSignature(any(), any())
        } returns Ok(mockVcSdJwtCredential)

        coEvery {
            mockFetchVcMetadataByFormat(any())
        } returns Ok(mockVcMetadata)

        coEvery {
            mockFetchTrustForIssuance(any(), any())
        } returns mockTrustCheckResult

        coEvery {
            mockOcaBundler(any())
        } returns Ok(mockOcaBundle)

        coEvery {
            mockGenerateAnyDisplays(any(), any(), any(), any(), any())
        } returns Ok(mockAnyDisplays)

        coEvery {
            mockCredentialOfferRepository.updateDeferredCredentialOffer(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns Ok(1L)

        coEvery {
            mockCredentialOfferRepository.saveCredentialFromDeferred(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns Ok(1L)

        // credential mocking
        coEvery { mockVcSdJwtCredential.issuer } returns vcIssuer01
        coEvery { mockVcSdJwtCredential.vcSchemaId } returns vcSchemaId01
        coEvery { mockVcSdJwtCredential.payload } returns vcPayload01
        coEvery { mockVcMetadata.rawOcaBundle } returns vcRawOcaBundle01
        coEvery { mockRawAndParsedIssuerCredentialInfo.issuerCredentialInfo } returns mockIssuerCredentialInfo
        coEvery { mockRawAndParsedIssuerCredentialInfo.rawIssuerCredentialInfo } returns RAW_ISSUER_CREDENTIAL_INFO
        coEvery { mockIssuerCredentialInfo.credentialRequestEncryption } returns mockCredentialRequestEncryption
        coEvery { mockIssuerCredentialInfo.credentialResponseEncryption } returns mockCredentialResponseEncryption
        coEvery { mockIssuerCredentialInfo.credentialIssuer } returns URL(ISSUER01_URL)
        coEvery { mockIssuerCredentialInfo.credentialConfigurations } returns listOf(mockAnyCredentialConfiguration)
        coEvery { mockAnyCredentialConfiguration.identifier } returns selectedConfigurationId01
        coEvery { mockTrustCheckResult.actorTrustStatement } returns null

        coEvery { mockVcSdJwtCredential.validFromInstant } returns null
        coEvery { mockVcSdJwtCredential.validUntilInstant } returns null
        coEvery { mockAnyDisplays.issuerDisplays } returns listOf(mockAnyIssuerDisplay)
        coEvery { mockAnyDisplays.credentialDisplays } returns listOf(mockAnyCredentialDisplay)
        coEvery { mockAnyDisplays.clusters } returns listOf(mockCluster)
    }

    // region DeferredCredential
    @Test
    fun `A simple refresh of the deferred credentials runs specific things`() = runTest {
        useCase().assertOk()

        coVerifyOrder {
            mockDeferredCredentialRepository.getAll()
            mockFetchExistingIssuerCredentialInfo(1L)
            mockGetPayloadEncryptionType(mockCredentialRequestEncryption, mockCredentialResponseEncryption)
            mockCreateCredentialRequest(mockPayloadEncryptionType, credentialType)
            mockOidCredentialOfferRepository.fetchDeferredCredential(
                issuerEndpoint = issuerEndpoint01,
                accessToken = accessToken01,
                credentialRequestType = mockCredentialRequestType,
                payloadEncryptionType = mockPayloadEncryptionType,
            )
            mockGenerateAnyDisplays(
                anyCredential = null,
                issuerInfo = mockIssuerCredentialInfo,
                trustStatement = null,
                metadata = mockAnyCredentialConfiguration,
                ocaBundle = null,
            )
            mockCredentialOfferRepository.updateDeferredCredentialOffer(
                credentialId = credentialEntity01.id,
                progressionState = deferredCredentialEntity01.progressionState,
                polledAt = currentTime,
                pollInterval = deferredCredentialEntity01.pollInterval,
                issuerDisplays = listOf(mockAnyIssuerDisplay),
                credentialDisplays = listOf(mockAnyCredentialDisplay),
                rawMetadata = compressedRawIssuerCredentialInfo,
            )
        }
    }

    @Test
    fun `Deferred credentials are not refreshed if their interval has not passed`() = runTest {
        coEvery { Instant.now().epochSecond } returns polledAt01 + pollInterval01 - 1

        useCase().assertOk()

        coVerifyOrder {
            mockDeferredCredentialRepository.getAll()
        }

        coVerify(exactly = 0) {
            mockOidCredentialOfferRepository.fetchDeferredCredential(any(), any(), any(), any())
        }
    }

    @Test
    fun `Deferred credentials are not refreshed if they are not in progress`() = runTest {
        coEvery { mockDeferredCredentialRepository.getAll() } returns Ok(
            listOf(
                DeferredCredentialWithKeyBinding(
                    deferredCredential = deferredCredentialEntity01.copy(progressionState = DeferredProgressionState.INVALID),
                    credential = credentialEntity01,
                    keyBinding = null,
                )
            )
        )

        useCase().assertOk()

        coVerifyOrder {
            mockDeferredCredentialRepository.getAll()
        }

        coVerify(exactly = 0) {
            mockOidCredentialOfferRepository.fetchDeferredCredential(any(), any(), any(), any())
        }
    }

    @Test
    fun `A deferred credential repository error is mapped`() = runTest {
        val exception = Exception("my exception")
        coEvery { mockDeferredCredentialRepository.getAll() } returns Err(SsiError.Unexpected(exception))

        val error = useCase().assertErrorType(CredentialError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    @Test
    fun `Error during fetching the issuer credential information is mapped`() = runTest {
        val exception = Exception("my exception")
        coEvery {
            mockFetchExistingIssuerCredentialInfo(any())
        } returns Err(CredentialError.Unexpected(exception))

        val error = useCase().assertErrorType(CredentialError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    @Test
    fun `Error during getting of payload encryption type is mapped`() = runTest {
        val exception = Exception("my exception")
        coEvery {
            mockGetPayloadEncryptionType(any(), any())
        } returns Err(PayloadEncryptionError.Unexpected(exception))

        val error = useCase().assertErrorType(CredentialError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    @Test
    fun `Error during creating credential request is mapped`() = runTest {
        val exception = Exception("my exception")
        coEvery {
            mockCreateCredentialRequest(any(), any())
        } returns Err(CredentialOfferError.Unexpected(exception))

        val error = useCase().assertErrorType(CredentialError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    @Test
    fun `A fetch deferred credential issuer backend error invalidates the credential`() = runTest {
        val deferredError = CredentialOfferError.InvalidRequest

        coEvery {
            mockOidCredentialOfferRepository.fetchDeferredCredential(
                any(),
                any(),
                any(),
                any(),
            )
        } returns Err(deferredError)

        useCase().assertOk()

        coVerifyOrder {
            mockDeferredCredentialRepository.updateStatus(
                credentialId = credentialEntity01.id,
                progressionState = DeferredProgressionState.INVALID,
                polledAt = currentTime,
                pollInterval = deferredCredentialEntity01.pollInterval,
            )
        }
    }

    @Test
    fun `A fetch deferred credential network error does not update the credential`() = runTest {
        val deferredError = CredentialOfferError.NetworkInfoError
        coEvery {
            mockOidCredentialOfferRepository.fetchDeferredCredential(
                any(),
                any(),
                any(),
                any(),
            )
        } returns Err(deferredError)

        useCase().assertOk()

        coVerify(exactly = 0) {
            mockDeferredCredentialRepository.updateStatus(
                credentialId = any(),
                progressionState = any(),
                polledAt = any(),
                pollInterval = any(),
            )
            mockCredentialOfferRepository.updateDeferredCredentialOffer(
                credentialId = any(),
                progressionState = any(),
                polledAt = any(),
                pollInterval = any(),
                issuerDisplays = any(),
                credentialDisplays = any(),
                rawMetadata = any(),
            )
        }
    }

    @Test
    fun `A credential response containing a different transaction id invalidates the credential`() = runTest {
        coEvery {
            mockOidCredentialOfferRepository.fetchDeferredCredential(
                issuerEndpoint = any(),
                accessToken = any(),
                credentialRequestType = any(),
                payloadEncryptionType = any(),
            )
        } returns Ok(deferredCredentialResponse01.copy(transactionId = "other id"))

        useCase().assertOk()

        coVerify(exactly = 1) {
            mockDeferredCredentialRepository.updateStatus(
                credentialId = credentialEntity01.id,
                progressionState = DeferredProgressionState.INVALID,
                polledAt = currentTime,
                pollInterval = deferredCredentialEntity01.pollInterval,
            )
        }
        coEvery { mockAnyCredentialConfiguration.identifier } returns selectedConfigurationId01
    }

    @Test
    fun `New metadata containing a different credential configuration id does not update the credential`() = runTest {
        coEvery { mockAnyCredentialConfiguration.identifier } returns "other id"

        useCase().assertOk()

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.updateDeferredCredentialOffer(
                credentialId = any(),
                progressionState = any(),
                polledAt = any(),
                pollInterval = any(),
                issuerDisplays = any(),
                credentialDisplays = any(),
                rawMetadata = any(),
            )
        }
    }

    @Test
    fun `Error during generating new displays from metadata does not update the credential`() = runTest {
        coEvery {
            mockGenerateAnyDisplays(any(), any(), any(), any(), any())
        } returns Err(CredentialError.Unexpected(IllegalStateException("display generation error")))

        useCase().assertOk()

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.updateDeferredCredentialOffer(
                credentialId = any(),
                progressionState = any(),
                polledAt = any(),
                pollInterval = any(),
                issuerDisplays = any(),
                credentialDisplays = any(),
                rawMetadata = any(),
            )
        }
    }
    // endregion

    // region Credential

    @Test
    fun `A received credential that is supported is properly saved`() = runTest {
        coEvery {
            mockOidCredentialOfferRepository.fetchDeferredCredential(
                any(),
                any(),
                any(),
                any()
            )
        } returns Ok(credentialResponse01)

        useCase().assertOk()

        coVerifyOrder {
            mockDeferredCredentialRepository.getAll()
            mockFetchExistingIssuerCredentialInfo(1L)
            mockGetPayloadEncryptionType(mockCredentialRequestEncryption, mockCredentialResponseEncryption)
            mockCreateCredentialRequest(mockPayloadEncryptionType, credentialType)
            mockOidCredentialOfferRepository.fetchDeferredCredential(
                issuerEndpoint = issuerEndpoint01,
                accessToken = accessToken01,
                credentialRequestType = mockCredentialRequestType,
                payloadEncryptionType = mockPayloadEncryptionType,
            )
            mockVerifyVcSdJwtSignature(
                keyBinding = null,
                payload = vcPayload01,
            )

            mockFetchVcMetadataByFormat(
                mockVcSdJwtCredential,
            )

            mockFetchTrustForIssuance(
                issuerDid = vcIssuer01,
                vcSchemaId = vcSchemaId01,
            )

            mockOcaBundler(
                jsonString = vcRawOcaBundle01.rawOcaBundle,
            )

            mockGenerateAnyDisplays(
                anyCredential = mockVcSdJwtCredential,
                issuerInfo = mockIssuerCredentialInfo,
                trustStatement = null,
                metadata = mockAnyCredentialConfiguration,
                ocaBundle = mockOcaBundle,
            )

            mockCredentialOfferRepository.saveCredentialFromDeferred(
                credentialId = deferredCredentialEntity01.credentialId,
                payloads = listOf(vcPayload01),
                validFrom = any(),
                validUntil = any(),
                issuer = vcIssuer01,
                issuerDisplays = listOf(mockAnyIssuerDisplay),
                credentialDisplays = listOf(mockAnyCredentialDisplay),
                clusters = listOf(mockCluster),
                rawCredentialData = any(),
            )
        }
    }

    @Test
    fun `A credential signature verification error is handled`() = runTest {
        coEvery { mockVerifyVcSdJwtSignature(any(), any()) } returns Err(VcSdJwtError.InvalidJwt)
        coEvery {
            mockOidCredentialOfferRepository.fetchDeferredCredential(
                any(),
                any(),
                any(),
                any(),
            )
        } returns Ok(credentialResponse01)

        useCase().assertOk()

        coVerifyOrder {
            mockDeferredCredentialRepository.updateStatus(
                credentialId = deferredCredentialEntity01.credentialId,
                progressionState = DeferredProgressionState.INVALID,
                polledAt = any(),
                pollInterval = any(),
            )
        }
    }

    @Test
    fun `A VcMetatdata fetching error is handled`() = runTest {
        coEvery {
            mockOidCredentialOfferRepository.fetchDeferredCredential(
                any(),
                any(),
                any(),
                any()
            )
        } returns Ok(credentialResponse01)

        coEvery { mockFetchVcMetadataByFormat(any()) } returns Err(
            OcaError.NetworkError
        )
        useCase().assertOk()

        coVerify(exactly = 0) {
            mockFetchTrustForIssuance(any(), any())
            mockDeferredCredentialRepository.updateStatus(
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `A credential offer repository error is handled`() = runTest {
        coEvery {
            mockOidCredentialOfferRepository.fetchDeferredCredential(
                any(),
                any(),
                any(),
                any(),
            )
        } returns Ok(credentialResponse01)

        coEvery {
            mockCredentialOfferRepository.saveCredentialFromDeferred(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns Err(
            SsiError.Unexpected(Exception("my exception"))
        )
        useCase().assertOk()

        coVerify(exactly = 0) {
            mockDeferredCredentialRepository.updateStatus(
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    // endregion
    @Suppress("MayBeConst")
    private companion object {

        private val currentTime = Instant.ofEpochSecond(15L).epochSecond

        private val transactionId01 = "transactionId01"
        private val credentialType = CredentialType.Deferred(transactionId01)
        private val accessToken01 = "accessToken01"
        private val issuerEndpoint01 = "https://example"
        private val polledAt01 = Instant.ofEpochSecond(5L).epochSecond
        private val pollInterval01 = 10
        private val selectedConfigurationId01 = "selectedConfigurationId01"

        private val vcIssuer01 = "vcIssuer01"
        private val vcPayload01 = "vcPayload01"
        private val vcSchemaId01 = "vcSchemaId01"
        private val vcRawOcaBundle01 = RawOcaBundle("vcRawOcaBundle")

        private const val ISSUER01_URL = "https://example.com/issuer"

        private const val RAW_ISSUER_CREDENTIAL_INFO = "rawIssuerCredentialInfo"
        private val compressedRawIssuerCredentialInfo = RAW_ISSUER_CREDENTIAL_INFO.toByteArray().compress()

        private val deferredCredentialEntity01 = DeferredCredentialEntity(
            credentialId = 1L,
            progressionState = DeferredProgressionState.IN_PROGRESS,
            transactionId = transactionId01,
            accessToken = accessToken01,
            endpoint = issuerEndpoint01,
            pollInterval = pollInterval01,
            createdAt = Instant.ofEpochSecond(4L).epochSecond,
            polledAt = polledAt01,
        )

        private val credentialEntity01 = Credential(
            id = 1L,
            format = CredentialFormat.VC_SD_JWT,
            createdAt = Instant.ofEpochSecond(1L).epochSecond,
            selectedConfigurationId = selectedConfigurationId01,
            issuerUrl = URL(ISSUER01_URL)
        )

        private val deferredCredentialEntityWithBinding01 = DeferredCredentialWithKeyBinding(
            deferredCredential = deferredCredentialEntity01,
            credential = credentialEntity01,
            keyBinding = null,
        )

        private val deferredCredentialResponse01 = CredentialResponse.DeferredCredential(
            transactionId = transactionId01,
            interval = pollInterval01,
        )

        private val credential = CredentialResponse.Credential(
            vcPayload01
        )

        private val credentialResponse01 = CredentialResponse.VerifiableCredential(
            credentials = listOf(credential),
        )
    }
}
