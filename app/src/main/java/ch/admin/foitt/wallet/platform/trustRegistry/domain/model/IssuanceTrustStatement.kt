package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface IssuanceTrustStatement : VcSchemaTrustStatement

@Serializable
data class IssuanceV1TrustStatement(
    override val vct: String,
    override val sub: String,
    override val iat: Long,
    override val status: TrustStatementStatus?,
    override val exp: Long?,
    override val nbf: Long?,

    @SerialName("canIssue")
    override val vcSchemaId: String,
) : IssuanceTrustStatement
