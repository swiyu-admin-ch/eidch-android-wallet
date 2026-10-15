package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import android.annotation.SuppressLint
import ch.admin.foitt.openid4vc.domain.model.DeferredCredential
import ch.admin.foitt.openid4vc.domain.model.GenerateDPoPKeyPairError
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.BatchCredentialIssuance
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialRequestEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.ProofTypeConfig
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryption
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.GetSignedMetadataDid
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.EvaluateBatchSize
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAllAnyCredentialsByCredentialId
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetCredentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshCredential
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.CredentialRefreshDataError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.GetBindingKeyPairError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.usecase.GetBindingKeyPair
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationWithDpopBinding
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithAuthentication
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.HolderBindingError
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateDPoPKeyPair
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateProofKeyPairs
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.PayloadEncryptionError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryption
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteBundleItems
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteKeyStoreEntry
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityTrustStatement
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.SwissProfile
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetSwissProfileStatus
import ch.admin.foitt.wallet.util.assertErr
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.Instant

@SuppressLint("CheckResult")
class RefreshCredentialImplTest {
    //region mock usecases
    @MockK
    private lateinit var mockBundleItemRepository: BundleItemRepository

    @MockK
    private lateinit var mockCredentialRefreshDataRepository: CredentialRefreshDataRepository

    @MockK
    private lateinit var mockGetCredentialConfig: GetCredentialConfig

    @MockK
    private lateinit var mockGetPayloadEncryption: GetPayloadEncryption

    @MockK
    private lateinit var mockFetchRawAndParsedIssuerCredentialInfo: FetchRawAndParsedIssuerCredentialInfo

    @MockK
    private lateinit var mockGetVerifiableCredentialParams: GetVerifiableCredentialParams

    @MockK
    private lateinit var mockEvaluateBatchSize: EvaluateBatchSize

    @MockK
    private lateinit var mockGetSignedMetadataDid: GetSignedMetadataDid

    @MockK
    private lateinit var mockGenerateProofKeyPairs: GenerateProofKeyPairs

    @MockK
    private lateinit var mockProcessIdentityTrustStatement: ProcessIdentityTrustStatement

    @MockK
    private lateinit var mockFetchCredentialByConfig: FetchCredentialByConfig

    @MockK
    private lateinit var mockGetBindingKeyPair: GetBindingKeyPair

    @MockK
    private lateinit var mockHandleBatchCredentialResult: HandleBatchCredentialResult

    @MockK
    private lateinit var mockHandleCredentialResult: HandleCredentialResult

    @MockK
    private lateinit var mockDeleteBundleItems: DeleteBundleItems

    @MockK
    private lateinit var mockGenerateDPoPKeyPair: GenerateDPoPKeyPair

    @MockK(relaxed = true)
    private lateinit var mockDeleteKeyStoreEntry: DeleteKeyStoreEntry

    @MockK(relaxed = true)
    private lateinit var mockVerifiableCredentialRepository: VerifiableCredentialRepository

    //endregion
    //region mock values
    private val credentialId = 1L
    private val credentialIssuerDid = "issuerDid"
    private val credentialIssuer = URL("https://issuer.example")
    private val credentialIssuerEndpoint = URL("https://issuer.example/credential")

    private val nonceEndpoint = URL("https://issuer.example/nonce")

    private val proofKeyId = "proof-key-id"
    private val dpopKeyId = "dpop-key-id"

    @MockK
    private lateinit var mockTrustStatementJwt: Jwt

    @MockK
    private lateinit var mockIssuerCredentialInfoJwt: Jwt

    @MockK
    private lateinit var mockTrustStatementV2: IdentityV2TrustStatement
    private lateinit var issuerInfo: IssuerCredentialInfo

    private val batchSize = 15
    private val batchCredentialIssuance = BatchCredentialIssuance(batchSize = batchSize)

    @MockK
    private lateinit var mockCredentialWithAuthenticationAndDpopBinding: CredentialAuthenticationWithDpopBinding

    @MockK
    private lateinit var mockCredentialWithAuthenticationEntity: VerifiableCredentialWithAuthentication

    @MockK
    private lateinit var mockCredentialEntity: Credential

    @MockK
    private lateinit var mockVerifiableCredentialEntity: VerifiableCredentialEntity

