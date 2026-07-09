package ch.admin.foitt.wallet.platform.ssi.domain.model

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer

data class CredentialClaimCluster(
    override val id: Long,
    override val order: Int,
    override val localizedLabel: String,
    override val isSensitive: Boolean = false,
    val parentId: Long?,
    val items: MutableList<CredentialElement>,
    var numberOfNonClusterChildren: Int = -1,
    val path: ClaimsPathPointer = emptyList(),
    var isSimpleTypeCluster: Boolean = false
) : CredentialElement

fun List<CredentialClaimCluster>.getClaimIds(): List<Long> = this.flatMap { it.getClaimIds() }

private fun CredentialClaimCluster.getClaimIds(): List<Long> {
    val ids = mutableListOf<Long>()

    items.forEach { element ->
        when (element) {
            is CredentialClaimText, is CredentialClaimImage -> ids.add(element.id)
            is CredentialClaimCluster -> ids.addAll(element.getClaimIds())
        }
    }

    return ids
}
