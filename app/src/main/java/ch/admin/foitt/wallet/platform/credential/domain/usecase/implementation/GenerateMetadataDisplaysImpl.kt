package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.pointsAtSetOf
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.Claim
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.GenerateMetadataDisplaysError
import ch.admin.foitt.wallet.platform.credential.domain.model.GeneratedElement
import ch.admin.foitt.wallet.platform.credential.domain.model.toAnyCredentialDisplay
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataClaimDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateMetadataDisplays
import ch.admin.foitt.wallet.platform.credential.domain.util.addFallbackLanguage
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.ClusterDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.oca.domain.model.MetaDisplays
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

class GenerateMetadataDisplaysImpl @Inject constructor(
    private val generateMetadataClaimDisplays: GenerateMetadataClaimDisplays,
) : GenerateMetadataDisplays {
    override suspend fun invoke(
        jsonObject: JsonObject?,
        credentialConfiguration: AnyCredentialConfiguration,
    ): Result<MetaDisplays, GenerateMetadataDisplaysError> = coroutineBinding {
        val rootCluster = jsonObject?.let {
            generateCluster(
                jsonObject = jsonObject,
                metadataClaims = credentialConfiguration.credentialMetadata?.claims ?: emptyList(),
                currentPath = emptyList(),
            ).bind().cluster
        }
        MetaDisplays(
            credentialDisplays = generateCredentialDisplays(credentialConfiguration),
            clusters = rootCluster?.let { listOf(it) } ?: emptyList(),
        )
    }

    private suspend fun generateCluster(
        jsonObject: JsonObject,
        metadataClaims: List<Claim>,
        currentPath: ClaimsPathPointer,
        order: Int? = null,
    ): Result<GeneratedElement.Cluster, GenerateMetadataDisplaysError> = coroutineBinding {
        val claims = mutableMapOf<CredentialClaim, List<AnyClaimDisplay>>()
        val childClusters = mutableListOf<Cluster>()
        jsonObject.forEach { (key, element) ->
            val path = currentPath + ClaimsPathPointerComponent.String(key)
            val order = metadataClaims.indexOfFirst {
                it.path.removedTrailingNull.pointsAtSetOf(path, enforceLength = true)
            }
            when (val element = generateElement(element, path, metadataClaims, order).bind()) {
                is GeneratedElement.Claim -> claims[element.claim] = element.display
                is GeneratedElement.Cluster -> childClusters.add(element.cluster)
            }
        }
        val clusterDisplays = generateClusterDisplays(currentPath, metadataClaims)
        val cluster = Cluster(
            claims = claims,
            clusterDisplays = clusterDisplays,
            childClusters = childClusters,
            order = order ?: -1,
            path = currentPath.toPointerString(),
            isSensitive = false,
        )
        GeneratedElement.Cluster(cluster)
    }

    private suspend fun generateElement(
        element: JsonElement,
        path: ClaimsPathPointer,
        metadataClaims: List<Claim>,
        index: Int,
    ): Result<GeneratedElement, GenerateMetadataDisplaysError> =
        when (element) {
            is JsonPrimitive -> generateClaim(
                path = path,
                element = element,
                metadataClaims = metadataClaims,
                index = index,
            )

            is JsonObject -> generateCluster(
                jsonObject = element,
                metadataClaims = metadataClaims,
                currentPath = path,
                order = index,
            )

            is JsonArray -> generateCluster(
                jsonArray = element,
                metadataClaims = metadataClaims,
                currentPath = path,
                order = index,
            )
        }

    private suspend fun generateClaim(
        path: ClaimsPathPointer,
        element: JsonPrimitive,
        metadataClaims: List<Claim>,
        index: Int,
    ): Result<GeneratedElement.Claim, GenerateMetadataDisplaysError> = coroutineBinding {
        val metadataClaim = metadataClaims.find { claim ->
            if (claim.path.last() == ClaimsPathPointerComponent.Null) return@find false
            claim.path.pointsAtSetOf(path, enforceLength = true)
        }
        val (claim, displays) = generateMetadataClaimDisplays(
            claimsPathPointer = path,
            jsonPrimitive = element,
            metadataClaim = metadataClaim,
            order = index,
        ).bind()
        GeneratedElement.Claim(claim, displays)
    }

    private suspend fun generateCluster(
        jsonArray: JsonArray,
        metadataClaims: List<Claim>,
        currentPath: ClaimsPathPointer,
        order: Int? = null
    ): Result<GeneratedElement.Cluster, GenerateMetadataDisplaysError> = coroutineBinding {
        val claims = mutableMapOf<CredentialClaim, List<AnyClaimDisplay>>()
        val childClusters = mutableListOf<Cluster>()
        jsonArray.forEachIndexed { index, element ->
            val path = currentPath + ClaimsPathPointerComponent.Index(index)
            when (val element = generateElement(element, path, metadataClaims, index).bind()) {
                is GeneratedElement.Claim -> claims[element.claim] = element.display
                is GeneratedElement.Cluster -> childClusters.add(element.cluster)
            }
        }
        val arrayPath = currentPath + ClaimsPathPointerComponent.Null
        val clusterDisplays = generateClusterDisplays(arrayPath, metadataClaims)
        val cluster = Cluster(
            claims = claims,
            clusterDisplays = clusterDisplays,
            childClusters = childClusters,
            order = order ?: -1,
            path = arrayPath.toPointerString(),
            isSensitive = false,
        )
        GeneratedElement.Cluster(cluster)
    }

    private fun generateClusterDisplays(clusterPath: ClaimsPathPointer, metadataClaims: List<Claim>): List<ClusterDisplay> {
        val metadataClaim = metadataClaims.find {
            it.path.removedTrailingNull == clusterPath.removedTrailingNull
        }
        return metadataClaim?.display?.mapNotNull {
            it.locale?.let { locale ->
                ClusterDisplay(name = it.name, locale = locale)
            }
        } ?: emptyList()
    }

    private fun generateCredentialDisplays(credentialConfiguration: AnyCredentialConfiguration): List<AnyCredentialDisplay> =
        credentialConfiguration.credentialMetadata?.display?.map {
            it.toAnyCredentialDisplay()
        }.addFallbackLanguage {
            AnyCredentialDisplay(name = credentialConfiguration.identifier, locale = DisplayLanguage.FALLBACK)
        }

    private val ClaimsPathPointer.removedTrailingNull: ClaimsPathPointer
        get() = if (lastOrNull() == ClaimsPathPointerComponent.Null) {
            dropLast(1)
        } else {
            this
        }
}
