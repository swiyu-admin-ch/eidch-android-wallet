package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface IdentityTrustStatement : TrustStatement

@Serializable
data class IdentityV1TrustStatement(
    override val vct: String,
    override val sub: String,
    override val iat: Long,
    override val status: TrustStatementStatus?,
    override val exp: Long?,
    override val nbf: Long?,

    @SerialName("entityName")
    val entityName: Map<String, String>,

    @SerialName("registryIds")
    val registryIds: List<RegistryId>?,
    @SerialName("isStateActor")
    val isStateActor: Boolean,
) : IdentityTrustStatement

@Serializable
data class RegistryId(
    @SerialName("type")
    val type: String,
    @SerialName("value")
    val value: String,
)
