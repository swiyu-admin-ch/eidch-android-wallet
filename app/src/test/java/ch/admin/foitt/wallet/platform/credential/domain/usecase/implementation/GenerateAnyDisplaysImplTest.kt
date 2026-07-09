package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.Logo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidIssuerDisplay
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayConst
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedCredentialInformationDisplay
import ch.admin.foitt.wallet.platform.oca.domain.model.MetaDisplays
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaDisplays
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV1TrustStatement
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(UnsafeResultValueAccess::class)
class GenerateAnyDisplaysImplTest {

    @MockK
    private lateinit var mockGetLocalizedCredentialInformationDisplay: GetLocalizedCredentialInformationDisplay

    @MockK
    private lateinit var mockGenerateOcaDisplays: GenerateOcaDisplays

    @MockK
    private lateinit var mockGenerateMetadataDisplays: GenerateMetadataDisplays

    @MockK
    private lateinit var mockAnyCredential: AnyCredential

    @MockK
    private lateinit var mockIssuerInfo: IssuerCredentialInfo

    @MockK
    private lateinit var mockTrustStatement: IdentityV1TrustStatement

    @MockK
    private lateinit var mockCredentialConfiguration: VcSdJwtCredentialConfiguration

    @MockK
    private lateinit var mockOcaBundle: OcaBundle

