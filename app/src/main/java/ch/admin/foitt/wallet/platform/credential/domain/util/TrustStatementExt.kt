package ch.admin.foitt.wallet.platform.credential.domain.util

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement

fun TrustStatement.entityNames(): Map<String, String>? = when (this) {
    is IdentityV1TrustStatement -> entityName
    else -> null
}
