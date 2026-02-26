package ch.admin.foitt.openid4vc.domain.usecase.implementation

import android.annotation.SuppressLint
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.usecase.FetchIssuerConfiguration
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.offerWithPreAuthorizedCode
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.offerWithoutMatchingCredentialIdentifier
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.validIssuerConfig
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.validIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.credentialConfigurationWithHardwareKeyBinding
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.credentialConfigurationWithOtherProofTypeSigningAlgorithms
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.credentialConfigurationWithoutProofTypesSupported
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.proofTypeConfigHardwareBinding
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.proofTypeConfigSoftwareBinding
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.vcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.util.assertErr
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetVerifiableCredentialParamsImplTest {

    @MockK
    private lateinit var mockCredentialOfferRepository: CredentialOfferRepository

    @MockK
    private lateinit var mockFetchIssuerConfiguration: FetchIssuerConfiguration

    private lateinit var useCase: GetVerifiableCredentialParams

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        initDefaultMocks()

        useCase = GetVerifiableCredentialParamsImpl(
            mockCredentialOfferRepository,
            mockFetchIssuerConfiguration
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `valid credential offer with software holder binding returns verifiable credential params`() = runTest {
        val verifiableCredentialParams = useCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration,
            credentialOffer = offerWithPreAuthorizedCode,
            issuerCredentialInfo = validIssuerCredentialInfo,
        ).assertOk()

        assertEquals(proofTypeConfigSoftwareBinding, verifiableCredentialParams.proofTypeConfig)
        assertEquals(validIssuerConfig.tokenEndpoint, verifiableCredentialParams.tokenEndpoint)
        assertEquals(offerWithPreAuthorizedCode.grants, verifiableCredentialParams.grants)
        assertEquals(validIssuerCredentialInfo.credentialIssuer, verifiableCredentialParams.issuerEndpoint)
        assertEquals(validIssuerCredentialInfo.credentialEndpoint, verifiableCredentialParams.credentialEndpoint)
        assertEquals(vcSdJwtCredentialConfiguration, verifiableCredentialParams.credentialConfiguration)
        assertEquals(validIssuerCredentialInfo.deferredCredentialEndpoint, verifiableCredentialParams.deferredCredentialEndpoint)
        assertEquals(validIssuerCredentialInfo.nonceEndpoint, verifiableCredentialParams.nonceEndpoint)

        coVerify(ordering = Ordering.SEQUENCE) {
            mockFetchIssuerConfiguration(any())
            mockCredentialOfferRepository.getIssuerCredentialInfo(any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `valid credential offer with hardware holder binding returns verifiable credential params`() = runTest {
        val verifiableCredentialParams = useCase(
            credentialConfiguration = credentialConfigurationWithHardwareKeyBinding,
            credentialOffer = offerWithPreAuthorizedCode,
            issuerCredentialInfo = validIssuerCredentialInfo,
        ).assertOk()

        assertEquals(proofTypeConfigHardwareBinding, verifiableCredentialParams.proofTypeConfig)
        assertEquals(validIssuerConfig.tokenEndpoint, verifiableCredentialParams.tokenEndpoint)
        assertEquals(offerWithPreAuthorizedCode.grants, verifiableCredentialParams.grants)
        assertEquals(validIssuerCredentialInfo.credentialIssuer, verifiableCredentialParams.issuerEndpoint)
        assertEquals(validIssuerCredentialInfo.credentialEndpoint, verifiableCredentialParams.credentialEndpoint)
        assertEquals(credentialConfigurationWithHardwareKeyBinding, verifiableCredentialParams.credentialConfiguration)
        assertEquals(validIssuerCredentialInfo.deferredCredentialEndpoint, verifiableCredentialParams.deferredCredentialEndpoint)
        assertEquals(validIssuerCredentialInfo.nonceEndpoint, verifiableCredentialParams.nonceEndpoint)

        coVerify(ordering = Ordering.SEQUENCE) {
            mockFetchIssuerConfiguration(any())
            mockCredentialOfferRepository.getIssuerCredentialInfo(any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `valid credential offer with no holder binding returns verifiable credential params`() = runTest {
        val verifiableCredentialParams = useCase(
            credentialConfiguration = credentialConfigurationWithoutProofTypesSupported,
            credentialOffer = offerWithPreAuthorizedCode,
            issuerCredentialInfo = validIssuerCredentialInfo,
        ).assertOk()

        assertEquals(null, verifiableCredentialParams.proofTypeConfig)
        assertEquals(validIssuerConfig.tokenEndpoint, verifiableCredentialParams.tokenEndpoint)
        assertEquals(offerWithPreAuthorizedCode.grants, verifiableCredentialParams.grants)
        assertEquals(validIssuerCredentialInfo.credentialIssuer, verifiableCredentialParams.issuerEndpoint)
        assertEquals(validIssuerCredentialInfo.credentialEndpoint, verifiableCredentialParams.credentialEndpoint)
        assertEquals(credentialConfigurationWithoutProofTypesSupported, verifiableCredentialParams.credentialConfiguration)
        assertEquals(validIssuerCredentialInfo.deferredCredentialEndpoint, verifiableCredentialParams.deferredCredentialEndpoint)
        assertEquals(validIssuerCredentialInfo.nonceEndpoint, verifiableCredentialParams.nonceEndpoint)

        coVerify(ordering = Ordering.SEQUENCE) {
            mockFetchIssuerConfiguration(any())
            mockCredentialOfferRepository.getIssuerCredentialInfo(any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `credential offer with non-matching issuer credential identifier should return an invalid credential offer error, access token not fetched`() =
        runTest {
            useCase(
                credentialConfiguration = vcSdJwtCredentialConfiguration,
                credentialOffer = offerWithoutMatchingCredentialIdentifier,
                issuerCredentialInfo = validIssuerCredentialInfo,
            ).assertErrorType(CredentialOfferError.InvalidCredentialOffer::class)

            coVerify(exactly = 0) {
                mockCredentialOfferRepository.fetchAccessToken(any(), any())
            }
        }

    @SuppressLint("CheckResult")
    @Test
    fun `when the proof type is not supported return an unsupported proof type error, access token not fetched`() = runTest {
        useCase(
            credentialConfiguration = credentialConfigurationWithOtherProofTypeSigningAlgorithms,
            credentialOffer = offerWithPreAuthorizedCode,
            issuerCredentialInfo = validIssuerCredentialInfo,
        ).assertErrorType(CredentialOfferError.UnsupportedProofType::class)

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.fetchAccessToken(any(), any())
        }
    }

    @Test
    fun `when multiple cryptographic binding methods are given use first`() = runTest {
        val methods = listOf(MockIssuerCredentialConfiguration.JWK_BINDING_METHOD, "other")
        useCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration.copy(cryptographicBindingMethodsSupported = methods),
            credentialOffer = offerWithPreAuthorizedCode,
            issuerCredentialInfo = validIssuerCredentialInfo,
        ).assertOk()
    }

    @Test
    fun `return error when proof but no cryptographic binding method is given`() = runTest {
        useCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration.copy(cryptographicBindingMethodsSupported = emptyList()),
            credentialOffer = offerWithPreAuthorizedCode,
            issuerCredentialInfo = validIssuerCredentialInfo,
        ).assertErr()
    }

    @Test
    fun `when null cryptographic binding method is given use did jwk`() = runTest {
        useCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration.copy(cryptographicBindingMethodsSupported = null),
            credentialOffer = offerWithPreAuthorizedCode,
            issuerCredentialInfo = validIssuerCredentialInfo,
        ).assertOk()
    }

    @Test
    fun `when the cryptographic binding method is not supported return an unsupported cryptographic suite error, access token not fetched`() =
        runTest {
            useCase(
                credentialConfiguration = vcSdJwtCredentialConfiguration.copy(cryptographicBindingMethodsSupported = listOf("other")),
                credentialOffer = offerWithPreAuthorizedCode,
                issuerCredentialInfo = validIssuerCredentialInfo,
            ).assertErrorType(CredentialOfferError.UnsupportedCryptographicSuite::class)

            coVerify(exactly = 0) {
                mockCredentialOfferRepository.fetchAccessToken(any(), any())
            }
        }

    @Test
    fun `when fetching issuer configuration fails return error`() = runTest {
        coEvery {
            mockFetchIssuerConfiguration(offerWithPreAuthorizedCode.credentialIssuer)
        } returns Err(CredentialOfferError.NetworkInfoError)

        val result = useCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration.copy(cryptographicBindingMethodsSupported = null),
            credentialOffer = offerWithPreAuthorizedCode,
            issuerCredentialInfo = validIssuerCredentialInfo,
        )

        result.assertErrorType(CredentialOfferError.NetworkInfoError::class)
    }

    @Test
    fun `when fetching issuer info fails return error`() = runTest {
        coEvery {
            mockCredentialOfferRepository.getIssuerCredentialInfo(offerWithPreAuthorizedCode.credentialIssuer)
        } returns Err(CredentialOfferError.NetworkInfoError)

        val result = useCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration.copy(cryptographicBindingMethodsSupported = null),
            credentialOffer = offerWithPreAuthorizedCode,
            issuerCredentialInfo = validIssuerCredentialInfo,
        )

        result.assertErrorType(CredentialOfferError.NetworkInfoError::class)
    }

    private fun initDefaultMocks() {
        coEvery {
            mockCredentialOfferRepository.getIssuerCredentialInfo(offerWithPreAuthorizedCode.credentialIssuer)
        } returns Ok(validIssuerCredentialInfo)

        coEvery {
            mockFetchIssuerConfiguration(offerWithPreAuthorizedCode.credentialIssuer)
        } returns Ok(validIssuerConfig)
    }
}
