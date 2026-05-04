package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.ClusterDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.imageValidation.domain.usecase.ValidateImage
import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaClaimData
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaCredentialData
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaDisplays
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GetRootCaptureBase
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.complexNestedOcaClaimsPathPointer
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.simpleNestedOcaClaimsPathPointer
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.OcaMocks.simpleNestedOcaJsonPath
import ch.admin.foitt.wallet.platform.oca.util.createClaimsPathPointer
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GenerateNestedOcaDisplaysImplTest {

    @MockK
    private lateinit var mockGetRootCaptureBase: GetRootCaptureBase

    @MockK
    private lateinit var mockValidateImage: ValidateImage

    private val generateOcaClaimDisplays by lazy { GenerateOcaClaimDisplaysImpl(mockValidateImage) }

    @MockK
    private lateinit var mockOcaBundle: OcaBundle

    @MockK
    private lateinit var mockRootCaptureBase: CaptureBase

    private val json = SafeJsonTestInstance.safeJson
    private val generateOcaClaimData = GenerateOcaClaimDataImpl()

    private lateinit var useCase: GenerateOcaDisplays

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = GenerateOcaDisplaysImpl(
            getRootCaptureBase = mockGetRootCaptureBase,
            generateOcaClaimDisplays = generateOcaClaimDisplays,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Generating Oca displays with complex nested capture bases returns one cluster with a sub cluster`() = runTest {
        val ocaBundle = setupNestedOcaTests(complexNestedOcaClaimsPathPointer)

        val result = useCase(complexNestedCredentialClaims, VC_SD_JWT, ocaBundle).assertOk()

        assertEquals(1, result.clusters.size)
        assertEquals(expectedComplexNestedCluster, result.clusters[0])
    }

    @Test
    fun `Generating Oca displays with complex nested capture bases and missing oca claim data returns two clusters`() = runTest {
        val claims = complexNestedCredentialClaims + mapOf(
            createClaimsPathPointer("other_claim") to JsonPrimitive("otherClaimValue")
        )
        val ocaBundle = setupNestedOcaTests(complexNestedOcaClaimsPathPointer)

        val result = useCase(claims, VC_SD_JWT, ocaBundle).assertOk()

        val expectedExtraClaim = CredentialClaim(
            clusterId = -1,
            path = "[\"other_claim\"]",
            value = "otherClaimValue",
            valueType = "string",
            order = -1
        )

        val expectedExtraCluster = Cluster(
            order = -1,
            claims = mapOf(
                expectedExtraClaim to listOf(
                    AnyClaimDisplay(locale = "fallback", name = "[\"other_claim\"]")
                )
            ),
        )

        assertEquals(2, result.clusters.size)
        assertEquals(expectedComplexNestedCluster, result.clusters[0])
        assertEquals(expectedExtraCluster, result.clusters[1])
    }

    @Test
    fun `Generating Oca displays with simple nested capture bases (containing json paths) returns 3 clusters`() = runTest {
        val ocaBundle = setupNestedOcaTests(simpleNestedOcaJsonPath)

        val result = useCase(simpleNestedOcaCredentialClaims, VC_SD_JWT, ocaBundle).assertOk()

        assertEquals(3, result.clusters.size)
        assertEquals(expectedSimpleNestedClusters[0], result.clusters[0])
        assertEquals(expectedSimpleNestedClusters[1], result.clusters[1])
        assertEquals(expectedSimpleNestedClusters[2], result.clusters[2])
    }

    @Test
    fun `Generating Oca displays with simple nested capture bases returns 3 clusters`() = runTest {
        val ocaBundle = setupNestedOcaTests(simpleNestedOcaClaimsPathPointer)

        val result = useCase(simpleNestedOcaCredentialClaims, VC_SD_JWT, ocaBundle).assertOk()

        assertEquals(3, result.clusters.size)
        assertEquals(expectedSimpleNestedClusters[0], result.clusters[0])
        assertEquals(expectedSimpleNestedClusters[1], result.clusters[1])
        assertEquals(expectedSimpleNestedClusters[2], result.clusters[2])
    }

    @Test
    fun `Generating Oca displays with simple nested capture bases (containing json paths) and missing oca claim data returns 4 clusters`() = runTest {
        val ocaBundle = setupNestedOcaTests(simpleNestedOcaJsonPath)
        val otherClaimPointer = createClaimsPathPointer("other_claim")
        val otherClaimValue = "otherClaimValue"
        val claims = simpleNestedOcaCredentialClaims + mapOf(
            otherClaimPointer to JsonPrimitive(otherClaimValue)
        )

        val result = useCase(claims, VC_SD_JWT, ocaBundle).assertOk()

        val expectedExtraClaim = CredentialClaim(
            clusterId = -1,
            path = otherClaimPointer.toPointerString(),
            value = otherClaimValue,
            valueType = "string",
            order = -1
        )

        val expectedExtraCluster = Cluster(
            order = -1,
            claims = mapOf(
                expectedExtraClaim to listOf(
                    AnyClaimDisplay(locale = "fallback", name = otherClaimPointer.toPointerString())
                )
            ),
        )

        assertEquals(4, result.clusters.size)
        assertEquals(expectedSimpleNestedClusters[0], result.clusters[0])
        assertEquals(expectedSimpleNestedClusters[1], result.clusters[1])
        assertEquals(expectedSimpleNestedClusters[2], result.clusters[2])
        assertEquals(expectedExtraCluster, result.clusters[3])
    }

    @Test
    fun `Generating Oca displays with simple nested capture bases and missing oca claim data returns 4 clusters`() = runTest {
        val ocaBundle = setupNestedOcaTests(simpleNestedOcaClaimsPathPointer)
        val claims = simpleNestedOcaCredentialClaims + mapOf(
            createClaimsPathPointer("other_claim") to JsonPrimitive("otherClaimValue")
        )

        val result = useCase(claims, VC_SD_JWT, ocaBundle).assertOk()

        val expectedExtraClaim = CredentialClaim(
            clusterId = -1,
            path = "[\"other_claim\"]",
            value = "otherClaimValue",
            valueType = "string",
            order = -1
        )

        val expectedExtraCluster = Cluster(
            order = -1,
            claims = mapOf(
                expectedExtraClaim to listOf(
                    AnyClaimDisplay(locale = "fallback", name = "[\"other_claim\"]")
                )
            ),
        )

        assertEquals(4, result.clusters.size)
        assertEquals(expectedSimpleNestedClusters[0], result.clusters[0])
        assertEquals(expectedSimpleNestedClusters[1], result.clusters[1])
        assertEquals(expectedSimpleNestedClusters[2], result.clusters[2])
        assertEquals(expectedExtraCluster, result.clusters[3])
    }

    private fun setupDefaultMocks() {
        every { mockRootCaptureBase.digest } returns "digest"
        val captureBases = listOf(mockRootCaptureBase)
        every { mockOcaBundle.getAttributes("digest") } returns listOf(ocaClaimData)
        every { mockOcaBundle.ocaClaimData } returns listOf(ocaClaimData)
        every { mockOcaBundle.ocaCredentialData } returns listOf(ocaCredentialData)
        every { mockOcaBundle.captureBases } returns captureBases
        every { mockValidateImage(any(), any()) } returns Ok(Unit)

        coEvery { mockGetRootCaptureBase(captureBases) } returns Ok(mockRootCaptureBase)
    }

    @OptIn(UnsafeResultValueAccess::class)
    private fun setupNestedOcaTests(inputOcaJson: String): OcaBundle {
        val bundle = json.safeDecodeStringTo<OcaBundle>(inputOcaJson).value
        val ocaClaimData = generateOcaClaimData(bundle.captureBases, bundle.overlays)
        val fullBundle = bundle.copy(ocaClaimData = ocaClaimData)
        coEvery { mockGetRootCaptureBase(fullBundle.captureBases) } returns Ok(fullBundle.captureBases.first())

        return fullBundle
    }

    private companion object {
        const val VC_SD_JWT = "vc+sd-jwt"
        const val CLAIM_KEY = "claim_key"
        const val LANGUAGE_EN = "en"
        const val CLAIM_VALUE_EN = "claim value en"
        val CLAIMS_PATH_POINTER = createClaimsPathPointer(CLAIM_KEY)

        val ocaClaimData = OcaClaimData(
            captureBaseDigest = "digest",
            name = "claim_key",
            attributeType = AttributeType.Text,
            labels = mapOf(LANGUAGE_EN to CLAIM_VALUE_EN),
            dataSources = mapOf("vc+sd-jwt" to CLAIMS_PATH_POINTER),
            isSensitive = false
        )

        val ocaCredentialData = OcaCredentialData(
            captureBaseDigest = "digest",
            locale = LANGUAGE_EN,
            name = "name",
            description = "description",
            logoData = "logoData",
            backgroundColor = "backgroundColor",
            theme = "theme"
        )

        val complexNestedCredentialClaims = mapOf(
            createClaimsPathPointer("capture_base_1_claim_1") to JsonPrimitive("captureBase1Claim1Value"),
            createClaimsPathPointer("capture_base_1_claim_2") to JsonPrimitive("captureBase1Claim2Value"),
            createClaimsPathPointer("capture_base_2", "claim_1") to JsonPrimitive("captureBase2Claim1Value"),
            createClaimsPathPointer("capture_base_2", "claim_2") to JsonPrimitive("captureBase2Claim2Value"),
            createClaimsPathPointer("capture_base_2", "claim_3") to JsonPrimitive("captureBase2Claim3Value"),
        )

        val claim1 = CredentialClaim(
            clusterId = -1,
            path = "[\"capture_base_1_claim_1\"]",
            value = "captureBase1Claim1Value",
            valueType = "string",
            order = 1
        )
        val claim2 = CredentialClaim(
            clusterId = -1,
            path = "[\"capture_base_1_claim_2\"]",
            value = "captureBase1Claim2Value",
            valueType = "string",
            order = 2
        )
        val claim3 = CredentialClaim(
            clusterId = -1,
            path = "[\"capture_base_2\",\"claim_1\"]",
            value = "captureBase2Claim1Value",
            valueType = "string",
            order = 1
        )
        val claim4 = CredentialClaim(
            clusterId = -1,
            path = "[\"capture_base_2\",\"claim_2\"]",
            value = "captureBase2Claim2Value",
            valueType = "string",
            order = 2
        )
        val claim5 = CredentialClaim(
            clusterId = -1,
            path = "[\"capture_base_2\",\"claim_3\"]",
            value = "captureBase2Claim3Value",
            valueType = "string",
            order = 3
        )

        val expectedComplexNestedCluster = Cluster(
            order = 1,
            claims = mapOf(
                claim1 to listOf(
                    AnyClaimDisplay("de", "capture_base_1_claim_1 de"),
                    AnyClaimDisplay("fallback", "[\"capture_base_1_claim_1\"]"),
                ),
                claim2 to listOf(
                    AnyClaimDisplay("de", "capture_base_1_claim_2 de"),
                    AnyClaimDisplay("fallback", "[\"capture_base_1_claim_2\"]"),
                )
            ),
            clusterDisplays = listOf(
                ClusterDisplay("capture_base_1 de", "de"),
                ClusterDisplay("capture_base_1 en", "en"),
            ),
            childClusters = listOf(
                Cluster(
                    order = 3,
                    claims = mapOf(
                        claim3 to listOf(
                            AnyClaimDisplay("de", "capture_base_2_claim_1 de"),
                            AnyClaimDisplay("fr", "capture_base_2_claim_1 fr"),
                            AnyClaimDisplay("fallback", "[\"capture_base_2\",\"claim_1\"]"),
                        ),
                        claim4 to listOf(
                            AnyClaimDisplay("de", "capture_base_2_claim_2 de"),
                            AnyClaimDisplay("fr", "capture_base_2_claim_2 fr"),
                            AnyClaimDisplay("fallback", "[\"capture_base_2\",\"claim_2\"]"),
                        ),
                        claim5 to listOf(
                            AnyClaimDisplay("de", "capture_base_2_claim_3 de"),
                            AnyClaimDisplay("fr", "capture_base_2_claim_3 fr"),
                            AnyClaimDisplay("fallback", "[\"capture_base_2\",\"claim_3\"]"),
                        ),
                    ),
                    clusterDisplays = listOf(
                        ClusterDisplay("capture_base_2 de", "de")
                    )
                )
            )
        )

        val simpleNestedOcaCredentialClaims = mapOf(
            createClaimsPathPointer("capture_base_1_claim_1") to JsonPrimitive("captureBase1Claim1Value"),
            createClaimsPathPointer("capture_base_2_claim_1") to JsonPrimitive("captureBase2Claim1Value"),
            createClaimsPathPointer("capture_base_3_claim_1") to JsonPrimitive("captureBase3Claim1Value"),
        )

        val expectedSimpleNestedClusters = simpleNestedOcaCredentialClaims.toList().mapIndexed { i, (claimsPathPointer, value) ->
            val index = i + 1
            val claim = CredentialClaim(
                clusterId = -1,
                path = claimsPathPointer.toPointerString(),
                value = value.content,
                valueType = "string",
                order = -1
            )
            val claimDisplays = listOf(
                AnyClaimDisplay(locale = "fallback", name = claimsPathPointer.toPointerString())
            )
            val clusterDisplays = listOf(
                ClusterDisplay(locale = "de", name = "capture_base_$index de")
            )
            Cluster(
                order = index,
                claims = mapOf(claim to claimDisplays),
                clusterDisplays = clusterDisplays
            )
        }
    }
}
