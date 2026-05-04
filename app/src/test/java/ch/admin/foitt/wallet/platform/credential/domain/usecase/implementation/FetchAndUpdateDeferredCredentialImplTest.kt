package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.CredentialRequestType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchDeferredCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.TokenResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialRequestEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequest
import ch.admin.foitt.openid4vc.domain.usecase.FetchIssuerConfiguration
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchAndUpdateDeferredCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchExistingIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveCredentialFromDeferred
import ch.admin.foitt.wallet.platform.credential.domain.usecase.UpdateDeferredCredential
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.PayloadEncryptionError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryptionType
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialRepository
import ch.admin.foitt.wallet.util.assertErr
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.net.URL
import java.time.Instant
import java.util.stream.Stream
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository as OidCredentialOfferRepository

class FetchAndUpdateDeferredCredentialImplTest {

    @MockK
    private lateinit var mockDeferredCredentialRepository: DeferredCredentialRepository

    @MockK
    private lateinit var mockFetchExistingIssuerCredentialInfo: FetchExistingIssuerCredentialInfo

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockGetPayloadEncryptionType: GetPayloadEncryptionType

    @MockK
    private lateinit var mockCreateCredentialRequest: CreateCredentialRequest

    @MockK
    private lateinit var mockOidCredentialOfferRepository: OidCredentialOfferRepository

    @MockK
    private lateinit var mockSaveCredentialFromDeferred: SaveCredentialFromDeferred

    @MockK
    private lateinit var mockUpdateDeferredCredential: UpdateDeferredCredential

    @MockK
    private lateinit var mockFetchIssuerConfiguration: FetchIssuerConfiguration

    @MockK
    private lateinit var mockRawAndParsedIssuerCredentialInfo: RawAndParsedIssuerCredentialInfo

    @MockK
    private lateinit var mockIssuerCredentialInfo: IssuerCredentialInfo

    @MockK
    private lateinit var mockCredentialRequestEncryption: CredentialRequestEncryption

    @MockK
    private lateinit var mockCredentialResponseEncryption: CredentialResponseEncryption

    @MockK
    private lateinit var mockPayloadEncryptionType: PayloadEncryptionType

    @MockK
    private lateinit var mockCredentialRequestType: CredentialRequestType

    private lateinit var useCase: FetchAndUpdateDeferredCredential

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(Instant::class)
        coEvery { Instant.now().epochSecond } returns currentTime

