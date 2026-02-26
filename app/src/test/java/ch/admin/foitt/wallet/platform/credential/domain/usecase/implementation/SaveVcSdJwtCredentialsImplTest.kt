package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import android.annotation.SuppressLint
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchema
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.CacheIssuerDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchTrustForIssuance
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveVcSdJwtCredentials
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.credentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.oneConfigCredentialInformation
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceData
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceState
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.FetchNonComplianceData
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.RawOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.VcMetadata
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchVcMetadataByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaBundler
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
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
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.Instant

class SaveVcSdJwtCredentialsImplTest {
    @MockK
    private lateinit var mockVcSdJwtCredential: VcSdJwtCredential

    @MockK
    private lateinit var mockTrustedTrustCheckResult: TrustCheckResult

    @MockK
    private lateinit var mockIdentityTrustStatement: IdentityV1TrustStatement

    @MockK
    private lateinit var mockFetchNonComplianceData: FetchNonComplianceData

    @MockK
    private lateinit var mockCacheIssuerDisplayData: CacheIssuerDisplayData

    @MockK
    private lateinit var mockCredentialOfferRepository: CredentialOfferRepository

    @MockK
    private lateinit var mockFetchTrustForIssuance: FetchTrustForIssuance

    @MockK
    private lateinit var mockFetchVcMetadataByFormat: FetchVcMetadataByFormat

    @MockK
    private lateinit var mockOcaBundler: OcaBundler

    @MockK
    private lateinit var mockGenerateAnyDisplays: GenerateAnyDisplays

    private lateinit var useCase: SaveVcSdJwtCredentials

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = SaveVcSdJwtCredentialsImpl(
            fetchNonComplianceData = mockFetchNonComplianceData,
            fetchVcMetadataByFormat = mockFetchVcMetadataByFormat,
            ocaBundler = mockOcaBundler,
            generateAnyDisplays = mockGenerateAnyDisplays,
            cacheIssuerDisplayData = mockCacheIssuerDisplayData,
            credentialOfferRepository = mockCredentialOfferRepository,
            fetchTrustForIssuance = mockFetchTrustForIssuance,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Saving credential maps errors from fetching the vc metadata`() = runTest {
        coEvery { mockFetchVcMetadataByFormat(any()) } returns Err(OcaError.InvalidOca)

        useCase(
            vcSdJwtCredentials = listOf(mockVcSdJwtCredential),
            issuerUrl = ISSUER_URL,
            rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = oneConfigCredentialInformation,
                rawIssuerCredentialInfo = ""
            ),
            credentialConfig = credentialConfig,
        ).assertErrorType(CredentialError.InvalidCredentialOffer::class)
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Saving a credential runs specific steps`() = runTest {
        val result = useCase(
            vcSdJwtCredentials = listOf(mockVcSdJwtCredential),
            issuerUrl = ISSUER_URL,
            rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = oneConfigCredentialInformation,
                rawIssuerCredentialInfo = ""
            ),
            credentialConfig = credentialConfig,
        )

        result.assertSuccessType(Long::class)
        assertEquals(Ok(CREDENTIAL_ID), result)

        coVerifyOrder {
            mockFetchVcMetadataByFormat(mockVcSdJwtCredential)
            mockOcaBundler(vcMetadata.rawOcaBundle!!.rawOcaBundle)
            mockGenerateAnyDisplays(
                anyCredential = mockVcSdJwtCredential,
                issuerInfo = oneConfigCredentialInformation,
                trustStatement = mockIdentityTrustStatement,
                metadata = credentialConfig,
                ocaBundle = ocaBundle
            )
            mockCredentialOfferRepository.saveCredentialOffer(
                keyBindings = listOf(keyBinding),
                payloads = listOf(VC_PAYLOAD),
                format = VC_FORMAT,
                selectedConfigurationId = any(),
                validFrom = any(),
                validUntil = any(),
                issuer = ISSUER_DID,
                issuerDisplays = anyDisplays.issuerDisplays,
                credentialDisplays = anyDisplays.credentialDisplays,
                clusters = anyDisplays.clusters,
                rawCredentialData = any(),
                issuerUrl = any(),
            )
        }
    }

