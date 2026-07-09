package ch.admin.foitt.wallet.platform.oca.domain.model

import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster

data class MetaDisplays(val credentialDisplays: List<AnyCredentialDisplay>, val clusters: List<Cluster>)
