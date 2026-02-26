package ch.admin.foitt.wallet.platform.credentialStatus.domain.model

import ch.admin.foitt.wallet.platform.utils.base64StringToByteArray
import ch.admin.foitt.wallet.platform.utils.decompressWithMaxSize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenStatusListResponse(
    @SerialName("ttl")
    val timeToLive: Long? = null,
    @SerialName("status_list")
    val statusList: TokenStatusList
)

@Serializable
data class TokenStatusList(
    @SerialName("bits")
    val bits: Int,
    @SerialName("lst")
    val lst: String,
) {
    fun decodeAndDeflate(): ByteArray {
        val zippedData = base64StringToByteArray(lst)
        return zippedData.decompressWithMaxSize()
    }
}
