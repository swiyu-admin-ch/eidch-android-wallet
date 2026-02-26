package ch.admin.foitt.wallet.platform.actorMetadata

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorMetaDataError
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.CacheIssuerDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchAndCacheIssuerDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.FetchAndCacheIssuerDisplayDataImpl
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchTrustForIssuance
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAllAnyCredentialsByCredentialId
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockNonComplianceData
import ch.admin.foitt.wallet.platform.credential.domain.util.entityNames
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceData
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.FetchNonComplianceData
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialIssuerDisplayRepo
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustCheckResult
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FetchAndCacheIssuerDisplayDataImplTest {

    @MockK
    private lateinit var mockGetAllAnyCredentialByCredentialId: GetAllAnyCredentialsByCredentialId

    @MockK
    private lateinit var mockFetchTrustForIssuance: FetchTrustForIssuance

    @MockK
    private lateinit var mockCredentialIssuerDisplayRepo: CredentialIssuerDisplayRepo

    @MockK
    private lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    @MockK
    private lateinit var mockFetchNonComplianceData: FetchNonComplianceData

    @MockK
    private lateinit var mockCacheIssuerDisplayData: CacheIssuerDisplayData

    private val nonComplianceData = MockNonComplianceData.nonComplianceData

    @MockK
    private lateinit var mockTrustedTrustCheckResult: TrustCheckResult

    @MockK
    private lateinit var mockIdentityTrustStatement: IdentityV1TrustStatement

    @MockK
    private lateinit var mockAnyCredential: AnyCredential

    private lateinit var useCase: FetchAndCacheIssuerDisplayData

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchAndCacheIssuerDisplayDataImpl(
            getAllAnyCredentialsByCredentialId = mockGetAllAnyCredentialByCredentialId,
            fetchTrustForIssuance = mockFetchTrustForIssuance,
            credentialIssuerDisplayRepo = mockCredentialIssuerDisplayRepo,
            getLocalizedDisplay = mockGetLocalizedDisplay,
            fetchNonComplianceData = mockFetchNonComplianceData,
            cacheIssuerDisplayData = mockCacheIssuerDisplayData,
        )

        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {
        coEvery { mockGetAllAnyCredentialByCredentialId(credentialId = any()) } returns Ok(listOf(mockAnyCredential))

        coEvery { mockFetchTrustForIssuance(any(), any()) } returns mockTrustedTrustCheckResult

        coEvery {
            mockCredentialIssuerDisplayRepo.getIssuerDisplays(credentialId = any())
        } returns Ok(listOf(issuerDisplayData1))

        coEvery {
            mockGetLocalizedDisplay(listOf(issuerDisplayData1), DISPLAY_LOCALE1)
        } returns issuerDisplayData1

        coEvery {
            mockGetLocalizedDisplay(listOf(issuerDisplayData1), DISPLAY_LOCALE2)
        } returns null

        coEvery {
            mockGetLocalizedDisplay(listOf(issuerDisplayData2))
        } returns issuerDisplayData2

        coEvery { mockFetchNonComplianceData(ISSUER_DID) } returns nonComplianceData

        coEvery {
            mockCacheIssuerDisplayData(any(), any(), any())
        } just Runs

        every { mockTrustedTrustCheckResult.actorTrustStatement } returns mockIdentityTrustStatement
        every { mockIdentityTrustStatement.entityNames() } returns TRUST_ISSUER_NAMES

        // anyCredential setup
        every { mockAnyCredential.id } returns CREDENTIAL_ID
        every { mockAnyCredential.issuer } returns ISSUER_DID
        every { mockAnyCredential.vcSchemaId } returns VC_SCHEMA_ID
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A successful execution follow specific steps`() = runTest {
        useCase(credentialId = CREDENTIAL_ID).assertOk()

        coVerifyOrder {
            mockGetAllAnyCredentialByCredentialId(credentialId = CREDENTIAL_ID)
            mockFetchTrustForIssuance(
                issuerDid = ISSUER_DID,
                vcSchemaId = VC_SCHEMA_ID,
            )
            mockCredentialIssuerDisplayRepo.getIssuerDisplays(credentialId = CREDENTIAL_ID)
            mockIdentityTrustStatement.entityNames()
            mockGetLocalizedDisplay(listOf(issuerDisplayData1), DISPLAY_LOCALE1)
            mockGetLocalizedDisplay(listOf(issuerDisplayData1), DISPLAY_LOCALE2)
            mockFetchNonComplianceData(actorDid = ISSUER_DID)
            mockCacheIssuerDisplayData(
                trustCheckResult = mockTrustedTrustCheckResult,
                issuerDisplays = any(),
                nonComplianceData = nonComplianceData,
            )
        }
    }

    @Test
    fun `The cached display uses the trust issuer name if available`() = runTest {
        useCase(credentialId = CREDENTIAL_ID).assertOk()

        val expectedIssuerDisplays = listOf(
            AnyIssuerDisplay(
                locale = DISPLAY_LOCALE1,
                name = TRUST_ISSUER_NAME1,
                logo = issuerDisplayData1.image,
                logoAltText = issuerDisplayData1.imageAltText,
            ),
            AnyIssuerDisplay(
                locale = DISPLAY_LOCALE2,
                name = TRUST_ISSUER_NAME2,
                logo = null,
                logoAltText = null,
            ),
        )

        coVerify {
            mockCacheIssuerDisplayData(
                trustCheckResult = any(),
                issuerDisplays = expectedIssuerDisplays,
                nonComplianceData = any(),
            )
        }
    }

    @Test
    fun `The cached display uses the metadata issuer name if trust statement is not available`() = runTest {
        coEvery {
            mockFetchTrustForIssuance(any(), any())
        } returns TrustCheckResult(
            actorEnvironment = ActorEnvironment.PRODUCTION,
            actorTrustStatement = null,
            vcSchemaTrustStatus = VcSchemaTrustStatus.NOT_TRUSTED,
        )
        coEvery {
            mockCredentialIssuerDisplayRepo.getIssuerDisplays(credentialId = any())
        } returns Ok(listOf(issuerDisplayData1, issuerDisplayData2))

        useCase(credentialId = CREDENTIAL_ID).assertOk()

        val expectedIssuerDisplays = listOf(
            AnyIssuerDisplay(
                locale = issuerDisplayData1.locale,
                name = issuerDisplayData1.name,
                logo = issuerDisplayData1.image,
                logoAltText = issuerDisplayData1.imageAltText,
            ),
            AnyIssuerDisplay(
                locale = issuerDisplayData2.locale,
                name = issuerDisplayData2.name,
                logo = issuerDisplayData2.image,
                logoAltText = issuerDisplayData2.imageAltText,
            ),
        )

        coVerify {
            mockCacheIssuerDisplayData(
                trustCheckResult = any(),
                issuerDisplays = expectedIssuerDisplays,
                nonComplianceData = any(),
            )
        }
    }

    @Test
    fun `A GetAnyCredential error is mapped`() = runTest {
        val exception = Exception("my exception")
        coEvery { mockGetAllAnyCredentialByCredentialId(any()) } returns Err(CredentialError.Unexpected(exception))

        val error = useCase(
            credentialId = CREDENTIAL_ID,
        ).assertErrorType(ActorMetaDataError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    @Test
    fun `An empty trustCheckResult does not stop the execution`() = runTest {
        coEvery { mockTrustedTrustCheckResult.actorTrustStatement } returns null

        useCase(CREDENTIAL_ID).assertOk()
    }

    @Test
    fun `A credentialIssuer repository error is mapped`() = runTest {
        val exception = Exception("my exception")
        coEvery {
            mockCredentialIssuerDisplayRepo.getIssuerDisplays(any())
        } returns Err(SsiError.Unexpected(exception))

        val error = useCase(CREDENTIAL_ID).assertErrorType(ActorMetaDataError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    @Test
    fun `A failed nonCompliance call does not stop the execution`() = runTest {
        coEvery { mockFetchNonComplianceData(actorDid = any()) } returns NonComplianceData(
            state = NonComplianceState.UNKNOWN,
            reasonDisplays = null,
        )

        useCase(CREDENTIAL_ID).assertOk()
    }

    @Test
    fun `A caching exception is not caught`() = runTest {
        coEvery {
            mockCacheIssuerDisplayData(any(), any(), any())
        } throws IllegalStateException("my exception")

        assertThrows<IllegalStateException> {
            useCase(CREDENTIAL_ID)
        }
    }

    private companion object {
        const val CREDENTIAL_ID = 1L
        const val ISSUER_DID = "issuer did"
        const val VC_SCHEMA_ID = "vcSchemaId"
        const val DISPLAY_LOCALE1 = "displayLocale1"
        const val DISPLAY_LOCALE2 = "displayLocale2"
        const val TRUST_ISSUER_NAME1 = "trustIssuerName1"
        const val TRUST_ISSUER_NAME2 = "trustIssuerName2"

        val TRUST_ISSUER_NAMES = mapOf(
            DISPLAY_LOCALE1 to TRUST_ISSUER_NAME1,
            DISPLAY_LOCALE2 to TRUST_ISSUER_NAME2,
        )

        val issuerDisplayData1 = CredentialIssuerDisplay(
            id = 1L,
            credentialId = CREDENTIAL_ID,
            name = "issuerName1",
            image = "issuerImage1",
            imageAltText = "issuerImageAltText1",
            locale = DISPLAY_LOCALE1,
        )

        val issuerDisplayData2 = CredentialIssuerDisplay(
            id = 2L,
            credentialId = CREDENTIAL_ID,
            name = "issuerName2",
            image = "issuerImage2",
            imageAltText = "issuerImageAltText2",
            locale = DISPLAY_LOCALE2,
        )
    }
}
