package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
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
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FetchRawAndParsedIssuerCredentialInfoImplTest {

    @MockK
    private lateinit var mockCredentialOfferRepository: CredentialOfferRepository

    @MockK
    private lateinit var mockValidateIssuerMetadataJwt: ValidateIssuerMetadataJwt

    @MockK
    private lateinit var mockRawAndParsedIssuerCredentialInfo: RawAndParsedIssuerCredentialInfo

    private lateinit var useCase: FetchRawAndParsedIssuerCredentialInfoImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = FetchRawAndParsedIssuerCredentialInfoImpl(
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
    fun `UseCase should return the raw and parsed info when metadata is unsigned`() = runTest {
        val result = useCase(CREDENTIAL_ISSUER).assertOk()

        assertEquals(result, mockRawAndParsedIssuerCredentialInfo)
    }

    @Test
    fun `UseCase should call the CredentialOffer repository only when metadata is unsigned`() = runTest {
        useCase(CREDENTIAL_ISSUER)

        coVerify(exactly = 1) {
            mockCredentialOfferRepository.fetchRawAndParsedIssuerCredentialInformation(CREDENTIAL_ISSUER)
        }
        coVerify(exactly = 0) {
            mockValidateIssuerMetadataJwt(any(), any(), any())
        }
    }

    @Test
    fun `UseCase should return the raw and parsed info when metadata is signed`() = runTest {
        coEvery {
            mockRawAndParsedIssuerCredentialInfo.rawIssuerCredentialInfo
        } returns TEST_JWT.rawJwt

        val result = useCase(CREDENTIAL_ISSUER).assertOk()

        assertEquals(result, mockRawAndParsedIssuerCredentialInfo)
        coVerify(exactly = 1) {
            mockCredentialOfferRepository.fetchRawAndParsedIssuerCredentialInformation(CREDENTIAL_ISSUER)
            mockValidateIssuerMetadataJwt(CREDENTIAL_ISSUER.toString(), any(), any())
        }
    }

    @Test
    fun `UseCase should return an error when the CredentialOffer repository returns an error`() = runTest {
        coEvery {
            mockCredentialOfferRepository.fetchRawAndParsedIssuerCredentialInformation(any())
        } returns Err(CredentialOfferError.NetworkInfoError)

        val result = useCase(CREDENTIAL_ISSUER)

        result.assertErrorType(CredentialOfferError.NetworkInfoError::class)
    }

    @Test
    fun `UseCase should return an error when the jwt validation returns an error`() = runTest {
        coEvery {
            mockRawAndParsedIssuerCredentialInfo.rawIssuerCredentialInfo
        } returns TEST_JWT.rawJwt
        coEvery {
            mockValidateIssuerMetadataJwt(any(), any(), any())
        } returns Err(CredentialOfferError.InvalidSignedMetadata("test"))

        val result = useCase(CREDENTIAL_ISSUER)

        result.assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    private fun success() {
        coEvery {
            mockRawAndParsedIssuerCredentialInfo.rawIssuerCredentialInfo
        } returns MOCK_RAW_INFO
        coEvery {
            mockCredentialOfferRepository.fetchRawAndParsedIssuerCredentialInformation(any())
        } returns Ok(mockRawAndParsedIssuerCredentialInfo)
        coEvery {
            mockValidateIssuerMetadataJwt(credentialIssuerIdentifier = CREDENTIAL_ISSUER.toString(), jwt = any(), any())
        } returns Ok(Unit)
    }

    private companion object {
        const val MOCK_RAW_INFO = "rawInfo"
    }
}
