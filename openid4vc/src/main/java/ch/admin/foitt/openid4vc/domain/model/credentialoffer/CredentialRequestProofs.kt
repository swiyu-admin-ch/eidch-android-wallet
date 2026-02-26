package ch.admin.foitt.openid4vc.domain.model.credentialoffer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class CredentialRequestProofs

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CredentialRequestProofsJwt(
    @SerialName("jwt")
    val jwt: List<String>
) : CredentialRequestProofs()