    @MockK
    private lateinit var mockAnyVerifiedCredential: AnyVerifiedCredential

    @MockK
    private lateinit var mockAnyVerifiedBatchCredential: AnyVerifiedBatchCredential

    @MockK
    private lateinit var mockAnyDeferredCredential: DeferredCredential

    @MockK
    private lateinit var mockGetAllAnyCredentialsByCredentialId: GetAllAnyCredentialsByCredentialId

    @MockK
    private lateinit var mockGetSwissProfileStatus: GetSwissProfileStatus

    @MockK
    private lateinit var mockVcSdJwtCredential: VcSdJwtCredential

    @MockK
    private lateinit var mockCurrentAnyCredential: AnyCredential

    @MockK
    private lateinit var mockAnyCredentialConfiguration: AnyCredentialConfiguration
    private val refreshToken = "refreshToken"
    private val selectedConfigurationId = "selectedConfigurationId"
    private val newProfileVersion = "swiss-profile-vc:2.0.0"
    private val currentProfileVersion = "swiss-profile-vc:1.0.0"

    @MockK
    private lateinit var mockVerifiableCredentialParams: VerifiableCredentialParams

    @MockK
    private lateinit var mockProofTypeConfig: ProofTypeConfig

    private lateinit var rawAndParsed: RawAndParsedIssuerCredentialInfo

    @MockK
    private lateinit var mockCredentialRequestEncryption: CredentialRequestEncryption

    @MockK
    private lateinit var mockCredentialResponseEncryption: CredentialResponseEncryption

    @MockK
    private lateinit var mockBindingKeyPair: BindingKeyPair

    @MockK
    private lateinit var mockDpopKeyPair: BindingKeyPair

    @MockK
    private lateinit var mockPayloadEncryption: PayloadEncryption
    //endregion

