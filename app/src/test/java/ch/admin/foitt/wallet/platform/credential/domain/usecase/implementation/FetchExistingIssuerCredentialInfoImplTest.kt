package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchExistingIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class FetchExistingIssuerCredentialInfoImplTest {

    @MockK
    private lateinit var mockCredentialRepository: CredentialRepo

    @MockK
    private lateinit var mockFetchRawAndParsedIssuerCredentialInfo: FetchRawAndParsedIssuerCredentialInfo

    @MockK
    private lateinit var mockRawAndParsedIssuerCredentialInfo: RawAndParsedIssuerCredentialInfo

    @MockK
    private lateinit var mockCredential: Credential

    private lateinit var useCase: FetchExistingIssuerCredentialInfo

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchExistingIssuerCredentialInfoImpl(
            credentialRepository = mockCredentialRepository,
            fetchRawAndParsedIssuerCredentialInfo = mockFetchRawAndParsedIssuerCredentialInfo,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun setupDefaultMocks() {
        every { mockCredential.issuerUrl } returns CREDENTIAL_ISSUER_URL

        coEvery {
            mockCredentialRepository.getById(CREDENTIAL_ID)
        } returns Ok(mockCredential)

        coEvery {
            mockFetchRawAndParsedIssuerCredentialInfo(CREDENTIAL_ISSUER_URL)
        } returns Ok(mockRawAndParsedIssuerCredentialInfo)
    }

    @Test
    fun `A successful fetch run specifics steps`() = runTest {
        val result = useCase(CREDENTIAL_ID).assertOk()

        assertEquals(mockRawAndParsedIssuerCredentialInfo, result)

        coVerifyOrder {
            mockCredentialRepository.getById(CREDENTIAL_ID)
            mockFetchRawAndParsedIssuerCredentialInfo(CREDENTIAL_ISSUER_URL)
        }
    }

    @Test
    fun `A CredentialRepository error is mapped`() = runTest {
        val exception = Exception("my exception")
        coEvery {
            mockCredentialRepository.getById(any())
        } returns Err(SsiError.Unexpected(exception))

        val error = useCase(credentialId = CREDENTIAL_ID).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `A IssuerCredentialInfo fetching error is mapped`() = runTest {
        val exception = Exception("Json error")
        coEvery {
            mockFetchRawAndParsedIssuerCredentialInfo(issuerEndpoint = any())
        } returns Err(CredentialOfferError.Unexpected(exception))

        val error = useCase(credentialId = CREDENTIAL_ID).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    private companion object {
        const val CREDENTIAL_ID = 1L
        val CREDENTIAL_ISSUER_URL = URL("https://issuer.example.com")
    }
}
