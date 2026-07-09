package ch.admin.foitt.wallet.platform.credentialCluster.domain.usercase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.allIndices
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.claimsPathPointerFrom
import ch.admin.foitt.wallet.platform.credential.domain.usecase.ResolveClaimTemplate
import ch.admin.foitt.wallet.platform.credentialCluster.domain.usercase.MapToCredentialClaimCluster
import ch.admin.foitt.wallet.platform.database.domain.model.ClusterWithDisplaysAndClaims
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimCluster
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimImage
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialElement
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialDetailFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.model.MapToCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toGetCredentialDetailFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.platform.utils.sortByOrder
import ch.admin.foitt.wallet.platform.utils.sortInPlaceByOrder
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import javax.inject.Inject

/**
 * returns cluster tree with all elements sorted by order fields
 */
class MapToCredentialClaimClusterImpl @Inject constructor(
    private val getLocalizedDisplay: GetLocalizedDisplay,
    private val mapToCredentialClaimData: MapToCredentialClaimData,
    private val resolveClaimTemplate: ResolveClaimTemplate,
) : MapToCredentialClaimCluster {
    override suspend fun invoke(clustersWithDisplaysAndClaims: List<ClusterWithDisplaysAndClaims>): List<CredentialClaimCluster> {
        val allClaims = mutableSetOf<CredentialClaimWithDisplays>()
        // Step 1: Create all nodes with only Claim items
        val nodes = clustersWithDisplaysAndClaims.associate { (clusterWithDisplays, claimsWithDisplays) ->
            allClaims.addAll(claimsWithDisplays)

            clusterWithDisplays.cluster.id to CredentialClaimCluster(
                id = clusterWithDisplays.cluster.id,
                localizedLabel = getLocalizedDisplay(clusterWithDisplays.displays)?.name ?: "",
                parentId = clusterWithDisplays.cluster.parentClusterId,
                order = clusterWithDisplays.cluster.order,
                items = getCredentialClaimData(claimsWithDisplays).get()?.toMutableList() ?: mutableListOf(),
                path = claimsPathPointerFrom(clusterWithDisplays.cluster.path) ?: emptyList(),
                isSensitive = clusterWithDisplays.cluster.isSensitive,
            )
        }

        // Step 2: Link each Cluster to its parent
        nodes.values.forEach { node ->
            node.parentId?.let { parentId ->
                nodes[parentId]?.items?.add(node)
            }
        }
        nodes.values.forEach { it.items.sortInPlaceByOrder() }

        // Step 3: Only return root nodes (parentId == null)
        val roots = nodes.values.filter { it.parentId == null }
        roots.setNumberOfNonClusterChildren()

        // Step 4: Determine if node is a simple type claim cluster
        roots.determineSimpleTypeClusterNodes()

        // Step 5: Return filtered and sorted roots
        return roots.filterEmptyItems()
            .resolveTemplates(allClaims.toList())
            .filterIsInstance<CredentialClaimCluster>()
            .sortByOrder()
    }

    private suspend fun getCredentialClaimData(
        claims: List<CredentialClaimWithDisplays>
    ): Result<List<CredentialElement>, GetCredentialDetailFlowError> = coroutineBinding {
        claims.map { claimWithDisplays ->
            mapToCredentialClaimData(
                claimWithDisplays
            ).mapError(MapToCredentialClaimDataError::toGetCredentialDetailFlowError).bind()
        }
    }

    private fun List<CredentialElement>.setNumberOfNonClusterChildren(): Int = sumOf { item ->
        when (item) {
            is CredentialClaimText, is CredentialClaimImage -> 1
            is CredentialClaimCluster -> {
                item.items.setNumberOfNonClusterChildren().also { count ->
                    item.numberOfNonClusterChildren = count
                }
            }
        }
    }

    /**
     * Determines if a node is a CredentialClaimCluster with only simple type items.
     * The function sets the isSimpleTypeCluster property of a node to true if:
     * - The node itself is of type [CredentialClaimCluster]
     * - The nodes [CredentialClaimCluster.path] has a [ClaimsPathPointerComponent.Null] value as its last entry.
     * - The nodes [CredentialClaimCluster.items] do not contain any other [CredentialClaimCluster]
     * If the nodes [CredentialClaimCluster.items] contain more [CredentialClaimCluster] then this function will
     * call itself recursively to apply the [CredentialClaimCluster.isSimpleTypeCluster] property to the child items as well.
     */
    private fun List<CredentialElement>.determineSimpleTypeClusterNodes() {
        filterIsInstance<CredentialClaimCluster>().forEach { node ->
            node.isSimpleTypeCluster =
                node.path.isNotEmpty() && node.path.last() == ClaimsPathPointerComponent.Null && node.items.none {
                    it is CredentialClaimCluster
                }
            if (!node.isSimpleTypeCluster) {
                node.items.determineSimpleTypeClusterNodes()
            }
        }
    }

    private fun List<CredentialElement>.filterEmptyItems(): List<CredentialElement> {
        return this.mapNotNull { item ->
            when (item) {
                is CredentialClaimCluster -> {
                    if (item.numberOfNonClusterChildren == 0) return@mapNotNull null
                    item.copy(
                        items = item.items.filterEmptyItems().toMutableList()
                    )
                }

                else -> item
            }
        }
    }

    private fun List<CredentialElement>.resolveTemplates(
        claims: List<CredentialClaimWithDisplays>,
    ): List<CredentialElement> {
        return this.map { element ->
            when (element) {
                is CredentialClaimCluster -> {
                    val resolvedLabel = resolveClaimTemplate(
                        template = element.localizedLabel,
                        claims = claims,
                        allIndices = element.path.allIndices
                    )
                    element.copy(
                        localizedLabel = resolvedLabel,
                        items = element.items.resolveTemplates(claims).toMutableList()
                    )
                }
                else -> element
            }
        }
    }
}
