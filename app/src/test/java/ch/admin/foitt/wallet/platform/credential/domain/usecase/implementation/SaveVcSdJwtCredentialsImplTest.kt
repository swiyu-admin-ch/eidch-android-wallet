package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchema
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchTrustForIssuance
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveVcSdJwtCredentials
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.credentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.oneConfigCredentialInformation
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.RawOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.VcMetadata
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchVcMetadataByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaBundler
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustCheckResult
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.Instant

class SaveVcSdJwtCredentialsImplTest {
    @MockK
    private lateinit var mockFetchVcMetadataByFormat: FetchVcMetadataByFormat

    @MockK
    private lateinit var mockOcaBundler: OcaBundler

    @MockK
    private lateinit var mockGenerateAnyDisplays: GenerateAnyDisplays

    @MockK
    private lateinit var mockCredentialOfferRepository: CredentialOfferRepository

    @MockK
    private lateinit var mockFetchTrustForIssuance: FetchTrustForIssuance

    @MockK
    private lateinit var mockIdentityTrustStatement: IdentityV2TrustStatement

    @MockK
    private lateinit var mockIssuerCredentialInfoJwt: Jwt

    @MockK
    private lateinit var mockVcSdJwtCredential: VcSdJwtCredential

    @MockK
    private lateinit var mockTrustCheckResult: TrustCheckResult

