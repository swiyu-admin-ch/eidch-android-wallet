package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.Claim
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialMetadata
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidClaimDisplay
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataClaimDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.ClusterDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
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

class GenerateMetadataDisplaysForArraysImplTest {
    @MockK
    private lateinit var mockGenerateMetadataClaimDisplays: GenerateMetadataClaimDisplays

    @MockK
    private lateinit var mockCredentialConfiguration: VcSdJwtCredentialConfiguration

    @MockK
    private lateinit var mockCredentialMetadata: CredentialMetadata

    private lateinit var useCase: GenerateMetadataDisplays

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = GenerateMetadataDisplaysImpl(
            generateMetadataClaimDisplays = mockGenerateMetadataClaimDisplays,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Generating metadata displays for a simple array returns meta displays`() = runTest {
        mockGeneratedClaims(claim1Path = arrayElement1Path, claim2Path = arrayElement2Path, metadataClaim = null, orderClaim2 = 1)

        val result = useCase(simpleArrayJson, mockCredentialConfiguration).assertOk()

        assertEquals(1, result.clusters.size)
        result.clusters[0].assert(order = -1, childClustersSize = 1)
        result.clusters[0].childClusters[0].assert(
            path = arrayPath.toPointerString(),
            displayName = ARRAY_NAME,
            claimsSize = 2,
        )
        assertEquals(claimDisplays1, result.clusters[0].childClusters[0].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[0].childClusters[0].claims[claim2])
    }

    @Test
    fun `Generating metadata displays for a simple array without metadata returns meta displays`() = runTest {
        mockGeneratedClaims(claim1Path = arrayElement1Path, claim2Path = arrayElement2Path)
        every { mockCredentialMetadata.claims } returns emptyList()

        val result = useCase(simpleArrayJson, mockCredentialConfiguration).assertOk()

        assertEquals(1, result.clusters.size)
        result.clusters[0].assert(order = -1, childClustersSize = 1)
        result.clusters[0].childClusters[0].assert(
            order = -1,
            path = arrayPath.toPointerString(),
            claimsSize = 2,
        )
        assertEquals(claimDisplays1, result.clusters[0].childClusters[0].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[0].childClusters[0].claims[claim2])
    }

    @Test
    fun `Generating metadata displays for an object array returns meta displays`() = runTest {
        val metadataClaims = listOf(arrayMetadata, objectArrayClaimMetadata)
        every { mockCredentialMetadata.claims } returns metadataClaims
        mockGeneratedClaims(
            claim1Path = objectArrayClaim1Path,
            claim2Path = objectArrayClaim2Path,
            metadataClaim = objectArrayClaimMetadata,
            orderClaim1 = 1,
            orderClaim2 = 1,
        )

        val result = useCase(objectArrayJson, mockCredentialConfiguration).assertOk()

        assertEquals(1, result.clusters.size)
        result.clusters[0].assert(order = -1, childClustersSize = 1)
        assertArrayCluster(result.clusters[0].childClusters[0], arrayElement1Path, arrayElement2Path)
    }

    @Test
    fun `Generating metadata displays for a nested array returns meta displays`() = runTest {
        mockGeneratedClaims(
            claim1Path = arrayArrayClaim1Path,
            claim2Path = arrayArrayClaim2Path,
            metadataClaim = null,
            orderClaim2 = 0,
        )

        val result = useCase(arrayArrayJson, mockCredentialConfiguration).assertOk()

        assertEquals(1, result.clusters.size)
        result.clusters[0].assert(order = -1, childClustersSize = 1)
        assertArrayCluster(
            result.clusters[0].childClusters[0],
            arrayElement1Path + ClaimsPathPointerComponent.Null,
            arrayElement2Path + ClaimsPathPointerComponent.Null
        )
    }

    @Test
    fun `Generating metadata displays works correctly with metadata array paths that do not contain null as last component`() = runTest {
        val arrayMetadata = mockk<Claim> {
            every { path } returns listOf(ClaimsPathPointerComponent.String(ARRAY_KEY))
            every { display } returns listOf(OidClaimDisplay(LANGUAGE, ARRAY_NAME))
        }
        every { mockCredentialMetadata.claims } returns listOf(arrayMetadata)
        mockGeneratedClaims(claim1Path = arrayElement1Path, claim2Path = arrayElement2Path, metadataClaim = null, orderClaim2 = 1)

        val result = useCase(simpleArrayJson, mockCredentialConfiguration).assertOk()

        assertEquals(1, result.clusters.size)
        result.clusters[0].assert(order = -1, childClustersSize = 1)
        result.clusters[0].childClusters[0].assert(
            path = arrayPath.toPointerString(),
            displayName = ARRAY_NAME,
            claimsSize = 2,
        )
    }

    private fun setupDefaultMocks() {
        every { mockCredentialConfiguration.identifier } returns "identifier"
        every { mockCredentialConfiguration.credentialMetadata } returns mockCredentialMetadata
        every { mockCredentialMetadata.display } returns emptyList()
        every { mockCredentialMetadata.claims } returns listOf(arrayMetadata)
    }

    private fun mockGeneratedClaims(
        claim1Path: ClaimsPathPointer,
        claim2Path: ClaimsPathPointer,
        metadataClaim: Claim? = null,
        orderClaim1: Int = 0,
        orderClaim2: Int = 1
    ) {
        coEvery {
            mockGenerateMetadataClaimDisplays(
                claimsPathPointer = claim1Path,
                jsonPrimitive = jsonPrimitive1,
                metadataClaim = metadataClaim,
                order = orderClaim1,
            )
        } returns Ok(claim1 to claimDisplays1)
        coEvery {
            mockGenerateMetadataClaimDisplays(
                claimsPathPointer = claim2Path,
                jsonPrimitive = jsonPrimitive2,
                metadataClaim = metadataClaim,
                order = orderClaim2,
            )
        } returns Ok(claim2 to claimDisplays2)
    }

    private fun assertArrayCluster(cluster: Cluster, cluster1Path: ClaimsPathPointer, cluster2Path: ClaimsPathPointer) {
        cluster.assert(
            path = arrayPath.toPointerString(),
            displayName = ARRAY_NAME,
            childClustersSize = 2,
        )
        cluster.childClusters[0].assert(
            order = 0,
            path = cluster1Path.toPointerString(),
            claimsSize = 1,
        )
        cluster.childClusters[1].assert(
            order = 1,
            path = cluster2Path.toPointerString(),
            claimsSize = 1,
        )
        assertEquals(claimDisplays1, cluster.childClusters[0].claims[claim1])
        assertEquals(claimDisplays2, cluster.childClusters[1].claims[claim2])
    }

    private fun Cluster.assert(
        order: Int = 0,
        path: String = "[]",
        displayName: String? = null,
        claimsSize: Int = 0,
        childClustersSize: Int = 0,
    ) {
        assertEquals(order, this.order)
        assertEquals(path, this.path)
        val expectedDisplays = displayName?.let { listOf(ClusterDisplay(displayName, LANGUAGE)) } ?: emptyList()
        assertEquals(expectedDisplays, this.clusterDisplays)
        assertEquals(claimsSize, claims.size)
        assertEquals(childClustersSize, childClusters.size)
    }

    private companion object {
        const val ARRAY_KEY = "arrayKey"
        const val KEY = "key"
        const val CLAIM_VALUE_1 = "claimValue"
        const val CLAIM_VALUE_2 = "claimValue2"
        val arrayPath = listOf(ClaimsPathPointerComponent.String(ARRAY_KEY), ClaimsPathPointerComponent.Null)
        val arrayElement1Path =
            listOf(ClaimsPathPointerComponent.String(ARRAY_KEY), ClaimsPathPointerComponent.Index(0))
        val arrayElement2Path =
            listOf(ClaimsPathPointerComponent.String(ARRAY_KEY), ClaimsPathPointerComponent.Index(1))
        val objectArrayClaim1Path = arrayElement1Path + ClaimsPathPointerComponent.String(KEY)
        val objectArrayClaim2Path = arrayElement2Path + ClaimsPathPointerComponent.String(KEY)
        val arrayArrayClaim1Path = arrayElement1Path + ClaimsPathPointerComponent.Index(0)
        val arrayArrayClaim2Path = arrayElement2Path + ClaimsPathPointerComponent.Index(0)
        val jsonPrimitive1 = JsonPrimitive(CLAIM_VALUE_1)
        val jsonPrimitive2 = JsonPrimitive(CLAIM_VALUE_2)
        val simpleArrayJson = JsonObject(
            mapOf(
                ARRAY_KEY to JsonArray(listOf(jsonPrimitive1, jsonPrimitive2))
            )
        )
        val objectArrayJson = JsonObject(
            mapOf(
                ARRAY_KEY to JsonArray(
                    listOf(
                        JsonObject(mapOf(KEY to jsonPrimitive1)),
                        JsonObject(mapOf(KEY to jsonPrimitive2))
                    )
                )
            )
        )
        val arrayArrayJson = JsonObject(
            mapOf(
                ARRAY_KEY to JsonArray(
                    listOf(
                        JsonArray(listOf(jsonPrimitive1)),
                        JsonArray(listOf(jsonPrimitive2))
                    )
                )
            )
        )
        const val LANGUAGE = "language"
        const val ARRAY_NAME = "arrayName"

        val arrayMetadata = mockk<Claim> {
            every { path } returns arrayPath
            every { display } returns listOf(OidClaimDisplay(LANGUAGE, ARRAY_NAME))
        }

        val objectArrayClaimMetadata = mockk<Claim> {
            every { path } returns arrayPath + ClaimsPathPointerComponent.String(KEY)
        }

        val claim1 = mockk<CredentialClaim>()
        val claim2 = mockk<CredentialClaim>()
        val claimDisplays1 = mockk<List<AnyClaimDisplay>>()
        val claimDisplays2 = mockk<List<AnyClaimDisplay>>()
    }
}
