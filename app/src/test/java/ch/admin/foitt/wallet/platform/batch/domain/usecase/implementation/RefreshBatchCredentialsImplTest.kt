package ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.BatchCredentialIssuance
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import ch.admin.foitt.wallet.platform.batch.domain.error.BatchRefreshDataRepositoryError
import ch.admin.foitt.wallet.platform.batch.domain.error.RefreshBatchCredentialsError
import ch.admin.foitt.wallet.platform.batch.domain.repository.BatchRefreshDataRepository
import ch.admin.foitt.wallet.platform.batch.domain.usecase.DeleteBundleItemsByAmount
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetCredentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.database.domain.model.BatchRefreshDataEntity
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.PresentableBatchItemCount
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateProofKeyPairs
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryptionType
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteBundleItems
import ch.admin.foitt.wallet.util.assertErrorType
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

class RefreshBatchCredentialsImplTest {

    @MockK
    private lateinit var mockBundleItemRepository: BundleItemRepository

    @MockK
    private lateinit var mockBatchRefreshDataRepository: BatchRefreshDataRepository

    @MockK
    private lateinit var mockCredentialRepository: CredentialRepo

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
    private lateinit var mockGenerateProofKeyPairs: GenerateProofKeyPairs

    @MockK
    private lateinit var mockFetchCredentialByConfig: FetchCredentialByConfig

    @MockK
    private lateinit var mockHandleBatchCredentialResult: HandleBatchCredentialResult

    @MockK
    private lateinit var mockDeleteBundleItems: DeleteBundleItems

    @MockK
    private lateinit var mockAnyCredentialConfiguration: AnyCredentialConfiguration

