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

class GenerateOcaDisplaysForArraysImplTest {

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
    fun `Generating oca displays for a simple array returns meta displays`() = runTest {
        mockGeneratedClaims(
            claim1Path = arrayElement1Path,
            claim2Path = arrayElement2Path,
            orderClaim1 = 0,
            orderClaim2 = 1
        )

        val result = useCase(simpleArrayJson, FORMAT, mockOcaBundle).assertOk()

        assertEquals(1, result.clusters.size)
        result.clusters[0].assert(
            path = arrayPath.toPointerString(),
            displayName = ARRAY_NAME,
            claimsSize = 2,
        )
        assertEquals(claimDisplays1, result.clusters[0].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[0].claims[claim2])
    }

    @Test
    fun `Generating oca displays for a simple array without null path returns meta displays`() = runTest {
        mockGeneratedClaims(
            claim1Path = arrayElement1Path,
            claim2Path = arrayElement2Path,
            orderClaim1 = 0,
            orderClaim2 = 1
        )
        val dataSources = mapOf(FORMAT to listOf(ClaimsPathPointerComponent.String(ARRAY_KEY)))
        every { mockOcaBundle.getAttributes(ROOT_DIGEST) } returns listOf(simpleArrayOcaData.copy(dataSources = dataSources))

        val result = useCase(simpleArrayJson, FORMAT, mockOcaBundle).assertOk()

        assertEquals(1, result.clusters.size)
        result.clusters[0].assert(
            path = arrayPath.toPointerString(),
            displayName = ARRAY_NAME,
            claimsSize = 2,
        )
        assertEquals(claimDisplays1, result.clusters[0].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[0].claims[claim2])
    }

    @Test
    fun `Generating metadata displays for a simple array without oca returns meta displays`() = runTest {
        mockGeneratedClaims(
            claim1Path = arrayElement1Path,
            claim2Path = arrayElement2Path,
            orderClaim1 = null,
            orderClaim2 = null,
        )
        every { mockOcaBundle.getAttributes(any()) } returns emptyList()

        val result = useCase(simpleArrayJson, FORMAT, mockOcaBundle).assertOk()

        val clusters = result.clusters
        assertEquals(2, clusters.size)
        result.clusters[0].assert()
        result.clusters[1].assert(
            order = -1,
            claimsSize = 2,
        )
        assertEquals(claimDisplays1, result.clusters[1].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[1].claims[claim2])
    }

    @Test
    fun `Generating metadata displays for an object array returns meta displays`() = runTest {
        every { mockOcaBundle.getAttributes(ROOT_DIGEST) } returns listOf(objectArrayOcaData)
        every { mockOcaBundle.getAttributes(NESTED_DIGEST) } returns listOf(objectArrayClaimOcaData)
        coEvery { mockGetClaimsPathPointers(objectArrayJson) } returns pathElementMapObjectArray
        mockGeneratedClaims(
            claim1Path = objectArrayClaim1Path,
            claim2Path = objectArrayClaim2Path,
            ocaClaim = objectArrayClaimOcaData,
            orderClaim1 = null,
            orderClaim2 = null,
        )

        val result = useCase(objectArrayJson, FORMAT, mockOcaBundle).assertOk()

        assertArrayCluster(
            clusters = result.clusters,
            cluster1IsSensitive = true,
            cluster1Path = arrayElement1Path,
            display1Name = ARRAY_NAME,
            cluster2IsSensitive = true,
            cluster2Path = arrayElement2Path,
            display2Name = ARRAY_NAME,
        )
    }

    @Test
    fun `Generating metadata displays for an object array without oca returns meta displays`() = runTest {
        every { mockOcaBundle.getAttributes(ROOT_DIGEST) } returns emptyList()
        coEvery { mockGetClaimsPathPointers(objectArrayJson) } returns pathElementMapObjectArray
        mockGeneratedClaims(
            claim1Path = objectArrayClaim1Path,
            claim2Path = objectArrayClaim2Path,
            orderClaim1 = null,
            orderClaim2 = null,
        )

        val result = useCase(objectArrayJson, FORMAT, mockOcaBundle).assertOk()

        assertEquals(2, result.clusters.size)
        result.clusters[0].assert()
        result.clusters[1].assert(order = -1, claimsSize = 2)
        assertEquals(claimDisplays1, result.clusters[1].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[1].claims[claim2])
    }

