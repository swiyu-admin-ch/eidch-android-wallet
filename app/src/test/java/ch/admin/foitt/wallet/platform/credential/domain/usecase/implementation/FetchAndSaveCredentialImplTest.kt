package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import android.annotation.SuppressLint
import ch.admin.foitt.openid4vc.domain.model.DeferredCredential
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionKeyPair
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionType
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchAndSaveCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetCredentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleDeferredCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.ValidateIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.CREDENTIAL_ISSUER
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.credentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.multipleConfigCredentialInformation
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.multipleIdentifiersCredentialOffer
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.noConfigCredentialInformation
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.noIdentifierCredentialOffer
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.noMatchingIdentifierCredentialOffer
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.noPayloadEncryptionCredentialInformation
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.oneConfigCredentialInformation
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.oneIdentifierCredentialOffer
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.onlyRequestEncryptionCredentialInformation
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.proofTypeConfigHardwareBinding
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.proofTypeConfigSoftwareBinding
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.requestEncryption
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.responseEncryption
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.validHardwareKeyPair
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.validSoftwareKeyPair
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.verifiableCredentialParamsHardwareBinding
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.verifiableCredentialParamsNoBinding
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.verifiableCredentialParamsSoftwareBinding
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.HolderBindingError
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateProofKeyPairs
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.PayloadEncryptionError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryptionType
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustCheckResult
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import ch.admin.foitt.wallet.util.assertSuccessType
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.Instant
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError as OpenIdCredentialOfferError

class FetchAndSaveCredentialImplTest {
    @MockK
    private lateinit var mockFetchRawAndParsedCredentialInfo: FetchRawAndParsedIssuerCredentialInfo

    @MockK
    private lateinit var mockValidateIssuerCredentialInfo: ValidateIssuerCredentialInfo

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockGetVerifiableCredentialParams: GetVerifiableCredentialParams

    @MockK
    private lateinit var mockGenerateProofKeyPairs: GenerateProofKeyPairs

    @MockK
    private lateinit var mockFetchCredentialByConfig: FetchCredentialByConfig

    @MockK
    private lateinit var mockCredentialOfferRepository: CredentialOfferRepository

    @MockK
    private lateinit var mockVcSdJwtCredential: VcSdJwtCredential

    @MockK
    private lateinit var mockIdentityTrustStatement: IdentityV1TrustStatement

    @MockK
    private lateinit var mockTrustedTrustCheckResult: TrustCheckResult

    @MockK
    private lateinit var mockGetPayloadEncryptionType: GetPayloadEncryptionType

    @MockK
    private lateinit var mockGetCredentialConfig: GetCredentialConfig

    @MockK
    private lateinit var mockHandleCredentialResult: HandleCredentialResult

    @MockK
    private lateinit var mockHandleBatchCredentialResult: HandleBatchCredentialResult

    @MockK
    private lateinit var mockHandleDeferredCredentialResult: HandleDeferredCredentialResult