    private lateinit var useCase: GenerateAnyDisplays

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = GenerateAnyDisplaysImpl(
            getLocalizedCredentialInformationDisplay = mockGetLocalizedCredentialInformationDisplay,
            generateOcaDisplays = mockGenerateOcaDisplays,
            generateMetadataDisplays = mockGenerateMetadataDisplays,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Generating valid credential displays returns success`() = runTest {
        val result = useCase(mockAnyCredential, mockIssuerInfo, mockTrustStatement, mockCredentialConfiguration, mockOcaBundle).assertOk()

        val expectedIssuerDisplays = listOf(
            AnyIssuerDisplay(locale = LANGUAGE_EN, name = TRUST_ISSUER_NAME_EN, logo = METADATA_LOGO_URI),
            AnyIssuerDisplay(locale = LANGUAGE_FR, name = TRUST_ISSUER_NAME_FR),
            AnyIssuerDisplay(locale = DisplayLanguage.FALLBACK, name = DisplayConst.ISSUER_FALLBACK_NAME)
        )

        assertEquals(expectedIssuerDisplays, result.issuerDisplays)
        assertEquals(ocaCredentialDisplays, result.credentialDisplays)
        assertEquals(ocaClusters, result.clusters)
    }

    @Test
    fun `Generating the credential displays uses the metadata issuer name when trust issuer name is not provided`() = runTest {
        val result = useCase(mockAnyCredential, mockIssuerInfo, null, mockCredentialConfiguration, mockOcaBundle).assertOk()

        val expectedIssuerDisplays = listOf(
            AnyIssuerDisplay(locale = LANGUAGE_EN, name = METADATA_ISSUER_NAME_EN, logo = METADATA_LOGO_URI),
            AnyIssuerDisplay(locale = DisplayLanguage.FALLBACK, name = DisplayConst.ISSUER_FALLBACK_NAME)
        )

        assertEquals(expectedIssuerDisplays, result.issuerDisplays)
    }

    @Test
    fun `Generating credential displays adds fallback language if displays empty`() = runTest {
        every { mockIssuerInfo.display } returns null

        val result = useCase(mockAnyCredential, mockIssuerInfo, null, mockCredentialConfiguration, mockOcaBundle).assertOk()

        val expectedIssuerDisplays = listOf(
            AnyIssuerDisplay(locale = DisplayLanguage.FALLBACK, name = DisplayConst.ISSUER_FALLBACK_NAME)
        )

        assertEquals(expectedIssuerDisplays, result.issuerDisplays)
    }

    @Test
    fun `Generating credential displays adds fallback language`() = runTest {
        val result = useCase(mockAnyCredential, mockIssuerInfo, mockTrustStatement, mockCredentialConfiguration, mockOcaBundle).assertOk()

        val expectedIssuerDisplays = listOf(
            AnyIssuerDisplay(locale = LANGUAGE_EN, name = TRUST_ISSUER_NAME_EN, logo = METADATA_LOGO_URI),
            AnyIssuerDisplay(locale = LANGUAGE_FR, name = TRUST_ISSUER_NAME_FR),
            AnyIssuerDisplay(locale = DisplayLanguage.FALLBACK, name = DisplayConst.ISSUER_FALLBACK_NAME)
        )

        assertEquals(expectedIssuerDisplays, result.issuerDisplays)
    }

    @Test
    fun `Generating credential displays maps errors from getting the credential claims`() = runTest {
        every { mockAnyCredential.getClaimsToSave() } throws IllegalStateException()

        useCase(mockAnyCredential, mockIssuerInfo, mockTrustStatement, mockCredentialConfiguration, mockOcaBundle)
            .assertErrorType(CredentialError.Unexpected::class)
    }

    @Test
    fun `Generating credential displays gets the oca displays if oca bundle is provided`() = runTest {
        val result = useCase(mockAnyCredential, mockIssuerInfo, mockTrustStatement, mockCredentialConfiguration, mockOcaBundle).assertOk()

        assertEquals(ocaCredentialDisplays, result.credentialDisplays)
        assertEquals(ocaClusters, result.clusters)
        coVerify(exactly = 1) {
            mockGenerateOcaDisplays(any(), any(), any())
        }
        coVerify(exactly = 0) {
            mockGenerateMetadataDisplays(any(), any())
        }
    }

    @Test
    fun `Generating credential displays gets the metadata displays if the oca bundle is not provided`() = runTest {
        val result = useCase(mockAnyCredential, mockIssuerInfo, mockTrustStatement, mockCredentialConfiguration, null).assertOk()

        assertEquals(metadataCredentialDisplays, result.credentialDisplays)
        assertEquals(metadataClusters, result.clusters)
        coVerify(exactly = 1) {
            mockGenerateMetadataDisplays(any(), any())
        }
        coVerify(exactly = 0) {
            mockGenerateOcaDisplays(any(), any(), any())
        }
    }

    @Test
    fun `Generating credential displays maps errors from generating the oca displays`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockGenerateOcaDisplays(any(), any(), any()) } returns Err(OcaError.Unexpected(exception))

        useCase(
            mockAnyCredential,
            mockIssuerInfo,
            mockTrustStatement,
            mockCredentialConfiguration,
            mockOcaBundle
        ).assertErrorType(CredentialError.Unexpected::class)
    }

    @Test
    fun `Generating credential displays maps errors from generating the metadata displays`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockGenerateMetadataDisplays(any(), any()) } returns Err(CredentialError.Unexpected(exception))

        useCase(
            mockAnyCredential,
            mockIssuerInfo,
            mockTrustStatement,
            mockCredentialConfiguration,
            null
        ).assertErrorType(CredentialError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        every { mockAnyCredential.format } returns CREDENTIAL_FORMAT
        every { mockAnyCredential.getClaimsToSave() } returns mockJsonObject

        every { mockIssuerInfo.display } returns listOf(issuerDisplay)

        every { mockTrustStatement.entityName } returns trustIssuerNames

        every { mockCredentialConfiguration.format } returns CREDENTIAL_FORMAT

        coEvery {
            mockGetLocalizedCredentialInformationDisplay(listOf(issuerDisplay), LANGUAGE_EN)
        } returns issuerDisplay
        coEvery {
            mockGetLocalizedCredentialInformationDisplay(listOf(issuerDisplay), LANGUAGE_FR)
        } returns null

        coEvery { mockGenerateOcaDisplays(mockJsonObject, CREDENTIAL_FORMAT.format, mockOcaBundle) } returns Ok(ocaDisplays)
        coEvery { mockGenerateMetadataDisplays(mockJsonObject, mockCredentialConfiguration) } returns Ok(metadataDisplays)
    }

    private companion object {
        const val LANGUAGE_EN = "en"
        const val LANGUAGE_FR = "fr"
        const val TRUST_ISSUER_NAME_EN = "trust issuer name en"
        const val TRUST_ISSUER_NAME_FR = "trust issuer name fr"
        const val METADATA_ISSUER_NAME_EN = "metadata issuer name en"
        const val METADATA_LOGO_URI = "logo uri"
        val CREDENTIAL_FORMAT = CredentialFormat.VC_SD_JWT
        val issuerDisplay =
            OidIssuerDisplay(locale = LANGUAGE_EN, name = METADATA_ISSUER_NAME_EN, logo = Logo(uri = METADATA_LOGO_URI))
        val trustIssuerNames = mapOf(
            LANGUAGE_EN to TRUST_ISSUER_NAME_EN,
            LANGUAGE_FR to TRUST_ISSUER_NAME_FR,
        )

        val mockJsonObject = mockk<JsonObject>()

        val metadataCredentialDisplays = mockk<List<AnyCredentialDisplay>>()
        val metadataClusters = mockk<List<Cluster>>()
        val metadataDisplays = mockk<MetaDisplays> {
            every { credentialDisplays } returns metadataCredentialDisplays
            every { clusters } returns metadataClusters
        }
        val ocaCredentialDisplays = mockk<List<AnyCredentialDisplay>>()
        val ocaClusters = mockk<List<Cluster>>()
        val ocaDisplays = mockk<MetaDisplays> {
            every { credentialDisplays } returns ocaCredentialDisplays
            every { clusters } returns ocaClusters
        }
    }
}
