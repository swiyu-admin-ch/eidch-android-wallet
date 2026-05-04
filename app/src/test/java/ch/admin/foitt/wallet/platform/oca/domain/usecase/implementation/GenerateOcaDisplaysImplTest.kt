package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
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
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GetRootCaptureBase
import ch.admin.foitt.wallet.platform.oca.util.createClaimsPathPointer
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GenerateOcaDisplaysImplTest {

    @MockK
    private lateinit var mockGetRootCaptureBase: GetRootCaptureBase

    @MockK
    private lateinit var mockGenerateOcaClaimDisplays: GenerateOcaClaimDisplays

    @MockK
    private lateinit var mockOcaBundle: OcaBundle

    @MockK
    private lateinit var mockRootCaptureBase: CaptureBase

    private lateinit var ocaClaimData: OcaClaimData

    private lateinit var useCase: GenerateOcaDisplays

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = GenerateOcaDisplaysImpl(
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
    fun `Generating valid Oca displays with returns success`() = runTest {
        val result = useCase(credentialClaims, VC_SD_JWT, mockOcaBundle).assertOk()

        val expectedCredentialDisplays = listOf(
            AnyCredentialDisplay(
                locale = LANGUAGE_EN,
                name = "name",
                description = "description",
                logo = "logoData",
                backgroundColor = "backgroundColor",
                theme = "theme"
            ),
        )

        val expectedCluster = Cluster(
            claims = mapOf(claim to claimDisplays)
        )

        assertEquals(expectedCredentialDisplays, result.credentialDisplays)
        assertEquals(1, result.clusters.size)
        assertEquals(expectedCluster, result.clusters.first())
    }

    @Test
    fun `Generating valid Oca displays with multiple claims returns success`() = runTest {
        val otherClaimsPathPointer = createClaimsPathPointer("otherClaim")
        val otherValue = JsonPrimitive("otherValue")
        val otherClaim = mockk<CredentialClaim>()
        val otherClaimDisplays = mockk<List<AnyClaimDisplay>>()
        val otherOcaClaimData = mockOcaClaimData(otherClaimsPathPointer)

        every {
            mockGenerateOcaClaimDisplays(
                claimsPathPointer = otherClaimsPathPointer.toPointerString(),
                value = otherValue.content,
                ocaClaimData = otherOcaClaimData
            )
        } returns Ok(otherClaim to otherClaimDisplays)
        every { mockOcaBundle.getAttributes("digest") } returns listOf(ocaClaimData, otherOcaClaimData)
        every { mockOcaBundle.ocaClaimData } returns listOf(ocaClaimData, otherOcaClaimData)

        val claims = credentialClaims + (otherClaimsPathPointer to otherValue)

        val result = useCase(claims, VC_SD_JWT, mockOcaBundle).assertOk()

        assertEquals(1, result.clusters.size)
        val clusterClaims = result.clusters.first().claims
        assertEquals(2, clusterClaims.size)
        assertEquals(claimDisplays, clusterClaims[claim])
        assertEquals(otherClaimDisplays, clusterClaims[otherClaim])
    }

    @Test
    fun `Generating valid Oca displays where a claim does not have oca data returns success`() = runTest {
        val otherClaimsPathPointer = createClaimsPathPointer("otherClaim")
        val otherValue = JsonPrimitive("otherValue")
        val otherClaim = mockk<CredentialClaim>()
        val otherClaimDisplays = mockk<List<AnyClaimDisplay>>()
        val otherOcaClaimData = mockOcaClaimData(otherClaimsPathPointer)

        every {
            mockGenerateOcaClaimDisplays(
                claimsPathPointer = otherClaimsPathPointer.toPointerString(),
                value = otherValue.content,
                ocaClaimData = otherOcaClaimData
            )
        } returns Ok(otherClaim to otherClaimDisplays)

        val claims = credentialClaims + (otherClaimsPathPointer to otherValue)

        val result = useCase(claims, VC_SD_JWT, mockOcaBundle).assertOk()

        val expectedOtherClaim = CredentialClaim(
            clusterId = -1,
            path = otherClaimsPathPointer.toPointerString(),
            value = otherValue.content,
            valueType = "string"
        )

        val expectedOtherClaimDisplay = AnyClaimDisplay(
            locale = "fallback",
            name = otherClaimsPathPointer.toPointerString()
        )

        assertEquals(2, result.clusters.size)
        assertEquals(mapOf(claim to claimDisplays), result.clusters[0].claims)
        assertEquals(mapOf(expectedOtherClaim to listOf(expectedOtherClaimDisplay)), result.clusters[1].claims)
    }

    @Test
    fun `Generating valid Oca displays without credential data returns credential without displays`() = runTest {
        every { mockOcaBundle.ocaCredentialData } returns emptyList()

        val result = useCase(credentialClaims, VC_SD_JWT, mockOcaBundle).assertOk()

        assertEquals(0, result.credentialDisplays.size)
    }

    @Test
    fun `Generating Oca displays maps errors from getting the root capture base`() = runTest {
        coEvery { mockGetRootCaptureBase(any()) } returns Err(OcaError.InvalidRootCaptureBase)

        useCase(credentialClaims, VC_SD_JWT, mockOcaBundle).assertErrorType(OcaError.InvalidRootCaptureBase::class)
    }

    private fun setupDefaultMocks() {
        every { mockRootCaptureBase.digest } returns "digest"
        val captureBases = listOf(mockRootCaptureBase)
        ocaClaimData = mockOcaClaimData(claimPathPointer)
        every { mockOcaBundle.getAttributes("digest") } returns listOf(ocaClaimData)
        every { mockOcaBundle.ocaClaimData } returns listOf(ocaClaimData)
        every { mockOcaBundle.ocaCredentialData } returns listOf(ocaCredentialData)
        every { mockOcaBundle.captureBases } returns captureBases

        coEvery { mockGetRootCaptureBase(captureBases) } returns Ok(mockRootCaptureBase)
        every {
            mockGenerateOcaClaimDisplays(
                claimsPathPointer = claimPathPointer.toPointerString(),
                value = claimValueElement.content,
                ocaClaimData = ocaClaimData
            )
        } returns Ok(claim to claimDisplays)
    }

    private fun mockOcaClaimData(claimsPathPointer: ClaimsPathPointer): OcaClaimData {
        val otherOcaClaimData = mockk<OcaClaimData> {
            every { attributeType } returns AttributeType.Text
            every { dataSources } returns mapOf(VC_SD_JWT to claimsPathPointer)
        }
        return otherOcaClaimData
    }

    private companion object {
        const val VC_SD_JWT = "vc+sd-jwt"
        const val CLAIM_NAME = "claimName"
        const val CLAIM_VALUE = "claim_value"
        val claimValueElement = JsonPrimitive(CLAIM_VALUE)
        const val LANGUAGE_EN = "en"
        val claimPathPointer = createClaimsPathPointer(CLAIM_NAME)
        val ocaCredentialData = OcaCredentialData(
            captureBaseDigest = "digest",
            locale = LANGUAGE_EN,
            name = "name",
            description = "description",
            logoData = "logoData",
            backgroundColor = "backgroundColor",
            theme = "theme"
        )

        val credentialClaims = mapOf(claimPathPointer to claimValueElement)
        val claim = mockk<CredentialClaim>()
        val claimDisplays = mockk<List<AnyClaimDisplay>>()
    }
}