    private lateinit var useCase: FetchAndSaveCredential

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchAndSaveCredentialImpl(
            fetchRawAndParsedIssuerCredentialInfo = mockFetchRawAndParsedCredentialInfo,
            validateIssuerCredentialInfo = mockValidateIssuerCredentialInfo,
            getPayloadEncryptionType = mockGetPayloadEncryptionType,
            getVerifiableCredentialParams = mockGetVerifiableCredentialParams,
            getCredentialConfig = mockGetCredentialConfig,
            generateProofKeyPairs = mockGenerateProofKeyPairs,
            fetchCredentialByConfig = mockFetchCredentialByConfig,
            handleCredentialResult = mockHandleCredentialResult,
            handleBatchCredentialResult = mockHandleBatchCredentialResult,
            handleDeferredCredentialResult = mockHandleDeferredCredentialResult,
            environmentSetupRepository = mockEnvironmentSetupRepository,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    //region Credential
    @SuppressLint("CheckResult")
    @Test
    fun `Fetching and saving the credential runs specific things`() = runTest {
        setupDefaultMocks()

        val result = useCase(oneIdentifierCredentialOffer)

        val credentialId = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialId.credentialId)

        coVerify {
            mockFetchRawAndParsedCredentialInfo(issuerEndpoint = CREDENTIAL_ISSUER)
            mockValidateIssuerCredentialInfo(oneConfigCredentialInformation)
            mockGetPayloadEncryptionType(
                requestEncryption = requestEncryption,
                responseEncryption = responseEncryption,
            )
            mockGetVerifiableCredentialParams(
                issuerCredentialInfo = oneConfigCredentialInformation,
                credentialConfiguration = credentialConfig,
                credentialOffer = oneIdentifierCredentialOffer
            )
            mockGetVerifiableCredentialParams(
                oneConfigCredentialInformation,
                credentialConfig,
                oneIdentifierCredentialOffer
            )
            mockFetchCredentialByConfig(
                verifiableCredentialParams = verifiableCredentialParamsHardwareBinding,
                bindingKeyPairs = listOf(
                    BindingKeyPair(
                        validHardwareKeyPair.keyPair,
                        validHardwareKeyPair.attestationJwt
                    )
                ),
                payloadEncryptionType = PayloadEncryptionType.Response(
                    requestEncryption = requestEncryption,
                    responseEncryption = responseEncryption,
                    responseEncryptionKeyPair = payloadEncryptionKeyPair,
                ),
            )
            mockHandleCredentialResult(
                issuerUrl = any(),
                anyVerifiedCredential = AnyVerifiedCredential(mockVcSdJwtCredential),
                rawAndParsedCredentialInfo = any(),
                credentialConfig = credentialConfig,
            )
        }
    }

