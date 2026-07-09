package ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.BatchCredentialIssuance
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.GenerateDPoPKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import ch.admin.foitt.wallet.platform.batch.domain.error.RefreshBatchCredentialsError
import ch.admin.foitt.wallet.platform.batch.domain.repository.BatchRefreshDataRepository
import ch.admin.foitt.wallet.platform.batch.domain.usecase.DeleteBundleItemsByAmount
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.mock.MockBatchRefreshData
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.mock.MockBatchRefreshData.AUTHENTICATION_ID
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.mock.MockBatchRefreshData.CREDENTIAL_ID
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.mock.MockBatchRefreshData.CREDENTIAL_ISSUER
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.mock.MockBatchRefreshData.KEY_ID
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.mock.MockBatchRefreshData.REFRESH_TOKEN
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.mock.MockBatchRefreshData.SELECTED_CONFIG_ID
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.EvaluateBatchSize
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetBindingKeyPair
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetCredentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.database.domain.model.DpopBindingEntity
import ch.admin.foitt.wallet.platform.database.domain.model.PresentableBatchItemCount
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateProofKeyPairs
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryptionType
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialWithBatchDataAndAuthenticationRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteBundleItems
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.security.KeyPair

class RefreshBatchCredentialsImplTest {

    @MockK
    private lateinit var mockBundleItemRepository: BundleItemRepository

    @MockK
    private lateinit var mockBatchRefreshDataRepository: BatchRefreshDataRepository

    @MockK
    private lateinit var mockVerifiableCredentialWithBatchDataAndAuthenticationRepository:
        VerifiableCredentialWithBatchDataAndAuthenticationRepository

    @MockK
    private lateinit var mockGetCredentialConfig: GetCredentialConfig

    @MockK
    private lateinit var mockGetPayloadEncryptionType: GetPayloadEncryptionType

    @MockK
    private lateinit var mockFetchRawAndParsedIssuerCredentialInfo: FetchRawAndParsedIssuerCredentialInfo

    @MockK
    private lateinit var mockGetVerifiableCredentialParams: GetVerifiableCredentialParams

    @MockK
    private lateinit var mockDeleteBundleItemsByAmount: DeleteBundleItemsByAmount

    @MockK
    private lateinit var mockEvaluateBatchSize: EvaluateBatchSize

    @MockK
    private lateinit var mockGenerateProofKeyPairs: GenerateProofKeyPairs

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockFetchCredentialByConfig: FetchCredentialByConfig

    @MockK
    private lateinit var mockGetBindingKeyPair: GetBindingKeyPair

    @MockK
    private lateinit var mockHandleBatchCredentialResult: HandleBatchCredentialResult

    @MockK
    private lateinit var mockDeleteBundleItems: DeleteBundleItems

    @MockK
    private lateinit var mockGenerateDPoPKeyPair: GenerateDPoPKeyPair

    @MockK
    private lateinit var mockAnyCredentialConfiguration: AnyCredentialConfiguration

