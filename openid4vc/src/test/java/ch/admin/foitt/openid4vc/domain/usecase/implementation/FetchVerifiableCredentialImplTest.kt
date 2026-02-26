package ch.admin.foitt.openid4vc.domain.usecase.implementation

import android.annotation.SuppressLint
import ch.admin.foitt.openid4vc.domain.model.CredentialRequestType
import ch.admin.foitt.openid4vc.domain.model.DeferredCredential
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialRequestEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwks
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionKeyPair
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequest
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequestProofsJwt
import ch.admin.foitt.openid4vc.domain.usecase.DeleteKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.FetchVerifiableCredential
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.CREDENTIAL
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.C_NONCE
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.jwtProofs
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.offerWithPreAuthorizedCode
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.offerWithoutPreAuthorizedCode
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.validCredentialResponse
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.validDeferredCredentialResponse
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.validIssuerConfig
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.validIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.validTokenResponse
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.verifiableCredentialParamsHardwareBinding
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.verifiableCredentialParamsSoftwareBinding
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.verifiableCredentialParamsWithoutBinding
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.verifiableDeferredCredentialParamsHardwareBinding
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockKeyPairs.VALID_KEY_PAIR_HARDWARE
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockKeyPairs.VALID_KEY_PAIR_SOFTWARE
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.nimbusds.jose.jwk.Curve.P_256
import com.nimbusds.jose.jwk.ECKey
import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

class FetchVerifiableCredentialImplTest {

    @MockK
    private lateinit var mockCredentialOfferRepository: CredentialOfferRepository

    @MockK
    private lateinit var mockCreateCredentialRequestProofsJwt: CreateCredentialRequestProofsJwt

    @MockK
    private lateinit var mockCreateCredentialRequest: CreateCredentialRequest

    @MockK
    private lateinit var mockDeleteKeyPair: DeleteKeyPair

    @MockK
    private lateinit var mockJsonCredentialRequestType: CredentialRequestType.Json

    @MockK
    private lateinit var mockJwtCredentialRequestType: CredentialRequestType.Jwt

    private lateinit var fetchCredentialUseCase: FetchVerifiableCredential

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        initDefaultMocks()