    @Test
    fun `Fetching and saving credential for offer with one identifier and one matching config returns a valid id`() = runTest {
        setupDefaultMocks(
            credentialOffer = oneIdentifierCredentialOffer,
            credentialInfo = oneConfigCredentialInformation,
        )

        val result = useCase(oneIdentifierCredentialOffer)

        val credentialResult = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialResult.credentialId)
    }

    @Test
    fun `Fetching and saving credential for offer with multiple identifiers and multiple matching configs returns a valid id for first identifier`() = runTest {
        setupDefaultMocks(
            credentialOffer = multipleIdentifiersCredentialOffer,
            credentialInfo = multipleConfigCredentialInformation,
        )

        val result = useCase(multipleIdentifiersCredentialOffer)

        val credentialResult = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialResult.credentialId)
    }

    @Test
    fun `Fetching and saving credential for offer with multiple identifiers and one matching config returns a valid id`() = runTest {
        setupDefaultMocks(
            credentialOffer = multipleIdentifiersCredentialOffer,
            credentialInfo = oneConfigCredentialInformation,
        )

        val result = useCase(multipleIdentifiersCredentialOffer)

        val credentialResult = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialResult.credentialId)
    }

    @Test
    fun `Fetching and saving credential for offer with one identifier and multiple matching configs returns a valid id`() = runTest {
        setupDefaultMocks(
            credentialOffer = oneIdentifierCredentialOffer,
            credentialInfo = multipleConfigCredentialInformation,
        )

        val result = useCase(oneIdentifierCredentialOffer)

        val credentialResult = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialResult.credentialId)
    }

    @Test
    fun `Fetching and saving credential for information with only request encryption creates a payload encryption type 'request'`() = runTest {
        setupDefaultMocks(
            credentialInfo = onlyRequestEncryptionCredentialInformation,
        )

        val result = useCase(oneIdentifierCredentialOffer)

        val credentialResult = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialResult.credentialId)

        coVerify {
            mockFetchCredentialByConfig(
                verifiableCredentialParamsHardwareBinding,
                listOf(
                    BindingKeyPair(
                        validHardwareKeyPair.keyPair,
                        validHardwareKeyPair.attestationJwt
                    )
                ),
                PayloadEncryptionType.Request(requestEncryption),
            )
        }
    }

    @Test
    fun `Fetching and saving credential for information with no payload encryption creates a payload encryption type 'none'`() = runTest {
        setupDefaultMocks(
            credentialInfo = noPayloadEncryptionCredentialInformation,
        )

        val result = useCase(oneIdentifierCredentialOffer)

        val credentialResult = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialResult.credentialId)

        coVerify {
            mockFetchCredentialByConfig(
                verifiableCredentialParamsHardwareBinding,
                listOf(
                    BindingKeyPair(
                        validHardwareKeyPair.keyPair,
                        validHardwareKeyPair.attestationJwt
                    )
                ),
                PayloadEncryptionType.None,
            )
        }
    }

    @Test
    fun `Fetching and saving credential for offer with no matching identifier returns an error`() = runTest {
        setupDefaultMocks(
            credentialOffer = noMatchingIdentifierCredentialOffer,
            credentialInfo = multipleConfigCredentialInformation,
        )
        coEvery {
            mockGetCredentialConfig(
                credentials = noMatchingIdentifierCredentialOffer.credentialConfigurationIds,
                credentialConfigurations = multipleConfigCredentialInformation.credentialConfigurations
            )
        } returns Err(CredentialError.UnsupportedCredentialIdentifier)

        val result = useCase(noMatchingIdentifierCredentialOffer)

        result.assertErrorType(CredentialError.UnsupportedCredentialIdentifier::class)
    }

    @Test
    fun `Fetching and saving credential for offer with no identifier returns an error`() = runTest {
        setupDefaultMocks(
            credentialOffer = noIdentifierCredentialOffer,
            credentialInfo = multipleConfigCredentialInformation,
        )
        coEvery {
            mockGetCredentialConfig(
                credentials = noIdentifierCredentialOffer.credentialConfigurationIds,
                credentialConfigurations = multipleConfigCredentialInformation.credentialConfigurations
            )
        } returns Err(CredentialError.UnsupportedCredentialIdentifier)

        val result = useCase(noIdentifierCredentialOffer)

        result.assertErrorType(CredentialError.UnsupportedCredentialIdentifier::class)
    }

    @Test
    fun `Fetching and saving credential for information with no config returns an error`() = runTest {
        setupDefaultMocks(
            credentialOffer = multipleIdentifiersCredentialOffer,
            credentialInfo = noConfigCredentialInformation,
        )
        coEvery {
            mockGetCredentialConfig(
                credentials = multipleIdentifiersCredentialOffer.credentialConfigurationIds,
                credentialConfigurations = noConfigCredentialInformation.credentialConfigurations
            )
        } returns Err(CredentialError.UnsupportedCredentialIdentifier)

        val result = useCase(multipleIdentifiersCredentialOffer)

        result.assertErrorType(CredentialError.UnsupportedCredentialIdentifier::class)
    }

    @Test
    fun `Fetching and saving credential maps errors from Fetching issuer credential information`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockFetchRawAndParsedCredentialInfo(any())
        } returns Err(OpenIdCredentialOfferError.Unexpected(exception))

        val result = useCase(oneIdentifierCredentialOffer)

        val error = result.assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Fetching and saving credential maps errors from validating issuer credential information`() = runTest {
        coEvery {
            mockValidateIssuerCredentialInfo(any())
        } returns false

        useCase(oneIdentifierCredentialOffer).assertErrorType(CredentialError.InvalidIssuerCredentialInfo::class)
    }

    @Test
    fun `Fetching and saving credential maps errors from creating payload encryption type`() = runTest {
        coEvery {
            mockGetPayloadEncryptionType(any(), any())
        } returns Err(PayloadEncryptionError.IncompatibleDeviceProofKeyStorage)

        useCase(oneIdentifierCredentialOffer).assertErrorType(CredentialError.IncompatibleDeviceKeyStorage::class)
    }

    @Test
    fun `Fetching and saving credential maps errors from fetching verifiable credential params`() = runTest {
        coEvery {
            mockGetVerifiableCredentialParams(any(), any(), any())
        } returns Err(OpenIdCredentialOfferError.UnsupportedProofType)

        useCase(oneIdentifierCredentialOffer).assertErrorType(CredentialError.UnsupportedProofType::class)
    }

    @Test
    fun `Fetching and saving credential generates a key pair for hardware bound credentials`() = runTest {
        setupDefaultMocks()

        useCase(oneIdentifierCredentialOffer).assertOk()

        coVerify(exactly = 1) {
            mockGenerateProofKeyPairs(1, proofTypeConfigHardwareBinding)
        }
    }

    @Test
    fun `Fetching and saving credential generates a key pair for software bound credentials`() = runTest {
        setupDefaultMocks(verifiableCredentialParams = verifiableCredentialParamsSoftwareBinding)

        useCase(oneIdentifierCredentialOffer).assertOk()

        coVerify(exactly = 1) {
            mockGenerateProofKeyPairs(1, proofTypeConfigSoftwareBinding)
        }
    }

    @Test
    fun `Fetching and saving credential does not generate a key pair for credentials without binding`() = runTest {
        setupDefaultMocks(verifiableCredentialParams = verifiableCredentialParamsNoBinding)

        val result = useCase(oneIdentifierCredentialOffer)

        result.assertOk()

        coVerify(exactly = 0) {
            mockGenerateProofKeyPairs(1, any())
        }
    }

    @Test
    fun `Fetching and saving credential maps errors from generating the proof key pair`() = runTest {
        coEvery {
            mockGenerateProofKeyPairs(1, any())
        } returns Err(HolderBindingError.IncompatibleDeviceProofKeyStorage)

        useCase(oneIdentifierCredentialOffer).assertErrorType(CredentialError.IncompatibleDeviceKeyStorage::class)
    }

    @Test
    fun `Fetching and saving credential maps errors from Fetching and saving credential by config`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any())
        } returns Err(OpenIdCredentialOfferError.Unexpected(exception))

        val result = useCase(oneIdentifierCredentialOffer)

        val error = result.assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }
    //endregion

    //region DeferredCredential
    @SuppressLint("CheckResult")
    @Test
    fun `Fetching and saving a deferred credential runs specific steps`() = runTest {
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any())
        } returns Ok(deferredCredential)

        val result = useCase(oneIdentifierCredentialOffer)

        val deferredResult = result.assertSuccessType(FetchCredentialResult.DeferredCredential::class)
        assertEquals(DEFERRED_CREDENTIAL_ID, deferredResult.credentialId)

        coVerifyOrder {
            mockFetchRawAndParsedCredentialInfo(issuerEndpoint = CREDENTIAL_ISSUER)
            mockGetVerifiableCredentialParams(
                issuerCredentialInfo = oneConfigCredentialInformation,
                credentialConfiguration = credentialConfig,
                credentialOffer = oneIdentifierCredentialOffer
            )
            mockGenerateProofKeyPairs(1, proofTypeConfigHardwareBinding)
            mockFetchCredentialByConfig(
                verifiableCredentialParamsHardwareBinding,
                listOf(
                    BindingKeyPair(
                        validHardwareKeyPair.keyPair,
                        validHardwareKeyPair.attestationJwt
                    )
                ),
                PayloadEncryptionType.Response(
                    requestEncryption = requestEncryption,
                    responseEncryption = responseEncryption,
                    responseEncryptionKeyPair = payloadEncryptionKeyPair,
                ),
            )
            mockHandleDeferredCredentialResult(
                issuerUrl = CREDENTIAL_ISSUER,
                deferredCredential = deferredCredential,
                rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                    issuerCredentialInfo = oneConfigCredentialInformation,
                    rawIssuerCredentialInfo = ""
                ),
                credentialConfig = credentialConfig,
            )
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Errors from the saveDeferredCredentialOffer() call are mapped`() = runTest {
        val exception = Exception("my exception")

        coEvery {
            mockFetchCredentialByConfig(any(), any(), any())
        } returns Ok(deferredCredential)

        coEvery {
            mockHandleDeferredCredentialResult(
                issuerUrl = CREDENTIAL_ISSUER,
                deferredCredential = deferredCredential,
                rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                    issuerCredentialInfo = oneConfigCredentialInformation,
                    rawIssuerCredentialInfo = ""
                ),
                credentialConfig = credentialConfig,
            )
        } returns Err(CredentialError.Unexpected(exception))

        val error = useCase(oneIdentifierCredentialOffer).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A deferred credential without key binding is accepted`() = runTest {
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any())
        } returns Ok(deferredCredential.copy(keyBindings = null))

        val result = useCase(oneIdentifierCredentialOffer)

        val deferredResult = result.assertSuccessType(FetchCredentialResult.DeferredCredential::class)
        assertEquals(DEFERRED_CREDENTIAL_ID, deferredResult.credentialId)
    }
    //endregion

    @Test
    fun `With disabled payload encryption the corresponding use cases are not called`() = runTest {
        coEvery { mockEnvironmentSetupRepository.payloadEncryptionEnabled } returns false

        useCase(oneIdentifierCredentialOffer).assertSuccessType(FetchCredentialResult.Credential::class)

        coVerify(exactly = 0) {
            mockValidateIssuerCredentialInfo(any())
            mockGetPayloadEncryptionType(any(), any())
        }
    }

    private fun setupDefaultMocks(
        credentialOffer: CredentialOffer = oneIdentifierCredentialOffer,
        credentialInfo: IssuerCredentialInfo = oneConfigCredentialInformation,
        verifiableCredentialParams: VerifiableCredentialParams = verifiableCredentialParamsHardwareBinding,
    ) {
        every {
            mockVcSdJwtCredential.getClaimsForPresentation()
        } returns parseToJsonElement(CREDENTIAL_CLAIMS_FOR_PRESENTATION)
        every { mockVcSdJwtCredential.issuer } returns ISSUER_DID
        every { mockVcSdJwtCredential.vcSchemaId } returns VC_SCHEMA_ID
        coEvery { mockVcSdJwtCredential.keyBinding } returns keyBinding
        coEvery { mockVcSdJwtCredential.payload } returns VC_PAYLOAD
        coEvery { mockVcSdJwtCredential.format } returns VC_FORMAT
        coEvery { mockVcSdJwtCredential.validFromInstant } returns VC_VALID_FROM
        coEvery { mockVcSdJwtCredential.validUntilInstant } returns VC_VALID_UNTIL

        coEvery {
            mockFetchRawAndParsedCredentialInfo(issuerEndpoint = CREDENTIAL_ISSUER)
        } returns Ok(RawAndParsedIssuerCredentialInfo(issuerCredentialInfo = credentialInfo, rawIssuerCredentialInfo = ""))

        coEvery { mockValidateIssuerCredentialInfo(credentialInfo) } returns true

        coEvery { mockEnvironmentSetupRepository.payloadEncryptionEnabled } returns true
        coEvery { mockEnvironmentSetupRepository.batchIssuanceEnabled } returns false

        coEvery {
            mockGetPayloadEncryptionType(null, null)
        } returns Ok(PayloadEncryptionType.None)

        coEvery {
            mockGetPayloadEncryptionType(requestEncryption, null)
        } returns Ok(PayloadEncryptionType.Request(requestEncryption))

        coEvery {
            mockGetPayloadEncryptionType(requestEncryption, responseEncryption)
        } returns Ok(
            PayloadEncryptionType.Response(
                requestEncryption = requestEncryption,
                responseEncryption = responseEncryption,
                responseEncryptionKeyPair = payloadEncryptionKeyPair,
            )
        )

        coEvery {
            mockGetVerifiableCredentialParams(
                credentialInfo,
                credentialConfig,
                credentialOffer
            )
        } returns Ok(verifiableCredentialParams)

        coEvery {
            mockGetPayloadEncryptionType(
                requestEncryption = requestEncryption,
                responseEncryption = null,
            )
        } returns Ok(
            PayloadEncryptionType.Request(
                requestEncryption = requestEncryption,
            )
        )

        coEvery {
            mockGetPayloadEncryptionType(
                requestEncryption = null,
                responseEncryption = null,
            )
        } returns Ok(PayloadEncryptionType.None)

        coEvery {
            mockGetCredentialConfig(
                credentials = credentialOffer.credentialConfigurationIds,
                credentialConfigurations = credentialInfo.credentialConfigurations
            )
        } returns Ok(credentialConfig)

        coEvery {
            mockHandleCredentialResult(
                any(), any(), any(), any()
            )
        } returns Ok(FetchCredentialResult.Credential(CREDENTIAL_ID))

        coEvery {
            mockHandleDeferredCredentialResult(
                any(), any(), any(), any()
            )
        } returns Ok(FetchCredentialResult.DeferredCredential(DEFERRED_CREDENTIAL_ID))

        coEvery { mockGenerateProofKeyPairs(1, proofTypeConfigHardwareBinding) } returns Ok(listOf(validHardwareKeyPair))
        coEvery { mockGenerateProofKeyPairs(1, proofTypeConfigSoftwareBinding) } returns Ok(listOf(validSoftwareKeyPair))

        coEvery {
            mockFetchCredentialByConfig(any(), any(), any())
        } returns Ok(AnyVerifiedCredential(mockVcSdJwtCredential))

        coEvery { mockTrustedTrustCheckResult.actorTrustStatement } returns mockIdentityTrustStatement
        coEvery { mockTrustedTrustCheckResult.actorEnvironment } returns ActorEnvironment.PRODUCTION
        coEvery { mockTrustedTrustCheckResult.vcSchemaTrustStatus } returns VcSchemaTrustStatus.TRUSTED

        coEvery {
            mockCredentialOfferRepository.saveCredentialOffer(
                keyBindings = any(),
                payloads = any(),
                format = any(),
                selectedConfigurationId = any(),
                validFrom = any(),
                validUntil = any(),
                issuer = any(),
                issuerDisplays = any(),
                credentialDisplays = any(),
                clusters = any(),
                rawCredentialData = any(),
                issuerUrl = any(),
            )
        } returns Ok(CREDENTIAL_ID)

        coEvery {
            mockCredentialOfferRepository.saveDeferredCredentialOffer(
                transactionId = any(),
                accessToken = any(),
                endpoint = any(),
                pollInterval = any(),
                keyBindings = any(),
                format = any(),
                issuerDisplays = any(),
                credentialDisplays = any(),
                rawCredentialData = any(),
                selectedConfigurationId = any(),
                issuerUrl = any(),
                refreshToken = any(),
            )
        } returns Ok(DEFERRED_CREDENTIAL_ID)
    }

    private companion object {
        const val CREDENTIAL_ID = 111L
        const val DEFERRED_CREDENTIAL_ID = 222L
        val CREDENTIAL_CLAIMS_FOR_PRESENTATION = """
            {
                "key":"value"
            }
        """.trimIndent()
        const val ISSUER_DID = "issuer did"
        const val VC_SCHEMA_ID = "vcSchemaId"
        const val VC_PAYLOAD = "payload"
        val VC_FORMAT = CredentialFormat.VC_SD_JWT
        val VC_VALID_FROM: Instant = Instant.ofEpochSecond(0)
        val VC_VALID_UNTIL: Instant = Instant.ofEpochSecond(100)

        private val keyBinding = KeyBinding(
            identifier = "keyId",
            algorithm = SigningAlgorithm.ES512,
            bindingType = KeyBindingType.SOFTWARE,
        )

        val deferredCredential = DeferredCredential(
            format = CredentialFormat.VC_SD_JWT,
            keyBindings = listOf(keyBinding),
            transactionId = "transactionId",
            accessToken = "accessToken",
            endpoint = URL("https://example"),
            pollInterval = 1,
            refreshToken = "refreshToken",
        )

        val mockPayloadEncryptionJwsKeyPair = mockk<JWSKeyPair>()
        val payloadEncryptionKeyPair = PayloadEncryptionKeyPair(
            keyPair = mockPayloadEncryptionJwsKeyPair,
            alg = "alg",
            enc = "enc",
            zip = null,
        )
    }
}
