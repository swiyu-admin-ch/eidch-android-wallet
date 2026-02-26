package ch.admin.foitt.openid4vc.domain.model.credentialoffer

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class DeferredCredentialRequest(
    @SerialName("transaction_id")
    val transactionId: String,
    @EncodeDefault(NEVER)
    @SerialName("credential_response_encryption")
    val credentialResponseEncryption: CredentialRequestCredentialResponseEncryption? = null
) : CredentialRequest
