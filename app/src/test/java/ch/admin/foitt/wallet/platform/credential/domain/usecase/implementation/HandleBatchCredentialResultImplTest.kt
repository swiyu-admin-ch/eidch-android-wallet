package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.batch.domain.error.BatchRefreshDataRepositoryError
import ch.admin.foitt.wallet.platform.batch.domain.repository.BatchRefreshDataRepository
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveVcSdJwtCredentials
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.credentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.oneConfigCredentialInformation
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertSuccessType
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class HandleBatchCredentialResultImplTest {

    @MockK
    private lateinit var mockVcSdJwtCredential: VcSdJwtCredential

    @MockK
    private lateinit var mockSaveVcSdJwtCredentials: SaveVcSdJwtCredentials

    @MockK
    private lateinit var mockBatchRefreshDataRepository: BatchRefreshDataRepository

    private lateinit var useCase: HandleBatchCredentialResultImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = HandleBatchCredentialResultImpl(
            saveVcSdJwtCredentials = mockSaveVcSdJwtCredentials,
            batchRefreshDataRepository = mockBatchRefreshDataRepository,
        )
    }

    @Test
    fun `Saving batch credential maps errors from SaveVcSdJwtCredentials`() = runTest {
        coEvery {
            mockSaveVcSdJwtCredentials(
                credentialId = any(),
                issuerUrl = any(),
                vcSdJwtCredentials = any(),
                rawAndParsedCredentialInfo = any(),
                credentialConfig = any(),
            )
        } returns Err(CredentialError.InvalidCredentialOffer)

        useCase(
            credentialId = CREDENTIAL_ID,
            issuerUrl = ISSUER_URL,
            batchSize = BATCH_SIZE,
            anyVerifiedBatchCredential = AnyVerifiedBatchCredential(
                refreshToken = "token",
                vcSdJwtCredentials = listOf(mockVcSdJwtCredential)
            ),
            rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = oneConfigCredentialInformation,
                rawIssuerCredentialInfo = "",
            ),
            credentialConfig = credentialConfig,
        ).assertErrorType(CredentialError.InvalidCredentialOffer::class)
    }

    @Test
    fun `Saving a batch credential with refreshToken runs specific steps`() = runTest {
        coEvery {
            mockSaveVcSdJwtCredentials(
                credentialId = any(),
                issuerUrl = any(),
                vcSdJwtCredentials = any(),
                rawAndParsedCredentialInfo = any(),
                credentialConfig = any(),
            )
        } returns Ok(CREDENTIAL_ID)

        coEvery {
            mockBatchRefreshDataRepository.saveBatchRefreshData(
                credentialId = any(),
                batchSize = any(),
                refreshToken = any(),
            )
        } returns Ok(1L)

        val result = useCase(
            issuerUrl = ISSUER_URL,
            batchSize = BATCH_SIZE,
            anyVerifiedBatchCredential = AnyVerifiedBatchCredential(
                refreshToken = REFRESH_TOKEN,
                vcSdJwtCredentials = listOf(mockVcSdJwtCredential)
            ),
            rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = oneConfigCredentialInformation,
                rawIssuerCredentialInfo = "",
            ),
            credentialConfig = credentialConfig,
        )

        val credentialResult = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialResult.credentialId)

        coVerifyOrder {
            mockSaveVcSdJwtCredentials(
                issuerUrl = ISSUER_URL,
                vcSdJwtCredentials = listOf(mockVcSdJwtCredential),
                rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                    issuerCredentialInfo = oneConfigCredentialInformation,
                    rawIssuerCredentialInfo = "",
                ),
                credentialConfig = credentialConfig,
            )
            mockBatchRefreshDataRepository.saveBatchRefreshData(
                credentialId = CREDENTIAL_ID,
                batchSize = BATCH_SIZE,
                refreshToken = REFRESH_TOKEN,
            )
        }
    }

    @Test
    fun `Saving a batch credential without refreshToken does not persist refresh data`() = runTest {
        coEvery {
            mockSaveVcSdJwtCredentials(
                credentialId = any(),
                issuerUrl = any(),
                vcSdJwtCredentials = any(),
                rawAndParsedCredentialInfo = any(),
                credentialConfig = any(),
            )
        } returns Ok(CREDENTIAL_ID)

        val result = useCase(
            issuerUrl = ISSUER_URL,
            batchSize = BATCH_SIZE,
            anyVerifiedBatchCredential = AnyVerifiedBatchCredential(
                refreshToken = null,
                vcSdJwtCredentials = listOf(mockVcSdJwtCredential)
            ),
            rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = oneConfigCredentialInformation,
                rawIssuerCredentialInfo = "",
            ),
            credentialConfig = credentialConfig,
        )

        val credentialResult = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialResult.credentialId)

        coVerify(exactly = 0) {
            mockBatchRefreshDataRepository.saveBatchRefreshData(any(), any(), any())
        }
    }

    @Test
    fun `Errors from the saveBatchRefreshData() call are mapped`() = runTest {
        coEvery {
            mockSaveVcSdJwtCredentials(
                credentialId = any(),
                issuerUrl = any(),
                vcSdJwtCredentials = any(),
                rawAndParsedCredentialInfo = any(),
                credentialConfig = any(),
            )
        } returns Ok(CREDENTIAL_ID)

        val exception = Exception("my exception")
        coEvery {
            mockBatchRefreshDataRepository.saveBatchRefreshData(
                credentialId = any(),
                batchSize = any(),
                refreshToken = any(),
            )
        } returns Err(BatchRefreshDataRepositoryError.Unexpected(exception))

        val error = useCase(
            issuerUrl = ISSUER_URL,
            batchSize = BATCH_SIZE,
            anyVerifiedBatchCredential = AnyVerifiedBatchCredential(
                refreshToken = REFRESH_TOKEN,
                vcSdJwtCredentials = listOf(mockVcSdJwtCredential)
            ),
            rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = oneConfigCredentialInformation,
                rawIssuerCredentialInfo = "",
            ),
            credentialConfig = credentialConfig,
        ).assertErrorType(CredentialError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    companion object {
        private const val CREDENTIAL_ID = 1337L
        private const val BATCH_SIZE: BatchSize = 10
        private const val REFRESH_TOKEN = "refresh-token"
        private val ISSUER_URL = URL("https://issuer.example")
    }
}