        fetchCredentialUseCase = FetchVerifiableCredentialImpl(
            credentialOfferRepository = mockCredentialOfferRepository,
            createCredentialRequestProofsJwt = mockCreateCredentialRequestProofsJwt,
            createCredentialRequest = mockCreateCredentialRequest,
            deleteKeyPair = mockDeleteKeyPair,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `when credential has software key binding it returns a VerifiableCredential with a software key binding`() = runTest {
        val result = fetchCredentialUseCase(
            verifiableCredentialParams = verifiableCredentialParamsSoftwareBinding,
            bindingKeyPairs = listOf(BindingKeyPair(VALID_KEY_PAIR_SOFTWARE, null)),
            payloadEncryptionType = noEncryptionType,
        ).assertOk() as VerifiableCredential

        assertEquals(CREDENTIAL, result.credential)
        assertEquals(VALID_KEY_PAIR_SOFTWARE.keyId, result.keyBinding?.identifier)
        assertEquals(VALID_KEY_PAIR_SOFTWARE.algorithm, result.keyBinding?.algorithm)
        assertEquals(KeyBindingType.SOFTWARE, result.keyBinding?.bindingType)
        assertNotNull(result.keyBinding?.publicKey)
        assertNotNull(result.keyBinding?.privateKey)

        verifySuccessCalls(
            keyPair = VALID_KEY_PAIR_SOFTWARE,
            payloadEncryptionType = noEncryptionType,
        )
    }

    @Test
    fun `when credential has software key binding but no nonce endpoint it returns a VerifiableCredential with a software key binding`() = runTest {
        val result = fetchCredentialUseCase(
            verifiableCredentialParams = verifiableCredentialParamsSoftwareBinding.copy(nonceEndpoint = null),
            bindingKeyPairs = listOf(BindingKeyPair(VALID_KEY_PAIR_SOFTWARE, null)),
            payloadEncryptionType = noEncryptionType,
        ).assertOk() as VerifiableCredential

        assertEquals(CREDENTIAL, result.credential)
        assertEquals(VALID_KEY_PAIR_SOFTWARE.keyId, result.keyBinding?.identifier)
        assertEquals(VALID_KEY_PAIR_SOFTWARE.algorithm, result.keyBinding?.algorithm)
        assertEquals(KeyBindingType.SOFTWARE, result.keyBinding?.bindingType)
        assertNotNull(result.keyBinding?.publicKey)
        assertNotNull(result.keyBinding?.privateKey)

        verifySuccessCalls(
            keyPair = VALID_KEY_PAIR_SOFTWARE,
            hasNonceEndpoint = false,
            payloadEncryptionType = noEncryptionType,
        )
    }

    @Test
    fun `when credential has hardware key binding it returns a VerifiableCredential with a hardware key binding`() = runTest {
        val result = fetchCredentialUseCase(
            verifiableCredentialParams = verifiableCredentialParamsHardwareBinding,
            bindingKeyPairs = listOf(BindingKeyPair(VALID_KEY_PAIR_HARDWARE, null)),
            payloadEncryptionType = noEncryptionType,
        ).assertOk() as VerifiableCredential

        assertEquals(CREDENTIAL, result.credential)
        assertEquals(VALID_KEY_PAIR_HARDWARE.keyId, result.keyBinding?.identifier)
        assertEquals(VALID_KEY_PAIR_HARDWARE.algorithm, result.keyBinding?.algorithm)
        assertEquals(KeyBindingType.HARDWARE, result.keyBinding?.bindingType)
        assertNull(result.keyBinding?.publicKey)
        assertNull(result.keyBinding?.privateKey)

        verifySuccessCalls(
            keyPair = VALID_KEY_PAIR_HARDWARE,
            payloadEncryptionType = noEncryptionType,
        )
    }

    @Test
    fun `when credential has no key binding (empty proof type) it returns a VerifiableCredential without key binding`() = runTest {
        val result = fetchCredentialUseCase(
            verifiableCredentialParams = verifiableCredentialParamsWithoutBinding,
            bindingKeyPairs = null,
            payloadEncryptionType = noEncryptionType,
        ).assertOk() as VerifiableCredential

        assertEquals(CREDENTIAL, result.credential)
        assertEquals(null, result.keyBinding)

        verifySuccessCalls(
            keyPair = null,
            payloadEncryptionType = noEncryptionType,
        )
    }

    @Test
    fun `when fetching the token fails return an invalid credential offer error`() = runTest {
        coEvery {
            mockCredentialOfferRepository.fetchAccessToken(any(), any())
        } returns Err(CredentialOfferError.InvalidCredentialOffer)

        fetchCredentialUseCase(
            verifiableCredentialParams = verifiableCredentialParamsSoftwareBinding,
            bindingKeyPairs = listOf(BindingKeyPair(VALID_KEY_PAIR_SOFTWARE, null)),
            payloadEncryptionType = noEncryptionType,
        ).assertErrorType(CredentialOfferError.InvalidCredentialOffer::class)

        coVerify(exactly = 0) {
            mockDeleteKeyPair(VALID_KEY_PAIR_SOFTWARE.keyId)
        }
    }

    @Test
    fun `when fetching the token fails return an invalid credential offer error and delete the key pair for a hardware bound credential`() =
        runTest {
            coEvery {
                mockCredentialOfferRepository.fetchAccessToken(any(), any())
            } returns Err(CredentialOfferError.InvalidCredentialOffer)

            fetchCredentialUseCase(
                verifiableCredentialParams = verifiableCredentialParamsHardwareBinding,
                bindingKeyPairs = listOf(BindingKeyPair(VALID_KEY_PAIR_HARDWARE, null)),
                payloadEncryptionType = noEncryptionType,
            ).assertErrorType(CredentialOfferError.InvalidCredentialOffer::class)

            coVerify(exactly = 1) {
                mockDeleteKeyPair(VALID_KEY_PAIR_HARDWARE.keyId)
            }
        }

    @Test
    fun `credential offer without pre-authorized code should return an unsupported grant type error, token not fetched`() = runTest {
        fetchCredentialUseCase(
            verifiableCredentialParams = verifiableCredentialParamsSoftwareBinding.copy(grants = offerWithoutPreAuthorizedCode.grants),
            bindingKeyPairs = listOf(BindingKeyPair(VALID_KEY_PAIR_SOFTWARE, null)),
            payloadEncryptionType = noEncryptionType,
        ).assertErrorType(CredentialOfferError.UnsupportedGrantType::class)

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.fetchAccessToken(any(), any())
        }
    }

