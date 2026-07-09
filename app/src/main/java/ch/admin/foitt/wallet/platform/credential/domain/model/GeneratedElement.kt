package ch.admin.foitt.wallet.platform.credential.domain.model

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim

sealed interface GeneratedElement {
    data class Claim(val claim: CredentialClaim, val display: List<AnyClaimDisplay>) : GeneratedElement
    data class Cluster(val cluster: ch.admin.foitt.wallet.platform.database.domain.model.Cluster) : GeneratedElement
}