    private lateinit var useCase: RefreshCredential

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = RefreshCredentialImpl(
            bundleItemRepository = mockBundleItemRepository,
            credentialRefreshDataRepository = mockCredentialRefreshDataRepository,
            getCredentialConfig = mockGetCredentialConfig,
            getPayloadEncryption = mockGetPayloadEncryption,
            fetchRawAndParsedIssuerCredentialInfo = mockFetchRawAndParsedIssuerCredentialInfo,
            getVerifiableCredentialParams = mockGetVerifiableCredentialParams,
            evaluateBatchSize = mockEvaluateBatchSize,
            getSignedMetadataDid = mockGetSignedMetadataDid,
            generateProofKeyPairs = mockGenerateProofKeyPairs,
            processIdentityTrustStatement = mockProcessIdentityTrustStatement,
            fetchCredentialByConfig = mockFetchCredentialByConfig,
            getBindingKeyPair = mockGetBindingKeyPair,
            handleCredentialResult = mockHandleCredentialResult,
            handleBatchCredentialResult = mockHandleBatchCredentialResult,
            deleteBundleItems = mockDeleteBundleItems,
            generateDPoPKeyPair = mockGenerateDPoPKeyPair,
            deleteKeyStoreEntry = mockDeleteKeyStoreEntry,
            verifiableCredentialRepository = mockVerifiableCredentialRepository,
            getAllAnyCredentialsByCredentialId = mockGetAllAnyCredentialsByCredentialId,
            getSwissProfileStatus = mockGetSwissProfileStatus,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A refresh returning a single credential succeeds and runs specific steps`() = runTest {
        useCase(
            credentialId = credentialId
        ).assertOk()

        coVerify(exactly = 1) {
            mockCredentialRefreshDataRepository.getCredentialRefreshDataById(credentialId)
            mockFetchRawAndParsedIssuerCredentialInfo(
                issuerEndpoint = credentialIssuer,
                forceRefresh = true,
            )
            mockGetCredentialConfig(
                credentials = listOf(selectedConfigurationId),
                credentialConfigurations = listOf(mockAnyCredentialConfiguration),
            )
            mockGetVerifiableCredentialParams(
                issuerCredentialInfo = issuerInfo,
                credentialConfiguration = mockAnyCredentialConfiguration,
                credentialOffer = any(),
            )

            mockGetSignedMetadataDid(mockIssuerCredentialInfoJwt)

            mockGenerateProofKeyPairs(
                amount = 1,
                proofTypeConfig = mockProofTypeConfig,
                actorDid = credentialIssuerDid
            )
            mockGetPayloadEncryption(
                requestEncryption = mockCredentialRequestEncryption,
                responseEncryption = mockCredentialResponseEncryption,
            )

            mockGetBindingKeyPair(
                authentication = mockCredentialWithAuthenticationAndDpopBinding,
            )

            mockProcessIdentityTrustStatement(mockTrustStatementJwt, credentialIssuerDid)

            mockFetchCredentialByConfig(
                verifiableCredentialParams = mockVerifiableCredentialParams,
                bindingKeyPairs = listOf(),
                payloadEncryption = mockPayloadEncryption,
                dpopKeyPair = mockBindingKeyPair,
            )

            mockHandleCredentialResult(
                credentialId = credentialId,
                issuerUrl = credentialIssuer,
                anyVerifiedCredential = mockAnyVerifiedCredential,
                identityTrustStatement = mockTrustStatementV2,
                rawAndParsedCredentialInfo = rawAndParsed,
                credentialConfig = mockAnyCredentialConfiguration,
            )
        }
        coVerify(exactly = 0) {
            mockEvaluateBatchSize(issuerInfo)
        }

        coVerify(exactly = 1) {
            mockBundleItemRepository.getAllByCredentialId(credentialId)
        }
    }

    @Test
    fun `A refresh for a batch credential succeeds and runs specific steps`() = runTest {
        issuerInfo = issuerInfo.copy(batchCredentialIssuance = batchCredentialIssuance)
        rawAndParsed = rawAndParsed.copy(issuerCredentialInfo = issuerInfo)
        coEvery {
            mockFetchRawAndParsedIssuerCredentialInfo(issuerEndpoint = any(), forceRefresh = any())
        } returns Ok(rawAndParsed)

        coEvery {
            mockVerifiableCredentialParams.isBatch
        } returns true

        coEvery {
            mockFetchCredentialByConfig(
                verifiableCredentialParams = mockVerifiableCredentialParams,
                bindingKeyPairs = listOf(),
                payloadEncryption = mockPayloadEncryption,
                dpopKeyPair = mockBindingKeyPair,
            )
        } returns Ok(mockAnyVerifiedBatchCredential)

        useCase(
            credentialId = credentialId
        ).assertOk()

        coVerify(exactly = 1) {
            mockEvaluateBatchSize(issuerInfo)
            mockGenerateProofKeyPairs(
                amount = batchSize,
                proofTypeConfig = mockProofTypeConfig,
                actorDid = credentialIssuerDid,
            )
            mockHandleBatchCredentialResult(
                credentialId = credentialId,
                issuerUrl = credentialIssuer,
                batchSize = batchSize,
                anyVerifiedBatchCredential = mockAnyVerifiedBatchCredential,
                identityTrustStatement = mockTrustStatementV2,
                rawAndParsedCredentialInfo = rawAndParsed,
                credentialConfig = mockAnyCredentialConfiguration,
            )
        }
    }

    @Test
    fun `A refresh returning a deferred credential fails`() = runTest {
        coEvery {
            mockFetchCredentialByConfig(
                verifiableCredentialParams = any(),
                bindingKeyPairs = any(),
                payloadEncryption = any(),
                dpopKeyPair = any(),
            )
        } returns Ok(mockAnyDeferredCredential)

        useCase(credentialId = credentialId).assertErr()
    }

    @Test
    fun `A refresh without authentication data is skipped`() = runTest {
        coEvery {
            mockCredentialWithAuthenticationEntity.authentication
        } returns null

        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 0) {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        }
    }

    @Test
    fun `A refresh without a refresh token is skipped`() = runTest {
        coEvery {
            mockCredentialWithAuthenticationAndDpopBinding.refreshToken
        } returns null

        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 0) {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        }
    }

    @Test
    fun `A refresh of an unaccepted credential is skipped`() = runTest {
        coEvery {
            mockVerifiableCredentialEntity.progressionState
        } returns VerifiableProgressionState.UNACCEPTED

        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 0) {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        }
    }

    @Test
    fun `A refresh of a revoked credential is a skipped`() = runTest {
        val revokedItem = mockk<BundleItemEntity> {
            every { status } returns CredentialStatus.REVOKED
        }
        coEvery {
            mockBundleItemRepository.getAllByCredentialId(credentialId)
        } returns Ok(listOf(revokedItem))

        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 0) {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        }
    }

    @Test
    fun `Generated hardware keys are cleaned up when fetching the credential fails`() = runTest {
        coEvery {
            mockGenerateProofKeyPairs(any(), any(), credentialIssuerDid)
        } returns Ok(listOf(bindingKeyPair(proofKeyId)))
        coEvery { mockGetBindingKeyPair(any()) } returns Ok(null)
        coEvery { mockGenerateDPoPKeyPair(any(), credentialIssuerDid) } returns Ok(bindingKeyPair(dpopKeyId))
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Err(CredentialOfferError.Unexpected(Exception("exception")))

        useCase(credentialId = credentialId).assertErr()

        coVerify(exactly = 1) {
            mockDeleteKeyStoreEntry(proofKeyId)
            mockDeleteKeyStoreEntry(dpopKeyId)
        }
    }

    @Test
    fun `Generated hardware keys are cleaned up when the refreshed credential is discarded`() = runTest {
        coEvery {
            mockGenerateProofKeyPairs(any(), any(), credentialIssuerDid)
        } returns Ok(listOf(bindingKeyPair(proofKeyId)))
        coEvery { mockGetBindingKeyPair(any()) } returns Ok(null)
        coEvery { mockGenerateDPoPKeyPair(any(), credentialIssuerDid) } returns Ok(bindingKeyPair(dpopKeyId))
        coEvery { mockGetSwissProfileStatus(newProfileVersion) } returns SwissProfile.Status.END_OF_LIFE
        coEvery { mockGetSwissProfileStatus(currentProfileVersion) } returns SwissProfile.Status.CURRENT

        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 1) {
            mockDeleteKeyStoreEntry(proofKeyId)
            mockDeleteKeyStoreEntry(dpopKeyId)
        }
    }

    @Test
    fun `Generated software keys are not deleted from the key store on failure`() = runTest {
        coEvery {
            mockGenerateProofKeyPairs(any(), any(), credentialIssuerDid)
        } returns Ok(listOf(bindingKeyPair(proofKeyId, KeyBindingType.SOFTWARE)))
        coEvery { mockGetBindingKeyPair(any()) } returns Ok(null)
        coEvery {
            mockGenerateDPoPKeyPair(any(), credentialIssuerDid)
        } returns Ok(bindingKeyPair(dpopKeyId, KeyBindingType.SOFTWARE))
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Err(CredentialOfferError.Unexpected(Exception("fetch failed")))

        useCase(credentialId = credentialId).assertErr()

        coVerify(exactly = 0) { mockDeleteKeyStoreEntry(any()) }
    }

    @Test
    fun `Pre-existing dpop binding key is not deleted on failure`() = runTest {
        coEvery {
            mockGenerateProofKeyPairs(any(), any(), credentialIssuerDid)
        } returns Ok(listOf(bindingKeyPair(proofKeyId)))
        coEvery { mockGetBindingKeyPair(any()) } returns Ok(bindingKeyPair(dpopKeyId))
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Err(CredentialOfferError.Unexpected(Exception("fetch failed")))

        useCase(credentialId = credentialId).assertErr()

        coVerify(exactly = 1) { mockDeleteKeyStoreEntry(proofKeyId) }
        coVerify(exactly = 0) {
            mockDeleteKeyStoreEntry(dpopKeyId)
            mockGenerateDPoPKeyPair(any(), credentialIssuerDid)
        }
    }

    @Test
    fun `Keys are not deleted when a failure happens after the credential was saved`() = runTest {
        coEvery {
            mockGenerateProofKeyPairs(any(), any(), credentialIssuerDid)
        } returns Ok(listOf(bindingKeyPair(proofKeyId)))
        coEvery {
            mockDeleteBundleItems(any())
        } returns Err(SsiError.Unexpected(IllegalStateException("db error")))

        useCase(credentialId = credentialId).assertErr()

        coVerify(exactly = 0) { mockDeleteKeyStoreEntry(any()) }
    }

    @Test
    fun `A refresh started while another one is in flight is skipped`() = runTest {
        val firstCallStarted = CompletableDeferred<Unit>()
        val releaseFirstCall = CompletableDeferred<Unit>()
        coEvery { mockCredentialRefreshDataRepository.getCredentialRefreshDataById(any()) } coAnswers {
            firstCallStarted.complete(Unit)
            releaseFirstCall.await()
            Ok(mockCredentialWithAuthenticationEntity)
        }

        val firstCall = async { useCase(credentialId = credentialId) }
        firstCallStarted.await()

        useCase(credentialId = credentialId).assertOk()
        releaseFirstCall.complete(Unit)
        firstCall.await().assertOk()

        coVerify(exactly = 1) {
            mockCredentialRefreshDataRepository.getCredentialRefreshDataById(any())
        }
    }

    @Test
    fun `A refresh can run again once the previous one completed`() = runTest {
        useCase(credentialId = credentialId).assertOk()
        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 2) {
            mockCredentialRefreshDataRepository.getCredentialRefreshDataById(any())
        }
    }

    @Test
    fun `A failing refresh releases the lock for the next one`() = runTest {
        coEvery {
            mockCredentialRefreshDataRepository.getCredentialRefreshDataById(any())
        } returns Err(CredentialRefreshDataError.Unexpected(IllegalStateException("exception")))

        useCase(credentialId = credentialId).assertErr()
        useCase(credentialId = credentialId).assertErr()

        coVerify(exactly = 2) {
            mockCredentialRefreshDataRepository.getCredentialRefreshDataById(any())
        }
    }

    @Test
    fun `In case of missing dpop keypair, a new one is generated `() = runTest {
        coEvery {
            mockGetBindingKeyPair(any())
        } returns Ok(null)

        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 1) {
            mockGenerateDPoPKeyPair(mockVerifiableCredentialParams, credentialIssuerDid)
        }
    }

    @Test
    fun `Credential without key-binding can be refreshed (null proofTypeConfig)`() = runTest {
        coEvery {
            mockVerifiableCredentialParams.proofTypeConfig
        } returns null

        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 1) {
            mockVerifiableCredentialParams.proofTypeConfig
            mockFetchCredentialByConfig(
                verifiableCredentialParams = any(),
                bindingKeyPairs = null,
                payloadEncryption = any(),
                dpopKeyPair = any()
            )
        }

        coVerify(exactly = 0) {
            mockGenerateProofKeyPairs(any(), any(), any())
        }
    }

    @Test
    fun `A failure in the fetch the credential info is mapped`() = runTest {
        val throwable = Exception("my exception")
        coEvery {
            mockFetchRawAndParsedIssuerCredentialInfo(issuerEndpoint = any(), forceRefresh = any())
        } returns Err(CredentialOfferError.Unexpected(throwable))

        val error = useCase(credentialId = credentialId).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(throwable, error.cause)
    }

    @Test
    fun `A failure to get the existing credential config is mapped`() = runTest {
        val throwable = Exception("my exception")
        coEvery {
            mockGetCredentialConfig(any(), any())
        } returns Err(CredentialError.Unexpected(throwable))

        val error = useCase(credentialId = credentialId).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(throwable, error.cause)
    }

    @Test
    fun `A failure to get the credential params is mapped`() = runTest {
        val throwable = Exception("my exception")
        coEvery {
            mockGetVerifiableCredentialParams(any(), any(), any())
        } returns Err(CredentialOfferError.Unexpected(throwable))

        val error = useCase(credentialId = credentialId).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(throwable, error.cause)
    }

    @Test
    fun `A failure to evaluate the batch size is mapped`() = runTest {
        coEvery {
            mockVerifiableCredentialParams.isBatch
        } returns true

        coEvery {
            mockEvaluateBatchSize(any())
        } returns Err(CredentialError.InvalidIssuerCredentialInfo)

        useCase(credentialId = credentialId).assertErrorType(CredentialError.Unexpected::class)
    }

    @Test
    fun `A failure to generate the proof key pairs is mapped`() = runTest {
        val throwable = Exception("my exception")
        coEvery {
            mockGenerateProofKeyPairs(any(), any(), any())
        } returns Err(HolderBindingError.Unexpected(throwable))

        val error = useCase(credentialId = credentialId).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(throwable, error.cause)
    }

    @Test
    fun `A failure to get a payload encryption is mapped`() = runTest {
        val throwable = Exception("my exception")
        coEvery {
            mockGetPayloadEncryption(any(), any())
        } returns Err(PayloadEncryptionError.Unexpected(throwable))

        val error = useCase(credentialId = credentialId).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(throwable, error.cause)
    }

    @Test
    fun `A failure to get an existing dpop key is mapped`() = runTest {
        val throwable = Exception("my exception")
        coEvery {
            mockGetBindingKeyPair(authentication = any())
        } returns Err(GetBindingKeyPairError.Unexpected(throwable))

        val error = useCase(credentialId = credentialId).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(throwable, error.cause)
    }

    @Test
    fun `A failure to generate a dpop key is mapped`() = runTest {
        val throwable = Exception("my exception")
        coEvery {
            mockGetBindingKeyPair(any())
        } returns Ok(null)

        coEvery {
            mockGenerateDPoPKeyPair(any(), any())
        } returns Err(GenerateDPoPKeyPairError.Unexpected(throwable))

        val error = useCase(credentialId = credentialId).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(throwable, error.cause)
    }

    @Test
    fun `A failure to get the signed metadata did is mapped`() = runTest {
        val throwable = Exception("my exception")
        coEvery {
            mockGetSignedMetadataDid(signedMetadataJwt = any())
        } returns Err(CredentialOfferError.Unexpected(throwable))

        val error = useCase(credentialId = credentialId).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(throwable, error.cause)
    }

    @Test
    fun `A failure to process the identity trust statement is mapped to unknown actor`() = runTest {
        val throwable = Exception("my exception")
        coEvery {
            mockProcessIdentityTrustStatement(identityTrustStatementJwt = any(), actorDid = any())
        } returns Err(TrustRegistryError.Unexpected(throwable))

        useCase(credentialId = credentialId).assertErrorType(CredentialError.UnverifiedIssuer::class)
    }

    @Test
    fun `A failure to fetch the credential by config is mapped`() = runTest {
        val throwable = Exception("my exception")
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Err(CredentialOfferError.Unexpected(throwable))

        val error = useCase(credentialId = credentialId).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(throwable, error.cause)
    }

    @Test
    fun `A failure to get the existing bundle items is mapped`() = runTest {
        val throwable = Exception("my exception")
        coEvery {
            mockBundleItemRepository.getAllByCredentialId(any())
        } returns Err(SsiError.Unexpected(throwable))

        val error = useCase(credentialId = credentialId).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(throwable, error.cause)
    }

    @Test
    fun `A failure to handle a credential result is mapped`() = runTest {
        val throwable = Exception("my exception")
        coEvery {
            mockHandleCredentialResult(any(), any(), any(), any(), any(), any())
        } returns Err(CredentialError.Unexpected(throwable))

        val error = useCase(credentialId = credentialId).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(throwable, error.cause)
    }

    @Test
    fun `A failure to handle a batch credential result is mapped`() = runTest {
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Ok(mockAnyVerifiedBatchCredential)
        val throwable = Exception("my exception")
        coEvery {
            mockHandleBatchCredentialResult(any(), any(), any(), any(), any(), any(), any())
        } returns Err(CredentialError.Unexpected(throwable))

        val error = useCase(credentialId = credentialId).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(throwable, error.cause)
    }

    @Test
    fun `A failure to delete the bundle items is mapped`() = runTest {
        val throwable = Exception("my exception")
        coEvery {
            mockDeleteBundleItems(any())
        } returns Err(SsiError.Unexpected(throwable))

        val error = useCase(credentialId = credentialId).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(throwable, error.cause)
    }

    @Test
    fun `A failed refresh does not delete the existing bundle items`() = runTest {
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Err(CredentialOfferError.Unexpected(Exception("my exception")))

        useCase(credentialId = credentialId).assertErr()

        coVerify(exactly = 0) {
            mockDeleteBundleItems(any())
        }
    }

    @Test
    fun `Records the renewal attempt on a successful refresh`() = runTest {
        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 1) {
            mockVerifiableCredentialRepository.updateLastRefreshAttemptAtByCredentialId(credentialId, any())
        }
    }

    @Test
    fun `Records the renewal attempt on a failed refresh`() = runTest {
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Err(CredentialOfferError.Unexpected(Exception("fetch failed")))

        useCase(credentialId = credentialId).assertErr()

        coVerify(exactly = 1) {
            mockVerifiableCredentialRepository.updateLastRefreshAttemptAtByCredentialId(credentialId, any())
        }
    }

    @Test
    fun `Does not record a renewal attempt when the credential cannot be refreshed`() = runTest {
        coEvery { mockVerifiableCredentialEntity.progressionState } returns VerifiableProgressionState.UNACCEPTED

        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 0) {
            mockVerifiableCredentialRepository.updateLastRefreshAttemptAtByCredentialId(any(), any())
        }
    }

    @Test
    fun `Does not fetch a new credential or record a renewal attempt when refresh is disabled for the credential configuration`() = runTest {
        every { mockAnyCredentialConfiguration.credentialRefreshDisabled } returns true

        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 0) {
            mockFetchCredentialByConfig(any(), any(), any(), any())
            mockVerifiableCredentialRepository.updateLastRefreshAttemptAtByCredentialId(any(), any())
        }
    }

    @Test
    fun `A refresh keeps the new credential when its profile status is newer than the current one`() = runTest {
        coEvery { mockGetSwissProfileStatus(newProfileVersion) } returns SwissProfile.Status.DEPRECATED
        coEvery { mockGetSwissProfileStatus(currentProfileVersion) } returns SwissProfile.Status.END_OF_LIFE

        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 1) {
            mockHandleCredentialResult(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `A refresh discards the new credential when its profile status is older than the current one`() = runTest {
        coEvery { mockGetSwissProfileStatus(newProfileVersion) } returns SwissProfile.Status.END_OF_LIFE
        coEvery { mockGetSwissProfileStatus(currentProfileVersion) } returns SwissProfile.Status.CURRENT

        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 0) {
            mockHandleCredentialResult(any(), any(), any(), any(), any(), any())
            mockDeleteBundleItems(any())
        }
    }

    @Test
    fun `A refresh keeps the new credential when the current profile status is unknown`() = runTest {
        coEvery { mockGetSwissProfileStatus(currentProfileVersion) } returns null
        coEvery { mockGetSwissProfileStatus(newProfileVersion) } returns SwissProfile.Status.END_OF_LIFE

        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 1) {
            mockHandleCredentialResult(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `A refresh discards the new credential when its profile status is unknown`() = runTest {
        coEvery { mockGetSwissProfileStatus(newProfileVersion) } returns null
        coEvery { mockGetSwissProfileStatus(currentProfileVersion) } returns SwissProfile.Status.CURRENT

        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 0) {
            mockHandleCredentialResult(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `A refresh discards the new credential when it is expired`() = runTest {
        every { mockVcSdJwtCredential.validity } returns Validity.Expired(Instant.now())

        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 0) {
            mockHandleCredentialResult(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `A discarded refresh still records the renewal attempt`() = runTest {
        every { mockVcSdJwtCredential.validity } returns Validity.Expired(Instant.now())

        useCase(credentialId = credentialId).assertOk()

        coVerify(exactly = 1) {
            mockVerifiableCredentialRepository.updateLastRefreshAttemptAtByCredentialId(credentialId, any())
        }
    }

    private fun bindingKeyPair(id: String, type: KeyBindingType = KeyBindingType.HARDWARE): BindingKeyPair =
        mockk {
            every { keyPair } returns mockk {
                every { bindingType } returns type
                every { keyId } returns id
            }
        }

    private fun setupDefaultMocks() {
        issuerInfo = IssuerCredentialInfo(
            credentialEndpoint = credentialIssuerEndpoint,
            credentialIssuer = credentialIssuer,
            credentialRequestEncryption = mockCredentialRequestEncryption,
            credentialResponseEncryption = mockCredentialResponseEncryption,
            credentialConfigurations = listOf(mockAnyCredentialConfiguration),
            display = null,
            batchCredentialIssuance = null,
            identityTrustStatement = mockTrustStatementJwt,
            nonceEndpoint = nonceEndpoint,
        )

        rawAndParsed = RawAndParsedIssuerCredentialInfo(
            rawIssuerCredentialInfo = mockIssuerCredentialInfoJwt,
            issuerCredentialInfo = issuerInfo,
        )

        coEvery {
            mockFetchRawAndParsedIssuerCredentialInfo(issuerEndpoint = credentialIssuer, forceRefresh = true)
        } returns Ok(rawAndParsed)

        coEvery {
            mockCredentialRefreshDataRepository.getCredentialRefreshDataById(any())
        } returns Ok(mockCredentialWithAuthenticationEntity)

        coEvery {
            mockCredentialWithAuthenticationEntity.authentication
        } returns mockCredentialWithAuthenticationAndDpopBinding

        coEvery {
            mockCredentialWithAuthenticationAndDpopBinding.refreshToken
        } returns refreshToken

        coEvery {
            mockCredentialEntity.issuerUrl
        } returns credentialIssuer

        coEvery {
            mockCredentialWithAuthenticationEntity.credential
        } returns mockCredentialEntity

        coEvery {
            mockCredentialWithAuthenticationEntity.verifiableCredential
        } returns mockVerifiableCredentialEntity

        coEvery {
            mockVerifiableCredentialEntity.progressionState
        } returns VerifiableProgressionState.ACCEPTED

        coEvery {
            mockCredentialEntity.id
        } returns credentialId

        coEvery {
            mockCredentialEntity.selectedConfigurationId
        } returns selectedConfigurationId

        coEvery {
            mockGetVerifiableCredentialParams(any(), any(), any())
        } returns Ok(mockVerifiableCredentialParams)

        coEvery {
            mockVerifiableCredentialParams.proofTypeConfig
        } returns mockProofTypeConfig

        coEvery {
            mockVerifiableCredentialParams.isBatch
        } returns false

        coEvery {
            mockVerifiableCredentialParams.credentialConfiguration
        } returns mockAnyCredentialConfiguration

        coEvery { mockEvaluateBatchSize(any()) } answers {
            (args.first() as IssuerCredentialInfo).batchCredentialIssuance?.batchSize?.let { batchSize ->
                Ok(batchSize)
            } ?: Err(CredentialError.InvalidIssuerCredentialInfo)
        }

        coEvery {
            mockGetCredentialConfig(
                credentials = any(),
                credentialConfigurations = any()
            )
        } returns Ok(mockAnyCredentialConfiguration)

        every { mockAnyCredentialConfiguration.credentialRefreshDisabled } returns false

        coEvery { mockDeleteBundleItems(any()) } returns Ok(0)
        coEvery { mockGetSignedMetadataDid(any()) } returns Ok(credentialIssuerDid)
        coEvery { mockGenerateProofKeyPairs(any(), any(), credentialIssuerDid) } returns Ok(listOf())
        coEvery {
            mockProcessIdentityTrustStatement(mockTrustStatementJwt, credentialIssuerDid)
        } returns Ok(mockTrustStatementV2)
        coEvery { mockGetPayloadEncryption(any(), any()) } returns Ok(mockPayloadEncryption)
        coEvery { mockGetBindingKeyPair(any()) } returns Ok(mockBindingKeyPair)
        coEvery { mockGenerateDPoPKeyPair(any(), credentialIssuerDid) } returns Ok(mockDpopKeyPair)
        coEvery {
            mockHandleCredentialResult(any(), any(), any(), any(), any(), any())
        } returns Ok(FetchCredentialResult.Credential(credentialId))
        coEvery {
            mockHandleBatchCredentialResult(any(), any(), any(), any(), any(), any(), any())
        } returns Ok(FetchCredentialResult.Credential(credentialId))
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Ok(mockAnyVerifiedCredential)
        coEvery {
            mockBundleItemRepository.getAllByCredentialId(credentialId)
        } returns Ok(listOf())

        every { mockAnyVerifiedCredential.vcSdJwtCredential } returns mockVcSdJwtCredential
        every { mockAnyVerifiedBatchCredential.vcSdJwtCredentials } returns listOf(mockVcSdJwtCredential)
        every { mockVcSdJwtCredential.validity } returns Validity.Valid
        every { mockVcSdJwtCredential.profileVersion } returns newProfileVersion
        every { mockCurrentAnyCredential.profileVersion } returns currentProfileVersion
        coEvery { mockGetAllAnyCredentialsByCredentialId(any()) } returns Ok(listOf(mockCurrentAnyCredential))
        coEvery { mockGetSwissProfileStatus(any()) } returns SwissProfile.Status.CURRENT
    }
}
