package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.UpdateDeferredCredential
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialRepository
import ch.admin.foitt.wallet.platform.utils.compress
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.Instant

class UpdateDeferredCredentialImplTest {

    @MockK
    private lateinit var mockDeferredCredentialRepository: DeferredCredentialRepository

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
    private lateinit var mockAnyDisplays: AnyDisplays

    @MockK
    private lateinit var mockAnyIssuerDisplay: AnyIssuerDisplay

    @MockK
    private lateinit var mockAnyCredentialDisplay: AnyCredentialDisplay

    @MockK
    private lateinit var mockCluster: Cluster

    private lateinit var useCase: UpdateDeferredCredential

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = UpdateDeferredCredentialImpl(
            deferredCredentialRepository = mockDeferredCredentialRepository,
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
        mockkStatic(Instant::class)
        coEvery { Instant.now().epochSecond } returns currentTime

        coEvery {
            mockDeferredCredentialRepository.updateStatus(
                credentialId = any(),
                progressionState = any(),
                polledAt = any(),
                pollInterval = any(),
            )
        } returns Ok(1)

        coEvery {
            mockGenerateAnyDisplays(any(), any(), any(), any(), any())
        } returns Ok(mockAnyDisplays)

        coEvery {
            mockCredentialOfferRepository.updateDeferredCredentialMetaData(
                any(),
                any(),
                any(),
                any(),
            )
        } returns Ok(Unit)

        coEvery { mockRawAndParsedIssuerCredentialInfo.issuerCredentialInfo } returns mockIssuerCredentialInfo
        coEvery { mockRawAndParsedIssuerCredentialInfo.rawIssuerCredentialInfo } returns RAW_ISSUER_CREDENTIAL_INFO
        coEvery { mockIssuerCredentialInfo.credentialConfigurations } returns listOf(mockAnyCredentialConfiguration)
        coEvery { mockAnyCredentialConfiguration.identifier } returns selectedConfigurationId01
        coEvery { mockAnyDisplays.issuerDisplays } returns listOf(mockAnyIssuerDisplay)
        coEvery { mockAnyDisplays.credentialDisplays } returns listOf(mockAnyCredentialDisplay)
        coEvery { mockAnyDisplays.clusters } returns listOf(mockCluster)
    }

    @Test
    fun `An update of the deferred credential runs specific things`() = runTest {
        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = deferredCredentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
        ).assertOk()

        coVerify {
            mockDeferredCredentialRepository.updateStatus(
                credentialId = credentialEntity01.id,
                progressionState = DeferredProgressionState.IN_PROGRESS,
                polledAt = currentTime,
                pollInterval = pollInterval01,
            )
            mockGenerateAnyDisplays(
                anyCredential = null,
                issuerInfo = mockIssuerCredentialInfo,
                trustStatement = null,
                metadata = mockAnyCredentialConfiguration,
                ocaBundle = null,
            )
            mockCredentialOfferRepository.updateDeferredCredentialMetaData(
                credentialId = credentialEntity01.id,
                issuerDisplays = listOf(mockAnyIssuerDisplay),
                credentialDisplays = listOf(mockAnyCredentialDisplay),
                rawMetadata = RAW_ISSUER_CREDENTIAL_INFO_COMPRESSED,
            )
        }
    }

    @Test
    fun `A credential response containing a different transaction id returns an early error`() = runTest {
        val result = useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = deferredCredentialResponse01.copy(transactionId = "otherId"),
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
        )

        result.assertErrorType(CredentialError.Unexpected::class)

        coVerify(exactly = 0) {
            mockDeferredCredentialRepository.updateStatus(
                credentialId = any(),
                progressionState = any(),
                polledAt = any(),
                pollInterval = any(),
            )
            mockCredentialOfferRepository.updateDeferredCredentialMetaData(
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `New metadata containing a different credential configuration id does not update the credential`() = runTest {
        coEvery { mockAnyCredentialConfiguration.identifier } returns "other id"

        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = deferredCredentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
        )

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.updateDeferredCredentialMetaData(
                credentialId = any(),
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

        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = deferredCredentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
        )

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.updateDeferredCredentialMetaData(
                credentialId = any(),
                issuerDisplays = any(),
                credentialDisplays = any(),
                rawMetadata = any(),
            )
        }
    }

    @Suppress("MayBeConst")
    private companion object {
        private val currentTime = Instant.ofEpochSecond(15L).epochSecond
        private val transactionId01 = "transactionId01"
        private val issuerEndpoint01 = "https://example"
        private val polledAt01 = 5L
        private val pollInterval01 = 10
        private val selectedConfigurationId01 = "selectedConfigurationId01"
        private const val ISSUER01_URL = "https://example.com/issuer"
        private const val RAW_ISSUER_CREDENTIAL_INFO = "rawIssuerCredentialInfo"

        private val RAW_ISSUER_CREDENTIAL_INFO_COMPRESSED = RAW_ISSUER_CREDENTIAL_INFO.toByteArray().compress()

        private val deferredCredentialEntity01 = DeferredCredentialEntity(
            credentialId = 1L,
            progressionState = DeferredProgressionState.IN_PROGRESS,
            transactionId = transactionId01,
            accessToken = "accessToken01",
            refreshToken = "refreshToken01",
            endpoint = issuerEndpoint01,
            pollInterval = pollInterval01,
            createdAt = 4L,
            polledAt = polledAt01,
        )

        private val credentialEntity01 = Credential(
            id = 1L,
            format = CredentialFormat.VC_SD_JWT,
            createdAt = 1L,
            selectedConfigurationId = selectedConfigurationId01,
            issuerUrl = URL(ISSUER01_URL)
        )

        private val deferredCredentialWithBinding01 = DeferredCredentialWithKeyBinding(
            deferredCredential = deferredCredentialEntity01,
            credential = credentialEntity01,
            keyBindings = listOf(),
        )

        private val deferredCredentialResponse01 = CredentialResponse.DeferredCredential(
            transactionId = transactionId01,
            interval = pollInterval01,
        )
    }
}