    private lateinit var useCase: SaveVcSdJwtCredentials

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = SaveVcSdJwtCredentialsImpl(
            fetchVcMetadataByFormat = mockFetchVcMetadataByFormat,
            ocaBundler = mockOcaBundler,
            generateAnyDisplays = mockGenerateAnyDisplays,
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
    fun `Saving a credential runs specific steps`() = runTest {
        val result = useCase(
            vcSdJwtCredentials = listOf(mockVcSdJwtCredential),
            issuerUrl = ISSUER_URL,
            rawAndParsedCredentialInfo = rawAndParsedIssuerCredentialInfo,
            identityTrustStatement = mockIdentityTrustStatement,
            credentialConfig = credentialConfig,
        ).assertOk()
        assertEquals(CREDENTIAL_ID, result)

        coVerifyOrder {
            mockFetchVcMetadataByFormat(mockVcSdJwtCredential)
            mockFetchTrustForIssuance(
                identityTrustStatement = mockIdentityTrustStatement,
                protectedIssuanceAuthorizationTrustStatement = null,
                issuerDid = ISSUER_DID,
                vcSchemaId = VC_SCHEMA_ID,
            )
            mockOcaBundler(vcMetadata.rawOcaBundle!!.rawOcaBundle)
            mockGenerateAnyDisplays(
                anyCredential = mockVcSdJwtCredential,
                issuerInfo = oneConfigCredentialInformation,
                trustStatement = mockIdentityTrustStatement,
                credentialConfiguration = credentialConfig,
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
                issuerUrl = ISSUER_URL,
            )
        }
    }

    @Test
    fun `A credential without key binding correctly calls the repo`() = runTest {
        coEvery { mockVcSdJwtCredential.keyBinding } returns null

        useCase(
            vcSdJwtCredentials = listOf(mockVcSdJwtCredential),
            issuerUrl = ISSUER_URL,
            rawAndParsedCredentialInfo = rawAndParsedIssuerCredentialInfo,
            identityTrustStatement = mockIdentityTrustStatement,
            credentialConfig = credentialConfig,
        ).assertOk()

        coVerify {
            mockCredentialOfferRepository.saveCredentialOffer(
                keyBindings = null,
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
        }
    }

    @Test
    fun `Saving credential maps errors from fetching the vc metadata`() = runTest {
        coEvery { mockFetchVcMetadataByFormat(any()) } returns Err(OcaError.InvalidOca)

        useCase(
            vcSdJwtCredentials = listOf(mockVcSdJwtCredential),
            issuerUrl = ISSUER_URL,
            rawAndParsedCredentialInfo = rawAndParsedIssuerCredentialInfo,
            identityTrustStatement = mockIdentityTrustStatement,
            credentialConfig = credentialConfig,
        ).assertErrorType(CredentialError.InvalidCredentialOffer::class)
    }

    @Test
    fun `Saving credential maps errors from fetching the trust result`() = runTest {
        val exception = IllegalStateException("trust error")
        coEvery {
            mockFetchTrustForIssuance(any(), any(), any(), any())
        } returns Err(CredentialError.Unexpected(exception))

        useCase(
            vcSdJwtCredentials = listOf(mockVcSdJwtCredential),
            issuerUrl = ISSUER_URL,
            rawAndParsedCredentialInfo = rawAndParsedIssuerCredentialInfo,
            identityTrustStatement = mockIdentityTrustStatement,
            credentialConfig = credentialConfig,
        ).assertErrorType(CredentialError.Unexpected::class)
    }

    @Test
    fun `An Untrusted trust check results in no trusted issuer names`() = runTest {
        every { mockTrustCheckResult.identityTrustStatement } returns null
        every { mockTrustCheckResult.vcSchemaTrustStatus } returns VcSchemaTrustStatus.NOT_TRUSTED

        useCase(
            vcSdJwtCredentials = listOf(mockVcSdJwtCredential),
            issuerUrl = ISSUER_URL,
            rawAndParsedCredentialInfo = rawAndParsedIssuerCredentialInfo,
            identityTrustStatement = mockIdentityTrustStatement,
            credentialConfig = credentialConfig,
        ).assertOk()

        coVerifyOrder {
            mockGenerateAnyDisplays(any(), any(), null, any(), any())
        }
    }

    @Test
    fun `Saving credential does not use oca bundle if error during processing`() = runTest {
        coEvery { mockOcaBundler(RAW_OCA_BUNDLE) } returns Err(OcaError.InvalidOca)

        useCase(
            vcSdJwtCredentials = listOf(mockVcSdJwtCredential),
            issuerUrl = ISSUER_URL,
            rawAndParsedCredentialInfo = rawAndParsedIssuerCredentialInfo,
            identityTrustStatement = mockIdentityTrustStatement,
            credentialConfig = credentialConfig,
        ).assertOk()

        coVerify {
            mockGenerateAnyDisplays(
                anyCredential = any(),
                issuerInfo = any(),
                trustStatement = any(),
                credentialConfiguration = any(),
                ocaBundle = null
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
            rawAndParsedCredentialInfo = rawAndParsedIssuerCredentialInfo,
            identityTrustStatement = mockIdentityTrustStatement,
            credentialConfig = credentialConfig,
        ).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
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
            rawAndParsedCredentialInfo = rawAndParsedIssuerCredentialInfo,
            identityTrustStatement = mockIdentityTrustStatement,
            credentialConfig = credentialConfig,
        ).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    private fun setupDefaultMocks(
        credentialInfo: IssuerCredentialInfo = oneConfigCredentialInformation,
    ) {
        every { credentialConfig.format } returns CredentialFormat.VC_SD_JWT
        every { credentialConfig.protectedIssuanceAuthorizationTrustStatement } returns null

        every { mockIssuerCredentialInfoJwt.payloadString } returns ""

        every {
            mockVcSdJwtCredential.getClaimsForPresentation()
        } returns parseToJsonElement(CREDENTIAL_CLAIMS_FOR_PRESENTATION).jsonObject
        every { mockVcSdJwtCredential.issuer } returns ISSUER_DID
        every { mockVcSdJwtCredential.vcSchemaId } returns VC_SCHEMA_ID
        coEvery { mockVcSdJwtCredential.keyBinding } returns keyBinding
        coEvery { mockVcSdJwtCredential.payload } returns VC_PAYLOAD
        coEvery { mockVcSdJwtCredential.format } returns VC_FORMAT
        coEvery { mockVcSdJwtCredential.validFromInstant } returns VC_VALID_FROM
        coEvery { mockVcSdJwtCredential.validUntilInstant } returns VC_VALID_UNTIL

        coEvery {
            mockFetchTrustForIssuance(any(), any(), any(), any())
        } returns Ok(mockTrustCheckResult)

        coEvery { mockFetchVcMetadataByFormat(mockVcSdJwtCredential) } returns Ok(vcMetadata)

        coEvery { mockOcaBundler(RAW_OCA_BUNDLE) } returns Ok(ocaBundle)

        every { mockIdentityTrustStatement.entityName } returns orgNames

        coEvery { mockTrustCheckResult.identityTrustStatement } returns mockIdentityTrustStatement
        coEvery { mockTrustCheckResult.vcSchemaTrustStatus } returns VcSchemaTrustStatus.TRUSTED

        coEvery {
            mockGenerateAnyDisplays(
                anyCredential = any(),
                issuerInfo = credentialInfo,
                trustStatement = any(),
                credentialConfiguration = credentialConfig,
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
                tokenType = any(),
                refreshToken = any(),
                endpoint = any(),
                pollInterval = any(),
                keyBindings = any(),
                dpopKeyBinding = any(),
                format = any(),
                issuerDisplays = any(),
                credentialDisplays = any(),
                rawCredentialData = any(),
                selectedConfigurationId = any(),
                issuerUrl = any(),
            )
        } returns Ok(DEFERRED_CREDENTIAL_ID)
    }

    private val rawAndParsedIssuerCredentialInfo by lazy {
        RawAndParsedIssuerCredentialInfo(
            issuerCredentialInfo = oneConfigCredentialInformation,
            rawIssuerCredentialInfo = mockIssuerCredentialInfoJwt
        )
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