    @Test
    fun `Saving credential maps errors from credential displays generator`() = runTest {
        val exception = IllegalStateException("my exception")
        coEvery {
            mockGenerateAnyDisplays(any(), any(), any(), any(), any())
        } returns Err(CredentialError.Unexpected(exception))

        val error = useCase(
            vcSdJwtCredentials = listOf(mockVcSdJwtCredential),
            issuerUrl = ISSUER_URL,
            rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = oneConfigCredentialInformation,
                rawIssuerCredentialInfo = ""
            ),
            credentialConfig = credentialConfig,
        ).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `An Untrusted trust check results in no trusted issuer names`() = runTest {
        val untrustedIssuer =
            TrustCheckResult(actorEnvironment = ActorEnvironment.PRODUCTION, null, VcSchemaTrustStatus.NOT_TRUSTED)
        coEvery {
            mockFetchTrustForIssuance(any(), any())
        } returns untrustedIssuer

        useCase(
            vcSdJwtCredentials = listOf(mockVcSdJwtCredential),
            issuerUrl = ISSUER_URL,
            rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = oneConfigCredentialInformation,
                rawIssuerCredentialInfo = ""
            ),
            credentialConfig = credentialConfig,
        ).assertOk()

        coVerifyOrder {
            mockGenerateAnyDisplays(any(), any(), null, any(), any())
        }
    }

    @Test
    fun `Errors from the saveCredentialOffer() call are mapped`() = runTest {
        val exception = Exception("my exception")
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
        } returns Err(SsiError.Unexpected(exception))

        val error = useCase(
            vcSdJwtCredentials = listOf(mockVcSdJwtCredential),
            issuerUrl = ISSUER_URL,
            rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = oneConfigCredentialInformation,
                rawIssuerCredentialInfo = ""
            ),
            credentialConfig = credentialConfig,
        ).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    private fun setupDefaultMocks(
        credentialInfo: IssuerCredentialInfo = oneConfigCredentialInformation,
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
            mockFetchTrustForIssuance(any(), any())
        } returns mockTrustedTrustCheckResult

        coEvery { mockFetchVcMetadataByFormat(mockVcSdJwtCredential) } returns Ok(vcMetadata)

        coEvery { mockFetchNonComplianceData(any()) } returns NonComplianceData(NonComplianceState.UNKNOWN, emptyList())

        coEvery { mockCacheIssuerDisplayData(any(), any(), any()) } returns Unit

        coEvery { mockOcaBundler(any()) } returns Ok(ocaBundle)

        every { mockIdentityTrustStatement.entityName } returns orgNames

        coEvery { mockTrustedTrustCheckResult.actorTrustStatement } returns mockIdentityTrustStatement
        coEvery { mockTrustedTrustCheckResult.actorEnvironment } returns ActorEnvironment.PRODUCTION
        coEvery { mockTrustedTrustCheckResult.vcSchemaTrustStatus } returns VcSchemaTrustStatus.TRUSTED

        coEvery {
            mockGenerateAnyDisplays(
                anyCredential = any(),
                issuerInfo = credentialInfo,
                trustStatement = any(),
                metadata = credentialConfig,
                ocaBundle = any(),
            )
        } returns Ok(anyDisplays)

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
            )
        } returns Ok(DEFERRED_CREDENTIAL_ID)
    }

    private companion object Companion {
        const val CREDENTIAL_ID = 111L
        const val DEFERRED_CREDENTIAL_ID = 222L
        val CREDENTIAL_CLAIMS_FOR_PRESENTATION = """
            {
                "key":"value"
            }
        """.trimIndent()
        const val ISSUER_DID = "issuer did"
        const val VC_SCHEMA = "schema"
        const val RAW_OCA_BUNDLE = "oca bundle"
        const val VC_SCHEMA_ID = "vcSchemaId"
        const val VC_PAYLOAD = "payload"
        val ISSUER_URL = URL("https://issuer.example.com")
        val VC_FORMAT = CredentialFormat.VC_SD_JWT
        val VC_VALID_FROM: Instant = Instant.ofEpochSecond(0)
        val VC_VALID_UNTIL: Instant = Instant.ofEpochSecond(100)

        val orgNames = mapOf(
            "en" to "issuer name en",
            "de" to "issuer name de",
        )

        val vcMetadata = VcMetadata(vcSchema = VcSchema(VC_SCHEMA), rawOcaBundle = RawOcaBundle(RAW_OCA_BUNDLE))
        val ocaBundle = OcaBundle(emptyList(), emptyList())
        val anyDisplays = AnyDisplays(emptyList(), emptyList(), emptyList())

        private val keyBinding = KeyBinding(
            identifier = "keyId",
            algorithm = SigningAlgorithm.ES512,
            bindingType = KeyBindingType.SOFTWARE,
        )
    }
}