    @Test
    fun `when fetching the nonce fails return an invalid credential offer error`() = runTest {
        coEvery {
            mockCredentialOfferRepository.fetchNonce(any())
        } returns Err(CredentialOfferError.NetworkInfoError)

        fetchCredentialUseCase(
            verifiableCredentialParams = verifiableCredentialParamsSoftwareBinding,
            bindingKeyPairs = listOf(BindingKeyPair(VALID_KEY_PAIR_SOFTWARE, null)),
            payloadEncryptionType = noEncryptionType,
        ).assertErrorType(CredentialOfferError.NetworkInfoError::class)

        coVerify(exactly = 0) {
            mockDeleteKeyPair(VALID_KEY_PAIR_SOFTWARE.keyId)
        }
    }

    @Test
    fun `when fetching the nonce fails return an invalid credential offer error and delete the key pair for a hardware bound credential`() =
        runTest {
            coEvery {
                mockCredentialOfferRepository.fetchNonce(any())
            } returns Err(CredentialOfferError.NetworkInfoError)

            fetchCredentialUseCase(
                verifiableCredentialParams = verifiableCredentialParamsHardwareBinding,
                bindingKeyPairs = listOf(BindingKeyPair(VALID_KEY_PAIR_HARDWARE, null)),
                payloadEncryptionType = noEncryptionType,
            ).assertErrorType(CredentialOfferError.NetworkInfoError::class)

            coVerify(exactly = 1) {
                mockDeleteKeyPair(VALID_KEY_PAIR_HARDWARE.keyId)
            }
        }

    @Test
    fun `when creating the CredentialRequestProof fails return an error`() = runTest {
        coEvery {
            mockCreateCredentialRequestProofsJwt(any(), any(), any())
        } returns Err(CredentialOfferError.UnsupportedCryptographicSuite)

        fetchCredentialUseCase(
            verifiableCredentialParams = verifiableCredentialParamsSoftwareBinding,
            bindingKeyPairs = listOf(BindingKeyPair(VALID_KEY_PAIR_SOFTWARE, null)),
            payloadEncryptionType = noEncryptionType,
        ).assertErrorType(CredentialOfferError.UnsupportedCryptographicSuite::class)

        coVerify(exactly = 0) {
            mockDeleteKeyPair(VALID_KEY_PAIR_SOFTWARE.keyId)
        }
    }

    @Test
    fun `when creating the CredentialRequestProof fails return error and delete the key pair for a hardware bound credential`() = runTest {
        coEvery {
            mockCreateCredentialRequestProofsJwt(any(), any(), any())
        } returns Err(CredentialOfferError.UnsupportedCryptographicSuite)

        fetchCredentialUseCase(
            verifiableCredentialParams = verifiableCredentialParamsHardwareBinding,
            bindingKeyPairs = listOf(BindingKeyPair(VALID_KEY_PAIR_HARDWARE, null)),
            payloadEncryptionType = noEncryptionType,
        ).assertErrorType(CredentialOfferError.UnsupportedCryptographicSuite::class)

        coVerify(exactly = 1) {
            mockDeleteKeyPair(VALID_KEY_PAIR_HARDWARE.keyId)
        }
    }

    @Test
    fun `Fetching a credential with request encryption creates a credential request as jwe`() = runTest {
        val result = fetchCredentialUseCase(
            verifiableCredentialParams = verifiableCredentialParamsSoftwareBinding,
            bindingKeyPairs = listOf(BindingKeyPair(VALID_KEY_PAIR_SOFTWARE, null)),
            payloadEncryptionType = requestEncryptionType,
        ).assertOk() as VerifiableCredential

        assertEquals(CREDENTIAL, result.credential)
        assertEquals(VALID_KEY_PAIR_SOFTWARE.keyId, result.keyBinding?.identifier)
        assertEquals(VALID_KEY_PAIR_SOFTWARE.algorithm, result.keyBinding?.algorithm)
        assertEquals(KeyBindingType.SOFTWARE, result.keyBinding?.bindingType)
        assertNotNull(result.keyBinding?.publicKey)
        assertNotNull(result.keyBinding?.privateKey)

        verifySuccessCalls(
            keyPair = VALID_KEY_PAIR_SOFTWARE,
            payloadEncryptionType = requestEncryptionType,
        )
    }

    @Test
    fun `Fetching a credential with response encryption creates a credential request as jwe (that also contains the wallet public key) and sends it as content type jwt`() = runTest {
        val result = fetchCredentialUseCase(
            verifiableCredentialParams = verifiableCredentialParamsSoftwareBinding,
            bindingKeyPairs = listOf(BindingKeyPair(VALID_KEY_PAIR_SOFTWARE, null)),
            payloadEncryptionType = responseEncryptionType,
        ).assertOk() as VerifiableCredential

        assertEquals(CREDENTIAL, result.credential)
        assertEquals(VALID_KEY_PAIR_SOFTWARE.keyId, result.keyBinding?.identifier)
        assertEquals(VALID_KEY_PAIR_SOFTWARE.algorithm, result.keyBinding?.algorithm)
        assertEquals(KeyBindingType.SOFTWARE, result.keyBinding?.bindingType)
        assertNotNull(result.keyBinding?.publicKey)
        assertNotNull(result.keyBinding?.privateKey)

        verifySuccessCalls(
            keyPair = VALID_KEY_PAIR_SOFTWARE,
            payloadEncryptionType = responseEncryptionType,
        )
    }