    private lateinit var useCase: RefreshBatchCredentialsImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = RefreshBatchCredentialsImpl(
            bundleItemRepository = mockBundleItemRepository,
            batchRefreshDataRepository = mockBatchRefreshDataRepository,
            credentialRepository = mockCredentialRepository,
            getCredentialConfig = mockGetCredentialConfig,
            getPayloadEncryptionType = mockGetPayloadEncryptionType,
            fetchRawAndParsedIssuerCredentialInfo = mockFetchRawAndParsedIssuerCredentialInfo,
            getVerifiableCredentialParams = mockGetVerifiableCredentialParams,
            deleteBundleItemsByAmount = mockDeleteBundleItemsByAmount,
            generateProofKeyPairs = mockGenerateProofKeyPairs,
            fetchCredentialByConfig = mockFetchCredentialByConfig,
            handleBatchCredentialResult = mockHandleBatchCredentialResult,
            deleteBundleItems = mockDeleteBundleItems,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `No matching credential for batch refresh data`() = runTest {
        val batchData = listOf(
            BatchRefreshDataEntity(credentialId = CREDENTIAL_ID, batchSize = 10, refreshToken = REFRESH_TOKEN)
        )
        coEvery { mockBatchRefreshDataRepository.getAll() } returns Ok(batchData)
        coEvery { mockBundleItemRepository.getCountOfNeverPresented() } returns Ok(
            listOf(
                PresentableBatchItemCount(
                    credentialId = 999L,
                    count = 5
                )
            )
        )

        useCase().assertOk()

        // Ensure we didn't attempt to fetch credential by id
        coVerify(exactly = 0) { mockCredentialRepository.getById(any()) }
    }

    @Test
    fun `Presentable count above threshold does not trigger refresh`() = runTest {
        val batchSize: BatchSize = 10
        val presentableCount = 3
        val batchData = listOf(
            BatchRefreshDataEntity(credentialId = CREDENTIAL_ID, batchSize = batchSize, refreshToken = REFRESH_TOKEN)
        )
        coEvery { mockBatchRefreshDataRepository.getAll() } returns Ok(batchData)
        coEvery { mockBundleItemRepository.getCountOfNeverPresented() } returns Ok(
            listOf(
                PresentableBatchItemCount(
                    credentialId = CREDENTIAL_ID,
                    count = presentableCount
                )
            )
        )

        useCase().assertOk()

        coVerify(exactly = 0) { mockCredentialRepository.getById(any()) }
    }

    @Test
    fun `Batch refresh without config change`() = runTest {
        val batchSize: BatchSize = 5
        val presentableCount = 1
        val batchRefreshData =
            listOf(BatchRefreshDataEntity(credentialId = CREDENTIAL_ID, batchSize = batchSize, refreshToken = REFRESH_TOKEN))

        coEvery { mockBatchRefreshDataRepository.getAll() } returns Ok(batchRefreshData)
        coEvery { mockBundleItemRepository.getCountOfNeverPresented() } returns Ok(
            listOf(
                PresentableBatchItemCount(
                    credentialId = CREDENTIAL_ID,
                    count = presentableCount
                )
            )
        )

        val credential = Credential(
            id = CREDENTIAL_ID,
            format = CredentialFormat.VC_SD_JWT,
            issuerUrl = URL(CREDENTIAL_ISSUER),
            selectedConfigurationId = SELECTED_CONFIG_ID,
        )
        coEvery { mockCredentialRepository.getById(CREDENTIAL_ID) } returns Ok(credential)

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
            mockGetCredentialConfig(
                credentials = any(),
                credentialConfigurations = any()
            )
        } returns Ok(io.mockk.mockk(relaxed = true))
        coEvery { mockDeleteBundleItemsByAmount(any(), any()) } returns Ok(Unit)
        coEvery {
            mockBatchRefreshDataRepository.saveBatchRefreshData(CREDENTIAL_ID, batchSize, REFRESH_TOKEN)
        } returns Ok(CREDENTIAL_ID)
        coEvery {
            mockGetVerifiableCredentialParams(issuerInfo, any(), any())
        } returns Ok(io.mockk.mockk(relaxed = true))
        coEvery {
            mockGenerateProofKeyPairs(batchSize, any())
        } returns Ok(io.mockk.mockk(relaxed = true))
        coEvery {
            mockGetPayloadEncryptionType(any(), any())
        } returns Ok(io.mockk.mockk(relaxed = true))
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any())
        } returns Ok(AnyVerifiedBatchCredential(REFRESH_TOKEN, io.mockk.mockk(relaxed = true)))
        coEvery {
            mockBundleItemRepository.getAllByCredentialId(CREDENTIAL_ID)
        } returns Ok(io.mockk.mockk(relaxed = true))
        coEvery {
            mockHandleBatchCredentialResult(CREDENTIAL_ID, any(), batchSize, any(), any(), any())
        } returns Ok(FetchCredentialResult.Credential(CREDENTIAL_ID))
        coEvery {
            mockDeleteBundleItems(any())
        } returns Ok(presentableCount)

        // Execute
        useCase().assertOk()

        coVerify(exactly = 1) {
            mockCredentialRepository.getById(CREDENTIAL_ID)
            mockFetchRawAndParsedIssuerCredentialInfo(issuerEndpoint = URL(CREDENTIAL_ISSUER))
            mockGetCredentialConfig(
                credentials = listOf(SELECTED_CONFIG_ID),
                credentialConfigurations = issuerInfo.credentialConfigurations
            )
            mockGetVerifiableCredentialParams(issuerInfo, any(), any())
            mockGenerateProofKeyPairs(any(), any())
            mockFetchCredentialByConfig(any(), any(), any())
            mockHandleBatchCredentialResult(any(), any(), any(), any(), any(), any())
            mockDeleteBundleItems(any())
        }

        coVerify(exactly = 0) {
            mockDeleteBundleItemsByAmount(any(), any())
            mockBatchRefreshDataRepository.saveBatchRefreshData(CREDENTIAL_ID, batchSize, REFRESH_TOKEN)
        }
    }

    @Test
    fun `New batch size is lower, so presentable count is higher than new threshold, so no refresh is needed`() = runTest {
        val oldBatchSize: BatchSize = 5
        val newBatchSize: BatchSize = 3
        val presentableCount = 1
        val batchRefreshData =
            listOf(BatchRefreshDataEntity(credentialId = CREDENTIAL_ID, batchSize = oldBatchSize, refreshToken = REFRESH_TOKEN))

        coEvery { mockBatchRefreshDataRepository.getAll() } returns Ok(batchRefreshData)
        coEvery { mockBundleItemRepository.getCountOfNeverPresented() } returns Ok(
            listOf(
                PresentableBatchItemCount(
                    credentialId = CREDENTIAL_ID,
                    count = presentableCount
                )
            )
        )

        val credential = Credential(
            id = CREDENTIAL_ID,
            format = CredentialFormat.VC_SD_JWT,
            issuerUrl = URL(CREDENTIAL_ISSUER),
            selectedConfigurationId = SELECTED_CONFIG_ID,
        )
        coEvery { mockCredentialRepository.getById(CREDENTIAL_ID) } returns Ok(credential)

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
            mockGetCredentialConfig(
                credentials = any(),
                credentialConfigurations = any()
            )
        } returns Ok(io.mockk.mockk(relaxed = true))
        coEvery { mockDeleteBundleItemsByAmount(any(), any()) } returns Ok(Unit)
        coEvery {
            mockBatchRefreshDataRepository.saveBatchRefreshData(CREDENTIAL_ID, newBatchSize, REFRESH_TOKEN)
        } returns Ok(1L)

        // Execute
        useCase().assertOk()

        coVerify(exactly = 1) {
            mockCredentialRepository.getById(CREDENTIAL_ID)
            mockFetchRawAndParsedIssuerCredentialInfo(
                issuerEndpoint = URL(CREDENTIAL_ISSUER),
            )
            mockGetCredentialConfig(
                credentials = listOf(SELECTED_CONFIG_ID),
                credentialConfigurations = issuerInfo.credentialConfigurations
            )
            mockBatchRefreshDataRepository.saveBatchRefreshData(CREDENTIAL_ID, newBatchSize, REFRESH_TOKEN)
        }

        coVerify(exactly = 0) {
            mockDeleteBundleItemsByAmount(any(), any())
            mockGetVerifiableCredentialParams(any(), any(), any())
            mockGenerateProofKeyPairs(any(), any())
            mockFetchCredentialByConfig(any(), any(), any())
            mockHandleBatchCredentialResult(any(), any(), any(), any(), any(), any())
            mockDeleteBundleItems(any())
        }
    }

    @Test
    fun `New batch size is lower, so presentable count is higher than new batch size, so old bundle items are deleted`() = runTest {
        val oldBatchSize: BatchSize = 100
        val newBatchSize: BatchSize = 10
        val presentableCount = 20
        val batchRefreshData =
            listOf(BatchRefreshDataEntity(credentialId = CREDENTIAL_ID, batchSize = oldBatchSize, refreshToken = REFRESH_TOKEN))

        coEvery { mockBatchRefreshDataRepository.getAll() } returns Ok(batchRefreshData)
        coEvery { mockBundleItemRepository.getCountOfNeverPresented() } returns Ok(
            listOf(
                PresentableBatchItemCount(
                    credentialId = CREDENTIAL_ID,
                    count = presentableCount
                )
            )
        )

        val credential = Credential(
            id = CREDENTIAL_ID,
            format = CredentialFormat.VC_SD_JWT,
            issuerUrl = URL(CREDENTIAL_ISSUER),
            selectedConfigurationId = SELECTED_CONFIG_ID,
        )
        coEvery { mockCredentialRepository.getById(CREDENTIAL_ID) } returns Ok(credential)

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
            mockGetCredentialConfig(
                credentials = any(),
                credentialConfigurations = any()
            )
        } returns Ok(io.mockk.mockk(relaxed = true))
        coEvery { mockDeleteBundleItemsByAmount(any(), any()) } returns Ok(Unit)
        coEvery {
            mockBatchRefreshDataRepository.saveBatchRefreshData(CREDENTIAL_ID, newBatchSize, REFRESH_TOKEN)
        } returns Ok(1L)

        // Execute
        useCase().assertOk()

        coVerify(exactly = 1) {
            mockCredentialRepository.getById(CREDENTIAL_ID)
            mockFetchRawAndParsedIssuerCredentialInfo(
                issuerEndpoint = URL(CREDENTIAL_ISSUER),
            )
            mockGetCredentialConfig(
                credentials = listOf(SELECTED_CONFIG_ID),
                credentialConfigurations = issuerInfo.credentialConfigurations
            )
            mockDeleteBundleItemsByAmount(CREDENTIAL_ID, any())
            mockBatchRefreshDataRepository.saveBatchRefreshData(CREDENTIAL_ID, newBatchSize, REFRESH_TOKEN)
        }

        coVerify(exactly = 0) {
            mockGetVerifiableCredentialParams(any(), any(), any())
            mockGenerateProofKeyPairs(any(), any())
            mockFetchCredentialByConfig(any(), any(), any())
            mockHandleBatchCredentialResult(any(), any(), any(), any(), any(), any())
            mockDeleteBundleItems(any())
        }
    }

    @Test
    fun `Error from getAll is mapped to RefreshBatchCredentialsError`() = runTest {
        coEvery {
            mockBatchRefreshDataRepository.getAll()
        } returns Err(BatchRefreshDataRepositoryError.Unexpected(Exception("boom")))

        useCase.invoke().assertErrorType(RefreshBatchCredentialsError.Unexpected::class)
    }

    @Test
    fun `Error from getCountOfNeverPresented is mapped to RefreshBatchCredentialsError`() = runTest {
        coEvery { mockBatchRefreshDataRepository.getAll() } returns Ok(emptyList())
        coEvery {
            mockBundleItemRepository.getCountOfNeverPresented()
        } returns Err(SsiError.Unexpected(Exception("fail")))

        useCase.invoke().assertErrorType(RefreshBatchCredentialsError.Unexpected::class)
    }

    private companion object {
        const val CREDENTIAL_ID = 42L
        const val REFRESH_TOKEN = "refresh-token"
        const val SELECTED_CONFIG_ID = "config-id"
        const val CREDENTIAL_ISSUER = "https://issuer.example"
    }
}
