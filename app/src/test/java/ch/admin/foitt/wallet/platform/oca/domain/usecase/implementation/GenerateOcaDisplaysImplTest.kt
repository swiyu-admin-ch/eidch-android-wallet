package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaClaimData
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaCredentialData
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaClaimDisplays
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaDisplays
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GetClaimsPathPointers
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GetRootCaptureBase
import ch.admin.foitt.wallet.platform.oca.util.createClaimsPathPointer
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GenerateOcaDisplaysImplTest {

    @MockK
    private lateinit var mockGetRootCaptureBase: GetRootCaptureBase

    @MockK
    private lateinit var mockGetClaimsPathPointers: GetClaimsPathPointers

    @MockK
    private lateinit var mockGenerateOcaClaimDisplays: GenerateOcaClaimDisplays

    @MockK
    private lateinit var mockOcaBundle: OcaBundle

    @MockK
    private lateinit var mockCaptureBase: CaptureBase

    @MockK
    private lateinit var mockClaim: CredentialClaim

    @MockK
    private lateinit var mockClaimDisplays: List<AnyClaimDisplay>

    private lateinit var useCase: GenerateOcaDisplays

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = GenerateOcaDisplaysImpl(
            getClaimsPathPointers = mockGetClaimsPathPointers,
            getRootCaptureBase = mockGetRootCaptureBase,
            generateOcaClaimDisplays = mockGenerateOcaClaimDisplays,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Generating oca displays for flat json returns meta displays`() = runTest {
        val result = useCase(jsonObject, FORMAT, mockOcaBundle).assertOk()

        assertCredentialDisplays(result.credentialDisplays)
        val clusters = result.clusters
        assertEquals(1, clusters.size)
        val cluster = clusters.first()
        assertEquals(cluster.path, "[]")
        assertEquals(mockClaimDisplays, cluster.claims[mockClaim])

        coVerify(exactly = 1) {
            mockGenerateOcaClaimDisplays(any(), any(), any(), any())
        }
    }

    @Test
    fun `Generating oca displays for null json object returns meta displays`() = runTest {
        val result = useCase(null, FORMAT, mockOcaBundle).assertOk()

        assertCredentialDisplays(result.credentialDisplays)
        assertEquals(0, result.clusters.size)
    }

    @Test
    fun `Generating oca displays when no oca is available returns meta displays`() = runTest {
        every { mockOcaBundle.ocaCredentialData } returns emptyList()
        every { mockOcaBundle.getAttributes(any()) } returns emptyList()
        mockGenerateClaim(ocaClaim = null)

        val result = useCase(jsonObject, FORMAT, mockOcaBundle).assertOk()

        assertEquals(0, result.credentialDisplays.size)
        val clusters = result.clusters
        assertEquals(2, clusters.size)
        assertEquals(Cluster(order = 0, path = "[]", claims = emptyMap(), isSensitive = false), clusters[0])
        assertEquals("[]", clusters[1].path)
        assertEquals(0, clusters[1].childClusters.size)
        assertEquals(mockClaimDisplays, clusters[1].claims[mockClaim])

        coVerify(exactly = 1) {
            mockGenerateOcaClaimDisplays(
                claimsPathPointer = claimPathPointer,
                value = jsonPrimitive.content,
                ocaClaimData = null,
                order = null,
            )
        }
    }

    @Test
    fun `Generating oca displays maps errors from getting root capture base`() = runTest {
        coEvery { mockGetRootCaptureBase(any()) } returns Err(OcaError.InvalidRootCaptureBase)

        useCase(jsonObject, FORMAT, mockOcaBundle).assertErrorType(OcaError.InvalidRootCaptureBase::class)
    }

    @Test
    fun `Generating oca displays maps errors from generating claim displays`() = runTest {
        coEvery { mockGenerateOcaClaimDisplays(any(), any(), any(), any()) } returns Err(OcaError.UnsupportedImageFormat)

        useCase(jsonObject, FORMAT, mockOcaBundle).assertErrorType(OcaError.UnsupportedImageFormat::class)
    }

    private fun setupDefaultMocks() {
        every { mockOcaBundle.ocaCredentialData } returns listOf(ocaCredentialData)
        every { mockOcaBundle.getAttributes(DIGEST) } returns listOf(ocaClaimData)
        mockGenerateClaim()
        coEvery { mockGetRootCaptureBase(mockOcaBundle.captureBases) } returns Ok(mockCaptureBase)
        every { mockCaptureBase.digest } returns DIGEST
        coEvery { mockGetClaimsPathPointers(jsonObject) } returns mapOf(
            emptyList<ClaimsPathPointerComponent>() to jsonObject,
            claimPathPointer to jsonPrimitive
        )
    }

    private fun mockGenerateClaim(ocaClaim: OcaClaimData? = ocaClaimData) {
        coEvery {
            mockGenerateOcaClaimDisplays(
                claimsPathPointer = claimPathPointer,
                value = jsonPrimitive.content,
                ocaClaimData = ocaClaim,
                order = null,
            )
        } returns Ok(mockClaim to mockClaimDisplays)
    }

    private fun assertCredentialDisplays(credentialDisplays: List<AnyCredentialDisplay>) {
        assertEquals(1, credentialDisplays.size)
        assertEquals(LANGUAGE, credentialDisplays[0].locale)
        assertEquals(CREDENTIAL_NAME, credentialDisplays[0].name)
        assertEquals(DESCRIPTION, credentialDisplays[0].description)
        assertEquals(LOGO, credentialDisplays[0].logo)
        assertEquals(BACKGROUND_COLOR, credentialDisplays[0].backgroundColor)
        assertEquals(THEME, credentialDisplays[0].theme)
    }

    private companion object {
        const val FORMAT = "format"
        const val CREDENTIAL_NAME = "credential"
        const val KEY = "key"
        const val LANGUAGE = "language"
        const val DESCRIPTION = "description"
        const val LOGO = "logo"
        const val BACKGROUND_COLOR = "backgroundColor"
        const val THEME = "theme"
        const val DIGEST = "digest"
        val claimPathPointer = createClaimsPathPointer(KEY)
        val jsonPrimitive = JsonPrimitive("value")
        val jsonObject = JsonObject(mapOf(KEY to jsonPrimitive))
        val ocaClaimData = OcaClaimData(
            attributeType = AttributeType.Text,
            captureBaseDigest = DIGEST,
            name = "name",
            dataSources = mapOf(FORMAT to claimPathPointer),
        )
        val ocaCredentialData = OcaCredentialData(
            captureBaseDigest = DIGEST,
            locale = LANGUAGE,
            name = CREDENTIAL_NAME,
            description = DESCRIPTION,
            logoData = LOGO,
            backgroundColor = BACKGROUND_COLOR,
            theme = THEME,
        )
    }
}
