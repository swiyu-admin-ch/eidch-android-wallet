package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerConfigurationResponse
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.usecase.ValidateIssuerMetadataJwt
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.CREDENTIAL_ISSUER
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.TEST_JWT
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FetchIssuerConfigurationImplTest {

    @MockK
    private lateinit var mockCredentialOfferRepository: CredentialOfferRepository

    @MockK
    private lateinit var mockValidateIssuerMetadataJwt: ValidateIssuerMetadataJwt

    @MockK
    private lateinit var mockPlainIssuerConfigurationResponse: IssuerConfigurationResponse.Plain

    @MockK
    private lateinit var mockSignedIssuerConfigurationResponse: IssuerConfigurationResponse.Signed

    @MockK
    private lateinit var mockIssuerConfiguration: IssuerConfiguration

    private lateinit var useCase: FetchIssuerConfigurationImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = FetchIssuerConfigurationImpl(
            credentialOfferRepository = mockCredentialOfferRepository,
            validateIssuerMetadataJwt = mockValidateIssuerMetadataJwt,
        )
        success()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `UseCase should return the issuer configuration when unsigned`() = runTest {
        val result = useCase(CREDENTIAL_ISSUER).assertOk()

        assertEquals(result, mockIssuerConfiguration)
    }

    @Test
    fun `UseCase should call the CredentialOffer repository only`() = runTest {
        useCase(CREDENTIAL_ISSUER)

        coVerify(exactly = 1) {
            mockCredentialOfferRepository.fetchIssuerConfiguration(CREDENTIAL_ISSUER)
        }
        coVerify(exactly = 0) {
            mockValidateIssuerMetadataJwt(any(), any(), any())
        }
    }

    @Test
    fun `UseCase should return the issuer configuration when signed`() = runTest {
        success(mockSignedIssuerConfigurationResponse)

        val result = useCase(CREDENTIAL_ISSUER).assertOk()

        assertEquals(result, mockIssuerConfiguration)
        coVerify(exactly = 1) {
            mockValidateIssuerMetadataJwt(CREDENTIAL_ISSUER.toString(), any(), any())
        }
    }

    @Test
    fun `UseCase should return an error when the CredentialOffer repository returns an error`() = runTest {
        coEvery {
            mockCredentialOfferRepository.fetchIssuerConfiguration(any())
        } returns Err(CredentialOfferError.NetworkInfoError)

        val result = useCase(CREDENTIAL_ISSUER)

        result.assertErrorType(CredentialOfferError.NetworkInfoError::class)
    }

    @Test
    fun `UseCase should return an error when the jwt validation returns an error`() = runTest {
        success(mockSignedIssuerConfigurationResponse)
        coEvery {
            mockValidateIssuerMetadataJwt(any(), any(), any())
        } returns Err(CredentialOfferError.InvalidSignedMetadata("test"))

        val result = useCase(issuerEndpoint = CREDENTIAL_ISSUER)

        result.assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    private fun success(response: IssuerConfigurationResponse = mockPlainIssuerConfigurationResponse) {
        every { mockPlainIssuerConfigurationResponse.config } returns mockIssuerConfiguration
        every { mockSignedIssuerConfigurationResponse.config } returns mockIssuerConfiguration
        every { mockSignedIssuerConfigurationResponse.jwt } returns TEST_JWT
        coEvery {
            mockCredentialOfferRepository.fetchIssuerConfiguration(any())
        } returns Ok(response)
        coEvery {
            mockValidateIssuerMetadataJwt(credentialIssuerIdentifier = CREDENTIAL_ISSUER.toString(), jwt = any(), any())
        } returns Ok(Unit)
    }
}
