package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.pointsAtSetOf
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyClaimDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.toAnyCredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.ClusterDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.GenerateOcaDisplaysError
import ch.admin.foitt.wallet.platform.oca.domain.model.GetRootCaptureBaseError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaClaimData
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaDisplays
import ch.admin.foitt.wallet.platform.oca.domain.model.getReferenceValue
import ch.admin.foitt.wallet.platform.oca.domain.model.toGenerateOcaDisplaysError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaClaimDisplays
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GenerateOcaDisplays
import ch.admin.foitt.wallet.platform.oca.domain.usecase.GetRootCaptureBase
import ch.admin.foitt.wallet.platform.ssi.domain.model.ValueType
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import javax.inject.Inject

class GenerateOcaDisplaysImpl @Inject constructor(
    private val getRootCaptureBase: GetRootCaptureBase,
    private val generateOcaClaimDisplays: GenerateOcaClaimDisplays,
) : GenerateOcaDisplays {

    override suspend fun invoke(
        credentialClaims: Map<ClaimsPathPointer, JsonElement>,
        credentialFormat: String,
        ocaBundle: OcaBundle,
    ): Result<OcaDisplays, GenerateOcaDisplaysError> = coroutineBinding {
        val rootCaptureBase = getRootCaptureBase(ocaBundle.captureBases)
            .mapError(GetRootCaptureBaseError::toGenerateOcaDisplaysError)
            .bind()

        val credentialDisplays = createLocalizedCredentialDisplays(ocaBundle)
        val clusters = createClusters(
            credentialClaims = credentialClaims,
            credentialFormat = credentialFormat,
            ocaBundle = ocaBundle,
            rootCaptureBaseDigest = rootCaptureBase.digest,
        ).bind()

        OcaDisplays(
            credentialDisplays = credentialDisplays,
            clusters = clusters,
        )
    }

    private fun createLocalizedCredentialDisplays(ocaBundle: OcaBundle): List<AnyCredentialDisplay> {
        return ocaBundle.ocaCredentialData.map { credentialData ->
            credentialData.toAnyCredentialDisplay()
        }
    }

    private fun createClusters(
        credentialClaims: Map<ClaimsPathPointer, JsonElement>,
        credentialFormat: String,
        ocaBundle: OcaBundle,
        rootCaptureBaseDigest: String,
    ): Result<List<Cluster>, GenerateOcaDisplaysError> = binding {
        val clusters = mutableListOf<Cluster>()
        val rootCluster = createCluster(
            credentialClaims = credentialClaims,
            credentialFormat = credentialFormat,
            ocaBundle = ocaBundle,
            captureBaseDigest = rootCaptureBaseDigest,
        ).bind()
        if (rootCluster.claims.isEmpty() && rootCluster.childClusters.isNotEmpty()) {
            clusters.addAll(rootCluster.childClusters)
        } else {
            clusters.add(rootCluster)
        }

        val claimsWithoutOca = credentialClaims.filterKeys { claimsPathPointer ->
            ocaBundle.ocaClaimData.none {
                it.dataSources.values.any { ocaClaimsPathPointer ->
                    ocaClaimsPathPointer.pointsAtSetOf(claimsPathPointer)
                }
            }
        }.map(::createFallbackClaim).toMap()

        if (claimsWithoutOca.isNotEmpty()) {
            val cluster = Cluster(claims = claimsWithoutOca)
            clusters.add(cluster)
        }

        clusters
    }

    private fun createCluster(
        credentialClaims: Map<ClaimsPathPointer, JsonElement>,
        credentialFormat: String,
        ocaBundle: OcaBundle,
        captureBaseDigest: String,
        labels: Map<String, String> = emptyMap(),
        order: Int? = null,
    ): Result<Cluster, GenerateOcaDisplaysError> = binding {
        val attributes = ocaBundle.getAttributes(captureBaseDigest)
        val referenceAttributes = attributes
            .filter { it.attributeType.getReferenceValue() != null }
            .toSet()
        val childClusters = createChildClusters(
            credentialClaims = credentialClaims,
            credentialFormat = credentialFormat,
            ocaBundle = ocaBundle,
            attributes = referenceAttributes
        ).bind()
        val otherAttributes = attributes - referenceAttributes
        val claims = createClaims(credentialClaims, credentialFormat, otherAttributes).bind()
        val clusterDisplays = createClusterDisplays(labels)
        Cluster(claims = claims, clusterDisplays = clusterDisplays, childClusters = childClusters, order = order ?: -1)
    }

    private fun createChildClusters(
        credentialClaims: Map<ClaimsPathPointer, JsonElement>,
        credentialFormat: String,
        ocaBundle: OcaBundle,
        attributes: Collection<OcaClaimData>
    ): Result<List<Cluster>, GenerateOcaDisplaysError> = binding {
        attributes.mapNotNull { attribute ->
            if (attribute.attributeType is AttributeType.Reference) {
                val referenceDigest = attribute.attributeType.getReferenceValue() ?: return@mapNotNull null
                createCluster(
                    credentialClaims = credentialClaims,
                    credentialFormat = credentialFormat,
                    ocaBundle = ocaBundle,
                    captureBaseDigest = referenceDigest,
                    labels = attribute.labels,
                    order = attribute.order
                ).bind()
            } else {
                null
            }
        }
    }

    private fun createClaims(
        credentialClaims: Map<ClaimsPathPointer, JsonElement>,
        credentialFormat: String,
        attributes: List<OcaClaimData>
    ): Result<Map<CredentialClaim, List<AnyClaimDisplay>>, GenerateOcaDisplaysError> = binding {
        attributes.mapNotNull { attribute ->
            val claim = credentialClaims.entries.find { (claimsPathPointer, _) ->
                val dataSourceClaimsPathPointer = attribute.dataSources[credentialFormat]

                dataSourceClaimsPathPointer?.let {
                    claimsPathPointer.pointsAtSetOf(it)
                } ?: false
            }?.toPair()

            if (claim == null) {
                null
            } else {
                generateOcaClaimDisplays(
                    claimsPathPointer = claim.first.toPointerString(),
                    value = when (claim.second) {
                        is JsonArray -> claim.second.toString()
                        is JsonObject -> claim.second.toString()
                        is JsonPrimitive -> (claim.second as JsonPrimitive).contentOrNull
                    },
                    ocaClaimData = attribute
                ).bind()
            }
        }.toMap()
    }

    private fun createClusterDisplays(labels: Map<String, String>) = labels.map { (locale, label) ->
        ClusterDisplay(name = label, locale = locale)
    }

    private fun createFallbackClaim(claim: Map.Entry<ClaimsPathPointer, JsonElement>): Pair<CredentialClaim, List<AnyClaimDisplay>> {
        val displays = listOf(AnyClaimDisplay(name = claim.key.toPointerString(), locale = DisplayLanguage.FALLBACK))
        return CredentialClaim(
            clusterId = UNKNOWN_CLUSTER_ID,
            path = claim.key.toPointerString(),
            value = when (claim.value) {
                is JsonArray -> claim.value.toString()
                is JsonObject -> claim.value.toString()
                is JsonPrimitive -> (claim.value as JsonPrimitive).contentOrNull
            },
            valueType = ValueType.STRING.value,
        ) to displays
    }

    companion object {
        private const val UNKNOWN_CLUSTER_ID = -1L
    }
}
