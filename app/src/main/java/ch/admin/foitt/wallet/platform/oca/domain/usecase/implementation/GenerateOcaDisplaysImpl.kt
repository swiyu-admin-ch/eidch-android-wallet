package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.allIndices
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.resolveWildCards
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.GeneratedElement
import ch.admin.foitt.wallet.platform.credential.domain.model.toAnyCredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.ClusterDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.GenerateOcaDisplaysError
import ch.admin.foitt.wallet.platform.oca.domain.model.GetRootCaptureBaseError
import ch.admin.foitt.wallet.platform.oca.domain.model.MetaDisplays
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaClaimData
import ch.admin.foitt.wallet.platform.oca.domain.model.getReferenceValue
import ch.admin.foitt.wallet.platform.oca.domain.model.toGenerateOcaDisplaysError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaClaimDisplays
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaDisplays
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GetClaimsPathPointers
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GetRootCaptureBase
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import javax.inject.Inject

class GenerateOcaDisplaysImpl @Inject constructor(
    private val getClaimsPathPointers: GetClaimsPathPointers,
    private val getRootCaptureBase: GetRootCaptureBase,
    private val generateOcaClaimDisplays: GenerateOcaClaimDisplays,
) : GenerateOcaDisplays {

    override suspend fun invoke(
        jsonObject: JsonObject?,
        credentialFormat: String,
        ocaBundle: OcaBundle,
    ): Result<MetaDisplays, GenerateOcaDisplaysError> = coroutineBinding {
        val clusters = jsonObject?.let {
            val credentialClaims = getClaimsPathPointers(jsonObject)
            generateClusters(
                credentialClaims = credentialClaims.toMutableMap(),
                credentialFormat = credentialFormat,
                ocaBundle = ocaBundle,
            ).bind()
        } ?: emptyList()
        MetaDisplays(
            credentialDisplays = generateCredentialDisplays(ocaBundle),
            clusters = clusters,
        )
    }

    private suspend fun generateClusters(
        credentialClaims: MutableMap<ClaimsPathPointer, JsonElement>,
        credentialFormat: String,
        ocaBundle: OcaBundle,
    ): Result<List<Cluster>, GenerateOcaDisplaysError> = coroutineBinding {
        val rootCluster = generateCluster(
            credentialClaims = credentialClaims,
            credentialFormat = credentialFormat,
            ocaBundle = ocaBundle,
            captureBaseDigest = getRootCaptureBaseDigest(ocaBundle).bind(),
            path = emptyList(),
            ocaClaimData = null,
        ).bind().cluster
        val clusters = expandChildCluster(rootCluster)
        val clusterWithoutOca = generateClusterWithoutOca(credentialClaims).bind()
        clusterWithoutOca?.let { clusters + it } ?: clusters
    }

    private suspend fun getRootCaptureBaseDigest(ocaBundle: OcaBundle) =
        getRootCaptureBase(ocaBundle.captureBases)
            .mapError(GetRootCaptureBaseError::toGenerateOcaDisplaysError)
            .map { it.digest }

    private suspend fun generateCluster(
        credentialClaims: MutableMap<ClaimsPathPointer, JsonElement>,
        credentialFormat: String,
        ocaBundle: OcaBundle,
        captureBaseDigest: String,
        ocaClaimData: OcaClaimData?,
        path: ClaimsPathPointer,
    ): Result<GeneratedElement.Cluster, GenerateOcaDisplaysError> = coroutineBinding {
        val claims = mutableMapOf<CredentialClaim, List<AnyClaimDisplay>>()
        val childClusters = mutableListOf<Cluster>()
        val ocaClaims = ocaBundle.getAttributes(captureBaseDigest)
        for (ocaClaim in ocaClaims) {
            val ocaPath = ocaClaim.dataSources[credentialFormat]?.resolveWildCards(path.allIndices)
            if (ocaPath != null) {
                val element = generateElement(
                    attributeType = ocaClaim.attributeType,
                    ocaClaim = ocaClaim,
                    credentialClaims = credentialClaims,
                    credentialFormat = credentialFormat,
                    ocaBundle = ocaBundle,
                    path = ocaPath,
                ).bind()
                when (element) {
                    is GeneratedElement.Claim -> claims[element.claim] = element.display
                    is GeneratedElement.Cluster -> childClusters.add(element.cluster)
                    null -> Unit
                }
            } else if (ocaClaim.attributeType is AttributeType.Reference) {
                // cluster that is just relevant for UI and not reflected in the JSON data
                val cluster = generateCluster(
                    credentialClaims = credentialClaims,
                    credentialFormat = credentialFormat,
                    ocaBundle = ocaBundle,
                    captureBaseDigest = ocaClaim.attributeType.captureBaseReference,
                    ocaClaimData = ocaClaim,
                    path = emptyList(),
                ).bind()
                childClusters.add(cluster.cluster)
            }
        }
        val cluster = Cluster(
            claims = claims,
            clusterDisplays = generateClusterDisplays(ocaClaimData?.labels ?: emptyMap()),
            childClusters = childClusters,
            order = path.lastIndex ?: ocaClaimData?.order ?: -1,
            path = path.toPointerString(),
            isSensitive = ocaClaimData?.isSensitive ?: false,
        )
        GeneratedElement.Cluster(cluster)
    }

    private suspend fun generateElement(
        attributeType: AttributeType,
        ocaClaim: OcaClaimData?,
        credentialClaims: MutableMap<ClaimsPathPointer, JsonElement>,
        credentialFormat: String,
        ocaBundle: OcaBundle,
        path: ClaimsPathPointer,
    ): Result<GeneratedElement?, GenerateOcaDisplaysError> = coroutineBinding {
        val elementPath = getElementPath(path, attributeType, credentialClaims) ?: path
        val element = credentialClaims.remove(elementPath)
        val referencedDigest = attributeType.getReferenceValue()
        when {
            attributeType is AttributeType.Array && element is JsonArray -> generateArrayCluster(
                attributeType = attributeType,
                jsonArray = element,
                credentialClaims = credentialClaims,
                credentialFormat = credentialFormat,
                ocaBundle = ocaBundle,
                ocaClaim = ocaClaim,
                path = elementPath,
            ).bind()

            referencedDigest != null -> generateCluster(
                credentialClaims = credentialClaims,
                credentialFormat = credentialFormat,
                ocaBundle = ocaBundle,
                captureBaseDigest = referencedDigest,
                ocaClaimData = ocaClaim,
                path = elementPath,
            ).bind()

            attributeType is AttributeType.Array && element is JsonPrimitive -> generateClaim(
                ocaClaim = null,
                jsonPrimitive = element,
                path = elementPath,
            ).bind()

            element is JsonPrimitive -> generateClaim(
                ocaClaim = ocaClaim,
                jsonPrimitive = element,
                path = elementPath,
            ).bind()

            else -> null
        }
    }

    private fun getElementPath(
        path: ClaimsPathPointer,
        attributeType: AttributeType,
        credentialClaims: Map<ClaimsPathPointer, JsonElement>
    ): ClaimsPathPointer? {
        if (credentialClaims[path] != null) return path
        if (attributeType is AttributeType.Array && path.lastOrNull() != ClaimsPathPointerComponent.Null) {
            val nullPath = path + ClaimsPathPointerComponent.Null
            if (credentialClaims[nullPath] is JsonArray) {
                return nullPath
            }
        }
        return null
    }

    private suspend fun generateArrayCluster(
        attributeType: AttributeType.Array,
        jsonArray: JsonArray,
        credentialClaims: MutableMap<ClaimsPathPointer, JsonElement>,
        credentialFormat: String,
        ocaBundle: OcaBundle,
        ocaClaim: OcaClaimData?,
        path: ClaimsPathPointer,
    ): Result<GeneratedElement.Cluster?, GenerateOcaDisplaysError> = coroutineBinding {
        val claims = mutableMapOf<CredentialClaim, List<AnyClaimDisplay>>()
        val childClusters = mutableListOf<Cluster>()
        for (index in jsonArray.indices) {
            val currentPath = (path.dropLast(1) + ClaimsPathPointerComponent.Index(index)).toMutableList()
            if (attributeType.attributeType is AttributeType.Array) {
                currentPath += ClaimsPathPointerComponent.Null
            }
            val element = generateElement(
                attributeType = attributeType.attributeType,
                ocaClaim = if (attributeType.attributeType.getReferenceValue() == null) null else ocaClaim,
                credentialClaims = credentialClaims,
                credentialFormat = credentialFormat,
                ocaBundle = ocaBundle,
                path = currentPath
            ).bind()
            when (element) {
                is GeneratedElement.Claim -> claims[element.claim] = element.display
                is GeneratedElement.Cluster -> childClusters.add(element.cluster)
                null -> Unit
            }
        }
        val cluster = Cluster(
            claims = claims,
            clusterDisplays = if (attributeType.attributeType.getReferenceValue() == null) {
                generateClusterDisplays(ocaClaim?.labels ?: emptyMap())
            } else {
                emptyList()
            },
            childClusters = childClusters,
            order = path.dropLast(1).lastIndex ?: ocaClaim?.order ?: -1,
            path = path.toPointerString(),
            isSensitive = ocaClaim?.isSensitive ?: false,
        )
        GeneratedElement.Cluster(cluster)
    }

    private fun generateClaim(
        ocaClaim: OcaClaimData?,
        jsonPrimitive: JsonPrimitive,
        path: ClaimsPathPointer,
    ): Result<GeneratedElement.Claim, GenerateOcaDisplaysError> = binding {
        val (claim, displays) = generateOcaClaimDisplays(
            claimsPathPointer = path,
            value = jsonPrimitive.contentOrNull,
            ocaClaimData = ocaClaim,
            order = path.lastIndex,
        ).bind()
        GeneratedElement.Claim(claim, displays)
    }

    private fun generateClusterDisplays(labels: Map<String, String>) = labels.map { (locale, label) ->
        ClusterDisplay(name = label, locale = locale)
    }

    private fun expandChildCluster(cluster: Cluster, index: Int = 0): List<Cluster> =
        if (cluster.claims.isEmpty() && cluster.childClusters.isNotEmpty()) {
            val clusters = mutableListOf<Cluster>()
            var currentIndex = index
            cluster.childClusters
                .sortedBy { it.order }
                .forEach { cluster ->
                    val childClusters = expandChildCluster(cluster, currentIndex)
                    currentIndex = childClusters.lastOrNull()?.order ?: currentIndex
                    currentIndex++
                    clusters += childClusters
                }
            clusters
        } else {
            listOf(cluster.copy(order = index))
        }

    private suspend fun generateClusterWithoutOca(
        credentialClaims: Map<ClaimsPathPointer, JsonElement>,
    ): Result<Cluster?, GenerateOcaDisplaysError> = coroutineBinding {
        val claimsWithoutOca = credentialClaims.mapNotNull { (path, element) ->
            val primitive = element as? JsonPrimitive
            primitive?.let {
                generateOcaClaimDisplays(
                    claimsPathPointer = path,
                    value = primitive.contentOrNull,
                    ocaClaimData = null,
                    order = null
                ).bind()
            }
        }.toMap()
        if (claimsWithoutOca.isNotEmpty()) {
            Cluster(claims = claimsWithoutOca, path = "[]", isSensitive = false)
        } else {
            null
        }
    }

    private fun generateCredentialDisplays(ocaBundle: OcaBundle) =
        ocaBundle.ocaCredentialData.map { credentialData ->
            credentialData.toAnyCredentialDisplay()
        }

    private val ClaimsPathPointer.lastIndex: Int?
        get() {
            val last = lastOrNull()
            if (last is ClaimsPathPointerComponent.Index) {
                return last.index
            }
            return null
        }
}