    @Test
    fun `when fetching the credential returns a deferred response, successfully return a deferred credential`() = runTest {
        coEvery {
            mockCredentialOfferRepository.fetchCredential(
                issuerEndpoint = validIssuerCredentialInfo.credentialEndpoint,
                tokenResponse = any(),
                credentialRequestType = any(),
                payloadEncryptionType = any(),
            )
        } returns Ok(validDeferredCredentialResponse)

        val result = fetchCredentialUseCase(
            verifiableCredentialParams = verifiableDeferredCredentialParamsHardwareBinding,
            bindingKeyPairs = listOf(BindingKeyPair(VALID_KEY_PAIR_HARDWARE, null)),
            payloadEncryptionType = noEncryptionType,
        ).assertOk() as DeferredCredential

        assertEquals(CredentialFormat.VC_SD_JWT, result.format)
        assertEquals(VALID_KEY_PAIR_HARDWARE.keyId, result.keyBindings?.first()?.identifier)
        assertEquals(VALID_KEY_PAIR_HARDWARE.algorithm, result.keyBindings?.first()?.algorithm)
        assertEquals(validDeferredCredentialResponse.transactionId, result.transactionId)
        assertEquals(verifiableDeferredCredentialParamsHardwareBinding.deferredCredentialEndpoint, result.endpoint)
        assertEquals(validDeferredCredentialResponse.interval, result.pollInterval)
        assertEquals(validTokenResponse.accessToken, result.accessToken)
    }

    @Test
    fun `when fetching the credential fails return error`() = runTest {
        coEvery {
            mockCredentialOfferRepository.fetchCredential(any(), any(), any(), any())
        } returns Err(CredentialOfferError.NetworkInfoError)

        fetchCredentialUseCase(
            verifiableCredentialParams = verifiableCredentialParamsSoftwareBinding,
            bindingKeyPairs = listOf(BindingKeyPair(VALID_KEY_PAIR_SOFTWARE, null)),
            payloadEncryptionType = noEncryptionType,
        ).assertErrorType(CredentialOfferError.NetworkInfoError::class)

        coVerify(exactly = 0) {
            mockDeleteKeyPair(any())
        }
    }

    @Test
    fun `when fetching the credential fails return error and delete the key pair for a hardware bound credential`() = runTest {
        coEvery {
            mockCredentialOfferRepository.fetchCredential(any(), any(), any(), any())
        } returns Err(CredentialOfferError.NetworkInfoError)

        fetchCredentialUseCase(
            verifiableCredentialParams = verifiableCredentialParamsHardwareBinding,
            bindingKeyPairs = listOf(BindingKeyPair(VALID_KEY_PAIR_HARDWARE, null)),
            payloadEncryptionType = noEncryptionType,
        ).assertErrorType(CredentialOfferError.NetworkInfoError::class)

        coVerify(exactly = 1) {
            mockDeleteKeyPair(VALID_KEY_PAIR_HARDWARE.keyId)
        }
    }

    private fun initDefaultMocks() {
        coEvery {
            mockCredentialOfferRepository.fetchAccessToken(validIssuerConfig.tokenEndpoint, any())
        } returns Ok(validTokenResponse)
        coEvery {
            mockCredentialOfferRepository.fetchNonce(validIssuerCredentialInfo.nonceEndpoint!!)
        } returns Ok(C_NONCE)
        coEvery {
            mockCreateCredentialRequestProofsJwt(any(), any(), any())
        } returns Ok(jwtProofs)

        coEvery {
            mockCreateCredentialRequest(noEncryptionType, any())
        } returns Ok(mockJsonCredentialRequestType)

        coEvery {
            mockCreateCredentialRequest(requestEncryptionType, any())
        } returns Ok(mockJwtCredentialRequestType)

        coEvery {
            mockCreateCredentialRequest(responseEncryptionType, any())
        } returns Ok(mockJwtCredentialRequestType)

        coEvery {
            mockCredentialOfferRepository.fetchCredential(
                issuerEndpoint = validIssuerCredentialInfo.credentialEndpoint,
                tokenResponse = any(),
                credentialRequestType = any(),
                payloadEncryptionType = any(),
            )
        } returns Ok(validCredentialResponse)

        coEvery {
            mockDeleteKeyPair(any())
        } returns Ok(Unit)
    }