    private lateinit var useCase: RefreshBatchCredentialsImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = RefreshBatchCredentialsImpl(
            bundleItemRepository = mockBundleItemRepository,
            batchRefreshDataRepository = mockBatchRefreshDataRepository,
            verifiableCredentialWithBatchDataAndAuthenticationRepository = mockVerifiableCredentialWithBatchDataAndAuthenticationRepository,
            getCredentialConfig = mockGetCredentialConfig,
            getPayloadEncryptionType = mockGetPayloadEncryptionType,
            fetchRawAndParsedIssuerCredentialInfo = mockFetchRawAndParsedIssuerCredentialInfo,
            getVerifiableCredentialParams = mockGetVerifiableCredentialParams,
            evaluateBatchSize = mockEvaluateBatchSize,
            deleteBundleItemsByAmount = mockDeleteBundleItemsByAmount,
            generateProofKeyPairs = mockGenerateProofKeyPairs,
            environmentSetupRepository = mockEnvironmentSetupRepository,
            fetchCredentialByConfig = mockFetchCredentialByConfig,
            getBindingKeyPair = mockGetBindingKeyPair,
            handleBatchCredentialResult = mockHandleBatchCredentialResult,
            deleteBundleItems = mockDeleteBundleItems,
            generateDPoPKeyPair = mockGenerateDPoPKeyPair,
        )
        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {
        coEvery { mockEvaluateBatchSize(any()) } answers {
            Ok(requireNotNull((args.first() as IssuerCredentialInfo).batchCredentialIssuance).batchSize)
        }
        coEvery {
            mockGetCredentialConfig(
                credentials = any(),
                credentialConfigurations = any()
            )
        } returns Ok(mockk(relaxed = true))
        coEvery { mockDeleteBundleItemsByAmount(any(), any()) } returns Ok(Unit)

        coEvery { mockDeleteBundleItems(any()) } returns Ok(0)
        coEvery { mockGenerateProofKeyPairs(any(), any()) } returns Ok(mockk(relaxed = true))
        coEvery { mockEnvironmentSetupRepository.isDPopEnabled } returns true
        coEvery { mockGetPayloadEncryptionType(any(), any()) } returns Ok(mockk(relaxed = true))
        coEvery { mockGetBindingKeyPair(any()) } returns Ok(null)
        coEvery { mockGenerateDPoPKeyPair(any()) } returns Ok(mockk(relaxed = true))
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `No matching credential for batch refresh data`() = runTest {
        val batchData = listOf(
            MockBatchRefreshData.createBatchRefreshData(batchSize = 10)
        )
        coEvery { mockVerifiableCredentialWithBatchDataAndAuthenticationRepository.getAll() } returns Ok(batchData)
        coEvery { mockBundleItemRepository.getCountOfNeverPresented() } returns Ok(
            listOf(
                PresentableBatchItemCount(
                    credentialId = 999L,
                    count = 5
                )
            )
        )

        useCase().assertOk()

        coVerify(exactly = 0) { mockFetchRawAndParsedIssuerCredentialInfo(any()) }
    }

    @Test
    fun `Presentable count above threshold does not trigger refresh`() = runTest {
        val batchSize: BatchSize = 10
        val presentableCount = 3
        val batchData = listOf(
            MockBatchRefreshData.createBatchRefreshData(batchSize = batchSize)
        )
        coEvery { mockVerifiableCredentialWithBatchDataAndAuthenticationRepository.getAll() } returns Ok(batchData)
        coEvery { mockBundleItemRepository.getCountOfNeverPresented() } returns Ok(
            listOf(
                PresentableBatchItemCount(
                    credentialId = CREDENTIAL_ID,
                    count = presentableCount
                )
            )
        )

        useCase().assertOk()

        coVerify(exactly = 0) { mockFetchRawAndParsedIssuerCredentialInfo(any()) }
    }

    @Test
    fun `Batch refresh without config change`() = runTest {
        val batchSize: BatchSize = 5
        val presentableCount = 1
        val batchRefreshData = listOf(MockBatchRefreshData.createBatchRefreshData(batchSize = batchSize))

        coEvery { mockVerifiableCredentialWithBatchDataAndAuthenticationRepository.getAll() } returns Ok(batchRefreshData)
        coEvery { mockBundleItemRepository.getCountOfNeverPresented() } returns Ok(
            listOf(
                PresentableBatchItemCount(
                    credentialId = CREDENTIAL_ID,
                    count = presentableCount
                )
            )
        )

        val issuerInfo = IssuerCredentialInfo(
            credentialEndpoint = URL("https://issuer.example/credential"),
            deferredCredentialEndpoint = null,
            nonceEndpoint = null,
            credentialIssuer = URL(CREDENTIAL_ISSUER),
            credentialRequestEncryption = null,
            credentialResponseEncryption = null,
            credentialConfigurations = emptyList(),
            display = null,
            batchCredentialIssuance = BatchCredentialIssuance(batchSize = batchSize),
        )
        val rawAndParsed = RawAndParsedIssuerCredentialInfo(
            rawIssuerCredentialInfo = "{}",
            issuerCredentialInfo = issuerInfo,
        )
        coEvery {
            mockFetchRawAndParsedIssuerCredentialInfo(
                issuerEndpoint = URL(CREDENTIAL_ISSUER),
            )
        } returns Ok(rawAndParsed)
        coEvery {
            mockGetVerifiableCredentialParams(issuerInfo, any(), any())
        } returns Ok(mockk(relaxed = true))
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any(), any())
        } returns Ok(
            AnyVerifiedBatchCredential(
                accessToken = "access-token",
                refreshToken = REFRESH_TOKEN,
                dpopKeyBinding = null,
                vcSdJwtCredentials = emptyList(),
            )
        )
        coEvery {
            mockBundleItemRepository.getAllByCredentialId(CREDENTIAL_ID)
        } returns Ok(mockk(relaxed = true))
        coEvery {
            mockHandleBatchCredentialResult(CREDENTIAL_ID, any(), batchSize, any(), any(), any())
        } returns Ok(FetchCredentialResult.Credential(CREDENTIAL_ID))

