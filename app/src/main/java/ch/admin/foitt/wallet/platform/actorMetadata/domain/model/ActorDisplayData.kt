package ch.admin.foitt.wallet.platform.actorMetadata.domain.model

import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import kotlinx.serialization.Serializable

@Serializable
data class ActorDisplayData(
    val name: List<ActorField<String>>?,
    val image: List<ActorField<String>>?,
    val preferredLanguage: String?,
    val trustStatus: TrustStatus,
    val vcSchemaTrustStatus: VcSchemaTrustStatus,
    val actorType: ActorType,
    val actorComplianceState: ActorComplianceState,
    val nonComplianceReason: List<ActorField<String>>?,
) {
    companion object {
        val EMPTY by lazy {
            ActorDisplayData(
                name = listOf(),
                image = listOf(),
                preferredLanguage = null,
                trustStatus = TrustStatus.UNKNOWN,
                vcSchemaTrustStatus = VcSchemaTrustStatus.UNPROTECTED,
                actorType = ActorType.UNKNOWN,
                actorComplianceState = ActorComplianceState.UNKNOWN,
                nonComplianceReason = null,
            )
        }
    }
}

@Serializable
data class ActorField<T>(
    val value: T?,
    override val locale: String,
) : LocalizedDisplay