        useCase = FetchAndUpdateDeferredCredentialImpl(
            deferredCredentialRepository = mockDeferredCredentialRepository,
            fetchExistingIssuerCredentialInfo = mockFetchExistingIssuerCredentialInfo,
            environmentSetupRepository = mockEnvironmentSetupRepository,
            getPayloadEncryptionType = mockGetPayloadEncryptionType,
            createCredentialRequest = mockCreateCredentialRequest,
            oidCredentialOfferRepository = mockOidCredentialOfferRepository,
            saveCredentialFromDeferred = mockSaveCredentialFromDeferred,
            updateDeferredCredential = mockUpdateDeferredCredential,
            fetchIssuerConfiguration = mockFetchIssuerConfiguration,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun setupDefaultMocks() {
        coEvery { mockEnvironmentSetupRepository.payloadEncryptionEnabled } returns true

        coEvery {
            mockFetchExistingIssuerCredentialInfo(any())
        } returns Ok(mockRawAndParsedIssuerCredentialInfo)

        coEvery { mockRawAndParsedIssuerCredentialInfo.issuerCredentialInfo } returns mockIssuerCredentialInfo
        coEvery { mockIssuerCredentialInfo.credentialRequestEncryption } returns mockCredentialRequestEncryption
        coEvery { mockIssuerCredentialInfo.credentialResponseEncryption } returns mockCredentialResponseEncryption

        coEvery {
            mockGetPayloadEncryptionType(any(), any())
        } returns Ok(mockPayloadEncryptionType)

        coEvery {
            mockCreateCredentialRequest(any(), any())
        } returns Ok(mockCredentialRequestType)

        coEvery {
            mockOidCredentialOfferRepository.fetchDeferredCredential(any(), any(), any(), any())
        } returns Ok(deferredCredentialResponse01)

        coEvery {
            mockUpdateDeferredCredential(any(), any(), any())
        } returns Ok(Unit)

        coEvery {
            mockSaveCredentialFromDeferred(any(), any(), any())
        } returns Ok(CREDENTIAL_ID_01)

        coEvery {
            mockDeferredCredentialRepository.updateStatus(any(), any(), any(), any())
        } returns Ok(1)

        coEvery {
            mockFetchIssuerConfiguration(any())
        } returns Ok(issuerConfig)

        coEvery {
            mockDeferredCredentialRepository.updateTokens(any(), any())
        } returns Ok(1)

        coEvery {
            mockOidCredentialOfferRepository.fetchAccessTokenByRefreshToken(any(), any())
        } returns Ok(tokenResponse)
    }

    @Test
    fun `Error during fetching the issuer credential information is mapped`() = runTest {
        val exception = Exception("my exception")
        coEvery {
            mockFetchExistingIssuerCredentialInfo(any())
        } returns Err(CredentialError.Unexpected(exception))

        val error = useCase(deferredCredentialWithBinding01).assertErrorType(CredentialError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    @Test
    fun `Error during getting of payload encryption type is mapped`() = runTest {
        val exception = Exception("my exception")
        coEvery {
            mockGetPayloadEncryptionType(any(), any())
        } returns Err(PayloadEncryptionError.Unexpected(exception))

        val error = useCase(deferredCredentialWithBinding01).assertErrorType(CredentialError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    @Test
    fun `Error during creating credential request is mapped`() = runTest {
        val exception = Exception("my exception")
        coEvery {
            mockCreateCredentialRequest(any(), any())
        } returns Err(CredentialOfferError.Unexpected(exception))

        val error = useCase(deferredCredentialWithBinding01).assertErrorType(CredentialError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    @ParameterizedTest
    @MethodSource("invalidatingErrors")
    fun `Specific deferred credential issuer backend errors invalidate the credential`(error: FetchDeferredCredentialError) = runTest {
        coEvery {
            mockOidCredentialOfferRepository.fetchDeferredCredential(
                any(),
                any(),
                any(),
                any(),
            )
        } returns Err(error)

        useCase(deferredCredentialEntity = deferredCredentialWithBinding01).assertErr()

        coVerify(exactly = 1) {
            mockDeferredCredentialRepository.updateStatus(
                credentialId = credentialEntity01.id,
                progressionState = DeferredProgressionState.INVALID,
                polledAt = currentTime,
                pollInterval = deferredCredentialEntity01.pollInterval,
            )
        }
    }

    @ParameterizedTest
    @MethodSource("unexpectedErrors")
    fun `Other deferred credential issuer backend errors do no invalidate the credential`(error: FetchDeferredCredentialError) = runTest {
        coEvery {
            mockOidCredentialOfferRepository.fetchDeferredCredential(
                any(),
                any(),
                any(),
                any(),
            )
        } returns Err(error)

        useCase(deferredCredentialWithBinding01).assertErr()

        coVerify(exactly = 0) {
            mockDeferredCredentialRepository.updateStatus(
                credentialId = credentialEntity01.id,
                progressionState = DeferredProgressionState.INVALID,
                polledAt = currentTime,
                pollInterval = deferredCredentialEntity01.pollInterval,
            )
        }
    }

    @Test
    fun `A fetch deferred credential invalidToken trigger a token refresh and update the tokens`() = runTest {
        coEvery {
            mockOidCredentialOfferRepository.fetchDeferredCredential(
                any(),
                ACCESS_TOKEN_01,
                any(),
                any(),
            )
        } returns Err(CredentialOfferError.InvalidToken)

        coEvery {
            mockOidCredentialOfferRepository.fetchDeferredCredential(
                any(),
                ACCESS_TOKEN_02,
                any(),
                any(),
            )
        } returns Ok(deferredCredentialResponse01)

        useCase(deferredCredentialWithBinding01).assertOk()

        coVerify(exactly = 2) {
            mockOidCredentialOfferRepository.fetchDeferredCredential(
                any(),
                any(),
                any(),
                any(),
            )
        }

        coVerify(exactly = 1) {
            mockDeferredCredentialRepository.updateTokens(
                credentialId = credentialEntity01.id,
                tokenResponse = tokenResponse,
            )
        }
    }

    private companion object {
        @JvmStatic
        fun invalidatingErrors(): Stream<Arguments> = Stream.of(
            Arguments.of(
                CredentialOfferError.InvalidCredentialOffer,
                CredentialOfferError.CredentialRequestDenied,
                CredentialOfferError.InvalidRequestBearerToken,
                CredentialOfferError.InvalidTransactionId,
            )
        )

        @JvmStatic
        fun unexpectedErrors(): Stream<Arguments> = Stream.of(
            Arguments.of(
                CredentialOfferError.InsufficientScope,
                CredentialOfferError.InvalidCredentialRequest,
                CredentialOfferError.UnknownCredentialConfiguration,
                CredentialOfferError.UnknownCredentialIdentifier,
                CredentialOfferError.InvalidProof,
                CredentialOfferError.InvalidNonce,
                CredentialOfferError.InvalidEncryptionParameters,
                CredentialOfferError.NetworkInfoError,
            )
        )

        private const val TRANSACTION_ID_01 = "transactionId01"
        private const val ACCESS_TOKEN_01 = "accessToken01"
        private const val ACCESS_TOKEN_02 = "accessToken02"
        private const val REFRESH_TOKEN_01 = "refreshToken01"
        private const val ISSUER_URL = "https://example.com/issuer"
        private const val CREDENTIAL_ID_01 = 1L
        private val currentTime = Instant.ofEpochSecond(15L).epochSecond

        private val issuerConfig = IssuerConfiguration(
            tokenEndpoint = URL("https://example.com/token"),
            issuer = URL(ISSUER_URL),
        )

        private val deferredCredentialEntity01 = DeferredCredentialEntity(
            credentialId = CREDENTIAL_ID_01,
            progressionState = DeferredProgressionState.IN_PROGRESS,
            transactionId = TRANSACTION_ID_01,
            accessToken = ACCESS_TOKEN_01,
            refreshToken = REFRESH_TOKEN_01,
            endpoint = ISSUER_URL,
            pollInterval = 10,
            createdAt = Instant.now().epochSecond,
            polledAt = Instant.now().epochSecond,
        )

        private val credentialEntity01 = Credential(
            id = CREDENTIAL_ID_01,
            format = CredentialFormat.VC_SD_JWT,
            createdAt = Instant.now().epochSecond,
            selectedConfigurationId = "configId",
            issuerUrl = URL(ISSUER_URL)
        )

        private val deferredCredentialWithBinding01 = DeferredCredentialWithKeyBinding(
            deferredCredential = deferredCredentialEntity01,
            credential = credentialEntity01,
            keyBindings = emptyList(),
        )

        private val deferredCredentialResponse01 = CredentialResponse.DeferredCredential(
            transactionId = TRANSACTION_ID_01,
            interval = 10,
        )

        private val tokenResponse = TokenResponse(
            accessToken = ACCESS_TOKEN_02,
            refreshToken = REFRESH_TOKEN_01,
            tokenType = "tokenType"
        )
    }
}