        useCase().assertOk()

        coVerify(exactly = 1) {
            mockFetchRawAndParsedIssuerCredentialInfo(issuerEndpoint = URL(CREDENTIAL_ISSUER))
            mockGetCredentialConfig(
                credentials = listOf(SELECTED_CONFIG_ID),
                credentialConfigurations = issuerInfo.credentialConfigurations
            )
            mockGetVerifiableCredentialParams(issuerInfo, any(), any())
            mockGenerateProofKeyPairs(any(), any())
            mockFetchCredentialByConfig(any(), any(), any(), any(), any())
            mockHandleBatchCredentialResult(any(), any(), any(), any(), any(), any())
            mockDeleteBundleItems(any())
        }

        coVerify(exactly = 0) {
            mockDeleteBundleItemsByAmount(any(), any())
            mockBatchRefreshDataRepository.updateBatchSize(CREDENTIAL_ID, batchSize)
        }
    }

    @Test
    fun `Batch refresh reuses stored software dpop key binding`() = runTest {
        val batchSize: BatchSize = 5
        val presentableCount = 1
        val publicKey = byteArrayOf(1, 2, 3)
        val privateKey = byteArrayOf(4, 5, 6)
        val batchRefreshData = listOf(
            MockBatchRefreshData.createBatchRefreshData(
                batchSize = batchSize,
                dpopBinding = DpopBindingEntity(
                    id = KEY_ID,
                    credentialAuthenticationId = AUTHENTICATION_ID,
                    algorithm = SigningAlgorithm.ES256.name,
                    bindingType = KeyBindingType.SOFTWARE,
                    publicKey = publicKey,
                    privateKey = privateKey,
                ),
            )
        )

        coEvery { mockVerifiableCredentialWithBatchDataAndAuthenticationRepository.getAll() } returns Ok(batchRefreshData)
        coEvery { mockBundleItemRepository.getCountOfNeverPresented() } returns Ok(
            listOf(PresentableBatchItemCount(credentialId = CREDENTIAL_ID, count = presentableCount))
        )

        val issuerInfo = IssuerCredentialInfo(
            credentialEndpoint = URL("https://issuer.example/credential"),
            deferredCredentialEndpoint = null,
            nonceEndpoint = null,
            credentialIssuer = URL(CREDENTIAL_ISSUER),
            credentialRequestEncryption = null,
            credentialResponseEncryption = null,
            credentialConfigurations = emptyList(),
            display = null,
            batchCredentialIssuance = BatchCredentialIssuance(batchSize = batchSize),
        )
        val rawAndParsed = RawAndParsedIssuerCredentialInfo(
            rawIssuerCredentialInfo = "{}",
            issuerCredentialInfo = issuerInfo,
        )
        val softwareKeyPair = mockk<KeyPair>(relaxed = true)

        coEvery {
            mockFetchRawAndParsedIssuerCredentialInfo(issuerEndpoint = URL(CREDENTIAL_ISSUER))
        } returns Ok(rawAndParsed)
        coEvery {
            mockGetVerifiableCredentialParams(issuerInfo, any(), any())
        } returns Ok(mockk(relaxed = true))
        coEvery {
            mockGetBindingKeyPair(batchRefreshData.first().authentication)
        } returns Ok(
            BindingKeyPair(
                keyPair = JWSKeyPair(
                    algorithm = SigningAlgorithm.ES256,
                    keyPair = softwareKeyPair,
                    keyId = KEY_ID,
                    bindingType = KeyBindingType.SOFTWARE,
                ),
                attestationJwt = null,
            )
        )
        coEvery {
            mockFetchCredentialByConfig(
                any(),
                any(),
                any(),
                any(),
                match {
                    it?.keyPair?.algorithm == SigningAlgorithm.ES256 &&
                        it.keyPair.keyId == KEY_ID &&
                        it.keyPair.bindingType == KeyBindingType.SOFTWARE &&
                        it.attestationJwt == null
                }
            )
        } returns Ok(
            AnyVerifiedBatchCredential(
                accessToken = "access-token",
                refreshToken = REFRESH_TOKEN,
                dpopKeyBinding = null,
                vcSdJwtCredentials = emptyList(),
            )
        )
        coEvery {
            mockBundleItemRepository.getAllByCredentialId(CREDENTIAL_ID)
        } returns Ok(mockk(relaxed = true))
        coEvery {
            mockHandleBatchCredentialResult(CREDENTIAL_ID, any(), batchSize, any(), any(), any())
        } returns Ok(FetchCredentialResult.Credential(CREDENTIAL_ID))
        coEvery {
            mockDeleteBundleItems(any())
        } returns Ok(presentableCount)

        useCase().assertOk()

        coVerify(exactly = 1) {
            mockGetBindingKeyPair(batchRefreshData.first().authentication)
            mockFetchCredentialByConfig(true, any(), any(), any(), any())
        }
    }

    @Test
    fun `New batch size is lower, so presentable count is higher than new threshold, so no refresh is needed`() = runTest {
        val oldBatchSize: BatchSize = 5
        val newBatchSize: BatchSize = 3
        val presentableCount = 1
        val batchRefreshData = listOf(MockBatchRefreshData.createBatchRefreshData(batchSize = oldBatchSize))

        coEvery { mockVerifiableCredentialWithBatchDataAndAuthenticationRepository.getAll() } returns Ok(batchRefreshData)
        coEvery { mockBundleItemRepository.getCountOfNeverPresented() } returns Ok(
            listOf(
                PresentableBatchItemCount(
                    credentialId = CREDENTIAL_ID,
                    count = presentableCount
                )
            )
        )

        val issuerInfo = IssuerCredentialInfo(
            credentialEndpoint = URL("https://issuer.example/credential"),
            deferredCredentialEndpoint = null,
            nonceEndpoint = null,
            credentialIssuer = URL(CREDENTIAL_ISSUER),
            credentialRequestEncryption = null,
            credentialResponseEncryption = null,
            credentialConfigurations = listOf(mockAnyCredentialConfiguration),
            display = null,
            batchCredentialIssuance = BatchCredentialIssuance(batchSize = newBatchSize),
        )
        val rawAndParsed = RawAndParsedIssuerCredentialInfo(
            rawIssuerCredentialInfo = "{}",
            issuerCredentialInfo = issuerInfo,
        )
        coEvery {
            mockFetchRawAndParsedIssuerCredentialInfo(
                issuerEndpoint = URL(CREDENTIAL_ISSUER),
            )
        } returns Ok(rawAndParsed)
        coEvery {
            mockBatchRefreshDataRepository.updateBatchSize(CREDENTIAL_ID, newBatchSize)
        } returns Ok(1)

        useCase().assertOk()

        coVerify(exactly = 1) {
            mockFetchRawAndParsedIssuerCredentialInfo(
                issuerEndpoint = URL(CREDENTIAL_ISSUER),
            )
            mockGetCredentialConfig(
                credentials = listOf(SELECTED_CONFIG_ID),
                credentialConfigurations = issuerInfo.credentialConfigurations
            )
            mockBatchRefreshDataRepository.updateBatchSize(CREDENTIAL_ID, newBatchSize)
        }

        coVerify(exactly = 0) {
            mockDeleteBundleItemsByAmount(any(), any())
            mockGetVerifiableCredentialParams(any(), any(), any())
            mockGenerateProofKeyPairs(any(), any())
            mockFetchCredentialByConfig(any(), any(), any(), any(), any())
            mockHandleBatchCredentialResult(any(), any(), any(), any(), any(), any())
            mockDeleteBundleItems(any())
        }
    }

    @Test
    fun `New batch size is lower, so presentable count is higher than new batch size, so old bundle items are deleted`() = runTest {
        val oldBatchSize: BatchSize = 100
        val newBatchSize: BatchSize = 10
        val presentableCount = 20
        val batchRefreshData = listOf(MockBatchRefreshData.createBatchRefreshData(batchSize = oldBatchSize))

        coEvery { mockVerifiableCredentialWithBatchDataAndAuthenticationRepository.getAll() } returns Ok(batchRefreshData)
        coEvery { mockBundleItemRepository.getCountOfNeverPresented() } returns Ok(
            listOf(
                PresentableBatchItemCount(
                    credentialId = CREDENTIAL_ID,
                    count = presentableCount
                )
            )
        )

        val issuerInfo = IssuerCredentialInfo(
            credentialEndpoint = URL("https://issuer.example/credential"),
            deferredCredentialEndpoint = null,
            nonceEndpoint = null,
            credentialIssuer = URL(CREDENTIAL_ISSUER),
            credentialRequestEncryption = null,
            credentialResponseEncryption = null,
            credentialConfigurations = listOf(mockAnyCredentialConfiguration),
            display = null,
            batchCredentialIssuance = BatchCredentialIssuance(batchSize = newBatchSize),
        )
        val rawAndParsed = RawAndParsedIssuerCredentialInfo(
            rawIssuerCredentialInfo = "{}",
            issuerCredentialInfo = issuerInfo,
        )
        coEvery {
            mockFetchRawAndParsedIssuerCredentialInfo(
                issuerEndpoint = URL(CREDENTIAL_ISSUER),
            )
        } returns Ok(rawAndParsed)
        coEvery {
            mockBatchRefreshDataRepository.updateBatchSize(CREDENTIAL_ID, newBatchSize)
        } returns Ok(1)

        useCase().assertOk()

        coVerify(exactly = 1) {
            mockFetchRawAndParsedIssuerCredentialInfo(
                issuerEndpoint = URL(CREDENTIAL_ISSUER),
            )
            mockGetCredentialConfig(
                credentials = listOf(SELECTED_CONFIG_ID),
                credentialConfigurations = issuerInfo.credentialConfigurations
            )
            mockDeleteBundleItemsByAmount(CREDENTIAL_ID, any())
            mockBatchRefreshDataRepository.updateBatchSize(CREDENTIAL_ID, newBatchSize)
        }

        coVerify(exactly = 0) {
            mockGetVerifiableCredentialParams(any(), any(), any())
            mockGenerateProofKeyPairs(any(), any())
            mockFetchCredentialByConfig(any(), any(), any(), any(), any())
            mockHandleBatchCredentialResult(any(), any(), any(), any(), any(), any())
            mockDeleteBundleItems(any())
        }
    }

    @Test
    fun `Error from getAll is mapped to RefreshBatchCredentialsError`() = runTest {
        coEvery {
            mockVerifiableCredentialWithBatchDataAndAuthenticationRepository.getAll()
        } returns Err(SsiError.Unexpected(Exception("boom")))

        useCase.invoke().assertErrorType(RefreshBatchCredentialsError.Unexpected::class)
    }

    @Test
    fun `Error from getCountOfNeverPresented is mapped to RefreshBatchCredentialsError`() = runTest {
        coEvery { mockVerifiableCredentialWithBatchDataAndAuthenticationRepository.getAll() } returns Ok(emptyList())
        coEvery {
            mockBundleItemRepository.getCountOfNeverPresented()
        } returns Err(SsiError.Unexpected(Exception("fail")))

        useCase.invoke().assertErrorType(RefreshBatchCredentialsError.Unexpected::class)
    }
}