    @SuppressLint("CheckResult")
    private fun verifySuccessCalls(
        keyPair: JWSKeyPair?,
        hasNonceEndpoint: Boolean = true,
        payloadEncryptionType: PayloadEncryptionType,
    ) {
        coVerify(ordering = Ordering.SEQUENCE) {
            mockCredentialOfferRepository.fetchAccessToken(
                tokenEndpoint = validIssuerConfig.tokenEndpoint,
                preAuthorizedCode = offerWithPreAuthorizedCode.grants.preAuthorizedCode!!.preAuthorizedCode
            )
            keyPair?.let {
                if (hasNonceEndpoint) {
                    mockCredentialOfferRepository.fetchNonce(
                        nonceEndpoint = validIssuerCredentialInfo.nonceEndpoint!!,
                    )
                }
                mockCreateCredentialRequestProofsJwt(
                    keyPairs = listOf(BindingKeyPair(it, null)),
                    issuer = offerWithPreAuthorizedCode.credentialIssuer.toString(),
                    cNonce = if (hasNonceEndpoint) C_NONCE else null
                )
            }

            mockCreateCredentialRequest(
                payloadEncryptionType = payloadEncryptionType,
                credentialType = any(),
            )

            val credentialRequestType = when (payloadEncryptionType) {
                is PayloadEncryptionType.None -> mockJsonCredentialRequestType
                is PayloadEncryptionType.Request,
                is PayloadEncryptionType.Response -> mockJwtCredentialRequestType
            }

            mockCredentialOfferRepository.fetchCredential(
                issuerEndpoint = validIssuerCredentialInfo.credentialEndpoint,
                tokenResponse = validTokenResponse,
                credentialRequestType = credentialRequestType,
                payloadEncryptionType = payloadEncryptionType,
            )
        }

        coVerify(exactly = 0) {
            mockDeleteKeyPair(any())
            if (keyPair == null) {
                mockCredentialOfferRepository.fetchNonce(any())
                mockCreateCredentialRequestProofsJwt(any(), any(), any())
            }
            if (!hasNonceEndpoint) {
                mockCredentialOfferRepository.fetchNonce(any())
            }
        }
    }

    private companion object {
        val issuerKeyPair = createKeyPair()
        val issuerPublicKey: ECKey = ECKey.Builder(P_256, issuerKeyPair.public as ECPublicKey).build()
        val walletKeyPair = createKeyPair()

        val responseEncryption = CredentialResponseEncryption(
            algValuesSupported = listOf("ECDH-ES"),
            encValuesSupported = listOf("A128GCM"),
            zipValuesSupported = listOf("DEF"),
            encryptionRequired = true
        )
        val requestEncryption = CredentialRequestEncryption(
            jwks = Jwks(
                listOf(
                    Jwk(
                        x = issuerPublicKey.x.toString(),
                        y = issuerPublicKey.y.toString(),
                        crv = issuerPublicKey.curve.name,
                        kty = issuerPublicKey.keyType.value,
                        alg = "ECDH-ES"
                    )
                )
            ),
            encValuesSupported = listOf("A128GCM"),
            zipValuesSupported = listOf("DEF"),
            encryptionRequired = true,
        )

        val noEncryptionType = PayloadEncryptionType.None
        val requestEncryptionType = PayloadEncryptionType.Request(
            requestEncryption = requestEncryption
        )
        val responseEncryptionType = PayloadEncryptionType.Response(
            requestEncryption = requestEncryption,
            responseEncryption = responseEncryption,
            responseEncryptionKeyPair = PayloadEncryptionKeyPair(
                keyPair = JWSKeyPair(
                    algorithm = SigningAlgorithm.ES256,
                    keyPair = walletKeyPair,
                    keyId = "walletKeyId",
                    bindingType = KeyBindingType.SOFTWARE,
                ),
                alg = responseEncryption.algValuesSupported.first(),
                enc = responseEncryption.encValuesSupported.first(),
                zip = responseEncryption.zipValuesSupported?.first(),
            )
        )

        fun createKeyPair(): KeyPair {
            val generator = KeyPairGenerator.getInstance("EC")
            val spec = ECGenParameterSpec("secp256r1")
            generator.initialize(spec)
            return generator.generateKeyPair()
        }
    }
}
