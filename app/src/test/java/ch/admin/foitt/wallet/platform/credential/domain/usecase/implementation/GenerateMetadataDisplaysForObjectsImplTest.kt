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

class GenerateMetadataDisplaysForObjectsImplTest {
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
    fun `Generating metadata displays for a simple object returns meta displays`() = runTest {
        mockGeneratedClaims(
            claim1Path = element1Path,
            claim2Path = element2Path,
            claim1Metadata = claim1Metadata,
            claim2Metadata = claim2Metadata,
            orderClaim2 = 1,
        )

        val result = useCase(simpleObjectJson, mockCredentialConfiguration).assertOk()

        assertEquals(1, result.clusters.size)
        result.clusters[0].assert(order = -1, claimsSize = 2)
        assertEquals(claimDisplays1, result.clusters[0].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[0].claims[claim2])
    }

    @Test
    fun `Generating metadata displays for a nested object returns meta displays`() = runTest {
        val metadataClaims = listOf(claim1Metadata, claim2Metadata, nestedClaim1Metadata, nestedClaim2Metadata)
        every { mockCredentialMetadata.claims } returns metadataClaims
        mockGeneratedClaims(
            claim1Path = nestedElement1Path,
            claim2Path = nestedElement2Path,
            claim1Metadata = nestedClaim1Metadata,
            claim2Metadata = nestedClaim2Metadata,
            orderClaim1 = 2,
            orderClaim2 = 3,
        )

        val result = useCase(nestedObjectJson, mockCredentialConfiguration).assertOk()

        assertEquals(1, result.clusters.size)
        result.clusters[0].assert(order = -1, childClustersSize = 2)
        val rootCluster = result.clusters[0]
        rootCluster.childClusters[0].assert(
            order = 0,
            path = element1Path.toPointerString(),
            displayName = CLAIM_1_NAME,
            claimsSize = 1,
        )
        rootCluster.childClusters[1].assert(
            order = 1,
            path = element2Path.toPointerString(),
            displayName = CLAIM_2_NAME,
            claimsSize = 1,
        )
        assertEquals(claimDisplays1, result.clusters[0].childClusters[0].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[0].childClusters[1].claims[claim2])
    }

    @Test
    fun `Generating metadata displays for a nested object without metadata returns meta displays`() = runTest {
        every { mockCredentialMetadata.claims } returns emptyList()
        mockGeneratedClaims(
            claim1Path = nestedElement1Path,
            claim2Path = nestedElement2Path,
            claim1Metadata = null,
            claim2Metadata = null,
            orderClaim1 = -1,
            orderClaim2 = -1,
        )

        val result = useCase(nestedObjectJson, mockCredentialConfiguration).assertOk()

        assertEquals(1, result.clusters.size)
        result.clusters[0].assert(order = -1, childClustersSize = 2)
        val rootCluster = result.clusters[0]
        rootCluster.childClusters[0].assert(
            order = -1,
            path = element1Path.toPointerString(),
            displayName = null,
            claimsSize = 1,
        )
        rootCluster.childClusters[1].assert(
            order = -1,
            path = element2Path.toPointerString(),
            displayName = null,
            claimsSize = 1,
        )
        assertEquals(claimDisplays1, result.clusters[0].childClusters[0].claims[claim1])
        assertEquals(claimDisplays2, result.clusters[0].childClusters[1].claims[claim2])
    }

    @Test
    fun `Generating metadata displays for a mixed object returns meta displays`() = runTest {
        val metadataClaims = listOf(claim1Metadata, claim2Metadata, nestedClaim2Metadata, arrayMetadata)
        every { mockCredentialMetadata.claims } returns metadataClaims
        mockGeneratedClaims(
            claim1Path = element1Path,
            claim2Path = nestedElement2Path,
            claim1Metadata = claim1Metadata,
            claim2Metadata = nestedClaim2Metadata,
            orderClaim2 = 2,
        )
        coEvery {
            mockGenerateMetadataClaimDisplays(
                claimsPathPointer = arrayElementPath,
                jsonPrimitive = jsonPrimitive3,
                metadataClaim = null,
                order = 0,
            )
        } returns Ok(claim3 to claimDisplays3)

        val result = useCase(mixedObjectJson, mockCredentialConfiguration).assertOk()

        assertEquals(1, result.clusters.size)
        result.clusters[0].assert(order = -1, childClustersSize = 2, claimsSize = 1)
        val rootCluster = result.clusters[0]
        rootCluster.childClusters[0].assert(
            order = 1,
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

    private fun setupDefaultMocks() {
        every { mockCredentialConfiguration.identifier } returns "identifier"
        every { mockCredentialConfiguration.credentialMetadata } returns mockCredentialMetadata
        every { mockCredentialMetadata.display } returns emptyList()
        every { mockCredentialMetadata.claims } returns listOf(claim1Metadata, claim2Metadata)
    }

    private fun mockGeneratedClaims(
        claim1Path: ClaimsPathPointer,
        claim2Path: ClaimsPathPointer,
        claim1Metadata: Claim?,
        claim2Metadata: Claim?,
        orderClaim1: Int = 0,
        orderClaim2: Int,
    ) {
        coEvery {
            mockGenerateMetadataClaimDisplays(
                claimsPathPointer = claim1Path,
                jsonPrimitive = jsonPrimitive1,
                metadataClaim = claim1Metadata,
                order = orderClaim1,
            )
        } returns Ok(claim1 to claimDisplays1)
        coEvery {
            mockGenerateMetadataClaimDisplays(
                claimsPathPointer = claim2Path,
                jsonPrimitive = jsonPrimitive2,
                metadataClaim = claim2Metadata,
                order = orderClaim2,
            )
        } returns Ok(claim2 to claimDisplays2)
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
        val nestedObjectJson = JsonObject(
            mapOf(
                KEY_1 to JsonObject(mapOf(KEY_3 to jsonPrimitive1)),
                KEY_2 to JsonObject(mapOf(KEY_4 to jsonPrimitive2)),
            )
        )
        val mixedObjectJson = JsonObject(
            mapOf(
                KEY_1 to jsonPrimitive1,
                KEY_2 to JsonObject(mapOf(KEY_4 to jsonPrimitive2)),
                KEY_3 to JsonArray(listOf(jsonPrimitive3)),
            )
        )
        const val LANGUAGE = "language"
        const val CLAIM_1_NAME = "claim1"
        const val CLAIM_2_NAME = "claim2"
        const val ARRAY_NAME = "array"

        val claim1Metadata = mockk<Claim> {
            every { path } returns element1Path
            every { display } returns listOf(OidClaimDisplay(LANGUAGE, CLAIM_1_NAME))
        }

        val claim2Metadata = mockk<Claim> {
            every { path } returns element2Path
            every { display } returns listOf(OidClaimDisplay(LANGUAGE, CLAIM_2_NAME))
        }

        val arrayMetadata = mockk<Claim> {
            every { path } returns arrayPath
            every { display } returns listOf(OidClaimDisplay(LANGUAGE, ARRAY_NAME))
        }

        val nestedClaim1Metadata = mockk<Claim> {
            every { path } returns nestedElement1Path
        }

        val nestedClaim2Metadata = mockk<Claim> {
            every { path } returns nestedElement2Path
        }

        val claim1 = mockk<CredentialClaim>()
        val claim2 = mockk<CredentialClaim>()
        val claim3 = mockk<CredentialClaim>()
        val claimDisplays1 = mockk<List<AnyClaimDisplay>>()
        val claimDisplays2 = mockk<List<AnyClaimDisplay>>()
        val claimDisplays3 = mockk<List<AnyClaimDisplay>>()
    }
}
