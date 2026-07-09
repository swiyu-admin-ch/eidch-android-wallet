package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface TrustStatement {
    @SerialName("vct")
    val vct: String

    @SerialName("sub")
    val sub: String

    @SerialName("iat")
    val iat: Long

    @SerialName("status")
    val status: TrustStatementStatus?

    @SerialName("exp")
    val exp: Long?

    @SerialName("nbf")
    val nbf: Long?
}

@Serializable
data class TrustStatementStatus(
    @SerialName("status_list")
    val statusList: TrustStatementStatusList
)

@Serializable
data class TrustStatementStatusList(
    @SerialName("idx")
    val idx: Int,
    @SerialName("uri")
    val uri: String
)