    @Test
    fun `Generating metadata displays for a nested array returns meta displays`() = runTest {
        every { mockOcaBundle.getAttributes(ROOT_DIGEST) } returns listOf(arrayArrayOcaData)
        coEvery { mockGetClaimsPathPointers(arrayArrayJson) } returns pathElementMapArrayArray
        mockGeneratedClaims(
            claim1Path = arrayArrayClaim1Path,
            claim2Path = arrayArrayClaim2Path,
            ocaClaim = null,
            orderClaim1 = 0,
            orderClaim2 = 0,
        )

        val result = useCase(arrayArrayJson, FORMAT, mockOcaBundle).assertOk()

        assertArrayCluster(
            clusters = result.clusters,
            cluster1Path = arrayElement1Path + ClaimsPathPointerComponent.Null,
            cluster2Path = arrayElement2Path + ClaimsPathPointerComponent.Null,
        )
    }

    @Test
    fun `Generating metadata displays for a nested array without oca returns meta displays`() = runTest {
        every { mockOcaBundle.getAttributes(ROOT_DIGEST) } returns emptyList()
        coEvery { mockGetClaimsPathPointers(arrayArrayJson) } returns pathElementMapArrayArray
        mockGeneratedClaims(
            claim1Path = arrayArrayClaim1Path,
            claim2Path = arrayArrayClaim2Path,
            orderClaim1 = null,
            orderClaim2 = null,
        )

        val result = useCase(arrayArrayJson, FORMAT, mockOcaBundle).assertOk()

        assertEquals(2, result.clusters.size)
        result.clusters[0].assert()
        result.clusters[1].assert(order = -1, claimsSize = 2)
        assertEquals(claimDisplays1, result.clusters[1].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[1].claims[claim2])
    }

    private fun setupDefaultMocks() {
        every { mockOcaBundle.getAttributes(ROOT_DIGEST) } returns listOf(simpleArrayOcaData)
        every { mockOcaBundle.ocaCredentialData } returns emptyList()
        coEvery { mockGetRootCaptureBase(mockOcaBundle.captureBases) } returns Ok(mockCaptureBase)
        every { mockCaptureBase.digest } returns ROOT_DIGEST
        coEvery { mockGetClaimsPathPointers(simpleArrayJson) } returns mapOf(
            emptyList<ClaimsPathPointerComponent>() to simpleArrayJson,
            arrayPath to simpleArray,
            arrayElement1Path to jsonPrimitive1,
            arrayElement2Path to jsonPrimitive2,
        )
    }

    private fun mockGeneratedClaims(
        claim1Path: ClaimsPathPointer,
        claim2Path: ClaimsPathPointer,
        ocaClaim: OcaClaimData? = null,
        orderClaim1: Int? = 0,
        orderClaim2: Int? = 1
    ) {
        coEvery {
            mockGenerateOcaClaimDisplays(
                claimsPathPointer = claim1Path,
                value = CLAIM_VALUE_1,
                ocaClaimData = ocaClaim,
                order = orderClaim1,
            )
        } returns Ok(claim1 to claimDisplays1)
        coEvery {
            mockGenerateOcaClaimDisplays(
                claimsPathPointer = claim2Path,
                value = CLAIM_VALUE_2,
                ocaClaimData = ocaClaim,
                order = orderClaim2,
            )
        } returns Ok(claim2 to claimDisplays2)
    }

