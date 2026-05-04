package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyVcSdJwtSignature
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchTrustForIssuance
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveCredentialFromDeferred
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.RawOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.VcMetadata
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchVcMetadataByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaBundler
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustCheckResult
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class SaveCredentialFromDeferredImplTest {

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
    private lateinit var mockRawAndParsedIssuerCredentialInfo: RawAndParsedIssuerCredentialInfo

    @MockK
    private lateinit var mockIssuerCredentialInfo: IssuerCredentialInfo

    @MockK
    private lateinit var mockAnyCredentialConfiguration: AnyCredentialConfiguration

    @MockK
    private lateinit var mockVcSdJwtCredential: VcSdJwtCredential

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

    private lateinit var useCase: SaveCredentialFromDeferred

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = SaveCredentialFromDeferredImpl(
            verifyVcSdJwtSignature = mockVerifyVcSdJwtSignature,
            fetchVcMetadataByFormat = mockFetchVcMetadataByFormat,
            fetchTrustForIssuance = mockFetchTrustForIssuance,
            ocaBundler = mockOcaBundler,
            generateAnyDisplays = mockGenerateAnyDisplays,
            credentialOfferRepository = mockCredentialOfferRepository,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun setupDefaultMocks() {
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
            mockCredentialOfferRepository.saveCredentialFromDeferred(
                any(), any(), any(), any(), any(), any(), any(), any(), any(),
            )
        } returns Ok(1L)

        // Result mocks
        coEvery { mockVcSdJwtCredential.issuer } returns vcIssuer01
        coEvery { mockVcSdJwtCredential.vcSchemaId } returns vcSchemaId01
        coEvery { mockVcSdJwtCredential.payload } returns vcPayload01
        coEvery { mockVcSdJwtCredential.validFromInstant } returns null
        coEvery { mockVcSdJwtCredential.validUntilInstant } returns null
        coEvery { mockVcMetadata.rawOcaBundle } returns vcRawOcaBundle01
        coEvery { mockRawAndParsedIssuerCredentialInfo.issuerCredentialInfo } returns mockIssuerCredentialInfo
        coEvery { mockRawAndParsedIssuerCredentialInfo.rawIssuerCredentialInfo } returns RAW_ISSUER_CREDENTIAL_INFO
        coEvery { mockIssuerCredentialInfo.credentialConfigurations } returns listOf(mockAnyCredentialConfiguration)
        coEvery { mockAnyCredentialConfiguration.identifier } returns selectedConfigurationId01
        coEvery { mockTrustCheckResult.actorTrustStatement } returns null
        coEvery { mockAnyDisplays.issuerDisplays } returns listOf(mockAnyIssuerDisplay)
        coEvery { mockAnyDisplays.credentialDisplays } returns listOf(mockAnyCredentialDisplay)
        coEvery { mockAnyDisplays.clusters } returns listOf(mockCluster)
    }

    @Test
    fun `A received credential that is supported is properly saved`() = runTest {
        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = credentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
        ).assertOk()

        coVerify {
            mockVerifyVcSdJwtSignature(
                keyBinding = null,
                payload = vcPayload01,
            )
            mockFetchVcMetadataByFormat(mockVcSdJwtCredential)
            mockFetchTrustForIssuance(issuerDid = vcIssuer01, vcSchemaId = vcSchemaId01)
            mockOcaBundler(vcRawOcaBundle01.rawOcaBundle)
            mockGenerateAnyDisplays(
                anyCredential = mockVcSdJwtCredential,
                issuerInfo = mockIssuerCredentialInfo,
                trustStatement = null,
                metadata = any(),
                ocaBundle = mockOcaBundle,
            )
            mockCredentialOfferRepository.saveCredentialFromDeferred(
                credentialId = deferredCredentialEntity01.credentialId,
                payloads = listOf(vcPayload01),
                validFrom = null,
                validUntil = null,
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

        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = credentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
        )

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.saveCredentialFromDeferred(any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `A VcMetadata fetching error is handled`() = runTest {
        coEvery { mockFetchVcMetadataByFormat(any()) } returns Err(OcaError.NetworkError)

        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = credentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
        )

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.saveCredentialFromDeferred(any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `A credential offer repository error is handled`() = runTest {
        coEvery {
            mockCredentialOfferRepository.saveCredentialFromDeferred(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Err(SsiError.Unexpected(Exception("my exception")))

        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = credentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
        )
    }

    @Suppress("MayBeConstant")
    private companion object {
        private val transactionId01 = "transactionId01"
        private val vcIssuer01 = "vcIssuer01"
        private val vcPayload01 = "vcPayload01"
        private val vcSchemaId01 = "vcSchemaId01"
        private val vcRawOcaBundle01 = RawOcaBundle("vcRawOcaBundle")
        private val issuer01_url = "https://example.com/issuer"
        private const val RAW_ISSUER_CREDENTIAL_INFO = "rawIssuerCredentialInfo"
        private val selectedConfigurationId01 = "selectedConfigurationId01"

        private val deferredCredentialEntity01 = DeferredCredentialEntity(
            credentialId = 1L,
            progressionState = DeferredProgressionState.IN_PROGRESS,
            transactionId = transactionId01,
            accessToken = "accessToken01",
            refreshToken = "refreshToken01",
            endpoint = "https://example",
            pollInterval = 10,
            createdAt = 4L,
            polledAt = 5L,
        )

        private val credentialEntity01 = Credential(
            id = 1L,
            format = CredentialFormat.VC_SD_JWT,
            createdAt = 1L,
            selectedConfigurationId = selectedConfigurationId01,
            issuerUrl = URL(issuer01_url)
        )

        private val deferredCredentialWithBinding01 = DeferredCredentialWithKeyBinding(
            deferredCredential = deferredCredentialEntity01,
            credential = credentialEntity01,
            keyBindings = listOf(),
        )

        private val credential = CredentialResponse.Credential(vcPayload01)

        private val credentialResponse01 = CredentialResponse.VerifiableCredential(
            credentials = listOf(credential),
        )
    }
}
