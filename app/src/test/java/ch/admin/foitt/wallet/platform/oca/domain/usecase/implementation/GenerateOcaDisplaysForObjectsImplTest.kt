package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.ClusterDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaClaimData
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaClaimDisplays
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaDisplays
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GetClaimsPathPointers
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GetRootCaptureBase
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GenerateOcaDisplaysForObjectsImplTest {

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
    fun `Generating oca displays for a simple object returns meta displays`() = runTest {
        mockGeneratedClaims(
            claim1Path = element1Path,
            claim2Path = element2Path,
            claim1OcaData = claim1OcaData,
            claim2OcaData = claim2OcaData,
        )

        val result = useCase(simpleObjectJson, FORMAT, mockOcaBundle).assertOk()

        assertEquals(1, result.clusters.size)
        result.clusters[0].assert(claimsSize = 2)
        assertEquals(claimDisplays1, result.clusters[0].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[0].claims[claim2])
    }

    @Test
    fun `Generating oca displays for a flat object with multiple captures bases returns meta displays`() = runTest {
        every { mockOcaBundle.getAttributes(ROOT_DIGEST) } returns listOf(
            nestedObject1OcaData.copy(dataSources = emptyMap()),
            nestedObject2OcaData.copy(dataSources = emptyMap())
        )
        every { mockOcaBundle.getAttributes(NESTED_1_DIGEST) } returns listOf(claim1OcaData)
        every { mockOcaBundle.getAttributes(NESTED_2_DIGEST) } returns listOf(claim2OcaData)
        mockGeneratedClaims(
            claim1Path = element1Path,
            claim2Path = element2Path,
            claim1OcaData = claim1OcaData,
            claim2OcaData = claim2OcaData,
        )

        val result = useCase(simpleObjectJson, FORMAT, mockOcaBundle).assertOk()

        assertEquals(2, result.clusters.size)
        result.clusters[0].assert(
            displayName = CLAIM_1_NAME,
            claimsSize = 1,
        )
        result.clusters[1].assert(
            order = 1,
            isSensitive = true,
            displayName = CLAIM_2_NAME,
            claimsSize = 1,
        )
        assertEquals(claimDisplays1, result.clusters[0].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[1].claims[claim2])
    }

    @Test
    fun `Generating oca displays for a nested object returns meta displays`() = runTest {
        every { mockOcaBundle.getAttributes(ROOT_DIGEST) } returns listOf(nestedObject1OcaData, nestedObject2OcaData)
        every { mockOcaBundle.getAttributes(NESTED_1_DIGEST) } returns listOf(nestedClaim1OcaData)
        every { mockOcaBundle.getAttributes(NESTED_2_DIGEST) } returns listOf(nestedClaim2OcaData)
        coEvery { mockGetClaimsPathPointers(nestedObjectJson) } returns pathElementMapNested
        mockGeneratedClaims(
            claim1Path = nestedElement1Path,
            claim2Path = nestedElement2Path,
            claim1OcaData = nestedClaim1OcaData,
            claim2OcaData = nestedClaim2OcaData,
        )

        val result = useCase(nestedObjectJson, FORMAT, mockOcaBundle).assertOk()

        assertEquals(2, result.clusters.size)
        result.clusters[0].assert(
            path = element1Path.toPointerString(),
            displayName = CLAIM_1_NAME,
            claimsSize = 1,
        )
        result.clusters[1].assert(
            order = 1,
            isSensitive = true,
            path = element2Path.toPointerString(),
            displayName = CLAIM_2_NAME,
            claimsSize = 1,
        )
        assertEquals(claimDisplays1, result.clusters[0].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[1].claims[claim2])
    }

    @Test
    fun `Generating metadata displays for a nested object without metadata returns meta displays`() = runTest {
        every { mockOcaBundle.getAttributes(ROOT_DIGEST) } returns emptyList()
        coEvery { mockGetClaimsPathPointers(nestedObjectJson) } returns pathElementMapNested
        mockGeneratedClaims(
            claim1Path = nestedElement1Path,
            claim2Path = nestedElement2Path,
            claim1OcaData = null,
            claim2OcaData = null,
        )

        val result = useCase(nestedObjectJson, FORMAT, mockOcaBundle).assertOk()

        assertEquals(2, result.clusters.size)
        result.clusters[0].assert()
        result.clusters[1].assert(order = -1, claimsSize = 2)
        assertEquals(claimDisplays1, result.clusters[1].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[1].claims[claim2])
    }

    @Test
    fun `Generating oca displays for a nested object with one capture base returns meta displays`() = runTest {
        val claim1Oca = claim1OcaData.copy(dataSources = mapOf(FORMAT to nestedElement1Path))
        val claim2Oca = claim2OcaData.copy(dataSources = mapOf(FORMAT to nestedElement2Path))
        every { mockOcaBundle.getAttributes(ROOT_DIGEST) } returns listOf(claim1Oca, claim2Oca)
        coEvery { mockGetClaimsPathPointers(nestedObjectJson) } returns pathElementMapNested
        mockGeneratedClaims(
            claim1Path = nestedElement1Path,
            claim2Path = nestedElement2Path,
            claim1OcaData = claim1Oca,
            claim2OcaData = claim2Oca,
        )

        val result = useCase(nestedObjectJson, FORMAT, mockOcaBundle).assertOk()

        assertEquals(1, result.clusters.size)
        result.clusters[0].assert(claimsSize = 2)
        assertEquals(claimDisplays1, result.clusters[0].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[0].claims[claim2])
    }

    @Test
    fun `Generating metadata displays for a mixed object returns meta displays`() = runTest {
        every { mockOcaBundle.getAttributes(ROOT_DIGEST) } returns listOf(claim1OcaData, nestedObject2OcaData, arrayOcaData)
        every { mockOcaBundle.getAttributes(NESTED_2_DIGEST) } returns listOf(nestedClaim2OcaData)
        coEvery { mockGetClaimsPathPointers(mixedObjectJson) } returns pathElementMapMixed
        mockGeneratedClaims(
            claim1Path = element1Path,
            claim2Path = nestedElement2Path,
            claim1OcaData = claim1OcaData,
            claim2OcaData = nestedClaim2OcaData,
            orderClaim2 = null,
        )
        coEvery {
            mockGenerateOcaClaimDisplays(
                claimsPathPointer = arrayElementPath,
                value = CLAIM_VALUE_3,
                ocaClaimData = null,
                order = 0,
            )
        } returns Ok(claim3 to claimDisplays3)

        val result = useCase(mixedObjectJson, FORMAT, mockOcaBundle).assertOk()

        assertEquals(1, result.clusters.size)
        result.clusters[0].assert(childClustersSize = 2, claimsSize = 1)
        val rootCluster = result.clusters[0]
        rootCluster.childClusters[0].assert(
            order = 2,
            isSensitive = true,
            path = element2Path.toPointerString(),
            displayName = CLAIM_2_NAME,
            claimsSize = 1,
        )
        rootCluster.childClusters[1].assert(
            order = 3,
            path = arrayPath.toPointerString(),
            displayName = ARRAY_NAME,
            claimsSize = 1,
        )
        assertEquals(claimDisplays1, result.clusters[0].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[0].childClusters[0].claims[claim2])
        assertEquals(claimDisplays3, result.clusters[0].childClusters[1].claims[claim3])
    }

    @Test
    fun `Generating metadata displays for a mixed object without oca returns meta displays`() = runTest {
        every { mockOcaBundle.getAttributes(ROOT_DIGEST) } returns emptyList()
        coEvery { mockGetClaimsPathPointers(mixedObjectJson) } returns pathElementMapMixed
        mockGeneratedClaims(
            claim1Path = element1Path,
            claim2Path = nestedElement2Path,
            claim1OcaData = null,
            claim2OcaData = null,
            orderClaim2 = null,
        )
        coEvery {
            mockGenerateOcaClaimDisplays(
                claimsPathPointer = arrayElementPath,
                value = CLAIM_VALUE_3,
                ocaClaimData = null,
                order = null,
            )
        } returns Ok(claim3 to claimDisplays3)

        val result = useCase(mixedObjectJson, FORMAT, mockOcaBundle).assertOk()

        assertEquals(2, result.clusters.size)
        result.clusters[0].assert()
        result.clusters[1].assert(order = -1, claimsSize = 3)
        assertEquals(claimDisplays1, result.clusters[1].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[1].claims[claim2])
        assertEquals(claimDisplays3, result.clusters[1].claims[claim3])
    }

    private fun setupDefaultMocks() {
        every { mockOcaBundle.getAttributes(ROOT_DIGEST) } returns listOf(claim1OcaData, claim2OcaData)
        every { mockOcaBundle.ocaCredentialData } returns emptyList()
        coEvery { mockGetRootCaptureBase(mockOcaBundle.captureBases) } returns Ok(mockCaptureBase)
        every { mockCaptureBase.digest } returns ROOT_DIGEST
        coEvery { mockGetClaimsPathPointers(simpleObjectJson) } returns mapOf(
            emptyList<ClaimsPathPointerComponent>() to simpleObjectJson,
            element1Path to jsonPrimitive1,
            element2Path to jsonPrimitive2,
        )
    }

    private fun mockGeneratedClaims(
        claim1Path: ClaimsPathPointer,
        claim2Path: ClaimsPathPointer,
        claim1OcaData: OcaClaimData?,
        claim2OcaData: OcaClaimData?,
        orderClaim1: Int? = null,
        orderClaim2: Int? = null,
    ) {
        coEvery {
            mockGenerateOcaClaimDisplays(
                claimsPathPointer = claim1Path,
                value = CLAIM_VALUE_1,
                ocaClaimData = claim1OcaData,
                order = orderClaim1,
            )
        } returns Ok(claim1 to claimDisplays1)
        coEvery {
            mockGenerateOcaClaimDisplays(
                claimsPathPointer = claim2Path,
                value = CLAIM_VALUE_2,
                ocaClaimData = claim2OcaData,
                order = orderClaim2,
            )
        } returns Ok(claim2 to claimDisplays2)
    }

    private fun Cluster.assert(
        order: Int = 0,
        isSensitive: Boolean = false,
        path: String = "[]",
        displayName: String? = null,
        claimsSize: Int = 0,
        childClustersSize: Int = 0,
    ) {
        assertEquals(order, this.order)
        assertEquals(isSensitive, this.isSensitive)
        assertEquals(path, this.path)
        val expectedDisplays = displayName?.let { listOf(ClusterDisplay(displayName, LANGUAGE)) } ?: emptyList()
        assertEquals(expectedDisplays, this.clusterDisplays)
        assertEquals(claimsSize, claims.size)
        assertEquals(childClustersSize, childClusters.size)
    }

    private companion object {
        const val FORMAT = "format"
        const val ROOT_DIGEST = "rootDigest"
        const val NESTED_1_DIGEST = "nestedDigest1"
        const val NESTED_2_DIGEST = "nestedDigest2"
        val element1Path = listOf(ClaimsPathPointerComponent.String(KEY_1))
        val element2Path = listOf(ClaimsPathPointerComponent.String(KEY_2))
        val arrayPath = listOf(ClaimsPathPointerComponent.String(KEY_3), ClaimsPathPointerComponent.Null)
        val nestedElement1Path =
            listOf(ClaimsPathPointerComponent.String(KEY_1), ClaimsPathPointerComponent.String(KEY_3))
        val nestedElement2Path =
            listOf(ClaimsPathPointerComponent.String(KEY_2), ClaimsPathPointerComponent.String(KEY_4))
        val arrayElementPath = listOf(ClaimsPathPointerComponent.String(KEY_3), ClaimsPathPointerComponent.Index(0))
        const val KEY_1 = "key1"
        const val KEY_2 = "key2"
        const val KEY_3 = "key3"
        const val KEY_4 = "key4"
        const val CLAIM_VALUE_1 = "claimValue"
        const val CLAIM_VALUE_2 = "claimValue2"
        const val CLAIM_VALUE_3 = "claimValue3"
        val jsonPrimitive1 = JsonPrimitive(CLAIM_VALUE_1)
        val jsonPrimitive2 = JsonPrimitive(CLAIM_VALUE_2)
        val jsonPrimitive3 = JsonPrimitive(CLAIM_VALUE_3)
        val simpleObjectJson = JsonObject(
            mapOf(
                KEY_1 to jsonPrimitive1,
                KEY_2 to jsonPrimitive2,
            )
        )
        val nestedObject1 = JsonObject(mapOf(KEY_3 to jsonPrimitive1))
        val nestedObject2 = JsonObject(mapOf(KEY_4 to jsonPrimitive2))
        val nestedObjectJson = JsonObject(
            mapOf(
                KEY_1 to nestedObject1,
                KEY_2 to nestedObject2,
            )
        )
        val pathElementMapNested = mapOf(
            emptyList<ClaimsPathPointerComponent>() to nestedObjectJson,
            element1Path to nestedObject1,
            nestedElement1Path to jsonPrimitive1,
            element2Path to nestedObject2,
            nestedElement2Path to jsonPrimitive2,
        )
        val jsonArray = JsonArray(listOf(jsonPrimitive3))
        val mixedObjectJson = JsonObject(
            mapOf(
                KEY_1 to jsonPrimitive1,
                KEY_2 to nestedObject2,
                KEY_3 to jsonArray,
            )
        )
        val pathElementMapMixed = mapOf(
            emptyList<ClaimsPathPointerComponent>() to mixedObjectJson,
            element1Path to jsonPrimitive1,
            element2Path to nestedObject2,
            nestedElement2Path to jsonPrimitive2,
            arrayPath to jsonArray,
            arrayElementPath to jsonPrimitive3,
        )
        const val LANGUAGE = "language"
        const val CLAIM_1_NAME = "claim1"
        const val CLAIM_2_NAME = "claim2"
        const val ARRAY_NAME = "array"

        val claim1OcaData = OcaClaimData(
            attributeType = AttributeType.Text,
            captureBaseDigest = ROOT_DIGEST,
            name = "claim1",
            dataSources = mapOf(FORMAT to element1Path),
            labels = mapOf(LANGUAGE to CLAIM_1_NAME),
        )

        val claim2OcaData = OcaClaimData(
            attributeType = AttributeType.Text,
            captureBaseDigest = ROOT_DIGEST,
            name = "claim2",
            dataSources = mapOf(FORMAT to element2Path),
            labels = mapOf(LANGUAGE to CLAIM_2_NAME),
        )

        val arrayOcaData = OcaClaimData(
            attributeType = AttributeType.Array(AttributeType.Text),
            captureBaseDigest = ROOT_DIGEST,
            name = "array",
            dataSources = mapOf(FORMAT to arrayPath),
            labels = mapOf(LANGUAGE to ARRAY_NAME),
            order = 3,
        )
        val nestedObject1OcaData = OcaClaimData(
            attributeType = AttributeType.Reference(NESTED_1_DIGEST),
            captureBaseDigest = ROOT_DIGEST,
            name = "object1",
            dataSources = mapOf(FORMAT to element1Path),
            labels = mapOf(LANGUAGE to CLAIM_1_NAME),
            order = 1,
        )

        val nestedObject2OcaData = OcaClaimData(
            attributeType = AttributeType.Reference(NESTED_2_DIGEST),
            captureBaseDigest = ROOT_DIGEST,
            name = "object2",
            dataSources = mapOf(FORMAT to element2Path),
            labels = mapOf(LANGUAGE to CLAIM_2_NAME),
            order = 2,
            isSensitive = true,
        )

        val nestedClaim1OcaData = OcaClaimData(
            attributeType = AttributeType.Text,
            captureBaseDigest = NESTED_1_DIGEST,
            name = "claim1",
            dataSources = mapOf(FORMAT to nestedElement1Path),
        )

        val nestedClaim2OcaData = OcaClaimData(
            attributeType = AttributeType.Text,
            captureBaseDigest = NESTED_2_DIGEST,
            name = "claim2",
            dataSources = mapOf(FORMAT to nestedElement2Path),
        )
        val claim1 = mockk<CredentialClaim>()
        val claim2 = mockk<CredentialClaim>()
        val claim3 = mockk<CredentialClaim>()
        val claimDisplays1 = mockk<List<AnyClaimDisplay>>()
        val claimDisplays2 = mockk<List<AnyClaimDisplay>>()
        val claimDisplays3 = mockk<List<AnyClaimDisplay>>()
    }
}