    private fun assertArrayCluster(
        clusters: List<Cluster>,
        cluster1IsSensitive: Boolean = false,
        cluster1Path: ClaimsPathPointer,
        display1Name: String? = null,
        cluster2IsSensitive: Boolean = false,
        cluster2Path: ClaimsPathPointer,
        display2Name: String? = null,
    ) {
        assertEquals(2, clusters.size)
        clusters[0].assert(
            order = 0,
            isSensitive = cluster1IsSensitive,
            path = cluster1Path.toPointerString(),
            displayName = display1Name,
            claimsSize = 1,
        )
        clusters[1].assert(
            order = 1,
            isSensitive = cluster2IsSensitive,
            path = cluster2Path.toPointerString(),
            displayName = display2Name,
            claimsSize = 1,
        )
        assertEquals(claimDisplays1, clusters[0].claims[claim1])
        assertEquals(claimDisplays2, clusters[1].claims[claim2])
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
        const val ARRAY_KEY = "arrayKey"
        const val KEY = "key"
        const val ROOT_DIGEST = "rootDigest"
        const val NESTED_DIGEST = "nestedDigest"
        const val CLAIM_VALUE_1 = "claimValue"
        const val CLAIM_VALUE_2 = "claimValue2"
        val arrayPath = listOf(ClaimsPathPointerComponent.String(ARRAY_KEY), ClaimsPathPointerComponent.Null)
        val arrayElement1Path =
            listOf(ClaimsPathPointerComponent.String(ARRAY_KEY), ClaimsPathPointerComponent.Index(0))
        val arrayElement2Path =
            listOf(ClaimsPathPointerComponent.String(ARRAY_KEY), ClaimsPathPointerComponent.Index(1))
        val objectArrayClaimPath = arrayPath + ClaimsPathPointerComponent.String(KEY)
        val objectArrayClaim1Path = arrayElement1Path + ClaimsPathPointerComponent.String(KEY)
        val objectArrayClaim2Path = arrayElement2Path + ClaimsPathPointerComponent.String(KEY)
        val arrayArrayClaim1Path = arrayElement1Path + ClaimsPathPointerComponent.Index(0)
        val arrayArrayClaim2Path = arrayElement2Path + ClaimsPathPointerComponent.Index(0)
        val jsonPrimitive1 = JsonPrimitive(CLAIM_VALUE_1)
        val jsonPrimitive2 = JsonPrimitive(CLAIM_VALUE_2)
        val simpleArray = JsonArray(listOf(jsonPrimitive1, jsonPrimitive2))
        val simpleArrayJson = JsonObject(
            mapOf(
                ARRAY_KEY to simpleArray
            )
        )
        val objectArrayObject1 = JsonObject(mapOf(KEY to jsonPrimitive1))
        val objectArrayObject2 = JsonObject(mapOf(KEY to jsonPrimitive2))
        val objectArray = JsonArray(
            listOf(
                objectArrayObject1,
                objectArrayObject2,
            )
        )
        val objectArrayJson = JsonObject(
            mapOf(
                ARRAY_KEY to objectArray
            )
        )
        val pathElementMapObjectArray = mapOf(
            emptyList<ClaimsPathPointerComponent>() to objectArrayJson,
            arrayPath to objectArray,
            arrayElement1Path to objectArrayObject1,
            objectArrayClaim1Path to jsonPrimitive1,
            arrayElement2Path to objectArrayObject2,
            objectArrayClaim2Path to jsonPrimitive2,
        )
        val arrayArray1 = JsonArray(listOf(jsonPrimitive1))
        val arrayArray2 = JsonArray(listOf(jsonPrimitive2))
        val arrayArray = JsonArray(
            listOf(
                arrayArray1,
                arrayArray2,
            )
        )
        val arrayArrayJson = JsonObject(
            mapOf(
                ARRAY_KEY to arrayArray
            )
        )
        val pathElementMapArrayArray = mapOf(
            emptyList<ClaimsPathPointerComponent>() to arrayArrayJson,
            arrayPath to arrayArray,
            arrayElement1Path + ClaimsPathPointerComponent.Null to arrayArray1,
            arrayArrayClaim1Path to jsonPrimitive1,
            arrayElement2Path + ClaimsPathPointerComponent.Null to arrayArray2,
            arrayArrayClaim2Path to jsonPrimitive2,
        )
        const val LANGUAGE = "language"
        const val ARRAY_NAME = "arrayName"

        val simpleArrayOcaData = OcaClaimData(
            attributeType = AttributeType.Array(AttributeType.Text),
            captureBaseDigest = ROOT_DIGEST,
            name = "array",
            dataSources = mapOf(FORMAT to arrayPath),
            labels = mapOf(LANGUAGE to ARRAY_NAME),
            order = 1,
        )

        val objectArrayOcaData = OcaClaimData(
            attributeType = AttributeType.Array(AttributeType.Reference(NESTED_DIGEST)),
            captureBaseDigest = ROOT_DIGEST,
            name = "array",
            dataSources = mapOf(FORMAT to arrayPath),
            labels = mapOf(LANGUAGE to ARRAY_NAME),
            order = 1,
            isSensitive = true,
        )

        val arrayArrayOcaData = OcaClaimData(
            attributeType = AttributeType.Array(AttributeType.Array(AttributeType.Text)),
            captureBaseDigest = ROOT_DIGEST,
            name = "array",
            dataSources = mapOf(FORMAT to arrayPath),
            labels = mapOf(LANGUAGE to ARRAY_NAME),
            order = 1,
            isSensitive = true
        )

        val objectArrayClaimOcaData = OcaClaimData(
            attributeType = AttributeType.Text,
            captureBaseDigest = NESTED_DIGEST,
            name = "claim",
            dataSources = mapOf(FORMAT to objectArrayClaimPath),
            order = 1,
        )

        val claim1 = mockk<CredentialClaim>()
        val claim2 = mockk<CredentialClaim>()
        val claimDisplays1 = mockk<List<AnyClaimDisplay>>()
        val claimDisplays2 = mockk<List<AnyClaimDisplay>>()
    }
}
