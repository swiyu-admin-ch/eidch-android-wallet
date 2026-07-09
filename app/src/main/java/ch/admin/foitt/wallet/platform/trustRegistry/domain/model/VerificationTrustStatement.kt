package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface VerificationTrustStatement : VcSchemaTrustStatement

@Serializable
data class VerificationV1TrustStatement(
    override val vct: String,
    override val sub: String,
    override val iat: Long,
    override val status: TrustStatementStatus?,
    override val exp: Long?,
    override val nbf: Long?,

    @SerialName("canVerify")
    override val vcSchemaId: String
) : VerificationTrustStatement
